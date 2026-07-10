package io.movetodata.build.shyne.Utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public static <T> T convertJsonNodeToClass(JsonNode data, Class<T> typeClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, typeClass);
    }

    public static boolean checkEnvVariables(List<String> variableNames) {
        Map<String, String> env = System.getenv();
        for (String name : variableNames) {
            if (!env.containsKey(name)) {
                log.error("variable not defined " + name);
                return false;
            }
//            System.out.println("variable is defined " + name);
        }
        return true;
    }

    public static boolean isBase64(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    public static boolean isPostgresTableNameValid(String str) {
        final String VALID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9 _-]*$";
        final Pattern pattern = Pattern.compile(VALID_PATTERN);

        return pattern.matcher(str).matches();
    }

    public static Repository getRepository(String repositoryId, String branch) throws IOException, InvalidRefNameException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(System.getenv("BOSLER_MOUNT_PATH") + "/" + repositoryId + File.separator + ".git"))
                .readEnvironment()
                .setInitialBranch(branch)
                .findGitDir()
                .build();
    }

    public static boolean branchExists(String RepositoryId,
                                       String branch) throws Exception {

        List<String> branches = (List<String>) getLocalBranches(RepositoryId).get("localBranches");
        for (String b : branches) {
            if (b.substring(b.lastIndexOf('/') + 1).equals(branch)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> getLocalBranches(String repositoryId) throws Exception {

        List<String> branches = new ArrayList<>();
        String masterBranch = "master";

        Repository repository = getRepository(repositoryId, masterBranch);

        if (repository.getObjectDatabase().exists()) {
            Git git = new Git(repository);
            final List<Ref> branchRefs = git.branchList().call();
            for (Ref ref : branchRefs) {
                String name = ref.getName();
                branches.add(name);
            }

            Map<String, Object> branchInfo = new HashMap<>();
            branchInfo.put("localBranches", branches);
            branchInfo.put("activeBranch", repository.getBranch());
            return branchInfo;
        } else {
            throw new Exception("Repository does not exist");
        }
    }

    public static <T> List<T> getListFromJsonNode(JsonNode data, String fieldName, Class<T> typeClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

//        if (fieldName == "sourcesSchema") {
//
//            JsonNode fieldNode = data.get("sourcesBranchType");
//            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, typeClass);
//            return objectMapper.convertValue(fieldNode, listType);
//
//            // TODO : below is sort of hack and short term
////        SimpleModule module = new SimpleModule();
////        module.addDeserializer(SchemaModel.class, new SchemaModelDeserializer());
////        objectMapper.registerModule(module);
//            // TODO : Ends
//        }


        try {
            JsonNode fieldNode = data.get(fieldName);
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, typeClass);
            return objectMapper.convertValue(fieldNode, listType);
        } catch (IllegalArgumentException e) {
            // Log the error and the data causing the issue for further investigation
            System.err.println("Error deserializing field '" + fieldName + "': " + e.getMessage());
            System.err.println("Data causing issue: " + data);
            throw e;
        }
    }


    public static String getViewName(String sourceDatasetPathOrId) {
        String viewName;

        if (sourceDatasetPathOrId.startsWith("/Projects")) {
            String[] parts = sourceDatasetPathOrId.split("/");
            viewName = parts[parts.length - 1];
        } else {
            String[] parts = sourceDatasetPathOrId.split("-");
            viewName = parts[parts.length - 1];

        }

        return viewName.substring(Math.max(0, viewName.length() - 100)).replace("-", "");
    }

    public static String getTargetDataset(String line) {
        Pattern targetPattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+`(.*)`");
        Matcher targetMatcher = targetPattern.matcher(line);
        return targetMatcher.find() ? targetMatcher.group(1) : null;
    }

    public static Matcher getSourceMatcher(String line) {
        // .*
//        Pattern sourcePattern = Pattern.compile("(?i)(?:FROM|JOIN|ON)\\s+`(/Projects/[^`]+|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})`", Pattern.DOTALL);
        Pattern sourcePattern = Pattern.compile("`(/Projects/[^`]+|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})`");
        return sourcePattern.matcher(line);
    }
    //        Pattern sourcePattern = Pattern.compile("`(/Projects[^`]+)`");

    public static void waitHere() {

        long startTime = System.currentTimeMillis();
        long duration = 3 * 60 * 1000; // 3 minutes in milliseconds
        long endTime = startTime + duration;

        while (System.currentTimeMillis() < endTime) {
            System.out.print("");
        }
    }


    public static void cloneShyneRepo(String repositoryPath, String repositoryId) throws IOException, GitAPIException {
        File file = new File(repositoryPath);

        if (!file.exists()) {
            Git.cloneRepository()
                    .setURI(System.getenv("JULIA_URL") + "/" + repositoryId + "/.git") // TODO : not working
                    .setDirectory(file)
                    .call();
        } else {
            FileUtils.deleteDirectory(file);

            Git.cloneRepository()
                    .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
                    .setDirectory(file)
                    .call();
        }
    }

}
