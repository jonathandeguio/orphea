package io.orphea.connect.library.services;

import io.orphea.connect.library.models.*;
import io.orphea.connect.library.repository.*;
import io.orphea.connect.library.requests.AgentRequestDTO;
import io.orphea.connect.library.responses.AgentResponse;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.passport.security.TokenProvider;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.*;

import static io.orphea.sharedutils.Utils.copyNonNullProperties;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class AgentService {
    private final ResourceService resourceService;
    private final AgentRepository agentRepository;
    private final ConnectConfigRepository connectConfigRepository;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AgentOneTimeCodeService agentOneTimeCodeService;
    private final AgentStatsRepository agentStatsRepository;
    private final SourcesRepository sourcesRepository;
    private final LinkRepository linkRepository;

    @Autowired
    public AgentService(ResourceService resourceService, AgentRepository agentRepository, ConnectConfigRepository connectConfigRepository, TokenProvider tokenProvider, UserRepository userRepository, AgentOneTimeCodeService agentOneTimeCodeService, AgentStatsRepository agentStatsRepository, SourcesRepository sourcesRepository, LinkRepository linkRepository) {
        this.resourceService = resourceService;
        this.agentRepository = agentRepository;
        this.connectConfigRepository = connectConfigRepository;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.agentOneTimeCodeService = agentOneTimeCodeService;
        this.agentStatsRepository = agentStatsRepository;
        this.sourcesRepository = sourcesRepository;
        this.linkRepository = linkRepository;
    }

    public boolean existsById(UUID id) {
        return agentRepository.existsById(id);
    }

    public Agents findById(UUID id) {
        return agentRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteById(@NotNull UUID id) {
        if (!existsById(id)) {
            throw new NoSuchElementException(MessageFormat.format("agent with Id {0} does not exist", id));
        }

        agentRepository.deleteById(id);
        resourceService.deleteById(id);
    }
    public List<Source> getSources(UUID agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException(MessageFormat.format("agent with Id {0} does not exist", agentId));
        }
        List<Source> sourceList = sourcesRepository.findAllByAgentId(agentId);
        return sourceList;
    }

    @NotNull
    public List<Link> getLinks(UUID agentId) {
        List<Source> sourceList = getSources(agentId);

        List<Link> linkList = new ArrayList<>();
        sourceList.parallelStream().forEach(source -> {
            linkList.addAll(linkRepository.findBySourceId(source.getId()));
        });
        return linkList;
    }

    public AgentResponse createNewAgent(AgentRequestDTO agents, UUID userId) {
        ResourceModel resourceModel = resourceService.newResource(agents.getName(), agents.getDescription(), ResourceType.AGENT, ResourceSubtype.NONE, userId, agents.getParent());

        Agents agent = Agents.builder().id(resourceModel.getId()).build();
        Agents agents1 = agentRepository.save(agent);

        // Also create initial config
        ConnectConfig connectConfig = new ConnectConfig();
        connectConfig.setAgentId(agents1.getId());
        connectConfig.setVersion(UUID.randomUUID());
        connectConfig.setCreatedAt(new Date());
        connectConfig.setCreatedBy(userId);
        connectConfigRepository.save(connectConfig);

        // Create download url
        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeService.newAgentOneTimeCode(agents1.getId(), userId);
        AgentResponse response = AgentResponse.builder().agent(agents1).agentOneTimeCode(agentOneTimeCode.getCode()).build();
        return response;
    }

    public AgentResponse getAgentResponse(UUID userId, UUID agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException(MessageFormat.format("agent with Id {0} does not exist", agentId));
        }

        Agents agents = agentRepository.findById(agentId).get();

        // Create download url
        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeService.newAgentOneTimeCode(agents.getId(), userId);
        AgentResponse response = AgentResponse.builder().agent(agents).agentOneTimeCode(agentOneTimeCode.getCode()).build();
        return response;
    }
    @NotNull
    public Agents updateAgent(Agents agent, UUID userId) {
        if (!agentRepository.existsById(agent.id)) {
            throw new NoSuchElementException(MessageFormat.format("agent with Id {0} does not exist", agent.id));
        }

        Agents agentsExisting = agentRepository.getById(agent.getId());
        ResourceModel folder = resourceService.findById(agent.getId()).orElseThrow();

        folder.copyNonNullProperties(agent);

        folder.setUpdatedAt(new Date());
        folder.setUpdatedBy(userId);
        agent.copyNonNullProperties(agent);

        Agents agents1 = agentRepository.save(agentsExisting);

        // Update config
        updateConfig(agents1.getId());
        return agents1;
    }


    public Map<String, String> resolveOsAndArch(String os, String arch) {

        HashMap<String, String> result = new HashMap<>();

        switch (os) {
            case "Windows":
                result.put("os", "win");
                break;
            case "Darwin":
                result.put("os", "macosx");
                break;
            case "Linux":
                result.put("os", "linux");
                break;
            default:
                result.put("os", "not supported");
                break;
        }

        switch (arch) {
            case "x86_64":
                result.put("arch", "x64");
                break;
            case "arm64":
                result.put("arch", "aarch64");
                break;
            default:
                result.put("arch", "not supported");
                break;
        }

        return result;
    }

    public void runOSCommand(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            do {
                p.waitFor();
                s = br.readLine();
            } while (s != null);

            p.destroy();
            // TODO : this causing issue : java.lang.IllegalThreadStateException: process hasn't exited
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public AgentStats getAgentStats(UUID agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException(MessageFormat.format("agent with Id {0} does not exist", agentId));
        }

        AgentStats agentStats = agentStatsRepository.findById(agentId).orElseThrow();
        return agentStats;
    }

    public void addX509CertificateToTrustStore(Certificate certificate, String certAlias, String storePath, String storePassword, String storeType)
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

    public void buildConnect(String connectRepositoryPath) {
        try (ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(connectRepositoryPath))
                .connect()) {

            BuildLauncher build = connection.newBuild();

            //select tasks to run:
            build.forTasks("build");

            //include some build arguments:
            build.withArguments("--stacktrace", "-x", "test");

            build.run();
        }
    }

    public void downloadJava(String javaPackage, File javaTarPackage) {
        String javaDownloadUrl = "https://cdn.azul.com/zulu/bin/" + javaPackage + ".tar.gz";

        //

        try (BufferedInputStream in = new BufferedInputStream(new URL(javaDownloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(javaTarPackage)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception


        }
    }

    public boolean cloneOrPullConnect(File connectRepository, String igniteRepositoryPath) throws GitAPIException, IOException {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(System.getenv("FRACTAL_TEMPLATES_TOKEN"), "");
        if (!connectRepository.exists()) {
            String igniteRepositoryUrl = "https://" + System.getenv("FRACTAL_TEMPLATES_TOKEN") + "@github.com/Orphea-io/Ignite.git";
            Git.cloneRepository()
                    .setURI(igniteRepositoryUrl)
                    .setCredentialsProvider(credentialsProvider)
                    .setDirectory(new File(igniteRepositoryPath))
                    .call();
            return true;
        } else {
            Repository igniteRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(igniteRepositoryPath + File.separator + ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            Git igniteGitRepository = new Git(igniteRepo);
            PullResult pullResult = igniteGitRepository.pull().setCredentialsProvider(credentialsProvider).setRemote("origin").setRemoteBranchName("main").call();

            return !pullResult.getFetchResult().getTrackingRefUpdates().isEmpty(); // return true if more tha 0
        }
    }

    public void addFilesToTar(TarArchiveOutputStream taos, File file, String parent) throws IOException {
        // Add the file to the tar archive
        TarArchiveEntry entry = new TarArchiveEntry(file, parent + file.getName());
        taos.putArchiveEntry(entry);
        if (file.isFile()) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) > 0) {
                    taos.write(buffer, 0, len);
                }
            }
        }
        taos.closeArchiveEntry();

        // Recursively add the child files and directories
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                addFilesToTar(taos, child, parent + file.getName() + "/");
            }
        }

    }

    @NotNull
    public List<Agents> getAllAgents(UUID userId) {
        List<ResourceModel> resourceModels = resourceService.getActiveResources(userId, ResourceType.AGENT);

        List<Agents> agentsList = new ArrayList<>();

        for (ResourceModel resourceModel : resourceModels) {
            Optional<Agents> agent = agentRepository.findById(resourceModel.getId());
            agent.ifPresent(agentsList::add);
        }
        return agentsList;
    }

    public void updateConfig(UUID agentId) {
        ConnectConfig connectConfig = new ConnectConfig();
        connectConfig.setAgentId(agentId);
        connectConfig.setVersion(UUID.randomUUID());
        connectConfig.setUpdatedAt(new Date());
        connectConfigRepository.save(connectConfig);
    }



    @NotNull
    public Resource getObjectResponseEntity(HttpServletRequest request, String code, HashMap<String, String> client) throws IOException, GitAPIException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        // Usage : curl -OJ http://localhost:8080/api/ignite/agent/download/sfdsfsf

        // Steps :
        // 1. clone or pull ignite - done
        // 2. download java -- done
        // 3. untar it - done
        // 3a. copy java into build directory - done
        // 3b. embed ssl certificate - done
        // 4. build ignite jar - done
        // 4a. embed it into ignite package to /bin - done
        // 5. create tar file for agentId with token etc
        // 6. present tar for download - done
        // 7. try catch to cleanup and delete build directory and build tar -- not working
        // 8. create config file into the /etc
        // 9. get os arch and name from POST from client requesting download

        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeService.findByCode(code);
        UUID agentId = agentOneTimeCode.getAgentId();

        Agents agents = agentRepository.getById(agentOneTimeCode.getAgentId());
        User user = userRepository.getById(agentOneTimeCode.getCreatedBy());

        if (!agentOneTimeCodeService.existsByCode(code)) {
            throw new NoSuchElementException("Code does not exist.");
        }

        if (agentOneTimeCode.isDownloadUsed()) {
            throw new UnsupportedOperationException("Download Code has already been used.");
        }

        if (agentStatsRepository.existsById(agentOneTimeCode.getAgentId())) {
            AgentStats agentStats = agentStatsRepository.getById(agentOneTimeCode.getAgentId());

            if (!client.get("hostname").equals(agentStats.getHostname())) {
                throw new UnsupportedOperationException("This agent is already installed on another host " + agentStats.getHostname() + " , you can not install same agent on two hosts");
            }

            if (client.get("os").equals("Linux") || client.get("os").equals("Darwin")) {

            } else {
                throw new UnsupportedOperationException("You operating system is not supported");
            }
        }
        agentOneTimeCodeService.setDownloadUsed(agentOneTimeCode);

        URL url = new URL(System.getenv("BASE_URL"));
        String serverName = url.getHost();
        String baseUrl = "https://" + serverName + "/api";

        if (Objects.equals(request.getServerName(), "localhost")) {
            baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());
        }

        Date expiresIn = new Date(System.currentTimeMillis() + 10L * 60 * 100000000);
        String access_token = tokenProvider.buildToken(user.getId(), expiresIn);

        String igniteBase = System.getenv("ORPHEA_MOUNT_PATH") + "/ignite";

        String buildPath = igniteBase + File.separator + "Ignite";
        File buildPathFolder = new File(buildPath);

        String tarPackage = igniteBase + File.separator + "Ignite" + ".tar.gz";
        File tarPackageFile = new File(tarPackage);

        String buildJavaPath = buildPath + File.separator + "java";

        String IgniteRepositoryPath = igniteBase + File.separator + "cloned";
        File igniteRepository = new File(IgniteRepositoryPath);

        String osName = resolveOsAndArch(client.get("os"), client.get("arch")).get("os");
        String arch = resolveOsAndArch(client.get("os"), client.get("arch")).get("arch");

        String javaPackage = "zulu11.52.13-ca-jdk11.0.13-" + osName + "_" + arch;
        String JavaPath = igniteBase + File.separator + "java";
        File javaPath = new File(JavaPath);
        File javaTarPackage = new File(JavaPath + File.separator + javaPackage + ".tar.gz");

        String javaBuildPath = buildPath + File.separator + "java";
        File javaBuildPathFolder = new File(javaBuildPath);

        String javaPathExtracted = JavaPath + File.separator + javaPackage;
        File javaPathExtractedFolder = new File(javaPathExtracted);


        boolean newVersion;

        // clean up first, its causing config issues
        if (buildPathFolder.exists()) {
            FileUtils.deleteDirectory(buildPathFolder);
        }

        if (tarPackageFile.exists()) {
            tarPackageFile.delete();
        }

        try {
            // clone or pull ignite
            newVersion = cloneOrPullConnect(igniteRepository, IgniteRepositoryPath);
            if (!buildPathFolder.exists()) {
                // create directories
                List<String> directories = Arrays.asList("/bin", "/etc", "/etc/certs", "/logs", "/run", "/tmp");
                for (String directory : directories) {
                    File folder = new File(buildPath + directory);
                    boolean dir = folder.mkdirs();
                }
            }

            // download java if not exists
            if (!javaPath.exists()) {
                javaPath.mkdir();
            }

            if (!javaTarPackage.exists()) {
                downloadJava(javaPackage, javaTarPackage);
            }

            if (!javaPathExtractedFolder.exists()) {
                runOSCommand("tar -xf " + javaTarPackage + " -C " + JavaPath); // TODO : do this programmatically in java

//              extractTar(javaTarPackage, javaPath);
            }

            runOSCommand("cp -r " + javaPathExtractedFolder + " " + javaBuildPathFolder);
            // get CA certificate : below will not work if the CA certificate is not in boson to add use below :
            // get orphea certificate form chrome. click on lock on url then (Mac) Option Key + drag the lock to notepad
            // this fails if cert is not there : keytool -import -trustcacerts -keystore ../Library/Java/JavaVirtualMachines/azul-16.0.2/Contents/Home/lib/security/cacerts --storepass changeit -noprompt -alias orphea -file ./orphea_public_cert.cer
            // keytool -import -trustcacerts -keystore /User/rakeshmalik/Library/Java/JavaVirtualMachines/openjdk-16.0.1/Contents/Home/lib/security/cacerts --storepass changeit -noprompt -alias orphea -file ./orphea_cert.pem

            // oncloud : keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts --storepass changeit -noprompt -alias orphea -file orphea_ca_cert.pem
            if (System.getenv("GENERATE_CERTIFICATE") != null) {
                if (System.getenv("GENERATE_CERTIFICATE").equals("true")) {
                    SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
                    SSLSocket socket = (SSLSocket) factory.createSocket(serverName, 443);
                    socket.startHandshake();
                    Certificate[] certs = socket.getSession().getPeerCertificates();
                    Certificate cert = certs[0];

                    // Add CA cert to ignite keystore
                    String storePath = buildJavaPath + "/lib/security/cacerts";
                    addX509CertificateToTrustStore(cert, "orphea", storePath, "changeit", "PKCS12");
                }
            }

            // copy files to build package
            Files.copy(Paths.get(IgniteRepositoryPath + "/ignite_package/bin/ignite.sh"), Paths.get(buildPath + "/bin/ignite.sh"), REPLACE_EXISTING);

            // create ignite config
            String igniteConfigPath = buildPath + "/etc/ignite.conf";
            List<String> lines = Arrays.asList("PORT=7845\n" + "AGENT_ID=" + agentId + "\n" + "BASE_URL=" + baseUrl + "\n" + "TOKEN=" + access_token + "\n" + "PROXY=" + agents.isProxy() + "\n" + "HTTP_PROXY=" + agents.getHttpProxy() + "\n" + "HTTPS_PROXY=" + agents.getHttpsProxy() + "\n");

            Files.write(Paths.get(igniteConfigPath), lines, StandardCharsets.UTF_8);


            // build gradle for ignite only if new version
            if (newVersion || !tarPackageFile.exists()) {
                buildConnect(IgniteRepositoryPath);
                // Copy jar to build path
                Files.copy(Paths.get(IgniteRepositoryPath + "/build/libs/Ignite-0.0.2.jar"), Paths.get(buildPath + "/bin/Ignite-0.0.2.jar"), REPLACE_EXISTING);

                if (tarPackageFile.exists()) {
                    tarPackageFile.delete();
                }

                // TODO : do this programmatically in java
                runOSCommand("tar cfz " + tarPackage + " -C " + igniteBase + "/ " + "Ignite");
            }

            // Present package for download
            Path path = Paths.get(tarPackage);
            Resource resource = null;
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            assert resource != null;
            return resource;
        } finally {
//             TODO : FileUtils.deleteDirectory(buildPathFolder);
            File installScriptPath = new File(igniteBase + "/installScript.sh");

            installScriptPath.delete();
//          tarPackageFile.delete();
        }
    }
    @NotNull
    public Resource getResource(HttpServletRequest request, String code) throws IOException {
        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeService.findByCode(code);

        if (!agentOneTimeCodeService.existsByCode(code)) {
            throw new NoSuchElementException("Code does not exist.");
        }

        if (agentOneTimeCode.isInstallUsed()) {
            throw new UnsupportedOperationException("Install Code has already been used.");
        }

        agentOneTimeCode.setInstallUsed(true);

        agentOneTimeCodeService.setDownloadUsed(agentOneTimeCode);

        String baseUrl = "https://" + request.getServerName() + "/api";

        if (Objects.equals(request.getServerName(), "localhost")) {
            baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());
        }

        String igniteBasePath = System.getenv("ORPHEA_MOUNT_PATH") + "/ignite";
        File igniteBase = new File(igniteBasePath);
        if (!igniteBase.exists()) {
            igniteBase.mkdirs();
        }
        String installScriptPath = igniteBasePath + "/installScript.sh";
        // TODO: make it more elegant. this is nasty below
        List<String> lines = Arrays.asList("# This is the main config for Ignite Agent\n" + "ONE_TIME_CODE=" + code + "\n" + "AGENT_ID=" + agentOneTimeCode.getAgentId() + "\n" + "BASE_URL=" + baseUrl + "\n" + "\n" + "echo \"`date` : Installing Agent : $AGENT_ID\"\n" + "echo \"`date` : Using platform   : $BASE_URL\"\n" + "echo \"`date` : Doing a lot here : Please wait...\"\n" + "\n" + "OS=`uname -s`\n" + "ARCH=`uname -m`\n" + "HOSTNAME=`uname -n`\n" + "\n" + "ps -eaf|grep -v grep|grep Ignite > /dev/null\n" + "if [ $? -eq 0 ]; then\n" + "  kill -9 `ps -eaf|grep -v grep|grep Ignite|awk '{print $2}'` >> /dev/null\n" + "  if [ -d Ignite ]; then\n" + "    mv Ignite Ignite-$(date '+%Y-%m-%d-%S')\n" + "  fi\n" + "fi\n" + "/usr/bin/curl -s -k -X POST \\\n" + "     -H \"Content-Type: application/json\" \\\n" + "     -d '{\"os\":\"'\"$OS\"'\", \"arch\":\"'\"$ARCH\"'\", \"hostname\":\"'\"$HOSTNAME\"'\"}' \\\n" + "     -OJ " + baseUrl + "/ignite/agent/download/" + code + "\n" + "\n" + "tar xf Ignite*.tar.gz\n" + "\n" + "if [ -f Ignite*.tar.gz ]; then\n" + "   rm Ignite*.tar.gz\n" + "fi\n" + "\n" + "cd Ignite*\n" + "chmod 755 bin/ignite.sh\n" + "\n" +
                "bin/ignite.sh start");

        Files.write(Paths.get(installScriptPath), lines, StandardCharsets.UTF_8);

        // make it executable
        File installScript = new File(installScriptPath);
        installScript.setExecutable(true);

        Path path = Paths.get(installScriptPath);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert resource != null;
        return resource;
    }
}
