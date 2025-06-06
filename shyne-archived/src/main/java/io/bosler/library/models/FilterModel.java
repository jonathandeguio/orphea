package io.bosler.library.models;

import io.bosler.library.models.ColumnsModel;
import io.bosler.library.models.ConditionsModel;
import io.bosler.library.models.RowsModel;
import io.bosler.library.models.SortModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilterModel {
    private List<ColumnsModel> columns;
    private List<ConditionsModel> conditions;
    private SortModel sort;
    private RowsModel rows;
}
