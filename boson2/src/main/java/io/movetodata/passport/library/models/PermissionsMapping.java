package io.movetodata.passport.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_permissions_mapping")
public class PermissionsMapping {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;


    UUID identityId;  // identityId(userId, groupId)
    UUID resourceId;  // resourceId(dataset. folder, project)

    @OneToOne(targetEntity = Role.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "rg_fk", referencedColumnName = "id")
    private Role role;

    public String status;

    @CreatedDate
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;
}
