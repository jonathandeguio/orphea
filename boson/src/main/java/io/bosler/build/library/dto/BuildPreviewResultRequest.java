package io.bosler.build.library.dto;

import io.bosler.dataset.library.DTOs.ColumnDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BuildPreviewResultRequest {
    private List<ColumnDTO> schema;
    private List<Map<String, Object>> data;
    private UUID repositoryId;
    private String scriptPath;
    private String target;
    private UUID buildId;
}
