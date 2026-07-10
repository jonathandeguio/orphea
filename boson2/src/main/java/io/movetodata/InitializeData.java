package io.movetodata;

import com.amazonaws.util.StringInputStream;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.passport.library.models.*;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.PermissionMappingRepository;
import io.movetodata.passport.library.repository.RoleRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.UserService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;

import static io.movetodata.sharedUtils.Utils.isBase64;
import static io.movetodata.sharedUtils.storage.MoveToDataMinio.MinioConnection;

@SpringBootApplication
@RequiredArgsConstructor
public class InitializeData {

    private final GitService gitServiceSampleData;
    private final DatasetRepository datasetRepository;
    private final RoleRepository roleRepository;
    private final PermissionMappingRepository permissionMappingRepository;

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

    // The below is to create MoveToData, first it deletes user.home/movetodata directory and then adds data and folders - -
    @Bean
    CommandLineRunner run(UserService userService,
                          GroupService groupService,
                          UserRepository userRepository,
                          GroupsRepository groupsRepository,
                          RoleRepository roleRepository,
                          PermissionMappingRepository permissionMappingRepository,
                          PasswordEncoder passwordEncoder
    ) {
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

                        String certPath = System.getenv("MOVETODATA_MOUNT_PATH") + "/movetodata_ca_cert.pem";

                        RunOSCommandToGetCA(new String[]{"/bin/sh", "-c", "echo -n | openssl s_client -connect " + host + ":443 -servername " + host + "  | openssl x509 > " + certPath });


//                        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
//                        SSLSocket socket = (SSLSocket) factory.createSocket(host, 443);
//                        socket.startHandshake();
//                        Certificate[] certs = socket.getSession().getPeerCertificates();
//                        Certificate cert = certs[0];
//
//                        // Add CA cert to ignite keystore
//                        addX509CertificateToTrustStore(cert, "movetodata", bosonStorePath, "changeit", "PKCS12");


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
//                            System.out.println(certificate);
//
//                            // Write the certificate to a file
//                            FileOutputStream fos = new FileOutputStream(System.getenv("MOVETODATA_MOUNT_PATH") + "/movetodata_ca_cert.pem");
//                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
//                            writer.write(certificate.toString());
//                            writer.close();
//                            fos.close();
//                        }
//
//                        // Close the socket
//                        socket.close();

                        try {
                            addX509CertificateToTrustStoreViaFile(certPath, "movetodata", bosonStorePath, "changeit", "PKCS12");
                        } catch (Exception error) {
                            System.out.println(error);
                        }
                    }
                }
            }


            File movetodataGitHome = new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/git");
            movetodataGitHome.mkdirs();

            File movetodataGitRemoteHome = new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/git/remote");
            movetodataGitRemoteHome.mkdirs();

            File movetodataGitClonedHome = new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/git/cloned");
            movetodataGitClonedHome.mkdirs();

            if (System.getenv("BACKING_FS").equals("s3")) { // Create directories if not exists in s3
                // TODO : this needs doing
                //  System.out.println("Not done");
                try {
                    MinioClient minioClient = MinioConnection(
                            System.getenv("MINIO_ENDPOINT"),
                            System.getenv("MINIO_ACCESS_KEY"),
                            System.getenv("MINIO_SECRET_KEY"));
                    // create bucket if not exist.
                    boolean found =
                            minioClient.bucketExists(BucketExistsArgs.builder().bucket("movetodata").build());
                    if (!found) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket("movetodata").build());
                    }
                }
                catch (MinioException e) {
                    e.printStackTrace();
                }

            } else if (System.getenv("BACKING_FS").equals("gs")) { // Create directories if not exists in gs

                String google_cloud_credentials_decoded;

                if (isBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
                    google_cloud_credentials_decoded = new String(Base64.decodeBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS")));
                } else {
                    google_cloud_credentials_decoded = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
                }

//                System.out.println(google_cloud_credentials_decoded);

                InputStream google_cloud_credentials = new StringInputStream(google_cloud_credentials_decoded);

                Credentials credentials = GoogleCredentials.fromStream(google_cloud_credentials);
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();


                BlobId movetodataPath = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/");
                BlobId datasetPath = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/datasets/");
                BlobId repositoryPath = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/repositories/");
                BlobId sparkStreamingPath = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/spark-streaming/");

                // Check if the directory exists
                BlobInfo movetodataPathInfo = storage.get(movetodataPath);
                if (movetodataPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(movetodataPath).setContentType("application/x-directory").build(), new byte[0]);
                }

                // Check if the directory exists
                BlobInfo datasetPathInfo = storage.get(datasetPath);
                if (datasetPathInfo == null) {
                    storage.create(BlobInfo.newBuilder(datasetPath).setContentType("application/x-directory").build(), new byte[0]);
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

                Path movetodataPath = new Path("/movetodata");
                Path datasetPath = new Path("/movetodata/datasets");
                Path repositoryPath = new Path("/movetodata/repositories");
                Path sparkStreamingPath = new Path("/movetodata/spark-streaming");

                if (!fs.exists(movetodataPath)) {
                    fs.mkdirs(movetodataPath);
                }

                if (!fs.exists(datasetPath)) {
                    fs.mkdirs(datasetPath);
                }

                if (!fs.exists(repositoryPath)) {
                    fs.mkdirs(repositoryPath);
                }

                if (!fs.exists(sparkStreamingPath)) {
                    fs.mkdirs(sparkStreamingPath);
                }
            }

            // create default users

            if (!userRepository.existsByUsername("platform-administrator")) {

                RegisterRequest request = new RegisterRequest();

                userService.saveUser(new User(null, "Platform Administrator", "platform-administrator", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Platform", "Administrator", "None",
                        "",
                        "platform-administrator@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }

            if (!userRepository.existsByUsername("project-administrator")) {
                userService.saveUser(new User(null, "Project Administrator", "project-administrator", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Project", "Administrator", "None",
                        "",
                        "project-administrator@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }


            if (!userRepository.existsByUsername("group-administrator")) {
                userService.saveUser(new User(null, "Group Administrator", "group-administrator", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Group", "Administrator", "None",
                        "",
                        "group-administrator@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }

            if (!userRepository.existsByUsername("user-administrator")) {
                userService.saveUser(new User(null, "User Administrator", "user-administrator", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "User", "Administrator", "None",
                        "",
                        "user-administrator@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }

            if (!userRepository.existsByUsername("ignite-administrator")) {
                userService.saveUser(new User(null, "Ignite Administrator", "ignite-administrator", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Ignite", "Administrator", "None",
                        "",
                        "ignite-administrator@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }

            if (!userRepository.existsByUsername("movetodata-internal")) {
                User movetodataInternal = userService.saveUser(new User(null, "MoveToData Internal", "movetodata-internal", passwordEncoder.encode(UUID.randomUUID().toString()), "MoveToData", "Internal", "None",
                        "",
                        "movetodata-internal@movetodata.io", "auto", "auto", AuthProvider.local, null, null, null, null, null, null, null,RoleS3.ADMIN,null));
            }

            User platformAdministrator = userRepository.findByUsername("platform-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : platform-administrator")
            );
            User groupAdministrator = userRepository.findByUsername("group-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : group-administrator")
            );
            User userAdministrator = userRepository.findByUsername("user-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : user-administrator")
            );
            User igniteAdministrator = userRepository.findByUsername("ignite-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : ignite-administrator")
            );
            User projectAdministrator = userRepository.findByUsername("project-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : project-administrator")
            );

            // default groups
            List<User> platformAdministratorsOwners = List.of(platformAdministrator);
            List<User> platformAdministratorsMembers = List.of(platformAdministrator);

            if (!groupsRepository.existsByName("platform-administrators")) {
                groupService.saveGroup(new Groups(null, "platform-administrators", "This group is used for platform administrators", "active", platformAdministratorsOwners, new ArrayList<>(), platformAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> groupAdministratorsOwners = List.of(platformAdministrator, groupAdministrator);
            List<User> groupAdministratorsMembers = List.of(groupAdministrator, platformAdministrator);

            if (!groupsRepository.existsByName("group-administrators")) {
                groupService.saveGroup(new Groups(null, "group-administrators", "This group is used for group administrators", "active", groupAdministratorsOwners, new ArrayList<>(), groupAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> userAdministratorsOwners = List.of(platformAdministrator, userAdministrator);
            List<User> userAdministratorsMembers = List.of(userAdministrator, platformAdministrator);

            if (!groupsRepository.existsByName("user-administrators")) {
                groupService.saveGroup(new Groups(null, "user-administrators", "This group is used for ignite administrators", "active", userAdministratorsOwners, new ArrayList<>(), userAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> igniteAdministratorsOwners = List.of(platformAdministrator, igniteAdministrator);
            List<User> igniteAdministratorsMembers = List.of(igniteAdministrator, platformAdministrator);

            if (!groupsRepository.existsByName("ignite-administrators")) {
                groupService.saveGroup(new Groups(null, "ignite-administrators", "This group is used for ignite administrators", "active", igniteAdministratorsOwners, new ArrayList<>(), igniteAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
            }

            List<User> projectAdministratorsOwners = List.of(platformAdministrator, projectAdministrator);
            List<User> projectAdministratorsMembers = List.of(projectAdministrator, platformAdministrator);

            if (!groupsRepository.existsByName("project-administrators")) {
                groupService.saveGroup(new Groups(null, "project-administrators", "This group is used for platform administrators", "active", projectAdministratorsOwners, new ArrayList<>(), projectAdministratorsMembers, new Date(), null, platformAdministrator.getId(), null));
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

            Role listRole = roleRepository.findByName("Explorer");
            if (listRole == null) {
                Role list = roleRepository.save(new Role(null, "Explorer", false, false, false, "active"));
            }

            Role ownerRole1 = roleRepository.findByName("Owner");

            // default groups
            Groups projectAdministratorsGroup = groupsRepository.findByName("project-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(projectAdministratorsGroup.getId(), new UUID(0, 0), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingProjectAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, projectAdministratorsGroup.getId(), new UUID(0, 0), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups groupAdministratorsGroup = groupsRepository.findByName("group-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(groupAdministratorsGroup.getId(), new UUID(1, 1), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingGroupAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, groupAdministratorsGroup.getId(), new UUID(1, 1), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups userAdministratorsGroup = groupsRepository.findByName("user-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(userAdministratorsGroup.getId(), new UUID(2, 2), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingUsersAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, userAdministratorsGroup.getId(), new UUID(2, 2), ownerRole1, "active", new Date(), null, null, null));
            }

            Groups igniteAdministratorsGroup = groupsRepository.findByName("ignite-administrators");
            if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(igniteAdministratorsGroup.getId(), new UUID(3, 3), ownerRole1.getId())) {
                PermissionsMapping permissionsMappingIgniteAdministratorsGroupRole = permissionMappingRepository.save(new PermissionsMapping(null, igniteAdministratorsGroup.getId(), new UUID(3, 3), ownerRole1, "active", new Date(), null, null, null));
            }
        };
    }
}