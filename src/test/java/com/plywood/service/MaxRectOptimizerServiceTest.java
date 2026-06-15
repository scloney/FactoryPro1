package com.plywood.service;

import com.plywood.model.OptimizationResult;
import com.plywood.model.Rectangle;
import com.plywood.model.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MaxRectOptimizerService}.
 *
 * <p>Tests are deliberately kept independent of Spring context (no @SpringBootTest)
 * so they run fast as pure unit tests. The service has no Spring-injected
 * dependencies, so a plain {@code new MaxRectOptimizerService()} works fine.</p>
 *
 * Test categories
 * ───────────────
 *  1. Basic placement — single / few pieces that trivially fit
 *  2. Perfect fill    — pieces whose total area equals the sheet area
 *  3. Multi-sheet     — overflow forces a second sheet
 *  4. Rotation        — rotated piece fits where upright doesn't
 *  5. Overlap check   — no two placed pieces may overlap
 *  6. Utilisation     — reported stats are consistent with placements
 *  7. Edge cases      — empty input, single huge piece, pieces larger than sheet
 *  8. Sort order      — large pieces placed before small ones
 */
@DisplayName("MaxRectOptimizerService")
class MaxRectOptimizerServiceTest {

    private MaxRectOptimizerService service;

    // A convenient sheet size used across most tests (cm or mm — units don't matter)
    private static final double SHEET_W = 244;
    private static final double SHEET_H = 122;
    private static final double EPS = 1e-6;

    @BeforeEach
    void setUp() {
        service = new MaxRectOptimizerService();
    }

    // =========================================================================
    //  1. Basic placement
    // =========================================================================

    @Test
    @DisplayName("Single piece smaller than sheet is placed on one sheet")
    void singleSmallPiecePlacedOnOneSheet() {
        List<Rectangle> input = List.of(new Rectangle(1, 50, 30, 1));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertNotNull(result);
        assertEquals(1, result.getTotalSheets(), "Should require only one sheet");
        assertEquals(1, result.getSheets().get(0).getPlacedRectangles().size(),
                "The single piece must appear in the placed list");
    }

