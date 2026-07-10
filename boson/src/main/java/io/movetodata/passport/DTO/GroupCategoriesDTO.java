package io.movetodata.passport.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupCategoriesDTO {
    private List<GroupDTO> system;
    private List<GroupDTO> resource;
}
