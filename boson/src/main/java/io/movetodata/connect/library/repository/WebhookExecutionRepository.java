package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.WebhookExecutionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookExecutionRepository extends JpaRepository<WebhookExecutionData, UUID> {
    List<WebhookExecutionData> findByWebhookId(UUID webhookId);
}