package io.orphea.library.models;

// Comments only to be removed when this model is treated as a table instead of a row

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomSchemaModel {

    private UUID Id;
    private HashMap<String, String> dateFormat;
    private HashMap<String, String> displayFormat;

    private String fieldDelimiter;
    private String lineDelimiter;
    private String escapeCharacter;
}
