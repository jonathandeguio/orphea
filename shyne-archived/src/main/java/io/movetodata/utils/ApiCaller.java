package io.movetodata.utils;

import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ApiCaller {
    private static final String BASE_URL = System.getenv("MOVETODATA_API");
    private static final String AUTH_HEADER = "Authorization";


    public static HashMap<String, String> callApi(String endpoint, String method, String token, Object payload, String payloadType) throws Exception {
        String url = BASE_URL + endpoint;


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + token);


        HashMap<String, String> response;
        switch (method) {
            case "GET":
//                System.out.println(url);
                response = HttpUtils.sendGetRequest(url, headers);
                return response;
            case "POST":

//                System.out.println(">>>>> API DEBUG");
//                System.out.println(url);
//                System.out.println(payload);
////                System.out.println(headers);
//                System.out.println(">>>>> API DEBUG");
                response = HttpUtils.sendPostRequest(url, payload, payloadType, headers);
//                System.out.println(response);
                return response;
            default:
                throw new Exception("Error: unsupported http method");
        }


//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod(method);
//
//        if (token != null) {
//            connection.setRequestProperty(AUTH_HEADER, "Bearer " + token);
//        }
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String response = in.readLine();
//            in.close();
//            return response;
//        } else {
//            throw new RuntimeException("Failed to call API: HTTP error code " + responseCode);
//        }
    }

}

