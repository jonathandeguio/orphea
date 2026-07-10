package io.movetodata.passport.library.models;


import io.movetodata.passport.DTO.UserDTO;
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

    private List<UserDTO> owners = new ArrayList<>();

    private List<UserDTO> managers = new ArrayList<>();

    private List<UserDTO> members = new ArrayList<>();

    private Date createdAt;
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
