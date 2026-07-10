package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
}