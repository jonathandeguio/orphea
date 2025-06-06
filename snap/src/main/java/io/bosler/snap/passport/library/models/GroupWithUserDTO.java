package io.bosler.snap.passport.library.models;


import io.bosler.snap.passport.DTO.UserDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GroupWithUserDTO {
    private UUID id;

    private String name;
    private String description;
    private String status;

    private List<UserDTO> owners;

    private List<UserDTO> managers;

    private List<UserDTO> members;

    private Date createdAt;
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
