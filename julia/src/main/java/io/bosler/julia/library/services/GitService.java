package io.bosler.julia.library.services;

import io.bosler.julia.library.models.GitCommit;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class GitService {

    public static Repository getRepository(UUID repositoryId, String branch) throws IOException, InvalidRefNameException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(System.getenv("JULIA_BASE_PATH") + "/" + repositoryId + File.separator + ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
    }
}
