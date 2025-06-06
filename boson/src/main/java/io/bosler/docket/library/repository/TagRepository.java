package io.bosler.docket.library.repository;

import io.bosler.docket.library.models.Tags;
import io.bosler.docket.library.models.TagsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface TagRepository extends JpaRepository<Tags, UUID> {

    boolean existsByName(String name);

    List<Tags> findAllByOrderByName();
    Page<Tags> findByNameContaining(String searchText, Pageable pageable);
    boolean existsByNameAndIdNot(String name, UUID id);
}
