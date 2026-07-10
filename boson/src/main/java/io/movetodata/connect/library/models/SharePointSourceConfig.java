package io.movetodata.connect.library.models;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.movetodata.connect.library.services.SharePointConnectorService;
import lombok.*;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sharepoint_source_config")
public class SharePointSourceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String url;

    private String siteId;
    private String driveId;
    private String token;

    public String getToken() throws IOException {
        if (token == null) {
            this.token = SharePointConnectorService.getAccessToken(this.clientId, this.clientSecret, this.tenantId);
        } else {
            try {
                DecodedJWT decodedJWT = JWT.decode(this.token);
                if (decodedJWT.getExpiresAt().before(new Date())) {
                    this.token = SharePointConnectorService.getAccessToken(this.clientId, this.clientSecret, this.tenantId);
                }
            } catch (JWTDecodeException e) {
                this.token = SharePointConnectorService.getAccessToken(this.clientId, this.clientSecret, this.tenantId);
            }
        }
        return token;
    }
}
