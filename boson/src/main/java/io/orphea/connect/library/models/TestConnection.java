package io.orphea.connect.library.models;

import io.orphea.connect.library.enums.SourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestConnection {


    private String server;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;

    private String type;
    private SourceTypeEnum dbmsType;

    private String path;

    private boolean directLoad = false;

}
