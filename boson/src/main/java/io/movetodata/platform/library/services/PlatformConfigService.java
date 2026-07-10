package io.movetodata.platform.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.movetodata.build.library.repository.BuildSpecificationsRepository;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.repository.ResourceRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.platform.library.models.LicenseInfoModel;
import io.movetodata.platform.library.models.PlatformConfig;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import io.movetodata.sharedutils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.movetodata.platform.enums.ProductType.DATA_HUB;
import static io.movetodata.platform.enums.ProductType.DATA_VIZ;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformConfigService {
    private static final String SECRET_KEY = "0720e7f10718fd5bbef379bccf3e5871";//should be 16,24,32 bytes
    private static final String SECRET_IV = "fc586c88ecf66614";
    private static final String AES = "AES";
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final BuildSpecificationsRepository buildSpecificationRepository;

    public static String encrypt(LicenseInfoModel licenseKeyRequestModel) {
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
    public static LicenseInfoModel decrypt(String encrypted) {
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

    public LicenseInfoModel getLicenseInfoModel() {
        if (platformConfigRepository.existsByName("platformConfig")) {
            PlatformConfig platformConfig = platformConfigRepository.findByName("platformConfig").get();
            if (platformConfig.getLicenseKey() != null) {
                return decrypt(platformConfig.getLicenseKey());
            }
        }
        return null;
    }

    public Boolean isResourceCreationLimitReached(ResourceType resourceType) {
        LicenseInfoModel licenseInfo = getLicenseInfoModel();
        if (licenseInfo == null) return true;
        Long limit = 0L;
        switch (resourceType) {
            case REPOSITORY:
                limit = licenseInfo.getMaximumRepositories();
                break;
            case CHART:
                limit = licenseInfo.getMaximumCharts();
                break;
            case DASHBOARD:
                limit = licenseInfo.getMaximumDashboards();
                break;
            case DATASET:
                limit = licenseInfo.getMaximumDatasets();
                break;
        }
        if (limit == -1) return false;

        return limit <= (long) resourceRepository.findAllByTypeEquals(resourceType).size();
    }

    public Boolean isUserLimitReached() {
        LicenseInfoModel licenseInfo = getLicenseInfoModel();
        if (licenseInfo == null) return true;

        Long limit = licenseInfo.getMaximumUsers();
        if (limit == -1) return false;
        return licenseInfo.getMaximumUsers() <= userRepository.findAll().size() - 1;
        // -1 is for handling platform Internal user.
    }

    public Boolean isTodayBuildsLimitReached() {
        LicenseInfoModel licenseInfo = getLicenseInfoModel();
        if (licenseInfo == null) return true;
        Long limit = licenseInfo.getMaximumBuildsPerDay();
        if (limit == -1) return false;
        return licenseInfo.getMaximumBuildsPerDay() <= buildSpecificationRepository.
                findAllByCreatedAtAfter(Utils.getStartOfDay()).size();
    }

    public PlatformConfig getPlatformConfig() {
        return platformConfigRepository.findByName("platformConfig").orElseThrow();
    }

    public boolean doesPlatformHasFractalUseCase() {
        LicenseInfoModel licenseInfo = getLicenseInfoModel();
        if (licenseInfo == null) return false;
        return !licenseInfo.getProduct().equals(DATA_VIZ.toString());
    }

    public boolean doesPlatformHasKeplerUseCase() {
        LicenseInfoModel licenseInfo = getLicenseInfoModel();
        if (licenseInfo == null) return false;
        return !licenseInfo.getProduct().equals(DATA_HUB.toString());
    }
}
