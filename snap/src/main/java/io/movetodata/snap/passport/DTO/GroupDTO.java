package io.movetodata.snap.passport.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class GroupDTO {
    public UUID id;
    public String name;
    public String description;
}
