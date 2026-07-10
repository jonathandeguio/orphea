package io.movetodata.synchro.library.repository;

import io.movetodata.synchro.library.models.SyncIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Repository
@Transactional
public interface SyncIndexRepository extends JpaRepository<SyncIndex, UUID> {

}
