package com.plywood.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class BarcodeService {

    private static final Logger log = LoggerFactory.getLogger(BarcodeService.class);

    /**
     * Generates a CODE_128 barcode string in format: PLY-{productId}-{timestamp}
     * Returns the barcode string value.
     */
    public String generateProductBarcode(Long productId, String productName) {
        long timestamp = System.currentTimeMillis();
        String barcodeValue = "PLY-" + productId + "-" + timestamp;
        log.info("Generated product barcode value: {} for product: {}", barcodeValue, productName);
        return barcodeValue;
    }

    /**
     * Uses ZXing to generate a CODE_128 barcode as PNG byte array.
     * Width: 300px, Height: 100px.
     */
    public byte[] generateBarcodeImage(String barcodeValue) {
        log.info("Generating barcode image for value: {}", barcodeValue);
        try {
            BitMatrix bitMatrix = new com.google.zxing.oned.Code128Writer().encode(
                    barcodeValue,
                    BarcodeFormat.CODE_128,
                    300,
                    100
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating barcode image for value: {}", barcodeValue, e);
            throw new RuntimeException("Failed to generate barcode image", e);
        }
    }

    /**
     * Uses ZXing BarcodeFormat.QR_CODE to generate QR code as PNG byte array.
     * Content should be JSON.
     * Width: 200px, Height: 200px.
     */
    public byte[] generateQRCode(String content) {
        log.info("Generating QR code image for content length: {}", content.length());
        try {
            BitMatrix bitMatrix = new com.google.zxing.qrcode.QRCodeWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    200,
                    200
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating QR code image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    /**
     * Uses ZXing MultiFormatReader to decode barcode or QR from uploaded image.
     * Returns the decoded string value.
     * Throws RuntimeException if no barcode found.
     */
    public String decodeBarcodeFromImage(byte[] imageBytes) {
        log.info("Attempting to decode barcode/QR code from uploaded image bytes (length: {})", imageBytes.length);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bais);
            if (bufferedImage == null) {
                log.warn("Uploaded file could not be read as a valid image.");
                throw new RuntimeException("Invalid image file");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            log.info("Successfully decoded barcode/QR code: {}", result.getText());
            return result.getText();
        } catch (com.google.zxing.NotFoundException e) {
            log.warn("No barcode or QR code detected in the image.");
            throw new RuntimeException("No barcode detected in image");
        } catch (IOException e) {
            log.error("IO Exception while reading image for decoding", e);
            throw new RuntimeException("Failed to read image bytes", e);
        } catch (Exception e) {
            log.error("Unexpected error during barcode decoding", e);
            throw new RuntimeException("Error decoding barcode", e);
        }
    }
}
