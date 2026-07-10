package io.movetodata.snap.passport.library.repository;

import io.movetodata.snap.passport.library.models.Groups;
import io.movetodata.snap.passport.library.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, UUID> {

    Groups findByName(String name);

    List<Groups> getByName(String name);

    boolean existsByName(String name);
    List <Groups> findAllByOrderByNameAsc();

    List<Groups> findByMembers(User user);
    List<Groups> findByManagers(UUID users);
    List<Groups> findByOwners(UUID users);
}