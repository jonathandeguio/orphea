package io.movetodata.passport.library.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PermissionsMapping {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    UUID identityId;  // identityId(userId, groupId)
    UUID resourceId;  // resourceId(dataset. folder, project)
    UUID roleId;

    // roleId, identityId, resourceId
    // roleId, identityId(userId, groupId), resourceId(dataset. folder, project)

    public String status;
    @CreatedDate
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public String createdBy;
    public String updatedBy;
}
