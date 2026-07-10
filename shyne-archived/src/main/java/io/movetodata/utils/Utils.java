package io.movetodata.utils;

import com.amazonaws.thirdparty.apache.codec.binary.Base64;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    public static boolean checkEnvVariables(List<String> variableNames) {
        Map<String, String> env = System.getenv();
        for (String name : variableNames) {
            if (!env.containsKey(name)) {
                System.out.println("variable not defined " + name);
                return false;
            }
//            System.out.println("variable is defined " + name);
        }
        return true;
    }

    public static boolean isBase64(String value) {
        try {
            Base64.decodeBase64(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

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

    public static boolean isSparkInKubernetes() {
        return Objects.equals(System.getenv("SPARK_TYPE"), "kubernetes");
    }

    public static Repository getRepository(String repositoryId, String branch) throws IOException, InvalidRefNameException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/" + repositoryId + File.separator + ".git"))
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

}
