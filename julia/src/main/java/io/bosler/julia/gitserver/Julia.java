//package io.bosler.julia.gitserver;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import org.eclipse.jetty.server.HttpConfiguration;
//import org.eclipse.jetty.server.HttpConnectionFactory;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
//import org.eclipse.jgit.http.server.GitServlet;
//import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;
//import org.eclipse.jgit.lfs.server.fs.FileLfsServlet;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.apache.http.HttpHeaders.AUTHORIZATION;
//
///**
// * Server to handle access to git repositories over HTTP, with LFS support.
// * This server uses Jetty as the embedded servlet container
// * running the servlet GitServlet from the JGit project.
// */
//
//
//public class Julia {
//    private static final Logger LOG = LoggerFactory.getLogger(Julia.class);
//
//    private static final String OBJECTS = "objects/";
//    private static final String STORE_PATH = "/info/lfs/" + OBJECTS + "*";
//    private static final String BATCH_API_PATH = "/info/lfs/" + OBJECTS + "batch";
//
//    //default parameter values
//    static String  host = System.getenv("HOST");
//    static int serverPort = Integer.parseInt(System.getenv("JULIA_PORT"));
//    static String basePath = System.getenv("JULIA_BASE_PATH");
//    static String lfsPath = "/git-lfs-storage";
//
//
//    public static void main(String[] args) throws Exception {
//
//        printServerInfo();
//
//
//        Server server = new Server();
//
//        HttpConfiguration httpConfig = new HttpConfiguration();
//        httpConfig.setOutputBufferSize(32768);
//
//        ServerConnector gitConnector = new ServerConnector(server,
//                new HttpConnectionFactory(httpConfig));
//        gitConnector.setName("git-connector");
//        gitConnector.setPort(serverPort);
//        server.addConnector(gitConnector);
//
//        ServletContextHandler gitContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        gitContext.setVirtualHosts(new String[]{"@git-connector"});
//
//        //set up LFS servlets for each valid git repo under base-path
//        List<Path> validRepos = getValidGitRepos(basePath);
//        LOG.info("Git repos with LFS support: " + validRepos);
//        setUpLfsServlets(validRepos, gitContext);
//
//        //set up the GitServlet
//
//
//        ServletHolder gitServletHolder = new ServletHolder(GitServlet.class);
//
//        gitServletHolder.setInitParameter("base-path", basePath); //path to the git repositories
//        gitServletHolder.setInitParameter("export-all", "true"); //yes, true, 1, on: export all repositories
//        //no, false, 0, off: export no repositories
//        gitContext.addServlet(gitServletHolder, "/*");
//
//        //start up the http server
//        server.setHandler(gitContext);
//        server.start();
//        server.join();
//    }
//
//    private static void printServerInfo() {
//        System.out.println("Running Git http server on port=" + serverPort +
//                ", base-path=" + basePath + ", lfs-path=" + lfsPath);
//        System.out.println("Available services:");
//        System.out.println(" - Reading is permitted by default for all repositories,");
//        System.out.println("   unless 'http.uploadpack=false' is set for a specific repository.");
//        System.out.println(" - Writing is permitted if any of the following is true:");
//        System.out.println("    * the servlet container has authenticated the user, and has set");
//        System.out.println("      HttpServletRequest.remoteUser field to the authenticated name");
//        System.out.println("    * the repository's configuration has 'http.receivepack=true' setting;");
//        System.out.println("   otherwise repository updating is explicitly rejected.");
//        System.out.println("FYI: This simple server has no user login facility.\n");
//    }
//
//    private static URI getBaseURI() throws URISyntaxException {
////        try (BufferedReader br = new BufferedReader(new InputStreamReader(
////                Runtime.getRuntime().exec("hostname").getInputStream()))) {
////            String hostname = br.readLine();
////            return new URI("http://" + hostname + ":" + serverPort);
////        } catch (IOException e) {
////            throw new RuntimeException("Cannot find hostname", e);
////        } catch (URISyntaxException e) {
////            throw new RuntimeException("Unexpected URI error", e);
////        }
//
//        return new URI("http://" + host + ":" + serverPort);
//    }
//
//    private static List<Path> getValidGitRepos(String basePath) {
//        try (Stream<Path> walk = Files.walk(Paths.get(basePath), 1)) {
//            List<Path> repos = walk.filter(Files::isDirectory)
//                    .filter(path -> isGitDirectory(path))
//                    .collect(Collectors.toList());
//            return repos;
//        } catch (IOException e) {
//            throw new IllegalArgumentException("Invalid base-path: " + basePath, e);
//        }
//    }
//
//    private static void setUpLfsServlets(List<Path> repos, ServletContextHandler context) throws URISyntaxException {
//        URI baseURI = getBaseURI();
//        repos.forEach(path -> {
//            String repoName = path.getFileName().toString();
//            if (!repoName.endsWith(".git")) {
//                repoName = repoName + ".git";
//            }
//            try {
//                //set up the LFS batch servlet for this repo
//                FileLfsRepository fsRepo = new FileLfsRepository(
//                        baseURI + "/" + repoName + "/info/lfs/" + OBJECTS, Paths.get(lfsPath, repoName));
//                context.addServlet(new ServletHolder(new LfsBatchServlet(fsRepo, path)),
//                        "/" + repoName + BATCH_API_PATH);
//                //set up the LFS content servlet for this repo
//                //with timeout of 5 minutes for object upload/download
//                FileLfsServlet lfsContentServlet = new FileLfsServlet(fsRepo, 300000);
//                context.addServlet(new ServletHolder(lfsContentServlet),
//                        "/" + repoName + STORE_PATH);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    private static boolean isGitDirectory(Path path) {
//        FileRepositoryBuilder repositoryBuilder =
//                new FileRepositoryBuilder().setGitDir(path.toFile())
//                        .setMustExist(true);
//
//        try (Repository repository = repositoryBuilder.build()) {
//            return repository.exactRef("HEAD") != null;
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//
//    static DecodedJWT getToken(HttpServletRequest httpRequest) throws Exception {
//
//
//        String authorizationHeader = httpRequest.getHeader(AUTHORIZATION);
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            try {
//                String token = authorizationHeader.substring("Bearer ".length());
//                Algorithm algorithm = Algorithm.HMAC256("passportSecret".getBytes());
//                JWTVerifier verifier = JWT.require(algorithm).build();
//                DecodedJWT decodedJWT = verifier.verify(token);
//                String username = decodedJWT.getSubject();
//
//                return decodedJWT;
//
//            } catch (Exception exception) {
//                throw new Exception("Could not extract token : {} " + exception.getMessage());
//            }
//        }
//        return null;
//    }
//
////    private ServletContextHandler smart(final String path) {
////        GitServlet gs = new GitServlet();
////        gs.setRepositoryResolver(new RepositoryResolver<HttpServletRequest>() {
////            public Repository open(HttpServletRequest req, String name)
////                    throws RepositoryNotFoundException,
////                    ServiceNotEnabledException {
////                if (!name.equals(nameOf(db)))
////                    throw new RepositoryNotFoundException(name);
////                db.incrementOpen();
////                return db;
////            }
////        });
////        ServletContextHandler ctx = server.addContext(path);
////        ctx.addServlet(new ServletHolder(gs), "/*");
////        return ctx;
////    }
//
//}
