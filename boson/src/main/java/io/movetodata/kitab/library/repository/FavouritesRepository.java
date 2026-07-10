package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.Favourites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavouritesRepository
        extends JpaRepository<Favourites, UUID> {
    List<Favourites> findAllDistinctByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndResourceId(UUID userId, UUID resourceId);
    Optional<Favourites> findByUserIdAndResourceId(UUID userId, UUID resourceId);
}
