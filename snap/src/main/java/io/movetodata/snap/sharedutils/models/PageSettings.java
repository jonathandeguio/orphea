package io.movetodata.snap.sharedutils.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

@Slf4j
@Data
public class PageSettings {

    private int page = 0;

    private int elementPerPage = 2;

    private String direction = "dsc";

    private String key;

    public Sort buildSort() {
        switch (direction) {
            case "dsc":
                return Sort.by(key).descending();
            case "asc":
                return Sort.by(key).ascending();
            default:
//                log.warn("Invalid direction provided in PageSettings, using descending direction as default value");
                return Sort.by(key).descending();
        }
    }
}