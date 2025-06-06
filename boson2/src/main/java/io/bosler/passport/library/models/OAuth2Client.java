package io.bosler.passport.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_oauth2_clients")
public class OAuth2Client {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public String name;
    public String description;
    public String status;

    private String providerName; // New field for provider name
    private String registrationId; // New field for registration ID
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizationUri;
    private String userInfoUri;
    private String tokenUri;
    private String userNameAttributeName;

//    private String jwkSetUri;
//    private String clientName;

    private String scope;


    @CreatedDate
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;


}
