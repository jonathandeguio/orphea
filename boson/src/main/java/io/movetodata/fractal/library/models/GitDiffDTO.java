package io.movetodata.fractal.library.models;

import lombok.*;
import org.eclipse.jgit.diff.DiffEntry;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitDiffDTO {
    DiffEntry.ChangeType changeType;
    String oldPath;
    String newPath;
    String content;
}
