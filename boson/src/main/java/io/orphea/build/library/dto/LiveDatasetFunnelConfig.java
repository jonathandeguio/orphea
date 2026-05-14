package io.orphea.build.library.dto;

import io.orphea.connect.library.enums.SourceAuthTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveDatasetFunnelConfig {
    private String query;
    private SourceAuthTypeEnum authType;
    private String username;
    private String password;
    private String privateKey;
    private String privateKeyPassphrase;
    private String driver;
    private String url;
}
