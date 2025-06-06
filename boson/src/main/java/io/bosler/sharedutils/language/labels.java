package io.bosler.sharedutils.language;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.arrow.vector.ipc.JsonFileReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class labels {


    public static Map<String, Map<String, String>> loadDataMapFromJson(String jsonFilePath) {

        // Process the JSON data and convert it to Map<String, Map<String, String>>
        Map<String, Map<String, String>> result = new HashMap<>();

        try {
            // Get the class loader
            ClassLoader classLoader = JsonFileReader.class.getClassLoader();

            // Use the class loader to get the input stream for the resource
            InputStream inputStream = classLoader.getResourceAsStream(jsonFilePath);

            // Check if the resource was found
            if (inputStream == null) {
                System.out.println("Resource not found: " + jsonFilePath);
//                return;
            }

            // Use Jackson ObjectMapper to read JSON content
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(inputStream);


            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();

                Map<String, String> innerMap = new HashMap<>();
                JsonNode innerNode = entry.getValue();
                Iterator<Map.Entry<String, JsonNode>> innerFields = innerNode.fields();

                while (innerFields.hasNext()) {
                    Map.Entry<String, JsonNode> innerEntry = innerFields.next();
                    innerMap.put(innerEntry.getKey(), innerEntry.getValue().asText());
                }

                result.put(key, innerMap);
            }

            // Print the resulting map
//            System.out.println(result);


            // Close the input stream
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getLabel(String key, String language) {
        Map<String, Map<String, String>> dataMap = loadDataMapFromJson("language.json");

        // Retrieve the label from the data map based on the key and language
        Map<String, String> languageData = dataMap.get(key);

        String label = languageData != null ? languageData.get(language) : null;

        if ("auto".equals(language) || label == null) {
            // If language is "auto" or the label for the specified language is not found, fallback to English ("en")
            label = languageData != null ? languageData.get("en") : null;
        }

        return label;
    }

}
