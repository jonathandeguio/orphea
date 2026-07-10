package io.movetodata.sharedutils;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.movetodata.sharedutils.Utils.isBase64;
import static io.movetodata.sharedutils.Utils.isValidJson;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommonService {
    public static Credentials getGoogleCredentials() throws IOException {

        String google_cloud_credentials_decoded;

        if (isValidJson(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
            google_cloud_credentials_decoded = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        } else {
            if (isBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
                google_cloud_credentials_decoded = new String(Base64.decodeBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS")));

//
            } else {
                google_cloud_credentials_decoded = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
            }
        }

        InputStream google_cloud_credentials = new ByteArrayInputStream(google_cloud_credentials_decoded.getBytes(StandardCharsets.UTF_8));

        return GoogleCredentials.fromStream(google_cloud_credentials);
    }
}
