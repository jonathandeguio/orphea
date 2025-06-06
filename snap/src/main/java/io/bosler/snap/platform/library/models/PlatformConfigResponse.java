package io.bosler.snap.platform.library.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class PlatformConfigResponse {

    PlatformConfig platformConfig;
    Versions versions;
    String lastUpdatedOn;
    String productKeyShowOptions;

}
