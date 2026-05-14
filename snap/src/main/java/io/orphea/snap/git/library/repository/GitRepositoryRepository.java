package io.orphea.snap.git.library.repository;

import io.orphea.snap.git.library.models.GitRepositoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepositoryModel, UUID> {
    boolean existsByFullName(String name);
    GitRepositoryModel findByFullName(String fullName);

}
