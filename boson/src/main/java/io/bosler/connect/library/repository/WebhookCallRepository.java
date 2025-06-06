package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.WebhookCallData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookCallRepository extends JpaRepository<WebhookCallData, UUID> {
    WebhookCallData findFirstByOrderByExecutionStartedAtDesc();
}