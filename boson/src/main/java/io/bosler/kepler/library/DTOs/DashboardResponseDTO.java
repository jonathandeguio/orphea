package io.bosler.kepler.library.DTOs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bosler.kepler.library.models.ChartsModel;
import io.bosler.kepler.library.models.DashboardsModel;
import io.bosler.kepler.library.models.TabsModel;
import io.bosler.sharedutils.Serializers.ResourceSerializer;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = ResourceSerializer.class)
public class DashboardResponseDTO {
    private UUID id;
    private Long versionId;
    private Date lastVersionedDate;
    private String description;
    private String type;
    private List<TabsModel> tabs;

    private Set<ChartsModel> charts;
    private String branch;


    public DashboardResponseDTO(DashboardsModel dashboard) {
        this.id = dashboard.getId();
        this.versionId = dashboard.getVersionId();
        this.lastVersionedDate = dashboard.getLastVersionedDate();
        this.type = "dashboard";
        this.tabs = dashboard.getTabs();
        this.charts = dashboard.getCharts();
        this.branch = dashboard.getBranch();
    }
}
