package io.bosler.snap.passport.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_groups")
public class Groups {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    @Unique
    public String name;
    public String description;
    public String status;

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "ugo_fk", referencedColumnName = "id")
    private List<User> owners = new ArrayList<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "ugm_fk", referencedColumnName = "id")
    private List<User> managers = new ArrayList<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "uge_fk", referencedColumnName = "id")
    private List<User> members = new ArrayList<>();

    @CreatedDate
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;

}
