package io.movetodata.platform.library.models;

import io.movetodata.sharedutils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
public class PlatformConfigResponse {

    PlatformConfig platformConfig;
    Map<String, String> versions;
    String lastUpdatedOn;
}
