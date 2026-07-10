package io.movetodata.build.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.library.dto.*;
import io.movetodata.build.library.exception.BuildSpecException;
import io.movetodata.build.library.exception.FunnelServiceException;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.models.Source;
import io.movetodata.connect.library.repository.LinkRepository;
import io.movetodata.connect.library.services.DatabaseSourceConfigService;
import io.movetodata.connect.library.services.JDBCService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import io.movetodata.sharedutils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.movetodata.sharedutils.Utils.isBase64;
import static io.movetodata.sharedutils.Utils.isValidUuid;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class FunnelService {
    private final DatasetMappingService datasetMappingService;
    private final KitabService kitabService;
    private final BranchService branchService;
    private final ResourceService resourceService;
    private final FunnelUtils funnelUtils;
    private final JDBCService jdbcService;
    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private SparkDataService sparkDataService;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DatabaseSourceConfigService databaseSourceConfigService;
    @Autowired
    private BuildSpecService buildSpecService;

    @Transactional
    public void previewPostTransform(BuildPreviewResultRequest request, UUID userId, UUID repoId) throws Exception {
        log.info(">>>>> INSIDE POST TR");
        funnelUtils.setPreviewResult(userId, request, repoId, "preview");
        log.info(">>>>> RESULT SENT");
    }

    public void checkForCircularPipelines(String target, List<SourceDataset> sources) throws FunnelServiceException {
        for (SourceDataset source : sources) {
            if (UUID.fromString(target).equals(UUID.fromString(source.getSource()))) {
                throw new FunnelServiceException("Source can't be a target");
            }
        }
    }

    @Transactional
    public HashMap<String, Object> preTransform(PreTransformRequest request, UUID userId) throws Exception {
        List<SourceDataset> sources = funnelUtils.verifyFunnelSources(request.getSources());
        List<UUID> sourcesTransactionIds = datasetMappingService.getDatasetsTransactions(sources);
        List<String> sourcesBranchType = branchService.getBranchTypes(sources);
        List<String> sourcesEncoding = branchService.getEncoding(sources, sourcesTransactionIds);
        List<Map<String, Object>> sourcesSchema = sparkDataService.getSchemasOfSources(sources, sourcesTransactionIds);
        List<LiveDatasetFunnelConfig> liveDatasetConfigs = getLiveDatasetConfig(sources, sourcesBranchType);

        HashMap<String, Object> response = new HashMap<>();
        response.put("sources", sources);
        response.put("sourcesTransactionIds", sourcesTransactionIds);
        response.put("sourcesBranchType", sourcesBranchType);
        response.put("sourcesEncoding", sourcesEncoding);
        response.put("sourcesSchema", sourcesSchema);
        response.put("liveDatasetConfigs", liveDatasetConfigs);
        response.put("physicalEndpoint", kitabService.getPhysicalEndpoint()); // Added for notebook

        log.info("COMPLETED WITH PRE TRANSFORM");
        return response;
    }

    @Transactional
    public List<LiveDatasetFunnelConfig> getLiveDatasetConfig(List<SourceDataset> sources, List<String> sourcesBranchType) throws Exception {
        List<LiveDatasetFunnelConfig> liveDatasetConfigs = new ArrayList<>();
        for (int idx = 0; idx < sources.size(); idx++) {
            if (sourcesBranchType.get(idx).equals(ResourceSubtype.LIVEDATASET.toString())) {
                UUID datasetId = UUID.fromString(sources.get(idx).getSource());
                String branch = sources.get(idx).getBranch();

                Link link = linkRepository.findByDatasetIdAndBranch(datasetId, branch).orElseThrow();
                Source source = sourceService.findById(link.getSourceId());
                DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(source.getSourceConfig());

                String username = databaseSourceConfig.getUsername();
                String password = databaseSourceConfig.getPassword();
                if (isBase64(password)) {
                    password = Utils.decodeBase64(password);
                }

                String url = JDBCService.JDBCUrl(databaseSourceConfig);
                String query = jdbcService.processQuery(link.getScript(), databaseSourceConfig.getDbmsType(), -1);

                LiveDatasetFunnelConfig liveDatasetFunnelConfig = new LiveDatasetFunnelConfig();
                liveDatasetFunnelConfig.setUrl(url);
                liveDatasetFunnelConfig.setQuery(query);
                liveDatasetFunnelConfig.setUsername(username);
                liveDatasetFunnelConfig.setPassword(password);
                liveDatasetFunnelConfig.setAuthType(databaseSourceConfig.getAuthType());
                liveDatasetFunnelConfig.setPrivateKey(databaseSourceConfig.getPrivateKey());
                liveDatasetFunnelConfig.setPrivateKeyPassphrase(databaseSourceConfig.getPrivateKeyPassPhrase());
                liveDatasetFunnelConfig.setDriver(jdbcService.getDriver(databaseSourceConfig.getDbmsType()));

                liveDatasetConfigs.add(liveDatasetFunnelConfig);
            } else {
                liveDatasetConfigs.add(null);
            }
        }

        return liveDatasetConfigs;
    }

    @Transactional
    public HashMap<String, Object> resolveTarget(
            ResolveTargetRequest request, UUID userId) throws JsonProcessingException {

        UUID transactionId;
        String target = request.getTarget();
        boolean isTargetPresent = false;
        if (isValidUuid(target)) {
            isTargetPresent = true;
        } else {
            ResourceModel resourceModel = kitabService.getResourceByPath(target, false);
            if (resourceModel == null) {
                isTargetPresent = false;
            } else {
                isTargetPresent = true;
                target = String.valueOf(resourceModel.getId());
            }
        }

        if (isTargetPresent) {
            // If target exist, then master branch would definitely be present.
            // Creating Transaction
            transactionId = datasetMappingService.createTransaction(UUID.fromString(target), request.getBranch(), userId, BuildLaunchedBy.MANUAL, request.getBuildId(), request.getWriteMode());

            // No build spec related checks for NOTEBOOK build
            if (!request.getBuildTrigger().equals(BuildTrigger.NOTEBOOK)) {
                // Exist BuildSpec with another
                boolean doesBuildSpecWithAnotherExist = buildSpecService.existsBuildSpecWithAnother(UUID.fromString(target), request.getBranch(), request.getRepositoryId(), request.getScriptPath(), request.getLanguage());
                if (doesBuildSpecWithAnotherExist) {
                    throw new BuildSpecException("Target dataset is linked with another code");
                }
            }
        } else {
            String[] parts = target.split("/");
            // Get the last item from the parts
            String name = parts[parts.length - 1];
            String description = "Dataset generated via Build";
            ResourceModel resourceModel = kitabService.getResourceByPath(target, true);
            if (resourceModel == null) {
                throw new ResourceNotFoundException("Parent Not found");
            }
            UUID parent = resourceModel.getId();


            ObjectMapper mapper = new ObjectMapper();

            System.out.println("Debug request " + mapper.writeValueAsString(request));

            ResourceSubtype resourceSubtype = ResourceSubtype.BUILDDATASET;
            BuildTrigger trigger = request.getBuildTrigger();

            if (trigger != null && trigger.equals(BuildTrigger.NOTEBOOK)) {
                resourceSubtype = ResourceSubtype.RAWDATASET;
            }

            // Creating Dataset
            ResourceModel newResource = resourceService.createDataset(name, description, userId, parent, resourceSubtype);
            target = String.valueOf(newResource.getId());
            // Creating Transaction
            transactionId = datasetMappingService.createTransaction(UUID.fromString(target), request.getBranch(), userId, BuildLaunchedBy.MANUAL, request.getBuildId(), request.getWriteMode());
        }

        if (!request.getBuildTrigger().equals(BuildTrigger.NOTEBOOK)) {
            checkForCircularPipelines(target, request.getSources());
            // Creating build spec
            buildSpecService.createBuildSpec(request.getRepositoryId(), request.getScriptPath(), request.getLanguage(), request.getBranchId(), request.getCommitId(), UUID.fromString(target), request.getBranch(), transactionId, request.getBuildId(), userId, request.getFileName(), request.getLineNo(), request.getWriteMode());
        }

        // Resolving branches
        branchService.resolveBranch(UUID.fromString(target),
                request.getBranch(),
                request.getRepositoryId(),
                request.getBuildId(),
                userId);

        // Check for circular pipelines
        HashMap<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("target", target);

        log.info("PRE TRANSFORM COMPLETED");
        return response;
    }


}
