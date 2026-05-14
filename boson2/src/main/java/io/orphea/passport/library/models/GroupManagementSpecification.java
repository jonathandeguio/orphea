package io.orphea.passport.library.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class GroupManagementSpecification {

    @NotEmpty
    private UUID id;
    @NotEmpty
    private String action = "add";
    @NotEmpty
    private String type = "members";
    private List<UUID> userIds;

}