    @Test
    @DisplayName("Multiple pieces all fitting on one sheet are placed on one sheet")
    void multiplePiecesOnOneSheet() {
        // Total area = 4 × (50×30) = 6 000, sheet area = 244×122 = 29 768 → ample room
        List<Rectangle> input = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            input.add(new Rectangle(i, 50, 30, 1));
        }

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertEquals(1, result.getTotalSheets());
        assertEquals(4, result.getSheets().get(0).getPlacedRectangles().size());
    }

    @Test
    @DisplayName("Placed piece position coordinates are non-negative")
    void placedPieceHasNonNegativeCoordinates() {
        List<Rectangle> input = List.of(new Rectangle(1, 100, 60, 3));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        for (Sheet sheet : result.getSheets()) {
            for (Rectangle r : sheet.getPlacedRectangles()) {
                assertTrue(r.getX() >= -EPS, "x must be >= 0, got " + r.getX());
                assertTrue(r.getY() >= -EPS, "y must be >= 0, got " + r.getY());
            }
        }
    }

    // =========================================================================
    //  2. Perfect-fill scenario
    // =========================================================================

    @Test
    @DisplayName("Four equal quadrants perfectly fill one sheet with ~100% utilisation")
    void perfectFillFourQuadrants() {
        double hw = SHEET_W / 2;  // 122
        double hh = SHEET_H / 2;  // 61

        List<Rectangle> input = List.of(new Rectangle(1, hw, hh, 4));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertEquals(1, result.getTotalSheets(), "All quadrants should fit on one sheet");

        double util = result.getSheets().get(0).getUtilization();
        assertEquals(100.0, util, 0.5, "Utilisation should be ~100% for a perfect fill");
    }

    @Test
    @DisplayName("Used area equals sum of individual rectangle areas")
    void usedAreaMatchesSumOfRectangleAreas() {
        List<Rectangle> input = List.of(
                new Rectangle(1, 80, 40, 2),
                new Rectangle(2, 60, 50, 3)
        );
        // Total area = 2×(80×40) + 3×(60×50) = 6 400 + 9 000 = 15 400
        double expectedUsed = 2 * 80 * 40 + 3 * 60 * 50;

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertEquals(expectedUsed, result.getUsedArea(), 0.1,
                "usedArea must equal the sum of all placed rectangle areas");
    }

    // =========================================================================
    //  3. Multi-sheet overflow
    // =========================================================================

    @Test
    @DisplayName("Pieces that do not all fit on one sheet overflow to a second sheet")
    void overflowToSecondSheet() {
        // Each piece is 200×100 — two fit on the 244×122 sheet, third needs another
        List<Rectangle> input = List.of(new Rectangle(1, 200, 100, 3));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertTrue(result.getTotalSheets() >= 2,
                "Three 200×100 pieces cannot all fit on one 244×122 sheet");
        // All three must be placed somewhere
        int totalPlaced = result.getSheets().stream()
                .mapToInt(s -> s.getPlacedRectangles().size())
                .sum();
        assertEquals(3, totalPlaced, "Every piece must be placed across sheets");
    }

    @Test
    @DisplayName("Total area reported equals sheetCount × sheetWidth × sheetHeight")
    void totalAreaIsConsistentWithSheetCount() {
        List<Rectangle> input = List.of(new Rectangle(1, 200, 100, 5));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        double expected = (double) result.getTotalSheets() * SHEET_W * SHEET_H;
        assertEquals(expected, result.getTotalArea(), EPS);
    }

    // =========================================================================
    //  4. Rotation
    // =========================================================================

    @Test
    @DisplayName("Piece that only fits when rotated is placed when allowRotation=true")
    void pieceOnlyFitsWhenRotated() {
        // Sheet is tall-narrow: 60 wide × 244 tall.
        // Piece is 50×100 — upright (50×100) fits; rotated (100×50) also fits.
        // Let's use a sheet that is exactly 50 tall but 100 wide.
        // Upright  = 80×50  → fits in 100×50 sheet normally.
        // Rotated  = 50×80  → also fits — but let's find a case that *requires* rotation.
        //
        // Sheet: 30 wide × 100 tall.
        // Piece: 100 wide × 25 tall. Upright (100×25) does NOT fit (100 > 30).
        // Rotated (25×100) fits perfectly.
        double narrowW = 30, tallH = 100;
        List<Rectangle> input = List.of(new Rectangle(1, 100, 25, 1));

        OptimizationResult resultNoRotation  = service.optimize(input, narrowW, tallH, false);
        OptimizationResult resultWithRotation = service.optimize(input, narrowW, tallH, true);

        // Without rotation the piece cannot fit
        int placedNoRot = resultNoRotation.getSheets().stream()
                .mapToInt(s -> s.getPlacedRectangles().size()).sum();
        // With rotation the piece must be placed
        int placedRot = resultWithRotation.getSheets().stream()
                .mapToInt(s -> s.getPlacedRectangles().size()).sum();

        assertEquals(0, placedNoRot,  "Upright 100×25 cannot fit in a 30-wide sheet");
        assertEquals(1, placedRot,    "Rotated 25×100 must fit in a 30×100 sheet");
        assertTrue(resultWithRotation.getSheets().get(0).getPlacedRectangles().get(0).isRotated(),
                "The placed piece should be marked as rotated");
    }

    @Test
    @DisplayName("allowRotation=false never marks any piece as rotated")
    void noRotationFlagHonoured() {
        List<Rectangle> input = List.of(
                new Rectangle(1, 80, 40, 5),
                new Rectangle(2, 50, 30, 3)
        );

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        for (Sheet sheet : result.getSheets()) {
            for (Rectangle r : sheet.getPlacedRectangles()) {
                assertFalse(r.isRotated(), "No piece should be rotated when rotation is disabled");
            }
        }
    }

    // =========================================================================
    //  5. No-overlap invariant
    // =========================================================================

    @Test
    @DisplayName("No two placed pieces overlap on any sheet")
    void noOverlapOnAnySheet() {
        List<Rectangle> input = new ArrayList<>();
        // Dense packing: many medium pieces
        for (int i = 1; i <= 10; i++) {
            input.add(new Rectangle(i, 60, 40, 1));
        }

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, true);

        for (Sheet sheet : result.getSheets()) {
            List<Rectangle> placed = sheet.getPlacedRectangles();
            for (int i = 0; i < placed.size(); i++) {
                for (int j = i + 1; j < placed.size(); j++) {
                    assertFalse(
                            overlaps(placed.get(i), placed.get(j)),
                            String.format("Pieces %d and %d overlap on sheet #%d",
                                    i, j, sheet.getSheetNumber())
                    );
                }
            }
        }
    }

    @Test
    @DisplayName("No placed piece exceeds sheet boundaries")
    void noPlacedPieceExceedsBoundaries() {
        List<Rectangle> input = List.of(
                new Rectangle(1, 100, 60, 6),
                new Rectangle(2, 50,  30, 4)
        );

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, true);

        for (Sheet sheet : result.getSheets()) {
            for (Rectangle r : sheet.getPlacedRectangles()) {
                double pieceW = r.isRotated() ? r.getHeight() : r.getWidth();
                double pieceH = r.isRotated() ? r.getWidth()  : r.getHeight();
                assertTrue(r.getX() + pieceW <= SHEET_W + EPS,
                        "Piece exceeds sheet width: x=" + r.getX() + " w=" + pieceW);
                assertTrue(r.getY() + pieceH <= SHEET_H + EPS,
                        "Piece exceeds sheet height: y=" + r.getY() + " h=" + pieceH);
            }
        }
    }

    // =========================================================================
    //  6. Utilisation stats
    // =========================================================================

    @Test
    @DisplayName("wasteArea = totalArea − usedArea")
    void wasteAreaIsConsistent() {
        List<Rectangle> input = List.of(new Rectangle(1, 80, 50, 4));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertEquals(result.getTotalArea() - result.getUsedArea(),
                result.getWasteArea(), 0.1,
                "wasteArea must equal totalArea - usedArea");
    }

    @Test
    @DisplayName("optimizationTimeMs is recorded and >= 0")
    void optimizationTimeIsRecorded() {
        List<Rectangle> input = List.of(new Rectangle(1, 50, 30, 5));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertTrue(result.getOptimizationTimeMs() >= 0,
                "optimizationTimeMs must be non-negative");
    }

    // =========================================================================
    //  7. Edge cases
    // =========================================================================

    @Test
    @DisplayName("Empty input list produces zero sheets")
    void emptyInputProducesZeroSheets() {
        OptimizationResult result = service.optimize(List.of(), SHEET_W, SHEET_H, false);

        assertNotNull(result);
        assertEquals(0, result.getTotalSheets(), "No input → no sheets");
        assertEquals(0.0, result.getUsedArea(), EPS);
    }

    @Test
    @DisplayName("Single piece exactly matching the sheet dimensions fills one sheet perfectly")
    void singlePieceExactSheetSize() {
        List<Rectangle> input = List.of(new Rectangle(1, SHEET_W, SHEET_H, 1));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        assertEquals(1, result.getTotalSheets());
        assertEquals(1, result.getSheets().get(0).getPlacedRectangles().size());
        assertEquals(100.0, result.getSheets().get(0).getUtilization(), 0.5);
    }

    @Test
    @DisplayName("Piece larger than sheet in both dimensions produces an empty sheet (cannot be placed)")
    void pieceLargerThanSheetIsNotPlaced() {
        List<Rectangle> input = List.of(
                new Rectangle(1, SHEET_W + 10, SHEET_H + 10, 1)
        );

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        // The piece cannot fit anywhere; the algorithm should still terminate cleanly.
        // Depending on implementation, it may produce 1 empty sheet or handle silently.
        // What must NOT happen: an exception or infinite loop.
        int totalPlaced = result.getSheets().stream()
                .mapToInt(s -> s.getPlacedRectangles().size())
                .sum();
        assertEquals(0, totalPlaced, "An oversized piece must not appear in placed list");
    }

    @Test
    @DisplayName("Quantity expansion: one Rectangle with quantity=3 results in 3 placed pieces")
    void quantityExpansionProducesCorrectPieceCount() {
        List<Rectangle> input = List.of(new Rectangle(1, 60, 40, 3));

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        int totalPlaced = result.getSheets().stream()
                .mapToInt(s -> s.getPlacedRectangles().size())
                .sum();
        assertEquals(3, totalPlaced, "Quantity=3 must expand to exactly 3 placed pieces");
    }

    // =========================================================================
    //  8. Sort order — large pieces should be placed before small ones
    // =========================================================================

    @Test
    @DisplayName("Large pieces appear first in the placement list when mixed sizes are given")
    void largePiecesPlacedFirstOnSheet() {
        // Mix of a big piece and several small pieces
        List<Rectangle> input = List.of(
                new Rectangle(1, 20,  10, 5),   // small
                new Rectangle(2, 200, 80, 1)    // large — must be placed first
        );

        OptimizationResult result = service.optimize(input, SHEET_W, SHEET_H, false);

        // The first placed rectangle on sheet 1 should be the large one (id=2 or area=16000)
        Rectangle first = result.getSheets().get(0).getPlacedRectangles().get(0);
        double firstArea = first.getWidth() * first.getHeight();
        // Large piece area = 200×80 = 16 000; small = 20×10 = 200
        assertTrue(firstArea > 1000,
                "The first placed piece should be the large one, area=" + firstArea);
    }

    // =========================================================================
    //  Helper: axis-aligned rectangle overlap test
    // =========================================================================

    /**
     * Returns true if two placed rectangles share any interior area.
     * Respects the {@code rotated} flag on each piece.
     */
    private boolean overlaps(Rectangle a, Rectangle b) {
        double aw = a.isRotated() ? a.getHeight() : a.getWidth();
        double ah = a.isRotated() ? a.getWidth()  : a.getHeight();
        double bw = b.isRotated() ? b.getHeight() : b.getWidth();
        double bh = b.isRotated() ? b.getWidth()  : b.getHeight();

        double ax1 = a.getX(), ay1 = a.getY(), ax2 = ax1 + aw, ay2 = ay1 + ah;
        double bx1 = b.getX(), by1 = b.getY(), bx2 = bx1 + bw, by2 = by1 + bh;

        return ax1 < bx2 - EPS && ax2 > bx1 + EPS
            && ay1 < by2 - EPS && ay2 > by1 + EPS;
    }
}
