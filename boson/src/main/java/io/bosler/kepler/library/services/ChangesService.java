package io.bosler.kepler.library.services;

import io.bosler.kepler.library.DTOs.ChangesDTO;
import io.bosler.kepler.library.models.*;
import io.bosler.kepler.library.repository.ChangesWrapperRepository;
import io.bosler.kepler.library.repository.ResourceVersionDetailsRepository;
import io.bosler.kepler.library.repository.ResourceVersionsRepository;
import io.bosler.kepler.library.repository.VersionChangesRepository;
import io.bosler.dataset.requests.ChartDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChangesService {
    private final ChangesWrapperRepository changesWrapperRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ResourceVersionDetailsRepository resourceVersionDetailsRepository;
    private final VersionChangesRepository versionChangesRepository;

    public static ChartsChangesDetectionResultDTO ChartsChangesDetection(ChartsModel existing, ChartRequest chartRequest) {
        List<List<ChangesDTO>> changesWrapper = new ArrayList<>();
        List<String> headings = new ArrayList<>();

        // Chart Config Changes
        ChartConfigModel existingConfig = existing.getChartConfig();
        ChartDataRequest newConfig = chartRequest.chartConfig;
        if (!existingConfig.getChartType().equals(newConfig.getChartType())) {

            List<ChangesDTO> changes = new ArrayList<>();

            changes.add(new ChangesDTO("switched", true));
            changes.add(new ChangesDTO("to", true));
            changes.add(new ChangesDTO(newConfig.getChartType(), false));

            changesWrapper.add(changes);
            headings.add("Switch");
        }
        if ((existingConfig.getMapSeries() == null && newConfig.getMapSeries() != null)
                || (existingConfig.getMapSeries() != null && newConfig.getMapSeries() == null)
                || (existingConfig.getMapSeries() != null && newConfig.getMapSeries() != null && existingConfig.getMapSeries().length() != (newConfig.getMapSeries().length()))) {
            List<ChangesDTO> changes = new ArrayList<>();

            changes.add(new ChangesDTO("modified", true));
            changes.add(new ChangesDTO("map", true));
            changes.add(new ChangesDTO("series", true));

            changesWrapper.add(changes);
            headings.add("Modify");
        }
        if (newConfig.getSeries() != null && existingConfig.getSeries() != null && existingConfig.getSeries().size() != (newConfig.getSeries().size())) {
            List<ChangesDTO> changes = new ArrayList<>();

            changes.add(new ChangesDTO("modified", true));
            changes.add(new ChangesDTO("series", true));

            changesWrapper.add(changes);
            headings.add("Modify");
        }
        if ((existingConfig.getFilter() == null && newConfig.getFilter() != null)
                || (existingConfig.getFilter() != null && newConfig.getFilter() == null)
                || (existingConfig.getFilter() != null && newConfig.getFilter() != null && existingConfig.getFilter().size() != (newConfig.getFilter().size()))) {
            List<ChangesDTO> changes = new ArrayList<>();

            changes.add(new ChangesDTO("modified", true));
            changes.add(new ChangesDTO("filters", true));

            changesWrapper.add(changes);
            headings.add("Modify");
        }

        return new ChartsChangesDetectionResultDTO(headings, changesWrapper);
    }


    public void SaveChanges(List<ChangesDTO> changes, ResourceVersionDetailsModel versionDetails, String heading, UUID userId) {
        List<ChangesWrapperModel> changesWrapper = versionDetails.getChangesWrapper();
        ChangesWrapperModel changeWrapper = new ChangesWrapperModel();
        changeWrapper.setResourceVersionDetails(versionDetails);
        List<VersionChangesModel> sentence = new ArrayList<>();


        for (ChangesDTO change : changes) {
            VersionChangesModel versionChange = new VersionChangesModel();
            versionChange.setChangesWrapper(changeWrapper);
            versionChange.setKey(change.key);
            versionChange.setTreat(change.treat);
            versionChangesRepository.save(versionChange);

            sentence.add(versionChange);
        }

        changeWrapper.setChanges(sentence);
        changeWrapper.setHeading(heading);
        changeWrapper.setUserId(userId);
        changeWrapper.setEntryTime(new Date());
        // Save ChangeWrapper
        changesWrapperRepository.save(changeWrapper);

        changesWrapper.add(changeWrapper);
        versionDetails.setChangesWrapper(changesWrapper);

        resourceVersionDetailsRepository.save(versionDetails);

    }
}

