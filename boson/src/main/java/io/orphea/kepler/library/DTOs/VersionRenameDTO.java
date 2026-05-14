package io.orphea.kepler.library.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VersionRenameDTO {
    private UUID resourceId;
    private UUID versionId;
    private String name;
}
