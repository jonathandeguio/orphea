package io.bosler.passport.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_account_activity")
        public class LoginHistory {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private UUID userId;
    private String remoteAddr;
    private String agent;
    private Date lastLoginAt;
    private Date lastLogoutAt;
}

