package io.bosler.kepler.library.DTOs;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChartSuggestedFilterDTO {
    private String searchText;

    private Date createdAtTo;

    private Date createdAtFrom;

    private Date updatedAtFrom;

    private Date updatedAtTo;

    private List<UUID> createdBy;
}
