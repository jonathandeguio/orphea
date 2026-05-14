package io.orphea.docket.library.repository;

import io.orphea.docket.library.models.TagsCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface TagCategoryRepository extends JpaRepository<TagsCategory, UUID> {

    boolean existsByName(String name);

    List<TagsCategory> findAllByOrderByName();

    boolean existsByNameAndIdNot(String name, UUID id);
}
