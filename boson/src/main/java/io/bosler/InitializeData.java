package io.bosler;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.bosler.passport.enums.AuthProvider;
import io.bosler.passport.library.models.*;
import io.bosler.passport.library.repository.GroupsRepository;
import io.bosler.passport.library.repository.PermissionMappingRepository;
import io.bosler.passport.library.repository.RoleRepository;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.GroupService;
import io.bosler.passport.library.service.UserService;
import io.bosler.platform.library.models.*;
import io.bosler.platform.library.repository.*;
import io.bosler.sharedutils.CommonService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;

import static io.bosler.passport.enums.PlatformUsers.PlatformInternal;
import static io.bosler.sharedutils.storage.FSMinio.MinioConnection;

@SpringBootApplication
@RequiredArgsConstructor
public class InitializeData {

    private final SMTPConfigRepository smtpConfigRepository;

    public static void addX509CertificateToTrustStoreViaFile(String certPath, String certAlias, String storePath, String storePassword, String storeType)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        char[] storePasswordCharArr = Objects.requireNonNull(storePassword, "").toCharArray();

        KeyStore keystore;
        try (FileInputStream storeInputStream = new FileInputStream(storePath);
             FileInputStream certInputStream = new FileInputStream(certPath)) {
            keystore = KeyStore.getInstance(storeType);
            keystore.load(storeInputStream, storePasswordCharArr);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(certInputStream);

            keystore.setCertificateEntry(certAlias, certificate);
        }

