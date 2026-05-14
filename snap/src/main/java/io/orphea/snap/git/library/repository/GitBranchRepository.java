package io.orphea.snap.git.library.repository;

import io.orphea.snap.git.library.models.GitBranchModel;
import io.orphea.snap.git.library.models.GitRepositoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GitBranchRepository extends JpaRepository<GitBranchModel, UUID> {
    GitBranchModel findByBranchNameAndRepository(String name, GitRepositoryModel repository);
}
