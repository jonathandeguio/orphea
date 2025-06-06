//package io.bosler.capture.logger.library.services;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
//@Service
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class LoggerService {
//    private final ObjectMapper objectMapper;
//    private final RestTemplate restTemplate;
//
//    String deploymentId = System.getenv("DEPLOYMENT_ID");
//    String token = System.getenv("TOKEN");
//
//
//    @Value("${log.api.lastLogTimestampEndpoint}")
//    private String lastLogTimestampEndpoint;
//
//
//    public String getLatestTimestampFromAPI(String metricType) {
//        try {
//            if (deploymentId == null || deploymentId.isEmpty()) {
//                throw new IllegalArgumentException("Deployment ID environment variable is not set or empty");
//            }
//            if (token == null || token.isEmpty()) {
//                throw new IllegalArgumentException("TOKEN environment variable is not set or empty");
//            }
//
//            // Replace {deploymentId} in the endpoint URL with the actual deploymentId
//            String url = lastLogTimestampEndpoint.replace("{deploymentId}", deploymentId);
//            String finalUrl = url.replace("{metricType}", metricType);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(token); // Set the Bearer token for authentication
//
//            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//            ResponseEntity<String> response = restTemplate.exchange(
//                    finalUrl,
//                    HttpMethod.GET,
//                    requestEntity,
//                    String.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                String responseBody = response.getBody();
//                if (responseBody != null) {
//                    return responseBody;
//                } else {
//                    throw new RuntimeException("Response body is null");
//                }
//            } else {
//                throw new RuntimeException("Failed to get the latest timestamp: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Exception occurred while fetching the latest timestamp: " + e.getMessage());
//            throw new RuntimeException("Exception occurred while fetching the latest timestamp", e);
//        }
//    }
//
//    public boolean sendLogBatch(List<Object> logBatch, String logApiEndpoint, String metricType) {
//        try {
//            if (deploymentId == null || deploymentId.isEmpty()) {
//                throw new IllegalArgumentException("Deployment ID environment variable is not set or empty");
//            }
//            if (token == null || token.isEmpty()) {
//                throw new IllegalArgumentException("TOKEN environment variable is not set or empty");
//            }
//
//            // Replace {deploymentId} in the logApiEndpoint with the actual deploymentId
//            String url = logApiEndpoint.replace("{deploymentId}", deploymentId);
//            String finalUrl = url.replace("{metricType}", metricType);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(token); // Set the Bearer token for authentication
//
//            String jsonArray = objectMapper.writeValueAsString(logBatch);
//            HttpEntity<String> requestEntity = new HttpEntity<>(jsonArray, headers);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                    finalUrl,
//                    requestEntity,
//                    String.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                System.out.println("Successfully sent log batch to API: " + finalUrl);
//                return true; // Indicate success
//            } else {
//                System.out.println("Failed to send log batch to API: " + response.getStatusCode());
//                return false; // Indicate failure
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Exception occurred while sending log batch to API: " + e.getMessage());
//            return false; // Indicate failure
//        }
//    }
//
//
//    public Object extractMessageFromLine(String line, Class<?> targetType) {
//        try {
//            return objectMapper.readValue(line, targetType);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public long getLastReadPosition(String MARKER_FILE_PATH) throws IOException {
//        if (Files.exists(Paths.get(MARKER_FILE_PATH))) {
//            String positionStr = new String(Files.readAllBytes(Paths.get(MARKER_FILE_PATH))).trim();
//            return positionStr.isEmpty() ? 0 : Long.parseLong(positionStr);
//        } else {
//            return 0;
//        }
//    }
//
//    public void updateLastReadPosition(long position, String MARKER_FILE_PATH) throws IOException {
//        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(MARKER_FILE_PATH))) {
//            writer.write(String.valueOf(position));
//        }
//    }
//}
