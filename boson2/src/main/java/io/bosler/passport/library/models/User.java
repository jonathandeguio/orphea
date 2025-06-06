package io.bosler.passport.library.models;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;

@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_users")
public class User implements UserDetails {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @OrderColumn
    private String name;
    /*    custom and SsoAttributes username is in this*/
    private String username;

    private String password;
    private String givenName;
    private String familyName;
    private String location;
    private String profileImageUrl;

    private String email;
    private String language = "fr";
    private String mode = "auto";


    /*Ouath2loginAttribute*/
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

//    @Column(name = "ssoAttributes", columnDefinition = "json")
//    private Map<String,Object> ssoAttributes;

    private String ssoAttributes;


    private Date lastLoginAt;

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt = new Date();
    private UUID createdBy;
    private UUID updatedBy;


    /* Spring 3

     */

    @Enumerated(EnumType.STRING)
    private RoleS3 role;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<Token> tokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}

