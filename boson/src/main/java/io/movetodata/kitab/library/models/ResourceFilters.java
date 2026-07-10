package io.movetodata.kitab.library.models;

import io.movetodata.passport.enums.AuthRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResourceFilters {
    private String searchText;

    @Enumerated(EnumType.STRING)
    private List<AuthRole> permissions;
}
