package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.RestAPISourceDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestSourceDomainRepository extends JpaRepository<RestAPISourceDomain, UUID> {
}