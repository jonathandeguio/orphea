package io.movetodata.passport.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_long_lived_token")
public class TokenLongLived {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public String createdBy = "Platform";
    public String updatedBy = "Platform";
    private UUID userId;
    private String name;
    private Date expiration;
    private boolean status = true;
}