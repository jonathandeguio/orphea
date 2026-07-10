package io.movetodata.library.models;

import io.movetodata.library.models.ColumnsModel;
import io.movetodata.library.models.ConditionsModel;
import io.movetodata.library.models.RowsModel;
import io.movetodata.library.models.SortModel;
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
