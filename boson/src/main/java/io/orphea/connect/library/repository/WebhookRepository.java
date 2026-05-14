package io.orphea.connect.library.repository;

import io.orphea.connect.library.models.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
}