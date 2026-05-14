package io.orphea.bezier.library.services;

import io.orphea.bezier.library.models.PipelineModel;
import io.orphea.bezier.library.repository.PipelineRepository;
import io.orphea.build.BobEnums.BuildStatus;
import io.orphea.build.library.dto.SourceDataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BezierService {

    private final PipelineRepository pipelineRepository;

    @Transactional
    public void resolveBezierLinks(List<SourceDataset> sources, UUID target, String branch, UUID repository, String scriptPath, UUID buildId, UUID userId) {
        log.info("datasetSources : " + target);
        pipelineRepository.deleteByTargetDatasetAndTargetBranch(target, branch);

        List<PipelineModel> deleteLinks = pipelineRepository.findAllByRepositoryIdAndRepositoryBranchAndScriptPathAndBuildIdNot(
                String.valueOf(repository),
                branch,
                scriptPath,
                String.valueOf(buildId)
        );

        pipelineRepository.deleteAll(deleteLinks);

        for (SourceDataset source : sources) {
            UUID datasetSource = UUID.fromString(source.getSource());
            log.info("datasetSource : " + datasetSource);
            PipelineModel pipelineModel = pipelineRepository.findBySourceDatasetAndTargetDataset(datasetSource, target);

            if (pipelineModel == null) {
                PipelineModel model = new PipelineModel();
                model.sourceDataset = datasetSource;
                model.targetDataset = target;

                // Not needed
                model.sourceBranch = source.getBranch();
                model.targetBranch = branch;
                model.repositoryId = String.valueOf(repository);
                model.repositoryBranch = branch;
                model.scriptPath = scriptPath;
                model.buildId = String.valueOf(buildId);


                model.status = BuildStatus.ACTIVE;
                model.type = "dataset";
                model.setCreatedBy(userId);
                model.setUpdatedBy(userId);
                pipelineRepository.saveAndFlush(model);
            }
        }
    }
}
