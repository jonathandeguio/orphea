package io.bosler.build.library.services;

import io.bosler.build.BobEnums.BuildLanguage;
import io.bosler.build.library.enums.WriteModeEnum;
import io.bosler.build.library.models.BuildSpecification;
import io.bosler.build.library.repository.BuildSpecificationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildSpecService {
    private final BuildSpecificationsRepository buildSpecificationsRepository;

    @Transactional
    public boolean existsBuildSpecWithAnother(UUID datasetId, String branch, UUID repositoryId, String scriptPath, BuildLanguage language) {
        List<BuildSpecification> allByDatasetIdAndBranch = buildSpecificationsRepository.findAllByDatasetIdAndBranch(datasetId, branch);
        List<BuildSpecification> allByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage = buildSpecificationsRepository.findAllByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(datasetId, branch, repositoryId, scriptPath, language);

        // if found build specification but with some other repository/branch and script then return True because above we already checked if exists
        return allByDatasetIdAndBranch.size() != allByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage.size();
    }

    @Transactional
    public void createBuildSpec(UUID repositoryId,
                                String scriptPath,
                                BuildLanguage language,
                                String branchId,
                                String commitId,
                                UUID datasetId,
                                String branch,
                                UUID transactionId,
                                UUID buildId,
                                UUID userId,
                                String fileName,
                                String lineNo,
                                WriteModeEnum writeMode) {
        BuildSpecification buildSpecification = new BuildSpecification();

        buildSpecification.setRepository(repositoryId);
        buildSpecification.setScriptPath(scriptPath);
        buildSpecification.setLanguage(language);
        buildSpecification.setBranchId(branchId);
        buildSpecification.setCommitId(commitId);
        buildSpecification.setFileName(fileName);
        buildSpecification.setLineNo(lineNo);

        buildSpecification.setDatasetId(datasetId);
        buildSpecification.setBranch(branch);
        buildSpecification.setTransactionId(transactionId);
        buildSpecification.setWriteMode(writeMode);
        log.info(">>>> Setting Build Id : " + buildId);
        buildSpecification.setBuildId(buildId);

        buildSpecification.setCreatedBy(userId);
        buildSpecification.setCreatedAt(new Date());

        buildSpecification.setUpdatedBy(userId);
        buildSpecification.setUpdatedAt(new Date());

        buildSpecificationsRepository.save(buildSpecification);
    }
}
