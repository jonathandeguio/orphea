package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.WebhookCallData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookCallRepository extends JpaRepository<WebhookCallData, UUID> {
    WebhookCallData findFirstByOrderByExecutionStartedAtDesc();
}