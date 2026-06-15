package com.plywood.service;

import com.plywood.model.FreeSpace;
import com.plywood.model.Rectangle;
import com.plywood.model.Sheet;
import com.plywood.model.OptimizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MaxRects bin-packing optimizer.
 *
 * Root cause of the original overlap bug
 * ─────────────────────────────────────
 * The old code only split the *one* free-space that was chosen for placement.
 * MaxRects requires clipping *every* free rectangle against the newly placed
 * piece. Any free rect that overlaps the piece must be split into up to four
 * axis-aligned sub-rects (left / right / bottom / top), the original removed,
 * and the sub-rects added back. After that a containment-prune pass removes
 * any free rect that is completely covered by another — keeping the list lean
 * and overlap-free.
 *
 * Heuristics used
 * ───────────────
 * Placement : BSSF (Best Short-Side Fit) with BAF (Best Area Fit) as tiebreak.
 *             Empirically gives the best packing density across random inputs.
 * Sort order : rectangles sorted by descending area, then descending perimeter,
 *              then descending max-side — ensures large pieces are placed first
 *              and leave fewer awkward gaps.
 * Rotation   : when allowRotation=true both orientations are scored and the
 *              better one is chosen (not just the first that fits).
 */
@Service
public class MaxRectOptimizerService {

    private static final Logger logger = LoggerFactory.getLogger(MaxRectOptimizerService.class);

