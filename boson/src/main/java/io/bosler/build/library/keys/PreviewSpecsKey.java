package io.bosler.build.library.keys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PreviewSpecsKey implements Serializable {
    private UUID repositoryId;
    private String branch;
}
