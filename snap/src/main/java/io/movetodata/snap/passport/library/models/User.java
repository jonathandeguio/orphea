package io.movetodata.snap.passport.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Table(name = "passport_users")
public class User {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @OrderColumn
    private String name;
    /*    custom and SsoAttributes username is in this*/
    private String username;

    @JsonIgnore
    private String password;
    private String givenName;
    private String familyName;
    private String location;
    @Column(columnDefinition = "TEXT")
    private String profileImage;

    private String email;


    /*Ouath2loginAttribute*/
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Type(type = "jsonb")
    @Column(name = "ssoAttributes", columnDefinition = "json")
    @JsonIgnore
    private Map<String, Object> ssoAttributes;

    private Boolean isMfaEnabled;

    @Type(type = "jsonb")
    @Column(name = "mfaAttributes", columnDefinition = "json")
    @JsonIgnore
    private Map<String, Object> mfaAttributes;

    @OneToOne(targetEntity = UserPreferences.class, cascade = CascadeType.ALL)
    private UserPreferences preferences;

    private UUID notificationPreferencesId;

    private Date lastLoginAt;

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt = new Date();
    private UUID createdBy;
    private UUID updatedBy;
}
