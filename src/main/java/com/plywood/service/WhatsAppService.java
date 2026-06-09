package com.plywood.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Helper to format phone number to Indian format 91XXXXXXXXXX (no + sign)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        // Remove non-digits
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "91" + digits;
        }
        return digits;
    }

    /**
     * Method 1: String uploadPdf(File pdfFile)
     * Uploads PDF to Meta media endpoint and returns media ID
     */
    public String uploadPdf(File pdfFile) {
        try {
            String url = apiUrl + "/" + phoneNumberId + "/media";
            logger.info("Uploading PDF to WhatsApp Cloud API Media endpoint: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("messaging_product", "whatsapp");
            body.add("file", new FileSystemResource(pdfFile));
            body.add("type", "application/pdf");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
                if (json.has("id")) {
                    String mediaId = json.get("id").getAsString();
                    logger.info("PDF uploaded successfully. Media ID: {}", mediaId);
                    return mediaId;
                }
            }
            throw new RuntimeException("Failed to get media ID from response: " + response.getBody());
        } catch (Exception e) {
            logger.error("Error uploading PDF to WhatsApp Media API", e);
            throw new RuntimeException("WhatsApp media upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Method 2: void sendPdfToCustomer(String phone, String mediaId, String filename, String caption)
     * Sends document message to customer WhatsApp
     */
    public void sendPdfToCustomer(String phone, String mediaId, String filename, String caption) {
        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";
            String formattedPhone = formatPhoneNumber(phone);
            logger.info("Sending document message to recipient: {}, URL: {}", formattedPhone, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Construct payload using Map and Gson
            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", formattedPhone);
            body.put("type", "document");

            Map<String, Object> document = new HashMap<>();
            document.put("id", mediaId);
            document.put("filename", filename);
            document.put("caption", caption);
            body.put("document", document);

            String jsonPayload = new Gson().toJson(body);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("WhatsApp document message sent successfully. Response: {}", response.getBody());
            } else {
                throw new RuntimeException("Send message returned status: " + response.getStatusCode() + ", body: " + response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error sending PDF via WhatsApp Message API", e);
            throw new RuntimeException("WhatsApp message sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * Method 3: void sendQuotationPdf(String phone, File pdfFile, String quotationNumber)
     * Calls uploadPdf then sendPdfToCustomer
     */
    public void sendQuotationPdf(String phone, File pdfFile, String quotationNumber) {
        logger.info("Initiating sending of Quotation {} to WhatsApp phone: {}", quotationNumber, phone);
        String mediaId = uploadPdf(pdfFile);
        String caption = "Dear Customer, please find your Quotation " + quotationNumber + " attached. Thank you!";
        sendPdfToCustomer(phone, mediaId, "Quotation-" + quotationNumber + ".pdf", caption);
    }

    /**
     * Method 4: void sendBillPdf(String phone, File pdfFile, String invoiceNumber)
     * Calls uploadPdf then sendPdfToCustomer
     */
    public void sendBillPdf(String phone, File pdfFile, String invoiceNumber) {
        logger.info("Initiating sending of Invoice {} to WhatsApp phone: {}", invoiceNumber, phone);
        String mediaId = uploadPdf(pdfFile);
        String caption = "Dear Customer, your Invoice " + invoiceNumber + " is attached. Please make payment by the due date.";
        sendPdfToCustomer(phone, mediaId, "Invoice-" + invoiceNumber + ".pdf", caption);
    }
}
