package io.bosler.connect.library.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.bosler.connect.library.models.SharePointSourceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class SharePointConnectorService {
    private static final String SCOPE = "https://graph.microsoft.com/.default";  // Microsoft Graph Scope
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HashMap<String, Object> testConnection(SharePointSourceConfig sharePointSourceConfig) {
        HashMap<String, Object> message = new HashMap<>();
        try {

            Map<String, Object> test = enrichSiteIdAndDriveId(sharePointSourceConfig);

            if (Objects.nonNull(test.get("siteId")) && Objects.nonNull(test.get("driveId"))) {
                message.put("status", true);
                message.put("message", "Valid sharepoint config");
            } else {
                message.put("status", false);
                message.put("message", "Invalid share point source");
            }
        } catch (Exception e) {
            message.put("status", false);
            message.put("message", "Invalid share point source");
        }

        return message;
    }

    public static Map<String, Object> enrichSiteIdAndDriveId(SharePointSourceConfig sharePointSourceConfig) {
        try {
            String url = sharePointSourceConfig.getUrl();
            String accessToken = sharePointSourceConfig.getToken();

            String siteIdUrl = "https://graph.microsoft.com/v1.0/sites/{host_name}:/sites/{site_name}";
            String siteName = extractSiteName(url);
            String hostname = extractHostname(url);

            siteIdUrl = siteIdUrl.replace("{host_name}", hostname).replace("{site_name}", siteName);

            JsonNode resp = objectMapper.readTree(getAPIResponse(accessToken, siteIdUrl));
            String ids = resp.get("id").asText();
            String siteId = ids.split(",")[1];


            JsonNode drives = fetchDrives(accessToken, siteId);
            JsonNode drive = StreamSupport.stream(drives.get("value").spliterator(), false).filter(jsonNode -> url.contains(jsonNode.get("webUrl").asText()) || jsonNode.get("webUrl").asText().contains(url)).findFirst().orElse(null);
            String driveId = drive.get("id").asText();

            sharePointSourceConfig.setSiteId(siteId);
            sharePointSourceConfig.setDriveId(driveId);

            return ImmutableMap.of("siteId", siteId, "driveId", driveId);
        } catch (Exception e) {
            sharePointSourceConfig.setSiteId(null);
            sharePointSourceConfig.setDriveId(null);
            Map<String, Object> res = new HashMap<>();
            res.put("siteId", null);
            res.put("driveId", null);
            return res;
        }
    }

    public static String getAccessToken(String clientId, String clientSecret, String tenantId) throws IOException {
        String tokenEndpoint = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        // Build the request body
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("scope", SCOPE);

        // Prepare the POST request
        HttpPost post = new HttpPost(tokenEndpoint);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity(createFormData(body)));

        // Execute the request and handle the response
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            // Parse the JSON response to get the access token
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(result);
            return jsonResponse.get("access_token").asText();
        }
    }

    // Helper function to build the form data for the POST request
    private static String createFormData(Map<String, String> data) {
        StringBuilder formData = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (formData.length() > 0) {
                formData.append("&");
            }
            formData.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return formData.toString();
    }

    // Function to fetch SharePoint sites from Microsoft Graph
    private static JsonNode fetchSites(String accessToken) throws IOException {
        String url = "https://graph.microsoft.com/v1.0/sites?search=*";
        String result = getAPIResponse(accessToken, url);
        return objectMapper.readTree(result);
    }

    // Function to fetch SharePoint sites from Microsoft Graph
    private static JsonNode fetchDrives(String accessToken, String siteId) throws IOException {
        String url = "https://graph.microsoft.com/v1.0/sites/{site_id}/drives".replace("{site_id}", siteId);
        String result = getAPIResponse(accessToken, url);
        return objectMapper.readTree(result);
    }

    // Function to fetch SharePoint sites from Microsoft Graph
    public static JsonNode fetchDriveFolderChildren(String accessToken, String siteId, String driveId, String folderId) throws IOException {
        String url = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/drives/" + driveId + "/items/" + folderId + "/children";
        String result = getAPIResponse(accessToken, url);
        return objectMapper.readTree(result);
    }

    public static JsonNode fetchItemData(String accessToken, String siteId, String driveId, String itemId) throws IOException {
        String url = "https://graph.microsoft.com/v1.0/sites/{site-id}/drives/{drive-id}/items/{item-id}";
        url = url.replace("{site-id}", siteId).replace("{drive-id}", driveId).replace("{item-id}", itemId);
        String result = getAPIResponse(accessToken, url);
        return objectMapper.readTree(result); //@microsoft.graph.downloadUrl
    }

    private static String getAPIResponse(String accessToken, String url) throws IOException {
        // Create the HTTP client and request object
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + accessToken);
        CloseableHttpResponse response = httpClient.execute(request);
        String result = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        return result;
    }

    // Method to extract the hostname
    private static String extractHostname(String url) {
        try {
            // Use java.net.URL to parse the hostname
            java.net.URL parsedUrl = new java.net.URL(url);
            return parsedUrl.getHost();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to extract the site name
    private static String extractSiteName(String url) {
        try {
            // Extracting the part after "/sites/"
            String[] parts = url.split("/sites/");
            if (parts.length > 1) {
                String[] siteParts = parts[1].split("/");
                return siteParts[0];  // The site name is the first part after "/sites/"
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
