package io.movetodata.zoro.library.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SqlFilterModel {
    private String columnName;
    private String operator;
    private String FilterValue;
}