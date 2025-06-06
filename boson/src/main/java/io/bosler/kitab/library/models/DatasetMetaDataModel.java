package io.bosler.kitab.library.models;

import io.bosler.build.BobEnums.BuildTrigger;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dataset_meta_data")
public class DatasetMetaDataModel {
    @Id
    private UUID id;
    private boolean isDefaultBranchPresent;
    private boolean showBranchSelector;
    private UUID buildId;
    private UUID transactionId;
    private BuildTrigger buildTrigger;
}
