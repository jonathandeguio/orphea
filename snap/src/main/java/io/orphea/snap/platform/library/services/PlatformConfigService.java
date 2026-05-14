package io.orphea.snap.platform.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.orphea.snap.passport.library.repository.UserRepository;
import io.orphea.snap.platform.library.models.LicenseInfoModel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformConfigService {
    private final UserRepository userRepository;
    private static final String SECRET_KEY = "0720e7f10718fd5bbef379bccf3e5871";//should be 16,24,32 bytes
    private static final String SECRET_IV = "fc586c88ecf66614";
    private static final String AES = "AES";
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
    public static String encrypt(LicenseInfoModel licenseKeyRequestModel)
    {
        try {
            // Convert data to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            String jsonString = objectMapper.writeValueAsString(licenseKeyRequestModel);

            IvParameterSpec iv = new IvParameterSpec(SECRET_IV.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(jsonString.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @SneakyThrows
    public static LicenseInfoModel decrypt(String encrypted)  {
            IvParameterSpec iv = new IvParameterSpec(SECRET_IV.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            final byte[] encryptedBytes = encrypted.getBytes(StandardCharsets.UTF_8);
            byte[] original = cipher.doFinal(Base64.getMimeDecoder().decode(encryptedBytes));

            // Convert the decrypted data from JSON string
            // Convert data to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return objectMapper.readValue(new String(original, StandardCharsets.UTF_8), LicenseInfoModel.class);
    }
}
