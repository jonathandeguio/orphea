package io.movetodata.connect.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.movetodata.connect.library.DTOs.ProcessOverlayResultBodyDTO;
import io.movetodata.connect.library.DTOs.ProcessOverlayResultDTO;
import io.movetodata.connect.library.enums.RestAPIAuthTypeEnum;
import io.movetodata.connect.library.enums.RestAPITypeEnum;
import io.movetodata.connect.library.models.*;
import io.movetodata.connect.library.repository.RestSourceDomainRepository;
import io.movetodata.connect.library.repository.SourcesRepository;
import io.movetodata.connect.library.repository.WebhookExecutionRepository;
import io.movetodata.connect.library.repository.WebhookRepository;
import io.movetodata.connect.library.requests.WebhookDTO;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final SourcesRepository sourcesRepository;
    private final ResourceService resourceService;
    private final RestSourceDomainRepository restSourceDomainRepository;
    private final WebhookExecutionRepository webhookExecutionRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public ProcessOverlayResultBodyDTO processOverlayForBody(String requestBody, String previousCallResponse) throws JsonProcessingException {
        List<String> errors = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (previousCallResponse != null && requestBody != null && requestBody.contains("@")) {
            requestBody = requestBody.replaceAll("@completeresponse", Matcher.quoteReplacement(previousCallResponse));

            Pattern keyPattern = Pattern.compile("@key\\[(.*?)\\]");
            Matcher matcher = keyPattern.matcher(requestBody);

            try {
                JsonNode rootNode = mapper.readTree(previousCallResponse);
                StringBuffer result = new StringBuffer();

                while (matcher.find()) {
                    String key = matcher.group(1);

                    JsonNode valueNode = rootNode.get(key);
                    String replacement = "null";
                    if (valueNode != null) {
                        replacement = valueNode.asText().isEmpty() ? valueNode.toString() : valueNode.asText();
                    } else {
                        errors.add("Key " + key + " not found in response.");
                    }

                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                }
                matcher.appendTail(result);
                requestBody = result.toString();

            } catch (Exception e) {
                errors.add("Error parsing key in request body");
            }

            Pattern indexPattern = Pattern.compile("@index\\[(.*?)\\]");
            Matcher indexMatcher = indexPattern.matcher(requestBody);

            try {
                JsonNode arrayNode = mapper.readTree(previousCallResponse);
                StringBuilder indexResult = new StringBuilder();

                while (indexMatcher.find()) {
                    int index;
                    try {
                        index = Integer.parseInt(indexMatcher.group(1));
                    } catch (NumberFormatException e) {
                        errors.add("Invalid index format: " + indexMatcher.group(1));
                        indexMatcher.appendReplacement(indexResult, "null");
                        continue;
                    }

                    if (arrayNode.isArray() && arrayNode.has(index)) {
                        String replacement = arrayNode.get(index).asText();
                        indexMatcher.appendReplacement(indexResult, Matcher.quoteReplacement(replacement));
                    } else {
                        errors.add("Index " + index + " not found in response.");
                        indexMatcher.appendReplacement(indexResult, "null");
                    }
                }
                indexMatcher.appendTail(indexResult);
                requestBody = indexResult.toString();
            } catch (Exception e) {
                errors.add("Error parsing index in request body");
            }
        }

        return new ProcessOverlayResultBodyDTO(requestBody, errors);
    }

    public ProcessOverlayResultDTO processOverlay(Map<String, String> keyValuePair, String previousCallResponse) {
        List<String> errors = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, String> entries : keyValuePair.entrySet()) {
            String key = entries.getKey();
            String value = entries.getValue();
            if (previousCallResponse != null && value.contains("@")) {
                if (value.contains("@completeresponse")) {
                    value = value.replace("@completeresponse", previousCallResponse);
                } else if (value.contains("@key")) {
                    Pattern pattern = Pattern.compile("@key\\[(.*?)\\]");
                    Matcher matcher = pattern.matcher(value);
                    try {
                        if (matcher.find()) {
                            String extractedData = matcher.group(1);
                            JsonNode rootNode = mapper.readTree(previousCallResponse);
                            JsonNode dataNode = rootNode.get(extractedData);
                            if (dataNode != null) {
                                value = matcher.replaceAll(dataNode.asText().isEmpty() ? dataNode.toString() : dataNode.asText());
                            } else {
                                value = null;
                                errors.add("Key " + extractedData + " not found in response.");
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Error in parsing key. For key " + (matcher.find() ? matcher.group(1) : "unknown"));
                    }

                } else if (value.contains("@index")) {
                    String regex = "@index\\[(.*?)\\]";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(value);
                    try {
                        if (matcher.find()) {
                            int index = Integer.parseInt(matcher.group(1));
                            JsonNode arrayNode = mapper.readTree(previousCallResponse);
                            if (arrayNode.isArray() && arrayNode.has(index)) {
                                value = arrayNode.get(index).asText();
                            } else {
                                value = null;
                                errors.add("Index " + index + " not found in response.");
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Error in parsing index. For Index ");
                    }
                }
            }
            keyValuePair.put(key, value);
        }

        return new ProcessOverlayResultDTO(keyValuePair, errors);
    }

    public Webhook findById(UUID id) {
        return webhookRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("Webhook with Id {0} does not exist", id)));
    }

    public RestAPISourceDomain getDomain(UUID id) {
        return restSourceDomainRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("Domain with Id {0} does not exist", id)));
    }

    public boolean existsById(UUID id) {
        return webhookRepository.existsById(id);
    }

    public Source getSource(UUID id) {
        return sourcesRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(MessageFormat.format("connectors with Id {0} does not exist", id)));
    }

    @Transactional
    public void createWebhook(WebhookDTO webhookDTO, UUID userId) {
        if (!resourceService.existsById(webhookDTO.getParent())) {
            throw new NoSuchElementException(MessageFormat.format("connectors with Id {0} does not exist", webhookDTO.getParent()));
        }

        ResourceModel resourceModel = resourceService.newResource(webhookDTO.getName(), webhookDTO.getDescription(), ResourceType.WEBHOOK, ResourceSubtype.NONE, userId, webhookDTO.getParent());
        List<RestAPIRequest> requests = webhookDTO.getRequests().stream().map(dto ->
                RestAPIRequest.builder()
                        .path(dto.getPath())
                        .domainId(dto.getDomainId())
                        .build()).collect(Collectors.toList());

        Webhook webhook = Webhook.builder()
                .id(resourceModel.getId())
                .sourceId(webhookDTO.getSourceId())
                .requests(requests)
                .build();

        webhookRepository.save(webhook);
    }

    public List<WebhookExecutionData> getWebhookExecutions(UUID webhookId, UUID userId) {
        List<WebhookExecutionData> executions = webhookExecutionRepository.findByWebhookId(webhookId);
        executions.sort(Comparator.comparing(WebhookExecutionData::getExecutedAt).reversed());
        return executions;
    }

    @Transactional
    public void updateWebhook(WebhookDTO webhookDTO, UUID userId) {
        if (!resourceService.existsById(webhookDTO.getParent())) {
            throw new NoSuchElementException(MessageFormat.format("connectors with Id {0} does not exist", webhookDTO.getParent()));
        }

        Webhook webhook = findById(webhookDTO.getId());
        ResourceModel resourceModel = resourceService.findById(webhook.getId()).get();
        resourceModel.setName(webhookDTO.getName());
        resourceModel.setDescription(webhookDTO.getDescription());
        resourceModel.setParent(webhookDTO.getParent());
        resourceService.save(resourceModel);

        webhook.setRequests(webhookDTO.getRequests());
        webhookRepository.save(webhook);
    }

    public List<WebhookCallData> executeWebhook(List<RestAPIRequest> requests, UUID webhookId, UUID userId) {
        List<WebhookCallData> webhookCallsData = new ArrayList<>();

        String previousCallResponse = null;
        for (RestAPIRequest request : requests) {
            List<String> extraErrors = new ArrayList<>();
            RestAPISourceDomain domain = getDomain(request.getDomainId());
            WebhookCallData callDetail = new WebhookCallData();
            if(domain.getPort() == 443 && domain.getProtocol().equals("https")) {
                callDetail.setUrl(domain.getProtocol() + "://" + domain.getDomain() + "/" + request.getPath());
            } else if (domain.getPort() == 80 && domain.getProtocol().equals("http")) {
                callDetail.setUrl(domain.getProtocol() + "://" + domain.getDomain() + "/" + request.getPath());
            } else {
                callDetail.setUrl(domain.getProtocol() + "://" + domain.getDomain() + ":" + domain.getPort() + "/" + request.getPath());
            }
            callDetail.setMethod(request.getMethod());
            callDetail.setExecutedBy(userId);
            callDetail.setExecutionStartedAt(new Date());

            try {
                ProcessOverlayResultDTO queryParamsProcessingResult = processOverlay(parseKeyValueListFromJson(request.getQueryParams()), previousCallResponse);
                Map<String, String> parsedQueryParams = queryParamsProcessingResult.getKeyValuePair();
                extraErrors.addAll(queryParamsProcessingResult.getErrors());

                ProcessOverlayResultDTO headersProcessingResult = processOverlay(parseKeyValueListFromJson(request.getHeaders()), previousCallResponse);
                Map<String, String> parsedRequestHeaders = headersProcessingResult.getKeyValuePair();
                extraErrors.addAll(headersProcessingResult.getErrors());

                // Authorization
                HttpHeaders parsedHeaders = convertMapToHeader(parsedRequestHeaders);

                // Handle Authorization Headers
                if (domain.getAuthType() == RestAPIAuthTypeEnum.BEARERTOKEN) {
                    parsedHeaders.set("Authorization", "Bearer " + domain.getBearerToken());
                } else if (domain.getAuthType() == RestAPIAuthTypeEnum.APIKEY) {
                    parsedHeaders.set("x-api-key", domain.getApiKeyName());
                    parsedHeaders.set("x-api-value", domain.getApiKeyValue());
                }

                // Handle extra headers
                if (request.getBodyType() == RestAPITypeEnum.JSON) {
                    parsedHeaders.set("Content-Type", "application/json");
                }

                // Request body setup
                Object requestBody;
                if (request.getBodyType() == RestAPITypeEnum.FORMDATA) {
                    ProcessOverlayResultDTO formDataProcessingResult = processOverlay(parseKeyValueListFromJson(request.getFormData()), previousCallResponse);
                    Map<String, String> parsedFormData = formDataProcessingResult.getKeyValuePair();
                    extraErrors.addAll(formDataProcessingResult.getErrors());

                    requestBody = parsedFormData;
                } else if (request.getBodyType() == RestAPITypeEnum.RAW || request.getBodyType() == RestAPITypeEnum.JSON) {

                    ProcessOverlayResultBodyDTO rawBodyProcessingResult = processOverlayForBody(request.getRawBody(), previousCallResponse);
                    String parsedRawBody = rawBodyProcessingResult.getBody();
                    extraErrors.addAll(rawBodyProcessingResult.getErrors());

                    requestBody = parsedRawBody;
                } else {
                    requestBody = null;
                }

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(callDetail.getUrl());
                parsedQueryParams.forEach(builder::queryParam);
                String fullUrl = builder.toUriString();
                System.out.println("Request Header : " + request.getHeaders());
                System.out.println(">> from backend after processing Header : " + parsedHeaders);
                HttpEntity<?> entity = new HttpEntity<>(requestBody, parsedHeaders);
                callDetail.setApiTitle(request.getApiTitle());
                callDetail.setRequestBody(String.valueOf(entity.getBody()));
                callDetail.setRequestHeaders(String.valueOf(entity.getHeaders()));
                callDetail.setFullUrl(fullUrl);

                ResponseEntity<String> response = restTemplate.exchange(
                        fullUrl,
                        HttpMethod.valueOf(request.getMethod().toString()),
                        entity,
                        String.class
                );

                callDetail.setResponseHeaders(String.valueOf(response.getHeaders()));
                callDetail.setStatus(String.valueOf(response.getStatusCodeValue()));
                callDetail.setResponseBody(response.getBody());
                previousCallResponse = response.getBody();

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                callDetail.setStatus(String.valueOf(e.getStatusCode().value()));
                callDetail.setResponseBody(e.getResponseBodyAsString());
                previousCallResponse = null;
            } catch (Exception e) {
                if (callDetail.getStatus() != null) {
                    callDetail.setStatus("500");
                }
                callDetail.setResponseBody(e.getMessage());
                previousCallResponse = null;
            } finally {
                callDetail.setExecutionEndedAt(new Date());
                callDetail.setExtraErrors(String.join(",", extraErrors));
                webhookCallsData.add(callDetail);
            }
        }

        WebhookExecutionData executionData = new WebhookExecutionData();
        executionData.setExecutedBy(userId);
        executionData.setExecutedAt(new Date());
        executionData.setWebhookId(webhookId);
        executionData.setCalls(webhookCallsData);

        webhookExecutionRepository.save(executionData);

        return webhookCallsData;
    }

    private HttpHeaders convertMapToHeader(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);
        return httpHeaders;
    }

    private HttpHeaders parseHeadersFromJson(String headersJson) {
        HttpHeaders headers = new HttpHeaders();
        if (headersJson != null && !headersJson.isEmpty()) {
            try {
                List<Map<String, String>> headersList = objectMapper.readValue(headersJson, new TypeReference<List<Map<String, String>>>() {
                });
                headersList.forEach(header -> headers.set(header.get("key"), header.get("value")));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse headers JSON", e);
            }
        }
        return headers;
    }

    private Map<String, String> parseKeyValueListFromJson(String jsonString) {
        Map<String, String> result = new HashMap<>();
        if (jsonString != null && !jsonString.isEmpty()) {
            try {
                List<Map<String, String>> keyValueList = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, String>>>() {
                });
                keyValueList.forEach(item -> result.put(item.get("key"), item.get("value")));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse JSON to map", e);
            }
        }
        return result;
    }

    public String processResponseParamForJson(String responseParam, String response) throws JsonProcessingException {
        ProcessOverlayResultBodyDTO resultBodyDTO = processOverlayForBody(responseParam, response);
        return resultBodyDTO.getBody();
    }

    public String getContentTypeFromResponseHeaders(String headersString) {
        String[] headers = headersString.split(", ");

        for (String header : headers) {
            if (header.toLowerCase().startsWith("content-type:")) {
                return header.split(":", 2)[1].trim().replaceAll("^\"|\"$", "");
            }
        }
        return null;
    }

}
