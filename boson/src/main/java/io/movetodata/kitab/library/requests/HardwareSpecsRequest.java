package io.movetodata.kitab.library.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HardwareSpecsRequest {
    private UUID repository;
    private String branch;
    private String scriptPath;
}
