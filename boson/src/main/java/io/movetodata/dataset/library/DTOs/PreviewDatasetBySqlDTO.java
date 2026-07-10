package io.movetodata.dataset.library.DTOs;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDatasetBySqlDTO {
    String query;
    UUID datasetId;
    String branch;
    UUID transactionId;
}
