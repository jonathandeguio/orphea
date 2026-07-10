package io.movetodata.passport.library.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.index.qual.SearchIndexBottom;

import java.util.UUID;

@Getter
@Setter
public class UpdateMetadataRequest {

    UUID id;

    private String name;
    private String password;
    private String givenName;
    private String familyName;
    private String location;
    private String profileImageUrl;

    private String email;
    private String language;
    private String mode;
}
