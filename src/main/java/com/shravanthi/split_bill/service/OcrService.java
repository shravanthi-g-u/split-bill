package com.shravanthi.split_bill.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

@Service
public class OcrService {

    @Value("${ocr.space.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractText(MultipartFile imageFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("apikey", apiKey);
        body.add("language", "eng");
        body.add("isTable", "true"); // helps with receipt-style layouts
        body.add("OCREngine", "2");
        body.add("file", new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject("https://api.ocr.space/parse/image", requestEntity, Map.class);

        return parseTextFromResponse(response);
    }

    @SuppressWarnings("unchecked")
    private String parseTextFromResponse(Map<String, Object> response) {
        var results = (java.util.List<Map<String, Object>>) response.get("ParsedResults");
        if (results == null || results.isEmpty()) {
            throw new IllegalStateException("OCR returned no results");
        }
        return (String) results.get(0).get("ParsedText");
    }

    public List<OcrItemDraft> extractItemDrafts(String rawText) {
        List<OcrItemDraft> drafts = new ArrayList<>();
        Pattern slNoPattern = Pattern.compile("^\\d{1,2}$");
        Pattern numberPattern = Pattern.compile("^\\d+\\.\\d+$"); // matches things like 1749.50

        String[] lines = rawText.split("[\\r\\n]+");
        for (String line : lines) {
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length < 2)
                continue;
            if (!slNoPattern.matcher(tokens[0]).matches())
                continue;

            StringBuilder nameBuilder = new StringBuilder();
            int i = 1;
            for (; i < tokens.length; i++) {
                if (tokens[i].matches("^\\d.*")) {
                    break; // hit HSN code, stop collecting name
                }
                if (nameBuilder.length() > 0)
                    nameBuilder.append(" ");
                nameBuilder.append(tokens[i]);
            }

            String name = nameBuilder.toString().trim();
            if (name.isEmpty())
                continue;

            // Walk the remaining tokens and remember the LAST clean decimal number found —
            // that's the "Amount" column (final price for this item).
            Double lastNumber = null;
            for (int j = i; j < tokens.length; j++) {
                String cleaned = tokens[j].replaceAll("[^0-9.]", ""); // strip stray '=', '|', etc.
                if (numberPattern.matcher(cleaned).matches()) {
                    try {
                        lastNumber = Double.parseDouble(cleaned);
                    } catch (NumberFormatException ignored) {
                        // skip malformed number, keep previous lastNumber
                    }
                }
            }

            drafts.add(new OcrItemDraft(name, lastNumber));
        }

        return drafts;
    }
}