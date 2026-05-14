package io.orphea.kitab.library.keys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HardwareSpecsKey implements Serializable {
    private UUID repository;
    private String branch;
    private String scriptPath;
}
