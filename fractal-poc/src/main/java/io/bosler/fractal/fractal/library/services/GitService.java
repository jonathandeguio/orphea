//package io.bosler.fractal.fractal.library.services;
//
//import io.bosler.fractal.library.models.GitCommit;
//import org.apache.commons.io.FileUtils;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.eclipse.jgit.api.CommitCommand;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.PushCommand;
//import org.eclipse.jgit.api.errors.InvalidRefNameException;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.lib.StoredConfig;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
//import org.eclipse.jgit.transport.CredentialsProvider;
//import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//public class GitService {
//
//    public Repository getRepository(String repositoryPath, String branch) throws IOException, InvalidRefNameException {
//        return new FileRepositoryBuilder()
//                .setGitDir(new File(System.getenv("GIT_CLONED_PATH") + "/" + repositoryPath + File.separator + ".git"))
//                .readEnvironment()
//                .setInitialBranch(branch)
//                .findGitDir()
//                .build();
//    }
//
//    public List<GitCommit> createRepository(UUID userId, GitCommit commitModel, String RepositoryId, String templateType) throws Exception {
//
//        createJuliaRepository(UUID.fromString(RepositoryId)); // TODO: put validations
//
//        Git gitJulia = Git.cloneRepository()
//                .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + RepositoryId + "/.git")
//                .setDirectory(new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + RepositoryId))
//                .call();
//        Repository repository = getRepository(userId + "/" + RepositoryId, "master");
//
//        String fractalTemplatePath = System.getenv("GIT_CLONED_PATH").replace("/cloned", "") + "/FractalTemplates";
//        File fractalTemplates = new File(fractalTemplatePath);
//
//        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(System.getenv("FRACTAL_TEMPLATES_TOKEN"), "");
//        if (!fractalTemplates.exists()) {
//            String fractalTemplatesUrl = "https://" + System.getenv("FRACTAL_TEMPLATES_TOKEN") + "@github.com/Bosler-io/FractalTemplates.git";
//            Git.cloneRepository()
//                    .setURI(fractalTemplatesUrl)
//                    .setCredentialsProvider(credentialsProvider)
//                    .setDirectory(new File(fractalTemplatePath))
//                    .call();
//        } else {
//
//            Repository templateRepository = new FileRepositoryBuilder()
//                    .setGitDir(new File(fractalTemplatePath + File.separator + ".git"))
//                    .readEnvironment()
//                    .setInitialBranch("master")
//                    .findGitDir()
//                    .build();
//
//            Git gitTemplate = new Git(templateRepository);
//            gitTemplate.pull().setCredentialsProvider(credentialsProvider).setRemote("origin").setRemoteBranchName("master").call();
//        }
//
//        File sourceDirectory = new File(fractalTemplatePath + "/" + templateType);
//        File targetDirectory = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + RepositoryId);
//
//        FileUtils.copyDirectory(sourceDirectory, targetDirectory);
//
//        gitJulia.add().addFilepattern(".").call();
//
//        CommitCommand commitCommand = gitJulia.commit().setMessage(commitModel.getMessage())
//                .setCommitter(commitModel.getUsername(), commitModel.getEmail());
//        commitCommand.call();
//
//        StoredConfig config = gitJulia.getRepository().getConfig();
//        config.setString("branch", "master", "remote", "origin");
//        config.setString("branch", "master", "merge", "refs/heads/master");
//        config.save();
//
//        PushCommand pushCommand = gitJulia.push();
//        pushCommand.call();
//
////      git log
//        Iterable<RevCommit> listLog = gitJulia.log().setMaxCount(1).call();
//        List<GitCommit> refObject = new ArrayList<>();
//        for (RevCommit log : listLog) {
//            GitCommit commit = new GitCommit();
//            commit.setId(log.getId().getName());
//            commit.setMessage(log.getFullMessage());
//            commit.setUsername(log.getCommitterIdent().getName());
//            commit.setEmail(log.getCommitterIdent().getEmailAddress());
//            refObject.add(commit);
//        }
//        repository.close();
//
//        return refObject;
//    }
//
//
//    public String createJuliaRepository(UUID repositoryId) throws Exception {
//        System.out.println("Creating repository: " + repositoryId);
//
//        String juliaHost = System.getenv("JULIA_HOST");
//        String juliaApiPort = System.getenv("JULIA_API_PORT");
//        String url = String.format("http://%s:%s/julia/repository/%s/create", juliaHost, juliaApiPort, repositoryId);
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        HttpGet getConfigStatus = new HttpGet(url);
//        CloseableHttpResponse response = httpClient.execute(getConfigStatus);
//        HttpEntity responseEntity = response.getEntity();
//
//        return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
//    }
//
//}
