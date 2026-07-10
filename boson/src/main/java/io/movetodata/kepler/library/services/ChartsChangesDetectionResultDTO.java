package io.movetodata.kepler.library.services;

import io.movetodata.kepler.library.DTOs.ChangesDTO;
import lombok.Getter;

import java.util.List;

@Getter()
public class ChartsChangesDetectionResultDTO {
    private final List<String> heading;
    private final List<List<ChangesDTO>> changes;
    private final int count;

    public ChartsChangesDetectionResultDTO(List<String> heading, List<List<ChangesDTO>> changes) {
        this.heading = heading;
        this.changes = changes;
        this.count = heading.size();
    }
}
