package io.movetodata.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ManageDashboardRequest {

        private UUID dashboardId;
        private List<UUID> chartIds;
        private String action;

}
