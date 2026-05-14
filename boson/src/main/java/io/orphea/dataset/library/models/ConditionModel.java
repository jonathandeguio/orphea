package io.orphea.dataset.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConditionModel {
    //    private String operator;
    private String sourceColumn;
    private String destinationColumn;

}


/*
[
    {
    dataset : id
    'type' : 'join',
    columns: [dataset1.columnName, dataset2.columnName],
    'condition' :
            {
            dataset1 : 'datasetid_key',
            dataset2 : 'datasetid_key'
            }
    },
    {
    dataset : id
    'type' : 'join',
    columns: [dataset1.columnName, dataset2.columnName],
    'condition' :
            {
            dataset1 : 'datasetid_key',
            dataset2 : 'datasetid_key'
            }
    },
    {
    dataset : id
    'type' : 'union',
    columns: [dataset1.columnName, dataset2.columnName],
    'condition' :
            {
            dataset1 : 'datasetid_key',
            dataset2 : 'datasetid_key'
            }
    }
]
 */