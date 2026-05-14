package io.orphea.dataset.library.models;

// Comments only to be removed when this model is treated as a table instead of a row

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_dataset_custom_schema")
public class CustomSchemaModel {

    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private HashMap<String, String> dateFormat;
    private HashMap<String, String> displayFormat;

    //    @ElementCollection
//    private List<HashMap<String, String>> timestampFormat;
    private boolean header = true;
    private String fieldDelimiter = ",";
    private String lineDelimiter = "\n";
    private String escapeCharacter = "\"";
    private String quote = "\"";
    private String encoding = Charset.defaultCharset().displayName();

    public CustomSchemaModel(CustomSchemaModel other) {
        this.id = other.id;
        this.dateFormat = other.dateFormat;
        this.displayFormat = other.displayFormat;
        this.fieldDelimiter = other.fieldDelimiter;
        this.lineDelimiter = other.lineDelimiter;
        this.escapeCharacter = other.escapeCharacter;
        this.encoding = other.encoding;
    }
}
