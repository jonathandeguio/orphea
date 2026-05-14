package io.orphea.passport.controllers;


//import io.orphea.passport.library.service.SSOService;
import io.orphea.passport.library.models.OAuth2Client;
import io.orphea.passport.library.repository.OAuth2ClientRepository;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/oauth2")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class OAuth2ClientsController {
    private final UserService userService;
    private final AuthzService authzService;


    private final OAuth2ClientRepository oAuth2ClientRepository;

    @GetMapping("/available")
    public ResponseEntity<Object> availableSSO() {

        List<OAuth2Client> OAuth2ClientList = oAuth2ClientRepository.findAll();

        List<String> availableSSO = new ArrayList<>();

        for(OAuth2Client OAuth2Client : OAuth2ClientList){
            availableSSO.add(OAuth2Client.getName());
        }

        return new ResponseEntity<>(availableSSO, HttpStatus.OK);

    }

    @GetMapping("/registrations/all")
    public ResponseEntity<List<OAuth2Client>> getAllRegistration(Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.checkSystemPermissions(userId, new UUID(0, 0), "platform-administrators")) {
            return new ResponseEntity("Access Denied to create SSO", HttpStatus.FORBIDDEN);
        }

        // Retrieve the OAuth2 client registration from the database by ID

            return ResponseEntity.ok(oAuth2ClientRepository.findAll());
    }

    @PostMapping("/registrations")
    public ResponseEntity<OAuth2Client> createRegistration(Principal principal, @RequestBody OAuth2Client registration) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.checkSystemPermissions(userId, new UUID(0, 0), "platform-administrators")) {
            return new ResponseEntity("Access Denied to create SSO", HttpStatus.FORBIDDEN);
        }

        if(oAuth2ClientRepository.existsByClientId(registration.getClientId())){
            return new ResponseEntity("Already exists with same clientId", HttpStatus.BAD_REQUEST);
        }

        registration.createdAt = new Date();
        registration.createdBy = userId;

        // Save the OAuth2 client registration to the database
        return ResponseEntity.ok(oAuth2ClientRepository.save(registration));
    }

    @GetMapping("/registrations/{id}")
    public ResponseEntity<OAuth2Client> getRegistration(Principal principal, @PathVariable UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.checkSystemPermissions(userId, new UUID(0, 0), "platform-administrators")) {
            return new ResponseEntity("Access Denied to create SSO", HttpStatus.FORBIDDEN);
        }

        // Retrieve the OAuth2 client registration from the database by ID
        Optional<OAuth2Client> registration = oAuth2ClientRepository.findById(id);
        if (registration.isPresent()) {
            return ResponseEntity.ok(registration.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/registrations/{id}")
    public ResponseEntity<OAuth2Client> updateRegistration(Principal principal, @PathVariable UUID id,
                                                                       @RequestBody OAuth2Client registration) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.checkSystemPermissions(userId, new UUID(0, 0), "platform-administrators")) {
            return new ResponseEntity("Access Denied to create SSO", HttpStatus.FORBIDDEN);
        }


        // Check if the OAuth2 client registration with the given ID exists
        if (!oAuth2ClientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Update the OAuth2 client registration in the database
        registration.setId(id);
        return ResponseEntity.ok(oAuth2ClientRepository.save(registration));
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> deleteRegistration(Principal principal, @PathVariable UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.checkSystemPermissions(userId, new UUID(0, 0), "platform-administrators")) {
            return new ResponseEntity("Access Denied to create SSO", HttpStatus.FORBIDDEN);
        }

        // Check if the OAuth2 client registration with the given ID exists
        if (!oAuth2ClientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Delete the OAuth2 client registration from the database
        oAuth2ClientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
