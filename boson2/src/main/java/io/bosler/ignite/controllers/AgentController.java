package io.bosler.ignite.controllers;

import io.bosler.ignite.library.models.*;
import io.bosler.ignite.library.repository.*;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.JwtService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedUtils.RandomString;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.bosler.sharedUtils.Utils.copyNonNullProperties;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/ignite/agent")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ignite", description = "Code Repository management service endpoints")
public class AgentController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthzService authzService;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final AgentStatsRepository agentStatsRepository;
    private final SourcesRepository sourcesRepository;
    private final LinkRepository linkRepository;
    private final IgniteConfigRepository igniteConfigRepository;
    private final FolderRepository folderRepository;
    private final AgentOneTimeCodeRepository agentOneTimeCodeRepository;


    public static HashMap<String, String> resolveOsAndArch(String Os, String Arch) {

        HashMap<String, String> result = new HashMap<>();

        switch (Os) {
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

        switch (Arch) {
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

    public static void RunOSCommand(String command) {

        String s;
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
//                System.out.println("line: " + s);
                p.waitFor();
            p.destroy();
            System.out.println(command + " \n exit: " + p.exitValue());  // TODO : this causing issue : java.lang.IllegalThreadStateException: process hasn't exited
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void buildIgnite(String IgniteRepositoryPath) {
        try (ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(IgniteRepositoryPath))
                .connect()) {

            BuildLauncher build = connection.newBuild();

            //select tasks to run:
            build.forTasks("build");

            //include some build arguments:
//            build.withArguments("--no-search-upward", "-i" ,"--project-dir", IgniteRepositoryPath);
            build.withArguments("--stacktrace", "-x", "test");

            //configure the standard input:
//            build.setStandardInput(new ByteArrayInputStream("consume this!".getBytes()));

            //in case you want the build to use java different than default:
//            build.setJavaHome(new File(buildJavaPath));

            //if your build needs crazy amounts of memory:
//            build.setJvmArguments("-Xmx2048m", "-XX:MaxPermSize=512m");

            //if you want to listen to the progress events:
//            ProgressListener listener = null; // use your implementation
//            build.addProgressListener(listener);

//            System.out.println(build.toString());

            //kick the build off:
            build.run();
        }
    }

    public static void downloadJava(String javaPackage, File javaTarPackage) {
        String javaDownloadUrl = "https://cdn.azul.com/zulu/bin/" + javaPackage + ".tar.gz";

        //  System.out.println("Downloading java from : " + javaDownloadUrl);

        try (BufferedInputStream in = new BufferedInputStream(new URL(javaDownloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(javaTarPackage)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception

            System.out.println("Not able to download : " + e);
        }
    }

    public static boolean cloneOrPullIgnite(File igniteRepository, String IgniteRepositoryPath) throws GitAPIException, IOException {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(System.getenv("FRACTAL_TEMPLATES_TOKEN"), "");
        if (!igniteRepository.exists()) {
            String igniteRepositoryUrl = "https://" + System.getenv("FRACTAL_TEMPLATES_TOKEN") + "@github.com/Bosler-io/Ignite.git";
            Git gitClone = Git.cloneRepository()
                    .setURI(igniteRepositoryUrl)
                    .setCredentialsProvider(credentialsProvider)
                    .setDirectory(new File(IgniteRepositoryPath))
                    .call();
            return true;
        } else {

            Repository igniteRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(IgniteRepositoryPath + File.separator + ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            Git igniteGitRepository = new Git(igniteRepo);
            PullResult pullResult = igniteGitRepository.pull().setCredentialsProvider(credentialsProvider).setRemote("origin").setRemoteBranchName("main").call();

            return pullResult.getFetchResult().getTrackingRefUpdates().size() > 0; // return true if more tha 0
        }
    }

    @Operation(summary = "Get all agents.")
    @GetMapping("/Getall")
    public ResponseEntity<Object> getAll(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse

    ) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        List<Agents> Agents = agentRepository.findAll();
        return new ResponseEntity<>(Agents, HttpStatus.OK);
    }

    @Operation(summary = "It provides agent by Id")
    @GetMapping("/GetById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id, HttpServletRequest httpRequest) {
        String user = httpRequest.getUserPrincipal().getName();
        /*System.out.println("*");
        System.out.println("*");
        System.out.println("*");
        System.out.println("*");
        System.out.println("*");
        System.out.println(user);
        System.out.println("\n");
        System.out.println("\n");
        System.out.println("\n");
        System.out.println("\n");*/
        UUID userId = userService.getUser(user).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!agentRepository.existsById(Id)) {
            return new ResponseEntity<>("agent with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(agentRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It delete agents by Id")
    @DeleteMapping("/DeleteById/{Id}")
    public ResponseEntity<Object> deleteById(@PathVariable("Id") UUID Id, HttpServletRequest httpRequest) {
        String user = httpRequest.getUserPrincipal().getName();
        UUID userId = userService.getUser(user).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!agentRepository.existsById(Id)) {
            return new ResponseEntity<>("agent with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }
        agentRepository.deleteById(Id);
        folderRepository.deleteById(Id);

        return new ResponseEntity<>(" Delete successfully ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create agents.")
    @PostMapping("/create")
    public ResponseEntity<Object> newAgent(Principal principal, HttpServletRequest request, @RequestBody Agents agents) {
        UUID userId = userService.getUser(principal.getName()).id;
        //UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!folderRepository.existsById(agents.getParent())) {
            return new ResponseEntity<>("parent folder " + agents.getParent() + " does not exist.", HttpStatus.NOT_FOUND);
        }

        // Create in kitab
        FolderModel folderModel = new FolderModel();
        folderModel.setName(agents.getName());
        folderModel.setDescription(agents.getDescription());
        folderModel.setParent(agents.getParent());
        folderModel.setStatus("active");
        folderModel.setType("agent");
        folderModel.setCreatedBy(userId);
        folderModel.setCreatedAt(new Date());

        FolderModel folderModel1 = folderRepository.save(folderModel);

        agents.setId(folderModel1.getId());
        agents.setCreatedAt(new Date());
        agents.setCreatedBy(userId);

        Agents agents1 = agentRepository.save(agents);

        // Also create initial config
        IgniteConfig igniteConfig = new IgniteConfig();
        igniteConfig.setAgentId(agents1.getId());
        igniteConfig.setVersion(UUID.randomUUID());
        igniteConfig.setCreatedAt(new Date());
        igniteConfig.setCreatedBy(userId);
        igniteConfigRepository.save(igniteConfig);

        // Create download url
        AgentOneTimeCode agentOneTimeCode = new AgentOneTimeCode();

        RandomString randomString = new RandomString(24, ThreadLocalRandom.current());

        agentOneTimeCode.setCode(randomString.nextString());
        agentOneTimeCode.setAgentId(agents1.getId());
        agentOneTimeCode.setExpiryTime(new Date());
        agentOneTimeCode.setDownloadUsed(false);
        agentOneTimeCode.setInstallUsed(false);
        agentOneTimeCode.setCreatedBy(userId);

        agentOneTimeCodeRepository.save(agentOneTimeCode);

        HashMap<String, Object> response = new HashMap<>();

        response.put("agent", agents1);

        response.put("oneTimeCode", agentOneTimeCode.getCode());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create agents.")
    @PostMapping("/update")
    public ResponseEntity<Object> update(Principal principal, @RequestBody Agents agents) {
        UUID userId = userService.getUser(principal.getName()).id;
        //UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access denied", HttpStatus.OK);
        }
        if (agents.getParent() != null) {
            if (!folderRepository.existsById(agents.getParent())) {
                return new ResponseEntity<>("parent folder " + agents.getParent() + " does not exist.", HttpStatus.NOT_FOUND);
            }
        }

        if (!agentRepository.existsById(agents.id)) {
            return new ResponseEntity<>("agent with Id " + agents.id + " does not exist", HttpStatus.NOT_FOUND);
        }

        Agents agentsExisting = agentRepository.getById(agents.getId());

        agentsExisting.setUpdatedAt(new Date());
        agentsExisting.setUpdatedBy(userId);

        copyNonNullProperties(agents, agentsExisting);


        Agents agents1 = agentRepository.save(agentsExisting);

        // Update config
        updateConfig(agents1.getId());

        return new ResponseEntity<>(agents1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get agents stats.")
    @GetMapping("/{agentId}/stats")
    public ResponseEntity<Object> agentStats(Principal principal, @PathVariable("agentId") UUID agentId) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

//        if (!agentStatsRepository.existsById(agentId)) {
//            return new ResponseEntity<>("No stats found for agentId " + agentId, HttpStatus.NOT_FOUND);
//        }

        AgentStats agentStats = agentStatsRepository.findById(agentId).get();

        return new ResponseEntity<>(agentStats, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get agents sources.")
    @GetMapping("/{agentId}/sources")
    public ResponseEntity<Object> agentSources(Principal principal, @PathVariable("agentId") UUID agentId) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(sourcesRepository.findAllByAgentId(agentId), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get agents links.")
    @GetMapping("/{agentId}/links")
    public ResponseEntity<Object> agentLinks(Principal principal, @PathVariable("agentId") UUID agentId) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Sources> sourcesList = sourcesRepository.findAllByAgentId(agentId);

        List<Links> linksList = new ArrayList<>();

        for (Sources sources : sourcesList) {

            linksList.addAll(linkRepository.findBySourceId(sources.getId()));
        }

        return new ResponseEntity<>(linksList, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to regenerate agents OTC.")
    @GetMapping("/{agentId}/regenerate")
    public ResponseEntity<Object> regenerateAgent(Principal principal, @PathVariable("agentId") UUID agentId) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        //UUID userId = userService.getUser(principal.getName()).id;

        Agents agents = agentRepository.findById(agentId).get();

        // Create download url
        AgentOneTimeCode agentOneTimeCode = new AgentOneTimeCode();

        RandomString randomString = new RandomString(24, ThreadLocalRandom.current());

        agentOneTimeCode.setCode(randomString.nextString());
        agentOneTimeCode.setAgentId(agents.getId());
        agentOneTimeCode.setExpiryTime(new Date());
        agentOneTimeCode.setDownloadUsed(false);
        agentOneTimeCode.setInstallUsed(false);
        agentOneTimeCode.setCreatedBy(userId);

        agentOneTimeCodeRepository.save(agentOneTimeCode);

        HashMap<String, Object> response = new HashMap<>();

        response.put("agent", agents);

        response.put("oneTimeCode", agentOneTimeCode.getCode());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to install agent.")
    @GetMapping("/install/{code}")
    public ResponseEntity<Object> installAgent(HttpServletRequest request, @PathVariable("code") String code) throws IOException {

        // Usage : bash <(curl -s http://localhost:8080/api/ignite/agent/install/dsadada)
        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeRepository.findByCode(code);
        UUID agentId = agentOneTimeCode.getAgentId();

        Agents agents = agentRepository.getById(agentOneTimeCode.getAgentId());
        User user = userRepository.getById(agentOneTimeCode.getCreatedBy());

        // below does not work because it has to be open
        //UUID userId = userService.getUser(user.name).getId();
//        if (!authzService.checkSystemPermissions(user.id, new UUID(3, 3), "ignite-administrators")) {
//            //List<String> EmptyList = Collections.<String>emptyList();
//            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
//        }
        if (!agentOneTimeCodeRepository.existsByCode(code)) {
            return new ResponseEntity<>("Code does not exist.", HttpStatus.NOT_FOUND);
        }

        //AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeRepository.findByCode(code);

        if (agentOneTimeCode.isInstallUsed()) {
            return new ResponseEntity<>("Install Code has already been used.", HttpStatus.NOT_FOUND);
        }

        agentOneTimeCode.setInstallUsed(true);

        agentOneTimeCodeRepository.save(agentOneTimeCode);

        // create install script
//        String baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());

        String baseUrl = "https://" + request.getServerName() + "/api";

        if (Objects.equals(request.getServerName(), "localhost")) {
            baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());
        }

        String igniteBasePath = System.getenv("BOSLER_MOUNT_PATH") + "/ignite";
        File igniteBase = new File(igniteBasePath);
        if (!igniteBase.exists()) {
            igniteBase.mkdirs();
        }
        String installScriptPath = igniteBasePath + "/installScript.sh";
        // TODO: make it more elegant. this is nasty below
        List<String> lines = Arrays.asList(
                "# This is the main config for Ignite Agent\n" +
                        "ONE_TIME_CODE=" + code + "\n" +
                        "AGENT_ID=" + agentOneTimeCode.getAgentId() + "\n" +
                        "BASE_URL=" + baseUrl + "\n" +
                        "\n" +
                        "echo \"`date` : Installing Agent : $AGENT_ID\"\n" +
                        "echo \"`date` : Using platform   : $BASE_URL\"\n" +
                        "echo \"`date` : Doing a lot here : Please wait...\"\n" +
                        "\n" +
                        "OS=`uname -s`\n" +
                        "ARCH=`uname -m`\n" +
                        "HOSTNAME=`uname -n`\n" +
                        "\n" +
                        "ps -eaf|grep -v grep|grep Ignite > /dev/null\n" +
                        "if [ $? -eq 0 ]; then\n" +
                        "  kill -9 `ps -eaf|grep -v grep|grep Ignite|awk '{print $2}'` >> /dev/null\n" +
                        "  if [ -d Ignite ]; then\n" +
                        "    mv Ignite Ignite-$(date '+%Y-%m-%d-%S')\n" +
                        "  fi\n" +
                        "fi\n" +
                        "/usr/bin/curl -s -k -X POST \\\n" +
                        "     -H \"Content-Type: application/json\" \\\n" +
                        "     -d '{\"os\":\"'\"$OS\"'\", \"arch\":\"'\"$ARCH\"'\", \"hostname\":\"'\"$HOSTNAME\"'\"}' \\\n" +
                        "     -OJ " + baseUrl + "/ignite/agent/download/" + code + "\n" +
                        "\n" +
                        "tar xf Ignite*.tar.gz\n" +
                        "\n" +
                        "if [ -f Ignite*.tar.gz ]; then\n" +
                        "   rm Ignite*.tar.gz\n" +
                        "fi\n" +
                        "\n" +
                        "cd Ignite*\n" +
                        "chmod 755 bin/ignite.sh\n" +
                        "\n" +
//                        "kill -9 `ps -eaf|grep -v grep|grep Ignite|awk '{print $2}'` >> /dev/null\n" +
                        "bin/ignite.sh start"
        );

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
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

    @Operation(summary = "This can be used to download code.")
    @PostMapping("/download/{code}")
    public ResponseEntity<Object> downloadAgent(Principal principal, HttpServletRequest request, @PathVariable("code") String code, @RequestBody HashMap<String, String> client) throws GitAPIException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

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

        AgentOneTimeCode agentOneTimeCode = agentOneTimeCodeRepository.findByCode(code);
        UUID agentId = agentOneTimeCode.getAgentId();

        Agents agents = agentRepository.getReferenceById(agentOneTimeCode.getAgentId());
        User user = userRepository.getReferenceById(agentOneTimeCode.getCreatedBy());

        // below does not work because it has to be open route
//        UUID userId = userService.getUser(principal.getName()).id;

//        if (!authzService.checkSystemPermissions(user.id, new UUID(3, 3), "ignite-administrators")) {
//            //List<String> EmptyList = Collections.<String>emptyList();
//            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
//        }

        if (!agentOneTimeCodeRepository.existsByCode(code)) {
            return new ResponseEntity<>("Code does not exist.", HttpStatus.NOT_FOUND);
        }

        if (agentOneTimeCode.isDownloadUsed()) {
            return new ResponseEntity<>("Download Code has already been used.", HttpStatus.NOT_FOUND);

        }

        if (agentStatsRepository.existsById(agentOneTimeCode.getAgentId())) {
            AgentStats agentStats = agentStatsRepository.getById(agentOneTimeCode.getAgentId());

            if (!client.get("hostname").equals(agentStats.getHostname())) {
                return new ResponseEntity<>("This agent is already installed on another host " + agentStats.getHostname() + " , you can not install same agent on two hosts", HttpStatus.NOT_FOUND);
            }

            if (client.get("os").equals("Linux") || client.get("os").equals("Darwin")) {
                System.out.println("Ignite Client OS : " + client.get("os"));
            } else {
                return new ResponseEntity<>("You operating system is not supported", HttpStatus.NOT_FOUND);
            }
        }


        agentOneTimeCode.setDownloadUsed(true);
        agentOneTimeCodeRepository.save(agentOneTimeCode);

//
//        String baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());
        URL url = new URL(System.getenv("BASE_URL"));
        String serverName = url.getHost();
        String baseUrl = "https://" + serverName + "/api";

        if (Objects.equals(request.getServerName(), "localhost")) {
            baseUrl = String.format("%s://%s:%d/api", request.getScheme(), request.getServerName(), request.getServerPort());
        }

        Date expiresIn = new Date(System.currentTimeMillis() + 10L * 60 * 100000000);
        var user1 = userRepository.findById(user.getId())
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        String igniteBase = System.getenv("BOSLER_MOUNT_PATH") + "/ignite";

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
            newVersion = cloneOrPullIgnite(igniteRepository, IgniteRepositoryPath);

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
//                System.out.println("downloading java");
                downloadJava(javaPackage, javaTarPackage);
            }


//            File javaPackagePath = new File(buildPath + File.separator + javaPackage);

            if (!javaPathExtractedFolder.exists()) {
                RunOSCommand("tar -xf " + javaTarPackage + " -C " + JavaPath); // TODO : do this programmatically in java

//                extractTar(javaTarPackage, javaPath);
            }


            RunOSCommand("cp -r " + javaPathExtractedFolder + " " + javaBuildPathFolder);

            // Set the source and destination directories
//            Path sourceDir = Paths.get(javaPathExtracted);
//            Path destDir = Paths.get(javaBuildPath);

//            copyFilesRecursively(sourceDir, destDir);

//            // Copy all files and subdirectories from the source to the destination
//            Files.walk(sourceDir)
//                    .forEach(path -> {
//                        try {
//                            Path targetPath = destDir.resolve(sourceDir.relativize(path));
//                            if (!Files.exists(targetPath)) {
//                                Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });


//            FileUtils.copyDirectory(javaPathExtractedFolder, javaBuildPathFolder);

            // get CA certificate : below will not work if the CA certificate is not in boson to add use below :
            // get bosler certificate form chrome. click on lock on url then (Mac) Option Key + drag the lock to notepad
            // this fails if cert is not there : keytool -import -trustcacerts -keystore ../Library/Java/JavaVirtualMachines/azul-16.0.2/Contents/Home/lib/security/cacerts --storepass changeit -noprompt -alias bosler -file ./bosler_public_cert.cer
            // keytool -import -trustcacerts -keystore /User/rakeshmalik/Library/Java/JavaVirtualMachines/openjdk-16.0.1/Contents/Home/lib/security/cacerts --storepass changeit -noprompt -alias bosler -file ./bosler_cert.pem

            // oncloud : keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts --storepass changeit -noprompt -alias bosler -file bosler_ca_cert.pem

            if (System.getenv("GENERATE_CERTIFICATE") != null) {


                if (System.getenv("GENERATE_CERTIFICATE").equals("true")) {
//                    System.out.println(request.getServerName());
//                    String hostname = "dev.bosler.io";  // TODO : change to auto
                    String hostname = request.getServerName();
                    System.out.println(hostname);

//                    URL url = new URL(System.getenv("BASE_URL"));
//                    String serverName = url.getHost();

                    SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
                    SSLSocket socket = (SSLSocket) factory.createSocket(serverName, 443);
                    socket.startHandshake();
                    Certificate[] certs = socket.getSession().getPeerCertificates();
                    Certificate cert = certs[0];

                    // Add CA cert to ignite keystore
                    String storePath = buildJavaPath + "/lib/security/cacerts";
                    addX509CertificateToTrustStore(cert, "bosler", storePath, "changeit", "PKCS12");
                }
            }

            // copy files to build package
            Files.copy(Paths.get(IgniteRepositoryPath + "/ignite_package/bin/ignite.sh"), Paths.get(buildPath + "/bin/ignite.sh"), REPLACE_EXISTING);

            // create ignite config
            String igniteConfigPath = buildPath + "/etc/ignite.conf";
            List<String> lines = Arrays.asList("PORT=7845\n" +
                    "AGENT_ID=" + agentId + "\n" +
                    "BASE_URL=" + baseUrl + "\n" +
                    "TOKEN=" + access_token + "\n" +
                    "PROXY=" + agents.isProxy() + "\n" +
                    "HTTP_PROXY=" + agents.getHttpProxy() + "\n" +
                    "HTTPS_PROXY=" + agents.getHttpsProxy() + "\n");

            Files.write(Paths.get(igniteConfigPath), lines, StandardCharsets.UTF_8);


            // build gradle for ignite only if new version
            if (newVersion || !tarPackageFile.exists()) {
                buildIgnite(IgniteRepositoryPath);
                // Copy jar to build path
                Files.copy(Paths.get(IgniteRepositoryPath + "/build/libs/Ignite-0.0.1.jar"), Paths.get(buildPath + "/bin/Ignite-0.0.1.jar"), REPLACE_EXISTING);

                if (tarPackageFile.exists()) {
                    tarPackageFile.delete();
                }

                RunOSCommand("tar cfz " + tarPackage + " -C " + igniteBase + "/ " + "Ignite");  // TODO : do this programmatically in java

                // Set the source and destination directories
//                File tarFile = new File(tarPackage);
//                tarBall(tarFile, new File(igniteBase + "/ " + "Ignite"));

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
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } finally {
            System.out.println("need to clean up here...."); // TODO :
//            FileUtils.deleteDirectory(buildPathFolder);

            File installScriptPath = new File(igniteBase + "/installScript.sh");

            installScriptPath.delete();

//            tarPackageFile.delete();
        }

    }

    public void updateConfig(UUID agentId) {
        IgniteConfig igniteConfig = new IgniteConfig();
        igniteConfig.setAgentId(agentId);
        igniteConfig.setVersion(UUID.randomUUID());
        igniteConfig.setUpdatedAt(new Date());
        igniteConfigRepository.save(igniteConfig);
    }

    public void extractTar(File tarFile, File extractDir) throws IOException {

        // Create the destination directory if it does not exist
        if (!extractDir.exists()) {
            extractDir.mkdirs();
        }

        // Create the input stream
        FileInputStream fis = new FileInputStream(tarFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
        TarArchiveInputStream tais = new TarArchiveInputStream(gzis);

        // Extract the files
        TarArchiveEntry entry;
        while ((entry = tais.getNextTarEntry()) != null) {
            // Create the destination file
            File destFile = new File(extractDir, entry.getName());

            // Create the parent directories if necessary
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            // Extract the file if it is not a directory
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = tais.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                bos.close();
            }
        }

        // Close the input stream
        tais.close();
    }

    public void tarBall(File tarPackage, File directoryToTar) throws IOException {

        // Create the output stream
        FileOutputStream fos = new FileOutputStream(tarPackage);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
        TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos);

        // Add the files to the tar archive
        addFilesToTar(taos, directoryToTar, "");

        // Close the output stream
        taos.close();
    }

    private static void addFilesToTar(TarArchiveOutputStream taos, File file, String parent) throws IOException {
        // Add the file to the tar archive
        TarArchiveEntry entry = new TarArchiveEntry(file, parent + file.getName());
        taos.putArchiveEntry(entry);
        if (file.isFile()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) > 0) {
                taos.write(buffer, 0, len);
            }
            bis.close();
        }
        taos.closeArchiveEntry();

        // Recursively add the child files and directories
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                addFilesToTar(taos, child, parent + file.getName() + "/");
            }
        }

    }

    public void copyFilesRecursively(Path sourceDir, Path destDir) throws IOException {
        FileVisitor<Path> fileCopier = new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Create the destination directory
                Path newDir = destDir.resolve(sourceDir.relativize(dir));
                Files.createDirectory(newDir);
                // Copy the directory's file attributes
                Files.setAttribute(newDir, "basic:lastModifiedTime", Files.getAttribute(dir, "basic:lastModifiedTime"));
                Files.setAttribute(newDir, "basic:lastAccessTime", Files.getAttribute(dir, "basic:lastAccessTime"));
                Files.setAttribute(newDir, "basic:creationTime", Files.getAttribute(dir, "basic:creationTime"));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Copy the file
                Path newFile = destDir.resolve(sourceDir.relativize(file));
                Files.copy(file, newFile, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        };

        // Traverse the file tree and copy the files
        Files.walkFileTree(sourceDir, fileCopier);
    }
}

