package io.orphea.snap.deployments.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.orphea.snap.deployments.library.Enums.ConfigurationState;
import io.orphea.snap.deployments.library.models.DeploymentModel;
import io.orphea.snap.deployments.library.models.LicenseModel;
import io.orphea.snap.deployments.library.repository.DeploymentRepository;
import io.orphea.snap.deployments.library.repository.LicenseRepository;
import io.orphea.snap.deployments.library.requests.LicenseRequestModel;
import io.orphea.snap.platform.library.models.LicenseInfoModel;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;
    private static final String SECRET_KEY = "0720e7f10718fd5bbef379bccf3e5871";//should be 16,24,32 bytes
    private static final String SECRET_IV = "fc586c88ecf66614";
    private static final String AES = "AES";
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
    @Transactional
    public LicenseModel createOrUpdateLicense(UUID deploymentId, LicenseRequestModel requestModel) {
        DeploymentModel deploymentModel = deploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment not found"));

        List<LicenseModel> licenses = deploymentModel.getLicenseModel();
        if (licenses != null && !licenses.isEmpty()) {
            for (LicenseModel license : licenses) {
                if (license.getState() == ConfigurationState.ACTIVE) {
                    license.setState(ConfigurationState.ARCHIVED);
                    license.setUpdatedAt(new Date());
                    license.setUpdatedBy(deploymentModel.getUpdatedBy());
                    licenseRepository.save(license);
                }
            }
        }

        LicenseModel newLicense = createNewLicense(requestModel, deploymentModel);
        licenseRepository.save(newLicense);

        return newLicense;
    }

    private LicenseModel createNewLicense(LicenseRequestModel requestModel, DeploymentModel deploymentModel) {
        LicenseModel newLicense = new LicenseModel();
        newLicense.setClient(requestModel.getClient());
        newLicense.setProduct(requestModel.getProduct());
        newLicense.setBaseUrl(requestModel.getBaseUrl());
        newLicense.setDisplayBlockedFeatures(requestModel.isDisplayBlockedFeatures());
        newLicense.setMaximumUsers(requestModel.getMaximumUsers());
        newLicense.setMaximumBuildsPerDay(requestModel.getMaximumBuildsPerDay());
        newLicense.setMaximumDatasets(requestModel.getMaximumDatasets());
        newLicense.setMaximumDashboards(requestModel.getMaximumDashboards());
        newLicense.setMaximumCharts(requestModel.getMaximumCharts());
        newLicense.setMaximumRepositories(requestModel.getMaximumRepositories());
        newLicense.setExpiresOn(requestModel.getExpiresOn());

        // Encrypted LicenseKey
        String licenseKey = encrypt(requestModel);
        newLicense.setLicenseKey(licenseKey);

        newLicense.setCreatedAt(new Date());
        newLicense.setCreatedBy(deploymentModel.getCreatedBy());
        newLicense.setUpdatedBy(deploymentModel.getUpdatedBy());
        newLicense.setState(ConfigurationState.ACTIVE);
        newLicense.setDeploymentModel(deploymentModel);
        return newLicense;
    }

    public static String encrypt(LicenseRequestModel requestModel)
    {
        try {
            // Convert data to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            String jsonString = objectMapper.writeValueAsString(requestModel);

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