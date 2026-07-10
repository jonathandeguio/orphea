package io.movetodata.kitab.library.models;

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
public class ManageTagsRequest {
    private UUID datasetId;
    private List<UUID> tagIds;
    private String action;
}