        try (FileOutputStream storeOutputStream = new FileOutputStream(storePath)) {
            keystore.store(storeOutputStream, storePasswordCharArr);
        }
    }

    public static void addX509CertificateToTrustStore(Certificate certificate, String certAlias, String storePath, String storePassword, String storeType)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        char[] storePasswordCharArr = Objects.requireNonNull(storePassword, "").toCharArray();

        KeyStore keystore;
        try (FileInputStream storeInputStream = new FileInputStream(storePath)
        ) {
            keystore = KeyStore.getInstance(storeType);
            keystore.load(storeInputStream, storePasswordCharArr);

            keystore.setCertificateEntry(certAlias, certificate);
        }

        try (FileOutputStream storeOutputStream = new FileOutputStream(storePath)) {
            keystore.store(storeOutputStream, storePasswordCharArr);
        }
    }

    public static void RunOSCommandToGetCA(String[] command) {

        String s;
        Process p;
        try {


            p = Runtime.getRuntime().exec(command);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            while ((s = br.readLine()) != null)
                p.waitFor();

            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The below is to create Bosler, first it deletes user.home/bosler directory and then adds data and folders - -
    @Bean
    CommandLineRunner run(UserService userService,
                          GroupService groupService,
                          UserRepository userRepository,
                          GroupsRepository groupsRepository,
                          RoleRepository roleRepository,
                          PlatformConfigRepository platformConfigRepository,
                          PermissionMappingRepository permissionMappingRepository,
                          GitConfigRepository gitConfigRepository,
                          SparkConfigRepository sparkConfigRepository,
                          BackingFsRepository backingFsRepository, DataMartConfigRepository datamartConfigRepository,
                          CacheRepository cacheRepository) {
        return args -> {

            // Add CA certificate to boson  -- - - -
            if (System.getenv("GENERATE_CERTIFICATE") != null) {


                if (System.getenv("GENERATE_CERTIFICATE").equals("true")) {
                    if (!System.getProperty("os.name").startsWith("Windows")) { // below only works on linux based systems

                        // Add CA cert to boson keystore
                        String javaHome = System.getProperty("java.home");
                        String bosonStorePath = javaHome + "/lib/security/cacerts";

                        URI uri = new URI(System.getenv("BASE_URL"));

                        String host = uri.getHost();

                        String certPath = System.getenv("BOSLER_MOUNT_PATH") + "/bosler_ca_cert.pem";

                        RunOSCommandToGetCA(new String[]{"/bin/sh", "-c", "echo -n | openssl s_client -connect " + host + ":443 -servername " + host + "  | openssl x509 > " + certPath});


//                        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
//                        SSLSocket socket = (SSLSocket) factory.createSocket(host, 443);
//                        socket.startHandshake();
//                        Certificate[] certs = socket.getSession().getPeerCertificates();
//                        Certificate cert = certs[0];
//
//                        // Add CA cert to connect keystore
//                        addX509CertificateToTrustStore(cert, "bosler", bosonStorePath, "changeit", "PKCS12");


//                        // Create an SSL socket
//                        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//                        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
//
//                        // Start the SSL handshake
//                        socket.startHandshake();
//
//                        // Get the server's certificate chain
//                        Certificate[] certificates = socket.getSession().getPeerCertificates();
//                        for (Certificate certificate : certificates) {
//                            
//
//                            // Write the certificate to a file
//                            FileOutputStream fos = new FileOutputStream(System.getenv("BOSLER_MOUNT_PATH") + "/bosler_ca_cert.pem");
//                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
//                            writer.write(certificate.toString());
//                            writer.close();
//                            fos.close();
//                        }
//
//                        // Close the socket
//                        socket.close();

                        try {
                            addX509CertificateToTrustStoreViaFile(certPath, "bosler", bosonStorePath, "changeit", "PKCS12");
                        } catch (Exception error) {

                        }
                    }
                }
            }


            File boslerGitHome = new File(System.getenv("BOSLER_MOUNT_PATH") + "/git");
            boslerGitHome.mkdirs();

            File boslerGitRemoteHome = new File(System.getenv("BOSLER_MOUNT_PATH") + "/git/remote");
            boslerGitRemoteHome.mkdirs();

            File boslerGitClonedHome = new File(System.getenv("BOSLER_MOUNT_PATH") + "/git/cloned");
            boslerGitClonedHome.mkdirs();

            if (System.getenv("BACKING_FS").equals("s3")) { // Create directories if not exists in s3
                // TODO : this needs doing
                //  
                try {
                    MinioClient minioClient = MinioConnection(
                            System.getenv("MINIO_ENDPOINT"),
                            System.getenv("MINIO_ACCESS_KEY"),
                            System.getenv("MINIO_SECRET_KEY"));
                    // create bucket if not exist.
                    boolean found =
                            minioClient.bucketExists(BucketExistsArgs.builder().bucket("bosler").build());
                    if (!found) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket("bosler").build());
                    }
                } catch (MinioException e) {
                    e.printStackTrace();
                }

            } else if (System.getenv("BACKING_FS").equals("gs")) { // Create directories if not exists in gs

                Storage storage = StorageOptions.newBuilder().setCredentials(CommonService.getGoogleCredentials()).build().getService();


                BlobId boslerPath = BlobId.of(System.getenv("GS_BUCKET"), "bosler/");
                BlobId datasetPath = BlobId.of(System.getenv("GS_BUCKET"), "bosler/dataset/");
                BlobId filePath = BlobId.of(System.getenv("GS_BUCKET"), "bosler/file/");
                BlobId repositoryPath = BlobId.of(System.getenv("GS_BUCKET"), "bosler/repositories/");
                BlobId sparkStreamingPath = BlobId.of(System.getenv("GS_BUCKET"), "bosler/spark-streaming/");

                // Check if the directory exists
                BlobInfo boslerPathInfo = storage.get(boslerPath);
                if (boslerPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(boslerPath).setContentType("application/x-directory").build(), new byte[0]);
                }

                // Check if the directory exists
                BlobInfo datasetPathInfo = storage.get(datasetPath);
                if (datasetPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(datasetPath).setContentType("application/x-directory").build(), new byte[0]);
                }

                // Check if the directory exists
                BlobInfo filePathInfo = storage.get(filePath);
                if (filePathInfo == null) {
                    storage.create(BlobInfo.newBuilder(filePath).setContentType("application/x-directory").build(), new byte[0]);
                }

                // Check if the directory exists
                BlobInfo repositoryPathInfo = storage.get(repositoryPath);
                if (repositoryPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(repositoryPath).setContentType("application/x-directory").build(), new byte[0]);
                }

                // Check if the directory exists
                BlobInfo sparkStreamingPathInfo = storage.get(sparkStreamingPath);
                if (sparkStreamingPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(sparkStreamingPath).setContentType("application/x-directory").build(), new byte[0]);
                }

            } else if (System.getenv("BACKING_FS").equals("hdfs")) { // Create directories if not exists in hdfs
                // Set up the configuration
                Configuration conf = new Configuration();

                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));


                // Get a reference to the filesystem
                FileSystem fs = FileSystem.get(conf);

                Path boslerPath = new Path("/bosler");
                Path datasetPath = new Path("/bosler/dataset");
                Path filePath = new Path("/bosler/file");
                Path repositoryPath = new Path("/bosler/repositories");
                Path sparkStreamingPath = new Path("/bosler/spark-streaming");

                if (!fs.exists(boslerPath)) {
                    fs.mkdirs(boslerPath);
                }

                if (!fs.exists(datasetPath)) {
                    fs.mkdirs(datasetPath);
                }

                if (!fs.exists(filePath)) {
                    fs.mkdirs(filePath);
                }

                if (!fs.exists(repositoryPath)) {
                    fs.mkdirs(repositoryPath);
                }

                if (!fs.exists(sparkStreamingPath)) {
                    fs.mkdirs(sparkStreamingPath);
                }
            } else if (System.getenv("BACKING_FS").equals("localfs")) { // Create directories if not exists in hdfs
                String boslerPath = System.getenv("LOCAL_FS_DIRECTORY");
                String datasetPath = System.getenv("LOCAL_FS_DIRECTORY") + "/dataset";
                String filePath = System.getenv("LOCAL_FS_DIRECTORY") + "/file";
                String repositoryPath = System.getenv("LOCAL_FS_DIRECTORY") + "/repositories";
                String sparkStreamingPath = System.getenv("LOCAL_FS_DIRECTORY") + "/spark-streaming";

                File boslerPathDirectory = new File(boslerPath);

                if (!boslerPathDirectory.exists()) {
                    boolean boslerResultDir = boslerPathDirectory.mkdirs();
                }

                File datasetPathDirectory = new File(datasetPath);
                if (!datasetPathDirectory.exists()) {
                    boolean res = datasetPathDirectory.mkdirs();
                    if (!res) {

                        System.exit(-1);
                    }
                }

                File filePathDirectory = new File(filePath);
                if (!filePathDirectory.exists()) {
                    filePathDirectory.mkdirs();
                }

                File repositoryPathDirectory = new File(repositoryPath);
                if (!repositoryPathDirectory.exists()) {
                    repositoryPathDirectory.mkdirs();
                }
                File sparkStreamingPathDirectory = new File(sparkStreamingPath);
                if (!sparkStreamingPathDirectory.exists()) {
                    sparkStreamingPathDirectory.mkdirs();
                }

            }

            // create default users

            final String defaultPassword = "bosler2024";

            if (!userRepository.existsByUsername("platform-administrator")) {
                userService.saveUser(new User(null, "Platform Administrator", "platform-administrator", defaultPassword, "Platform", "Administrator", "None",
                        "",
                        "platform-administrator@bosler.io", AuthProvider.local, null, null, false, null, false, null, new UserPreferences(), new NotificationPreferences().getId(), null, null, null, null, null));
            }

            if (!userRepository.existsByUsername(PlatformInternal.toString())) {
                User platformInternal = userService.saveUser(new User(null, "Platform Internal", PlatformInternal.toString(), "b071181ada05d564a60d", "Platform", "Internal", "None",
                        "",
                        "platform-internal@bosler.io", AuthProvider.local, null, null, false, null, false, null, new UserPreferences(), new NotificationPreferences().getId(), null, null, null, null, null));
            }

            User platformAdministrator = userRepository.findByUsername("platform-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : platform-administrator")
            );

            // default groups
            List<User> platformAdministratorsOwners = List.of(platformAdministrator);
            List<User> platformAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("platform-administrators")) {
                groupService.saveGroup(new Groups(null, "platform-administrators", "This group is used for platform administrators", "active", platformAdministratorsOwners, new ArrayList<>(), platformAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> groupAdministratorsOwners = List.of(platformAdministrator);
            List<User> groupAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("group-administrators")) {
                groupService.saveGroup(new Groups(null, "group-administrators", "This group is used for group administrators", "active", groupAdministratorsOwners, new ArrayList<>(), groupAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> userAdministratorsOwners = List.of(platformAdministrator);
            List<User> userAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("user-administrators")) {
                groupService.saveGroup(new Groups(null, "user-administrators", "This group is used for user administrators", "active", userAdministratorsOwners, new ArrayList<>(), userAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> connectAdministratorsOwners = List.of(platformAdministrator);
            List<User> connectAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("connect-administrators")) {
                groupService.saveGroup(new Groups(null, "connect-administrators", "This group is used for connect administrators", "active", connectAdministratorsOwners, new ArrayList<>(), connectAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> projectAdministratorsOwners = List.of(platformAdministrator);
            List<User> projectAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("project-administrators")) {
                groupService.saveGroup(new Groups(null, "project-administrators", "This group is used for project administrators", "active", projectAdministratorsOwners, new ArrayList<>(), projectAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            // Role Sample Data
            Role viewerRole = roleRepository.findByName("Viewer");
            if (viewerRole == null) {
                Role viewer = roleRepository.save(new Role(null, "Viewer", false, false, true, "active"));
            }

            Role editorRole = roleRepository.findByName("Editor");
            if (editorRole == null) {
                Role editor = roleRepository.save(new Role(null, "Editor", false, true, true, "active"));
            }

            Role ownerRole = roleRepository.findByName("Owner");
            if (ownerRole == null) {
                Role owner = roleRepository.save(new Role(null, "Owner", true, true, true, "active"));
            }

//            Role listRole = roleRepository.findByName("Explorer");
//            if (listRole == null) {
//                Role list = roleRepository.save(new Role(null, "Explorer", false, false, false, "active"));
//            }

            Role ownerRole1 = roleRepository.findByName("Owner");

            // default groups


            Groups platformAdministratorsGroup = groupsRepository.findByName("platform-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(platformAdministratorsGroup.getId(), new UUID(0, 0), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingProjectAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, platformAdministratorsGroup.getId(), new UUID(0, 0), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups groupAdministratorsGroup = groupsRepository.findByName("group-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(groupAdministratorsGroup.getId(), new UUID(1, 1), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingGroupAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, groupAdministratorsGroup.getId(), new UUID(1, 1), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups userAdministratorsGroup = groupsRepository.findByName("user-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(userAdministratorsGroup.getId(), new UUID(2, 2), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingUsersAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, userAdministratorsGroup.getId(), new UUID(2, 2), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups connectAdministratorsGroup = groupsRepository.findByName("connect-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(connectAdministratorsGroup.getId(), new UUID(3, 3), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingConnectAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, connectAdministratorsGroup.getId(), new UUID(3, 3), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups projectAdministratorsGroup = groupsRepository.findByName("project-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(projectAdministratorsGroup.getId(), new UUID(4, 4), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingProjectAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, projectAdministratorsGroup.getId(), new UUID(4, 4), ownerRole1, "active", new Date(), null, null, null));
            }

            if (!platformConfigRepository.existsByName("platformConfig")) {
                platformConfigRepository.save(new PlatformConfig(null, new Date(), new Date(), null, null, "Bosler", "platformConfig", true, 250000, 0L, 500, true, 2592000L, true, "Europe/Paris", 2, 2, false, false, null, "https://pypi.python.org/simple/", "http://username:pssword@proxy.example.com", null, "[]", "master", false, false, false));
            }

            if (!smtpConfigRepository.existsByConfig("platform")) {
                smtpConfigRepository.save(new SMTPConfigModel("platform", "bosler.mailer@gmail.com", "lvrbzwlbkivpnpkn", "smtp.gmail.com", 587, "true", "true"));
            }

            if (!gitConfigRepository.existsByConfig("platform")) {
                gitConfigRepository.save(new GitConfigModel("platform", "julia", 58080, 58542));
            }

            if (!sparkConfigRepository.existsByConfig("platform")) {
                sparkConfigRepository.save(new SparkConfigModel("platform", "local[*]", "local", "local", "local", "kubernetes", "kubernetes", "local", "kubernetes"));
            }

            if (!backingFsRepository.existsByConfig("platform")) {
                backingFsRepository.save(new BackingFsConfigModel("platform", "localfs", "datasets_collections_junebos23", "ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAianVuZWJvczIzIiwKICAicHJpdmF0ZV9rZXlfaWQiOiAiM2ExOWI3ODJlOTkwNGNjNzZkODc3MzQ5MDBjMDg2YjdhMGQwM2Y3MSIsCiAgInByaXZhdGVfa2V5IjogIi0tLS0tQkVHSU4gUFJJVkFURSBLRVktLS0tLVxuTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFDNVhsSU82bzh3OHRMVlxuZkdNTDRNcDJCY1VUTE9wdUdWMFl1N1hLeW9LbEhYekFWcXhYUTB3aFVWclJnZ2hLZEVuRlJhMnpRRUJEeDdlL1xuaWRRaGk4ZTlDZU9oYnQyaE5LVlcwdS9WUFFjR3F1Z0UrYThROHdwY0NqZzFPVVpHNlZoNmtZS053Z0tPeko3YVxudlFtN2JIWWFyaFRBUkc1N3dGZzBqb1hBR3locVdtRVRhZVdFZnV2NGtzVEpOVkMranlPSVFDYmFuV0crQVJHclxuWlZXSmFva1E5anZSOEF1RzhTa2tMRUpLYlh3c0xObzFibHVkUUE3Vlc1Ry9xSmlNc1dnWjNpT2RFUTB4Y3ZIaFxuaEZicWR2MWRjYytpTklFcmhTYUpEWWtGK1ZaU0Z0TVFvWm9OaWNnQ010ME1kNFYzc3p4azM2N3JGVzRtOU5IYVxucEV5WmFuUXJBZ01CQUFFQ2dnRUFCMkRZdTdiY2FLS1JzdjdqZk1kWk5IOXRHSWxNakU2TFdFdXUxMW5FbVJWL1xuL0xKUUZ3T0JWZ3lSSktpQ2pTd2dMNVhKYlpkUHQ0bGFLQU5qMXNyS0pZT0hEakZTZ3d5L21tNkUrMWRJdUJ3NFxuLzU1LzBmOTl2eElnUTlMZ1pRajdoVFBWSHhiNENySGNXejNCQnRDWFk2d1RZdmxqSWdWNVZ1eWE5UmNzY2FjMVxuamZNcDVyVlZ2K21zSnYzNzZJQUJTa2Jram02ZUdnM2VJNlZROUZ4Uzd0Y04xbU11ek9zUXI4VFF4YkxpQXQyY1xucXRqLzRKYXpUbjk4Yi82dXNpSk5Ock5NUHFZcUJITmpjVG0vbDc3Z2I1RGV2UDFCRkxoc3NVdExieXhoNE9CYlxuUnlRNXQxZUQ1ZzZzOFhhTTdSRFZYUXZ5RituUnlCMFhKamVYYThReUtRS0JnUURiYWFEaU1ZR1h2SktCMDFLTVxuZjg3WlUvdWwvSmJSUlRYU2JYNjF1N1hreU5TSnY4MnZpQWhqN3RNUk9jM0Jta25ROXJCKy9NWDNzWTduMWhsa1xuTERIb3I5cWtPMGRHOEFxWHZsdDRUeDlPbzhERVZFa2pkbTZVUkFnc0FWWjV5V1FsNWsxbURRQ0dEbWl6ci8xd1xuaUFuMjR6Z1lkUUZYOTZMVTRKZmNyb1BydFFLQmdRRFlSMmF4dyt3SUdGWDgwMVFHd1F2V0xidThpcnJGRmZrWVxuNWZ2L21PWjNhc1oxdTNBazFUYlkvVTFjdEh6U0RKMVlpS3ZqOS9wZG1qRk10cXhtdURhTVFBOGFUbUEyT25CM1xuL1QyT0ZnYkh2d0FOeFllSXhiaktXbUIyQ1U1Tk5ZTi9KaTFwNi9hdEkrRWJIaWFXZzd2ME03UWRxeVBNZ3B5ZlxuL2NPVG1yT01Yd0tCZ1FDVW5oU2FnZHY4MG44T0x2eWdMRVlJMFNrSVFuSkpwNjZRTFJpQnNuYS9WQVVhbE8rV1xuY1BiMThhUEg3RFpoWjhlT0lqcGttWVRpaE1OK1ozZmxjVVlSRFdSMGo2NTA3MHpOZ2VQbE52ckd3b1prdFk2Z1xuMWF5TEF4WGg4TG9KWGc5VzJJc2VqN29HRTZaNTU4QXNSQWt6MEs2RGFtV1JseXdWTTFmSjQrOTViUUtCZ1FDN1xueEJscklaNU1WTnhxT0FwT0p0RWc2K2wrUWE3dXBpbnhyTkUvMFQzYzFRMWhidzVvVzlkWFFXb2xudWh5NzlLMlxuNTBqY0RSRUdySFE2bHFsTU1JZmk4VVpyWHpuMk9ud2tIKzZONlorUFVNbzZLcENGZEtyK3VzSGVHQ3dycWRiMFxuejI5RDgwRGdoakxMZVR5bEZjOWFybnlqK1UyR0lxcloxOVV3MDNFbjN3S0JnRGt0aWFieEd2TVNCRWpDcFg0YlxuM09NR1dHcTlnMGFwQlJkd2VxV0lMajc5b21tOEFaY292a1g1WGdoRHdkaU1JMEtnRWxqbnhYZG1rSUFIbkp5RFxubjh0dzc5dm9jMzJTRUlCTTFWUzZLV3pCdmpkTGZkeTByMDZTcGlkZVJuSTlFUXZJVkF4N2pzUktzczNhSDVlbFxuZWpJNjRMamlGV0ZvVmRxUEYvczFPdmZqXG4tLS0tLUVORCBQUklWQVRFIEtFWS0tLS0tXG4iLAogICJjbGllbnRfZW1haWwiOiAiZ29vZ2xlLXN0b3JhZ2Utc2FAanVuZWJvczIzLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwKICAiY2xpZW50X2lkIjogIjEwNzg1Mzc4ODA3ODkwMTE5NDQ2NyIsCiAgImF1dGhfdXJpIjogImh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi9hdXRoIiwKICAidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwKICAiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsCiAgImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvZ29vZ2xlLXN0b3JhZ2Utc2ElNDBqdW5lYm9zMjMuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJ1bml2ZXJzZV9kb21haW4iOiAiZ29vZ2xlYXBpcy5jb20iCn0K", "C:\\Bosler", "hdfs://localhost:8020", "http://localhost:9000", "minioadmin", "minioadmin"));
            }
            
            if (!datamartConfigRepository.existsByConfig("platform")) {
                DataMartConfigModel datamartConfigModel = new DataMartConfigModel();
                datamartConfigModel.setConfig("platform");
                datamartConfigRepository.save(datamartConfigModel);
            }

            if (!cacheRepository.existsByConfig("platform")) {
                cacheRepository.save(CacheConfigModel.builder()
                        .config("platform")
                        .cache(true)
                        .cacheExpiration(2592000L)
                        .useRedis(true)
                        .redisUrl("http://boson-redis:6379")
                        .build());
            }
        };
    }
}