package io.orphea.zoro.library.models;

import io.orphea.kitab.library.models.DatasetStatsModel;
import io.orphea.kitab.library.models.FolderModel;
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
    public FolderModel folderModel;

}
