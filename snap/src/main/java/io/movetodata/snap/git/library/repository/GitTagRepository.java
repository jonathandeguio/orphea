package io.movetodata.snap.git.library.repository;

import io.movetodata.snap.git.library.models.GitRepositoryModel;
import io.movetodata.snap.git.library.models.GitTagModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GitTagRepository extends JpaRepository<GitTagModel, UUID> {
    List<GitTagModel> findByRepository(GitRepositoryModel repository);
}
