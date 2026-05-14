package io.orphea.zoro.library.models;

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
