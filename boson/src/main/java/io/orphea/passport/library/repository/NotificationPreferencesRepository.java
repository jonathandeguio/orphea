package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, UUID> {
}
