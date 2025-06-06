package io.bosler.cassini.library.repository;

import io.bosler.docket.library.models.Tags;
import io.bosler.docket.library.models.TagsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CassiniRepository extends JpaRepository<Tags, UUID> {

    boolean existsByName(String name);

    List<Tags> findAllByOrderByName();

    boolean existsByNameAndIdNot(String name, UUID id);
}
