package io.movetodata.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ColumnsModel {
    private String name;
    private String type;
    private String expression;
    private String value;
}

