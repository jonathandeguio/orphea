package io.movetodata.dataset.library.Keys;

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
public class DatasetMappingKey implements Serializable {
    private UUID datasetId;
    private String branch;
}
