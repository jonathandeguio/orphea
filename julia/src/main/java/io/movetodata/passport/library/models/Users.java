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
@Table(name = "passport_users")
public class Users {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private String name;
    private String username;
//    @JsonIgnore
    private String password;
    private String firstName;
    private String lastName;
    private String location;
    private String profileImageUrl;

    private String email;

    private Date lastLoginAt;

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt = new Date();
    private UUID createdBy;
    private UUID updatedBy;

//    @ManyToMany(fetch = FetchType.EAGER)
//    private Collection<Role> roles = new ArrayList<>();

//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
////    @JsonIgnore
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
}

