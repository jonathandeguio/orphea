package io.bosler.zoro.library.models;

// Comments only to be removed when this model is treated as a table instead of a row

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.List;
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



//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<HashMap<String, String>> dateFormat;

//    @ElementCollection
//    private List<HashMap<String, String>> timestampFormat;

    private String fieldDelimiter;
    private String lineDelimiter;
    private String escapeCharacter;
}
