package io.orphea.library.models;

import io.orphea.library.models.ColumnsModel;
import io.orphea.library.models.ConditionsModel;
import io.orphea.library.models.RowsModel;
import io.orphea.library.models.SortModel;
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
