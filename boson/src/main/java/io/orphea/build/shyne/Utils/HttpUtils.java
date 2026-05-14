package io.orphea.build.shyne.Utils;

import org.apache.hadoop.shaded.com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpUtils {

    public static HashMap<String, String> sendGetRequest(String url, Map<String, String> headers) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }

        int responseCode = connection.getResponseCode();

        HashMap<String, String> response = new HashMap<>();

//        if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String responseMessage = in.readLine();
        in.close();

        response.put("response", responseMessage);
        response.put("responseCode", String.valueOf(responseCode));

        return response;
//        } else {
//            throw new RuntimeException(responseCode);
//        }
    }

    public static HashMap<String, String> sendPostRequest(String url, Object payload, String payloadType, Map<String, String> headers) throws Exception {
        URL url1 = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }

        String json;
        if (Objects.equals(payloadType, "json")) {
            Gson gson = new Gson();
            json = gson.toJson(payload);
        } else {
            json = payload.toString();
        }

        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        byte[] input = json.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);

        // Send the request and read the response
//        int status = connection.getResponseCode();
//        String response = connection.getResponseMessage();


//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        wr.writeBytes(payload);
//        wr.flush();
//        wr.close();

        int responseCode = connection.getResponseCode();
//        System.out.println(responseCode);
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String response = in.readLine();
//        in.close();

//        System.out.println(response);

        HashMap<String, String> response = new HashMap<>();

//        if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String responseMessage = in.readLine();
        in.close();


        response.put("response", responseMessage);
        response.put("responseCode", String.valueOf(responseCode));

        return response;
//        } else {
//            throw new RuntimeException("Failed to call API: HTTP error code " + responseCode);
//        }
    }
}