package com.plywood.service;

import com.plywood.model.OptimizationResult;
import com.plywood.model.Rectangle;
import com.plywood.model.Sheet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OptimizerPdfService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizerPdfService.class);
    private static final DecimalFormat df2 = new DecimalFormat("#.##");
    private static final DecimalFormat df1 = new DecimalFormat("#.#");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy");

    // ── Page dimensions (A4 points) ──────────────────────────────────────────
    private static final float PW = PDRectangle.A4.getWidth();   // 595
    private static final float PH = PDRectangle.A4.getHeight();  // 842

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final float SIDEBAR_W  = 68f;
    private static final float CONTENT_X  = SIDEBAR_W + 18f;
    private static final float CONTENT_W  = PW - CONTENT_X - 30f;
    private static final float MARGIN_BOT = 45f;

    // ── Color palette — Option C Modern Dashboard ────────────────────────────
    private static final Color NAVY        = new Color(26,  36,  56);
    private static final Color NAVY_EDGE   = new Color(45,  63,  90);
    private static final Color NAVY_DIM    = new Color(36,  52,  82);
    private static final Color BLUE        = new Color(37,  99, 235);
    private static final Color BLUE_LIGHT  = new Color(224, 231, 255);
    private static final Color BLUE_LABEL  = new Color(74,  96, 144);
    private static final Color BLUE_MUTED  = new Color(58,  85, 128);
    private static final Color GREEN       = new Color(22, 163,  74);
    private static final Color GREEN_LIGHT = new Color(220, 252, 231);
    private static final Color AMBER       = new Color(217, 119,   6);
    private static final Color AMBER_LIGHT = new Color(254, 243, 199);
    private static final Color RED         = new Color(220,  38,  38);
    private static final Color RED_LIGHT   = new Color(254, 226, 226);
    private static final Color CARD_BG     = new Color(240, 244, 255);
    private static final Color PAGE_BG     = new Color(248, 250, 252);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BORDER      = new Color(226, 232, 240);
    private static final Color RING_BG     = new Color(220, 225, 235);
    private static final Color WHITE       = Color.WHITE;

    // ── Piece fill / stroke palette (10 slots, cycles) ──────────────────────
    private static final Color[] PIECE_FILL = {
        new Color(187, 212, 255), new Color(179, 236, 212), new Color(253, 232, 192),
        new Color(255, 214, 214), new Color(226, 214, 255), new Color(201, 240, 255),
        new Color(255, 230, 153), new Color(212, 240, 194), new Color(255, 214, 240),
        new Color(204, 229, 255)
    };
    private static final Color[] PIECE_STROKE = {
        new Color(59,  109, 181), new Color(29,  158,  93), new Color(186, 117,  23),
        new Color(163,  45,  45), new Color(83,   74, 183), new Color(24,  110, 164),
        new Color(151, 110,  16), new Color(59,  138,  48), new Color(153,  51,  87),
        new Color(36,  100, 164)
    };

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    public byte[] generatePdf(OptimizationResult result,
                               double sheetWidth, double sheetHeight) throws IOException {
        logger.info("Generating Option-C dashboard PDF");
        try (PDDocument doc = new PDDocument()) {
            addSummaryPage(doc, result, sheetWidth, sheetHeight);
            if (result.getSheets() != null) {
                int total = result.getSheets().size();
                for (int i = 0; i < total; i++) {
                    addSheetPage(doc, result.getSheets().get(i), i + 1, total, sheetWidth, sheetHeight);
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            logger.info("PDF generated — {} page(s)", 1 + (result.getSheets() != null ? result.getSheets().size() : 0));
            return baos.toByteArray();
        }
    }

    // =========================================================================
    //  SUMMARY PAGE
    // =========================================================================

    private void addSummaryPage(PDDocument doc, OptimizationResult result,
                                 double sheetWidth, double sheetHeight) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            fillRect(cs, 0, 0, PW, PH, PAGE_BG);
            drawSidebar(cs, "PLYWOOD OPTIMIZER", "OPTIMIZATION REPORT",
                        DATE_FMT.format(new Date()), null, 0);

            float cx = CONTENT_X;
            float cy = PH;

            // ── Page header ──────────────────────────────────────────────────
            cy -= 48f;
            setFont(cs, PDType1Font.HELVETICA_BOLD, 18, TEXT_DARK);
            drawText(cs, "Optimization Report", cx, cy);

            cy -= 16f;
            setFont(cs, PDType1Font.HELVETICA, 9, TEXT_MUTED);
            drawText(cs, "Sheet dimensions: " + df2.format(sheetWidth)
                    + " \u00D7 " + df2.format(sheetHeight) + " mm", cx, cy);

            cy -= 8f;
            drawHLine(cs, cx, cy, CONTENT_W, BORDER, 0.5f);
            cy -= 22f;

            // ── Utilization ring + stat cards ────────────────────────────────
            double util    = result.getAverageUtilization();
            Color utilCol  = utilColor(util);
            float ringCx   = cx + 58f;
            float ringCy   = cy - 52f;
            float ringR    = 42f;
            float ringLW   = 12f;

            // Background ring
            drawRingBackground(cs, ringCx, ringCy, ringR, RING_BG, ringLW);
            // Utilized arc
            drawRingArc(cs, ringCx, ringCy, ringR, util, utilCol, ringLW);
            // Center labels
            setFont(cs, PDType1Font.HELVETICA_BOLD, 12, TEXT_DARK);
            drawTextCentered(cs, df1.format(util) + "%", ringCx, ringCy + 5f, 12);
            setFont(cs, PDType1Font.HELVETICA, 7, TEXT_MUTED);
            drawTextCentered(cs, "avg util", ringCx, ringCy - 9f, 7);

            // Stat cards (2 × 2 grid to the right of the ring)
            float cardX   = cx + 128f;
            float cardW   = (CONTENT_W - 128f - 8f) / 2f - 6f;
            float cardH   = 44f;
            float cardGap = 8f;

            drawStatCard(cs, cardX,             cy - cardH + 4f,
                         cardW, cardH, "Total sheets",
                         String.valueOf(result.getTotalSheets()), "", CARD_BG, TEXT_DARK, BLUE);

            drawStatCard(cs, cardX + cardW + cardGap, cy - cardH + 4f,
                         cardW, cardH, "Optimization time",
                         String.valueOf(result.getOptimizationTimeMs()), "ms", CARD_BG, TEXT_DARK, BLUE);

            drawStatCard(cs, cardX, cy - cardH * 2 - cardGap + 4f,
                         cardW, cardH, "Used area",
                         df2.format(result.getUsedArea() / 1_000_000), "m\u00B2",
                         GREEN_LIGHT, GREEN, GREEN);

            drawStatCard(cs, cardX + cardW + cardGap, cy - cardH * 2 - cardGap + 4f,
                         cardW, cardH, "Waste area",
                         df2.format(result.getWasteArea() / 1_000_000), "m\u00B2",
                         RED_LIGHT, RED, RED);

            cy -= (ringR * 2 + 34f);

            // ── Per-sheet utilization bars ────────────────────────────────────
            drawHLine(cs, cx, cy, CONTENT_W, BORDER, 0.5f);
            cy -= 14f;

            setFont(cs, PDType1Font.HELVETICA_BOLD, 9, TEXT_DARK);
            drawText(cs, "Utilization by sheet", cx, cy);
            cy -= 16f;

            List<Sheet> sheets = result.getSheets();
            if (sheets != null) {
                for (int i = 0; i < sheets.size() && cy > MARGIN_BOT + 30; i++) {
                    Sheet s      = sheets.get(i);
                    double u     = s.getUtilization();
                    Color barCol = utilColor(u);
                    int pieces   = s.getPlacedRectangles() != null ? s.getPlacedRectangles().size() : 0;

                    // Row label
                    setFont(cs, PDType1Font.HELVETICA, 8, TEXT_MUTED);
                    drawText(cs, "Sheet " + s.getSheetNumber() + "  \u00B7  " + pieces + " pieces", cx, cy);

                    // Percentage right-aligned
                    setFont(cs, PDType1Font.HELVETICA_BOLD, 8, barCol);
                    drawTextRight(cs, df1.format(u) + "%", cx + CONTENT_W, cy, 8);

                    cy -= 11f;

                    // Bar track
                    float barH = 8f;
                    fillRoundedRect(cs, cx, cy - barH, CONTENT_W, barH, 3f, new Color(218, 224, 235));
                    // Bar fill
                    float fillW = Math.max(6f, (float)(CONTENT_W * u / 100.0));
                    fillRoundedRect(cs, cx, cy - barH, fillW, barH, 3f, barCol);

                    cy -= (barH + 14f);
                }
            }

            // ── Total area summary row ────────────────────────────────────────
            if (cy > MARGIN_BOT + 20) {
                drawHLine(cs, cx, cy, CONTENT_W, BORDER, 0.5f);
                cy -= 14f;
                setFont(cs, PDType1Font.HELVETICA, 8, TEXT_MUTED);
                drawText(cs, "Total area: " + df2.format(result.getTotalArea() / 1_000_000) + " m\u00B2", cx, cy);
                drawTextRight(cs, "Waste: " + df2.format(result.getWasteArea() / 1_000_000) + " m\u00B2",
                              cx + CONTENT_W, cy, 8);
            }

            drawFooter(cs, 1, 1 + (sheets != null ? sheets.size() : 0));
        }
    }

    // =========================================================================
    //  SHEET VISUALIZATION PAGE
    // =========================================================================

    private void addSheetPage(PDDocument doc, Sheet sheet, int idx, int total,
                               double sheetWidth, double sheetHeight) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            fillRect(cs, 0, 0, PW, PH, PAGE_BG);

            double util  = sheet.getUtilization();
            int    pieces = sheet.getPlacedRectangles() != null ? sheet.getPlacedRectangles().size() : 0;

            drawSidebar(cs, "SHEET " + idx + " / " + total,
                        pieces + " PIECES",
                        df1.format(util) + "%",
                        utilColor(util), util);

            float cx = CONTENT_X;
            float cy = PH;

            // ── Page header ──────────────────────────────────────────────────
            cy -= 48f;
            setFont(cs, PDType1Font.HELVETICA_BOLD, 16, TEXT_DARK);
            drawText(cs, "Sheet " + idx + " layout", cx, cy);

            cy -= 15f;
            setFont(cs, PDType1Font.HELVETICA, 9, TEXT_MUTED);
            drawText(cs, df2.format(sheetWidth) + " \u00D7 " + df2.format(sheetHeight)
                    + " mm  \u00B7  " + pieces + " pieces"
                    + "  \u00B7  waste: " + df1.format(100 - util) + "%", cx, cy);

            cy -= 8f;
            drawHLine(cs, cx, cy, CONTENT_W, BORDER, 0.5f);
            cy -= 14f;

            // ── Visualization box ────────────────────────────────────────────
            float legendH = 50f;   // reserve space for legend at bottom
            float vizMaxH = cy - MARGIN_BOT - legendH;
            float vizMaxW = CONTENT_W;

            double scale  = Math.min(vizMaxW / sheetWidth, vizMaxH / sheetHeight) * 0.96;
            float  vizW   = (float)(sheetWidth  * scale);
            float  vizH   = (float)(sheetHeight * scale);
            float  vizX   = cx + (vizMaxW - vizW) / 2f;
            float  vizY   = cy - vizH;

            // Sheet background & border
            fillRect  (cs, vizX, vizY, vizW, vizH, WHITE);
            strokeRect(cs, vizX, vizY, vizW, vizH, NAVY, 1.5f);

            // Draw placed pieces
            if (sheet.getPlacedRectangles() != null) {
                int ci = 0;
                for (Rectangle rect : sheet.getPlacedRectangles()) {
                    double pw = rect.isRotated() ? rect.getHeight() : rect.getWidth();
                    double ph = rect.isRotated() ? rect.getWidth()  : rect.getHeight();

                    float rx = vizX + (float)(rect.getX() * scale);
                    float ry = vizY + (float)(rect.getY() * scale);
                    float rw = (float)(pw * scale);
                    float rh = (float)(ph * scale);

                    Color fill   = PIECE_FILL  [ci % PIECE_FILL.length];
                    Color stroke = PIECE_STROKE [ci % PIECE_STROKE.length];

                    fillRect  (cs, rx, ry, rw, rh, fill);
                    strokeRect(cs, rx, ry, rw, rh, stroke, 0.8f);

                    // Dimension label inside piece (only when large enough)
                    if (rw > 32f && rh > 16f) {
                        int fz = (rw > 55f && rh > 22f) ? 6 : 5;
                        String lbl = df2.format(rect.getWidth()) + "\u00D7" + df2.format(rect.getHeight());
                        if (rect.isRotated()) lbl += " R";
                        setFont(cs, PDType1Font.HELVETICA, fz, stroke);
                        drawTextCentered(cs, lbl, rx + rw / 2f, ry + rh / 2f - fz / 2f, fz);
                    }
                    ci++;
                }
            }

            // Scale note & waste badge
            setFont(cs, PDType1Font.HELVETICA, 7, TEXT_MUTED);
            drawText(cs, "Scale 1:" + Math.round(1.0 / scale), vizX, vizY - 9f);

            float wastePct = (float)(100.0 - util);
            drawBadge(cs, vizX + vizW, vizY + vizH + 5f,
                      "Waste " + df1.format(wastePct) + "%", RED_LIGHT, RED, true);

            // ── Piece legend (colored pills) ─────────────────────────────────
            cy = vizY - 26f;
            setFont(cs, PDType1Font.HELVETICA_BOLD, 7, TEXT_MUTED);
            drawText(cs, "Piece index:", cx, cy);
            cy -= 13f;

            if (sheet.getPlacedRectangles() != null) {
                float pillX = cx;
                float pillH = 13f;
                int ci = 0;
                for (Rectangle rect : sheet.getPlacedRectangles()) {
                    Color fill   = PIECE_FILL  [ci % PIECE_FILL.length];
                    Color stroke = PIECE_STROKE [ci % PIECE_STROKE.length];

                    String lbl  = "#" + (ci + 1) + "  "
                                + df2.format(rect.getWidth()) + "\u00D7" + df2.format(rect.getHeight());
                    if (rect.isRotated()) lbl += " R";

                    float pillW = lbl.length() * 5.0f + 10f;

                    // Wrap to next row if needed
                    if (pillX + pillW > cx + CONTENT_W) {
                        pillX = cx;
                        cy   -= (pillH + 4f);
                        if (cy < MARGIN_BOT) break;
                    }

                    fillRoundedRect  (cs, pillX, cy - pillH + 2f, pillW, pillH, 3f, fill);
                    strokeRoundedRect(cs, pillX, cy - pillH + 2f, pillW, pillH, 3f, stroke, 0.5f);
                    setFont(cs, PDType1Font.HELVETICA, 6, stroke);
                    drawText(cs, lbl, pillX + 5f, cy - 7f);

                    pillX += pillW + 5f;
                    ci++;
                }
            }

            drawFooter(cs, idx + 1, total + 1);
        }
    }

    // =========================================================================
    //  SIDEBAR
    // =========================================================================

    private void drawSidebar(PDPageContentStream cs,
                              String topLabel, String midLabel,
                              String badge, Color badgeColor,
                              double util) throws IOException {
        // Background fill
        fillRect(cs, 0, 0, SIDEBAR_W, PH, NAVY);

        // Inner right edge accent
        cs.setStrokingColor(NAVY_EDGE);
        cs.setLineWidth(0.5f);
        cs.moveTo(SIDEBAR_W - 0.5f, 0);
        cs.lineTo(SIDEBAR_W - 0.5f, PH);
        cs.stroke();

        float midX = SIDEBAR_W / 2f;

        // Top rotated label (app name / sheet number)
        setFont(cs, PDType1Font.HELVETICA_BOLD, 7, BLUE_LABEL);
        drawTextRotated(cs, topLabel, midX, PH - 28f, (float)Math.PI / 2);

        // Thin divider
        cs.setStrokingColor(NAVY_EDGE);
        cs.setLineWidth(0.5f);
        cs.moveTo(midX - 12f, PH - 118f);
        cs.lineTo(midX + 12f, PH - 118f);
        cs.stroke();

        // Badge — colored util% on sheet pages, text on summary
        if (badgeColor != null) {
            float bW = 46f, bH = 20f;
            float bX = midX - bW / 2f, bY = PH - 164f;
            fillRoundedRect(cs, bX, bY, bW, bH, 5f, badgeColor);
            setFont(cs, PDType1Font.HELVETICA_BOLD, 9, WHITE);
            drawTextCentered(cs, badge, midX, bY + 6f, 9);
        } else {
            setFont(cs, PDType1Font.HELVETICA_BOLD, 8, BLUE_LIGHT);
            drawTextRotated(cs, badge, midX, PH - 178f, (float)Math.PI / 2);
        }

        // Lower rotated label (piece count / report subtitle)
        setFont(cs, PDType1Font.HELVETICA, 7, BLUE_MUTED);
        drawTextRotated(cs, midLabel, midX, 90f, (float)Math.PI / 2);

        // Decorative dots at the bottom
        for (int i = 0; i < 4; i++) {
            fillCircle(cs, midX, 28f + i * 9f, 2f, NAVY_DIM);
        }
    }

    // =========================================================================
    //  STAT CARD
    // =========================================================================

    private void drawStatCard(PDPageContentStream cs,
                               float x, float y, float w, float h,
                               String label, String value, String unit,
                               Color bg, Color valueColor, Color labelColor) throws IOException {
        fillRoundedRect(cs, x, y, w, h, 5f, bg);

        setFont(cs, PDType1Font.HELVETICA, 7, labelColor);
        drawText(cs, label, x + 9f, y + h - 12f);

        setFont(cs, PDType1Font.HELVETICA_BOLD, 16, valueColor);
        drawText(cs, value, x + 9f, y + 10f);

        if (!unit.isEmpty()) {
            float valW = value.length() * 9.0f;
            setFont(cs, PDType1Font.HELVETICA, 8, labelColor);
            drawText(cs, unit, x + 9f + valW + 2f, y + 10f);
        }
    }

    // =========================================================================
    //  RING CHART
    // =========================================================================

    private void drawRingBackground(PDPageContentStream cs, float cx, float cy,
                                     float r, Color color, float lineW) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(lineW);
        arcPath(cs, cx, cy, r, 0f, 360f);
        cs.stroke();
    }

    private void drawRingArc(PDPageContentStream cs, float cx, float cy, float r,
                              double utilPct, Color color, float lineW) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(lineW);
        cs.setLineCapStyle(1);   // round caps for clean arc ends
        float startDeg = 90f;
        float endDeg   = (float)(90.0 - utilPct / 100.0 * 360.0);
        arcPath(cs, cx, cy, r, startDeg, endDeg);
        cs.stroke();
        cs.setLineCapStyle(0);
    }

    /**
     * Appends a clockwise arc from startDeg → endDeg using cubic Bezier segments.
     * Angles are in degrees; Y-axis points up (standard PDF coordinates).
     */
    private void arcPath(PDPageContentStream cs, float cx, float cy, float r,
                          float startDeg, float endDeg) throws IOException {
        float start = (float)Math.toRadians(startDeg);
        float end   = (float)Math.toRadians(endDeg);
        float total = end - start;
        int   segs  = Math.max(1, (int)Math.ceil(Math.abs(total) / (Math.PI / 2)));
        float step  = total / segs;

        cs.moveTo(cx + r * (float)Math.cos(start), cy + r * (float)Math.sin(start));

        float angle = start;
        for (int i = 0; i < segs; i++) {
            float a1 = angle, a2 = angle + step;
            float k  = (float)(4.0 / 3.0 * Math.tan((a2 - a1) / 4.0));
            float c1 = (float)Math.cos(a1), s1 = (float)Math.sin(a1);
            float c2 = (float)Math.cos(a2), s2 = (float)Math.sin(a2);
            cs.curveTo(
                cx + r * (c1 - k * s1), cy + r * (s1 + k * c1),
                cx + r * (c2 + k * s2), cy + r * (s2 - k * c2),
                cx + r * c2,            cy + r * s2
            );
            angle = a2;
        }
    }

    // =========================================================================
    //  BADGE
    // =========================================================================

    private void drawBadge(PDPageContentStream cs, float rightX, float y,
                            String text, Color bg, Color fg,
                            boolean rightAligned) throws IOException {
        float pad = 5f, h = 13f;
        float w   = text.length() * 4.8f + pad * 2;
        float x   = rightAligned ? rightX - w : rightX;
        fillRoundedRect(cs, x, y, w, h, 3f, bg);
        setFont(cs, PDType1Font.HELVETICA, 6, fg);
        drawText(cs, text, x + pad, y + 3f);
    }

    // =========================================================================
    //  FOOTER
    // =========================================================================

    private void drawFooter(PDPageContentStream cs, int pageNum, int totalPages) throws IOException {
        drawHLine(cs, CONTENT_X, MARGIN_BOT - 6f, CONTENT_W, BORDER, 0.5f);
        setFont(cs, PDType1Font.HELVETICA, 7, TEXT_MUTED);
        drawText(cs, "Plywood Optimizer  \u00B7  " + DATE_FMT.format(new Date()),
                 CONTENT_X, MARGIN_BOT - 18f);
        drawTextRight(cs, "Page " + pageNum + " of " + totalPages,
                      CONTENT_X + CONTENT_W, MARGIN_BOT - 18f, 7);
    }

    // =========================================================================
    //  COLOR HELPER
    // =========================================================================

    private Color utilColor(double pct) {
        if (pct >= 85) return GREEN;
        if (pct >= 70) return AMBER;
        return RED;
    }

    // =========================================================================
    //  DRAWING PRIMITIVES
    // =========================================================================

    private void setFont(PDPageContentStream cs, PDType1Font font,
                          int size, Color color) throws IOException {
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
    }

    private void fillRect(PDPageContentStream cs,
                           float x, float y, float w, float h,
                           Color c) throws IOException {
        cs.setNonStrokingColor(c);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void strokeRect(PDPageContentStream cs,
                             float x, float y, float w, float h,
                             Color c, float lw) throws IOException {
        cs.setStrokingColor(c);
        cs.setLineWidth(lw);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private void fillRoundedRect(PDPageContentStream cs,
                                  float x, float y, float w, float h,
                                  float r, Color c) throws IOException {
        cs.setNonStrokingColor(c);
        roundedRectPath(cs, x, y, w, h, r);
        cs.fill();
    }

    private void strokeRoundedRect(PDPageContentStream cs,
                                    float x, float y, float w, float h,
                                    float r, Color c, float lw) throws IOException {
        cs.setStrokingColor(c);
        cs.setLineWidth(lw);
        roundedRectPath(cs, x, y, w, h, r);
        cs.stroke();
    }

    /** Appends a rounded-rectangle path. Uses the standard 0.5523 Bezier constant. */
    private void roundedRectPath(PDPageContentStream cs,
                                  float x, float y, float w, float h,
                                  float r) throws IOException {
        float k = r * 0.5523f;
        cs.moveTo(x + r, y);
        cs.lineTo(x + w - r, y);
        cs.curveTo(x + w - r + k, y,         x + w, y + k,         x + w, y + r);
        cs.lineTo(x + w, y + h - r);
        cs.curveTo(x + w, y + h - r + k,     x + w - r + k, y + h, x + w - r, y + h);
        cs.lineTo(x + r, y + h);
        cs.curveTo(x + r - k, y + h,         x, y + h - r + k,     x, y + h - r);
        cs.lineTo(x, y + r);
        cs.curveTo(x, y + r - k,             x + r - k, y,         x + r, y);
        cs.closePath();
    }

    private void fillCircle(PDPageContentStream cs,
                             float cx, float cy, float r,
                             Color c) throws IOException {
        cs.setNonStrokingColor(c);
        float k = r * 0.5523f;
        cs.moveTo(cx + r, cy);
        cs.curveTo(cx + r, cy + k,   cx + k, cy + r,  cx,     cy + r);
        cs.curveTo(cx - k, cy + r,   cx - r, cy + k,  cx - r, cy);
        cs.curveTo(cx - r, cy - k,   cx - k, cy - r,  cx,     cy - r);
        cs.curveTo(cx + k, cy - r,   cx + r, cy - k,  cx + r, cy);
        cs.fill();
    }

    private void drawHLine(PDPageContentStream cs,
                            float x, float y, float w,
                            Color c, float lw) throws IOException {
        cs.setStrokingColor(c);
        cs.setLineWidth(lw);
        cs.moveTo(x, y);
        cs.lineTo(x + w, y);
        cs.stroke();
    }

    // ── Text helpers ─────────────────────────────────────────────────────────

    private void drawText(PDPageContentStream cs, String text,
                           float x, float y) throws IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /** Horizontally centres text around cx (approximate — uses char-count heuristic). */
    private void drawTextCentered(PDPageContentStream cs, String text,
                                   float cx, float y, int fontSize) throws IOException {
        float approxW = text.length() * fontSize * 0.52f;
        drawText(cs, text, cx - approxW / 2f, y);
    }

    /** Right-aligns text so its right edge is at rightX (approximate). */
    private void drawTextRight(PDPageContentStream cs, String text,
                                float rightX, float y, int fontSize) throws IOException {
        float approxW = text.length() * fontSize * 0.52f;
        drawText(cs, text, rightX - approxW, y);
    }

    /** Draws text rotated by angleRad radians around point (x, y). */
    private void drawTextRotated(PDPageContentStream cs, String text,
                                  float x, float y, float angleRad) throws IOException {
        float cos = (float)Math.cos(angleRad);
        float sin = (float)Math.sin(angleRad);
        cs.beginText();
        cs.setTextMatrix(new Matrix(cos, sin, -sin, cos, x, y));
        cs.showText(text);
        cs.endText();
    }
}