    // Floating-point tolerance for overlap / containment checks
    private static final double EPS = 1e-6;

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    public OptimizationResult optimize(List<Rectangle> rectangles,
                                       double sheetWidth, double sheetHeight,
                                       boolean allowRotation) {
        long startTime = System.currentTimeMillis();
        logger.info("MaxRects optimizing {} rect types, sheet {}×{}, rotation={}",
                    rectangles.size(), sheetWidth, sheetHeight, allowRotation);

        try {
            // ── Expand by quantity and sort (largest-first) ───────────────────
            List<Rectangle> items = expandAndSort(rectangles);
            logger.debug("Expanded to {} individual items", items.size());

            // ── Pack ──────────────────────────────────────────────────────────
            List<Sheet> sheets = new ArrayList<>();
            int sheetNumber = 1;

            // Remove pieces that can NEVER fit even on a fresh empty sheet.
            // Without this guard, oversized pieces cause an infinite loop of empty sheets.
            items.removeIf(r -> {
                boolean fitsNormal  = r.getWidth()  <= sheetWidth  + EPS && r.getHeight() <= sheetHeight + EPS;
                boolean fitsRotated = allowRotation &&
                                      r.getHeight() <= sheetWidth  + EPS && r.getWidth()  <= sheetHeight + EPS;
                if (!fitsNormal && !fitsRotated) {
                    logger.warn("Skipping piece × — too large for sheet ×{}: {}x{} vs {}x{}",
                                r.getWidth(), r.getHeight(), sheetWidth, sheetHeight);
                    return true;
                }
                return false;
            });

            while (!items.isEmpty()) {
                Sheet sheet = new Sheet(sheetNumber++, sheetWidth, sheetHeight);
                // The sheet must start with exactly one free rect covering the whole area.
                sheet.getFreeSpaces().clear();
                sheet.getFreeSpaces().add(new FreeSpace(0, 0, sheetWidth, sheetHeight));

                // Try to place every remaining item on this sheet (one full pass).
                // Re-scan after each successful placement — a gap opened by placing
                // item i may now accept item i+1 that was previously skipped.
                boolean placedAny;
                do {
                    placedAny = false;
                    Iterator<Rectangle> it = items.iterator();
                    while (it.hasNext()) {
                        Rectangle rect = it.next();
                        Placement p = findBestPlacement(sheet, rect, allowRotation);
                        if (p != null) {
                            applyPlacement(sheet, rect, p);
                            it.remove();
                            placedAny = true;
                            logger.trace("Placed {}x{} at ({},{}) rotated={}",
                                         rect.getWidth(), rect.getHeight(),
                                         p.x, p.y, p.rotated);
                        }
                    }
                } while (placedAny && !items.isEmpty());

                // Safety net: if a full fresh sheet placed nothing, remaining items
                // will NEVER fit — break immediately instead of looping forever.
                if (sheet.getPlacedRectangles().isEmpty()) {
                    logger.warn("Sheet #{} placed 0 pieces — {} item(s) cannot fit, aborting.",
                                sheet.getSheetNumber(), items.size());
                    items.clear();
                    break;
                }

                logger.debug("Sheet #{} done — {} pieces, {}% util",
                             sheet.getSheetNumber(),
                             sheet.getPlacedRectangles().size(),
                             String.format("%.2f", sheet.getUtilization()));
                sheets.add(sheet);
            }

            // ── Statistics ────────────────────────────────────────────────────
            double totalArea   = (long) sheets.size() * sheetWidth * sheetHeight;
            double usedArea    = sheets.stream().mapToDouble(Sheet::getUsedArea).sum();
            double wasteArea   = totalArea - usedArea;
            double avgUtil     = sheets.stream().mapToDouble(Sheet::getUtilization).average().orElse(0);
            long   elapsed     = System.currentTimeMillis() - startTime;

            logger.info("Done — {} sheet(s), avg util {}%, {}ms",
                        sheets.size(), String.format("%.2f", avgUtil), elapsed);

            return new OptimizationResult(sheets, sheets.size(),
                                          totalArea, usedArea, wasteArea, avgUtil, elapsed);
        } catch (Exception e) {
            logger.error("Optimization failed", e);
            throw new RuntimeException("Optimization failed: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    //  PLACEMENT SEARCH  (BSSF + BAF tiebreak)
    // =========================================================================

    /**
     * Scores every free-rect × orientation combination and returns the best one.
     * Returns null if the rectangle cannot fit anywhere on this sheet.
     */
    private Placement findBestPlacement(Sheet sheet, Rectangle rect, boolean allowRotation) {
        Placement best = null;

        for (FreeSpace fs : sheet.getFreeSpaces()) {
            // Normal orientation
            Placement p = score(fs, rect.getWidth(), rect.getHeight(), false);
            if (p != null && isBetter(p, best)) best = p;

            // Rotated orientation (only if allowed and dimensions differ)
            if (allowRotation && !isSquare(rect)) {
                Placement pr = score(fs, rect.getHeight(), rect.getWidth(), true);
                if (pr != null && isBetter(pr, best)) best = pr;
            }
        }
        return best;
    }

    /**
     * Attempts to fit w×h into the given free space.
     * Returns a scored Placement, or null if it doesn't fit.
     *
     * BSSF score  = min leftover side  (lower is better)
     * BAF score   = leftover area      (lower is better, used as tiebreak)
     */
    private Placement score(FreeSpace fs, double w, double h, boolean rotated) {
        if (w > fs.getWidth() + EPS || h > fs.getHeight() + EPS) return null;

        double leftH = fs.getWidth()  - w;
        double leftV = fs.getHeight() - h;
        double shortSide = Math.min(leftH, leftV);
        double areaLeft  = fs.getWidth() * fs.getHeight() - w * h;

        return new Placement(fs.getX(), fs.getY(), w, h, rotated, shortSide, areaLeft);
    }

    /** True when p is strictly better than current best (may be null). */
    private boolean isBetter(Placement p, Placement best) {
        if (best == null) return true;
        if (p.shortSideFit < best.shortSideFit - EPS) return true;
        if (p.shortSideFit > best.shortSideFit + EPS) return false;
        return p.areaFit < best.areaFit - EPS;
    }

    // =========================================================================
    //  PLACEMENT APPLICATION  (the key correctness fix)
    // =========================================================================

    /**
     * Records the placement on the sheet and updates the free-rect list via
     * the correct MaxRects clipping procedure.
     */
    private void applyPlacement(Sheet sheet, Rectangle rect, Placement p) {
        // Record placed piece
        Rectangle placed = new Rectangle(
            rect.getId(),
            rect.getWidth(), rect.getHeight(),
            rect.getQuantity(),
            p.x, p.y,
            p.rotated
        );
        sheet.getPlacedRectangles().add(placed);

        // Update free-rect list: clip every free rect against the placed piece
        clipFreeRects(sheet, p.x, p.y, p.w, p.h);
    }

    /**
     * THE CORE FIX — correct MaxRects free-space update.
     *
     * For every free rect that overlaps the placed piece (px,py,pw,ph):
     *   1. Remove the original free rect.
     *   2. Generate up to 4 axis-aligned sub-rects (left, right, bottom, top).
     *   3. Keep only those with positive area.
     *
     * After all splits, prune free rects that are completely contained
     * inside another — this keeps the list efficient and correct.
     */
    private void clipFreeRects(Sheet sheet, double px, double py, double pw, double ph) {
        List<FreeSpace> result = new ArrayList<>();

        for (FreeSpace fs : sheet.getFreeSpaces()) {
            if (!overlaps(fs, px, py, pw, ph)) {
                // No intersection — keep as-is
                result.add(fs);
                continue;
            }
            // This free rect overlaps the placed piece — split it

            // Left sub-rect
            if (fs.getX() < px - EPS) {
                double subW = px - fs.getX();
                addIfValid(result, fs.getX(), fs.getY(), subW, fs.getHeight());
            }
            // Right sub-rect
            if (fs.getX() + fs.getWidth() > px + pw + EPS) {
                double newX = px + pw;
                double subW = fs.getX() + fs.getWidth() - newX;
                addIfValid(result, newX, fs.getY(), subW, fs.getHeight());
            }
            // Bottom sub-rect
            if (fs.getY() < py - EPS) {
                double subH = py - fs.getY();
                addIfValid(result, fs.getX(), fs.getY(), fs.getWidth(), subH);
            }
            // Top sub-rect
            if (fs.getY() + fs.getHeight() > py + ph + EPS) {
                double newY = py + ph;
                double subH = fs.getY() + fs.getHeight() - newY;
                addIfValid(result, fs.getX(), newY, fs.getWidth(), subH);
            }
        }

        // Replace the sheet's free-space list
        sheet.getFreeSpaces().clear();
        sheet.getFreeSpaces().addAll(result);

        // Prune: remove any free rect completely covered by another
        pruneFreeSpaces(sheet);
    }

    /** Adds a FreeSpace to the list only if its dimensions are meaningfully positive. */
    private void addIfValid(List<FreeSpace> list, double x, double y, double w, double h) {
        if (w > EPS && h > EPS) {
            list.add(new FreeSpace(x, y, w, h));
        }
    }

    /**
     * Removes free rects that are completely contained within another free rect.
     * These are redundant and inflate the search space without adding new
     * placement options.
     */
    private void pruneFreeSpaces(Sheet sheet) {
        List<FreeSpace> spaces = sheet.getFreeSpaces();
        List<FreeSpace> pruned = new ArrayList<>(spaces.size());

        outer:
        for (int i = 0; i < spaces.size(); i++) {
            FreeSpace a = spaces.get(i);
            for (int j = 0; j < spaces.size(); j++) {
                if (i == j) continue;
                FreeSpace b = spaces.get(j);
                // If 'a' is completely inside 'b', discard 'a'
                if (containedIn(a, b)) continue outer;
            }
            pruned.add(a);
        }

        spaces.clear();
        spaces.addAll(pruned);
    }

    // =========================================================================
    //  GEOMETRY HELPERS
    // =========================================================================

    /** True if rectangles (px,py,pw,ph) and free-space fs share any area. */
    private boolean overlaps(FreeSpace fs, double px, double py, double pw, double ph) {
        return px < fs.getX() + fs.getWidth()  - EPS
            && px + pw > fs.getX()             + EPS
            && py < fs.getY() + fs.getHeight() - EPS
            && py + ph > fs.getY()             + EPS;
    }

    /** True if free rect 'inner' is completely covered by free rect 'outer'. */
    private boolean containedIn(FreeSpace inner, FreeSpace outer) {
        return inner.getX()                  >= outer.getX()                  - EPS
            && inner.getY()                  >= outer.getY()                  - EPS
            && inner.getX() + inner.getWidth()  <= outer.getX() + outer.getWidth()  + EPS
            && inner.getY() + inner.getHeight() <= outer.getY() + outer.getHeight() + EPS;
    }

    private boolean isSquare(Rectangle r) {
        return Math.abs(r.getWidth() - r.getHeight()) < EPS;
    }

    // =========================================================================
    //  INPUT PREPARATION
    // =========================================================================

    /**
     * Expands each Rectangle by its quantity into individual items,
     * then sorts them for best packing:
     *   1. Descending area       (large pieces first)
     *   2. Descending perimeter  (squarish pieces before thin ones of same area)
     *   3. Descending max side   (final tiebreak)
     */
    private List<Rectangle> expandAndSort(List<Rectangle> rects) {
        List<Rectangle> items = new ArrayList<>();
        for (Rectangle r : rects) {
            for (int i = 0; i < r.getQuantity(); i++) {
                items.add(new Rectangle(r.getId(), r.getWidth(), r.getHeight(), 1));
            }
        }
        items.sort(Comparator
            .comparingDouble(Rectangle::getArea)
            .thenComparingDouble((Rectangle r) -> r.getWidth() + r.getHeight())
            .thenComparingDouble((Rectangle r) -> Math.max(r.getWidth(), r.getHeight()))
            .reversed());
        return items;
    }

    // =========================================================================
    //  INTERNAL VALUE OBJECT
    // =========================================================================

    /** Immutable result of a placement-score calculation. */
    private static final class Placement {
        final double  x, y, w, h;
        final boolean rotated;
        final double  shortSideFit; // lower = better
        final double  areaFit;      // lower = better (tiebreak)

        Placement(double x, double y, double w, double h,
                  boolean rotated, double shortSideFit, double areaFit) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.rotated      = rotated;
            this.shortSideFit = shortSideFit;
            this.areaFit      = areaFit;
        }
    }
}