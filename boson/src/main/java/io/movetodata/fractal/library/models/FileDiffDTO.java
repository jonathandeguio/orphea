package io.movetodata.fractal.library.models;

import lombok.*;
import org.eclipse.jgit.diff.DiffEntry;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDiffDTO {
    DiffEntry.ChangeType changeType;
    String path;
    String oldContent;
    String updatedContent;
}
