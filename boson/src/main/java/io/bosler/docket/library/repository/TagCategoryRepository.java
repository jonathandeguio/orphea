package io.bosler.docket.library.repository;

import io.bosler.docket.library.models.TagsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface TagCategoryRepository extends JpaRepository<TagsCategory, UUID> {

    boolean existsByName(String name);
    @Query("Select t.name FROM TagsCategory t")
    List<String> findCategoryNames();

    TagsCategory findByName(String name);
    Page<TagsCategory> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<TagsCategory> findByNameContaining(String searchText, Pageable pageable);
    boolean existsByNameAndIdNot(String name, UUID id);
}
