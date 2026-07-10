package io.movetodata.dataset.library.models;

import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.models.ResourceModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DatasetStatsResponse {

    public DatasetStatsModel datasetStatsModel;
    public ResourceModel resourceModel;

}
