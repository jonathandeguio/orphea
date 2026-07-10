package io.movetodata.passport.library.repository;

import io.movetodata.passport.library.models.OAuth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, UUID> {

    OAuth2Client findByProviderName(String providerName); // Custom method to retrieve by provider name
    OAuth2Client findByRegistrationId(String registrationId); // Custom method to retrieve by registration ID

    boolean existsByProviderNameAndStatus(String providerName, String status);
    boolean existsByClientId(String clientId);
}