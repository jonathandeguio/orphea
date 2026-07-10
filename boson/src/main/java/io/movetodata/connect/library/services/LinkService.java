package io.movetodata.connect.library.services;

import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.library.models.BuildLog;
import io.movetodata.build.library.services.BuildLogService;
import io.movetodata.connect.library.configurations.ConnectConfigs;
import io.movetodata.connect.library.exceptions.LinkException;
import io.movetodata.connect.library.models.*;
import io.movetodata.connect.library.repository.*;
import io.movetodata.connect.library.requests.LinkRequestDTO;
import io.movetodata.dataset.library.DTOs.ColumnDTO;
import io.movetodata.dataset.library.DTOs.CsvPreprocessingDTO;
import io.movetodata.dataset.library.models.CsvPreprocessingModel;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.library.services.SparkService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkService {
    public static final String SELECT = "select";
    private final LinkRepository linkRepository;
    private final DatabaseSourceConfigService databaseSourceConfigService;
    private final PipelineRepository pipelineRepository;
    private final DatasetRepository datasetRepository;
    private final ResourceService resourceService;
    private final JDBCService jdbcService;
    private final ConnectConfigRepository connectConfigRepository;
    private final DatasetMappingService datasetMappingService;
    private final BranchRepository branchRepository;
    private final UploadService uploadService;
    private final BuildLogService buildLogService;
    private final SourcesRepository sourcesRepository;
    private final SparkDataService sparkDataService;
    private final FolderSourceConfigService folderSourceConfigService;
    private final SharepointConfigService sharepointConfigService;
    private final SharepointLinkConfigRepository sharepointLinkConfigRepository;
    private final FolderLinkConfigRepository folderLinkConfigRepository;
    private final WebhookRepository webhookRepository;
    private final WebhookService webhookService;

    public boolean existsByDatasetId(UUID id) {
        return linkRepository.existsByDatasetId(id);
    }

    public List<Link> findBySourceId(UUID id) {
        return linkRepository.findBySourceId(id);
    }

    public boolean existsSourceById(UUID id) {
        return sourcesRepository.existsById(id);
    }

    public boolean existsById(UUID id) {
        return linkRepository.existsById(id);
    }

    public Link findById(UUID id) {
        return linkRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("link by id {0} does not exist.", id)));
    }

    public Source findSourceById(UUID id) {
        return sourcesRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("source with Id {0} does not exist", id)));
    }

    @NotNull
    public List<Link> getAllLinks(UUID userId) {
        return resourceService
                .getActiveResources(userId, ResourceType.LINK)
                .parallelStream()
                .map(resourceModel -> linkRepository.findById(resourceModel.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Transactional
    public Link createLink(LinkRequestDTO linkRequestDTO, UUID userId) throws Exception {
        if (!existsSourceById(linkRequestDTO.getSourceId())) {
            throw new LinkException("Source does not exits.");
        }
        // Create in kitab
        ResourceSubtype linkResourceSubType = linkRequestDTO.isDataLiveLoad() ? ResourceSubtype.LIVELINK : ResourceSubtype.STORELINK;
        ResourceModel resourceModel = resourceService.newResource(linkRequestDTO.getName(), linkRequestDTO.getDescription(), ResourceType.LINK, linkResourceSubType, userId, linkRequestDTO.getParent());

        ResourceModel newResourceForDataset = resourceService.createDataset(linkRequestDTO.getName() + "-ds", "Auto created dataset for link", userId, linkRequestDTO.getParent(), ResourceSubtype.BUILDDATASET);
        linkRequestDTO.setDatasetId(newResourceForDataset.getId());
        Source source = findSourceById(linkRequestDTO.getSourceId());
        Link link = Link.builder()
                .id(resourceModel.getId())
                .script(linkRequestDTO.getScript())
                .dataLiveLoad(linkRequestDTO.isDataLiveLoad())
                .type(source.getType())
                .datasetId(linkRequestDTO.getDatasetId())
                .branch(linkRequestDTO.getBranch())
                .sourceId(linkRequestDTO.getSourceId())
                .writeMode(linkRequestDTO.getWriteMode())
                .trigger(linkRequestDTO.getTrigger())
                .build(null)
                .buildId(linkRequestDTO.getBuildId())
                .cronExpression(linkRequestDTO.getCronExpression()).build();


        if (Objects.equals(source.getType(), "jdbc")) {
            String processedQuery = jdbcService.processQuery(linkRequestDTO.getScript(), null, -1);

            // TODO: CHANGED BASED ON DBMS TYPE (already existing function is present)
            if (!processedQuery.toLowerCase().startsWith(SELECT)) {
                throw new LinkException("Only select queries are allowed.");
            }
        } else if (Objects.equals(source.getType(), "SHAREPOINT")) {
            SharepointLinkConfig sharepointLinkConfig = SharepointLinkConfig.builder()
                    .fileId(linkRequestDTO.getFileId())
                    .fileType(linkRequestDTO.getFileType())
                    .sheetName(linkRequestDTO.getSheetName())
                    .build();
            link.setLinkConfigId(sharepointLinkConfigRepository.save(sharepointLinkConfig).getId());
        } else if (Objects.equals(source.getType(), "FOLDER")) {
            FolderLinkConfig folderLinkConfig = FolderLinkConfig.builder()
                    .subFolder(linkRequestDTO.getSubFolder())
                    .build();
            link.setLinkConfigId(folderLinkConfigRepository.save(folderLinkConfig).getId());
        } else if (Objects.equals(source.getType(), "rest")) {
            List<RestAPIRequest> requests = linkRequestDTO.getRequests().stream().map(dto ->
                    RestAPIRequest.builder()
                            .path(dto.getPath())
                            .domainId(dto.getDomainId())
                            .build()).collect(Collectors.toList());
            CsvPreprocessingDTO csvPreprocessingDTO = linkRequestDTO.getCsvPreprocessing();
            CsvPreprocessingModel csvPreprocessingModel = CsvPreprocessingModel.builder()
                    .header(csvPreprocessingDTO.getHeader())
                    .firstLineAsHeader(csvPreprocessingDTO.isFirstLineAsHeader())
                    .customHeader(csvPreprocessingDTO.getCustomHeader())
                    .fieldDelimiter(csvPreprocessingDTO.getFieldDelimiter())
                    .lineDelimiter(csvPreprocessingDTO.getLineDelimiter())
                    .customLineDelimiter(csvPreprocessingDTO.getCustomLineDelimiter())
                    .quote(csvPreprocessingDTO.getQuote())
                    .escape(csvPreprocessingDTO.getEscape())
                    .customFieldDelimiter(csvPreprocessingDTO.getCustomFieldDelimiter())
                    .trimSpace(csvPreprocessingDTO.getTrimSpace())
                    .replaceInvalidChars(csvPreprocessingDTO.getReplaceInvalidChars())
                    .timeFormat(csvPreprocessingDTO.getTimeFormat())
                    .customTimeFormat(csvPreprocessingDTO.getCustomTimeFormat())
                    .dateFormat(csvPreprocessingDTO.getDateFormat())
                    .customDateFormat(csvPreprocessingDTO.getCustomDateFormat())
                    .timestampFormat(csvPreprocessingDTO.getTimestampFormat())
                    .customTimestampFormat(csvPreprocessingDTO.getCustomTimestampFormat())
                    .build();

            Webhook webhook = Webhook.builder()
                    .id(resourceModel.getId())
                    .sourceId(linkRequestDTO.getSourceId())
                    .requests(requests)
                    .responseParam(linkRequestDTO.getResponseParam())
                    .csvPreprocessing(csvPreprocessingModel)
                    .build();

            link.setLinkConfigId(webhookRepository.save(webhook).getId());
        }

        if (link.isDataLiveLoad()) {
            ResourceModel dataset = resourceService.createDataset(linkRequestDTO.getName() + " -  data", linkRequestDTO.getName() + " live dataset", userId, linkRequestDTO.getParent(), ResourceSubtype.LIVEDATASET);
            link.setDatasetId(dataset.getId());

            UUID newTransactionId = datasetMappingService.createTransaction(link.getDatasetId(), link.getBranch(), userId, BuildLaunchedBy.UPLOAD, linkRequestDTO.getBuildId(), link.getWriteMode());
            datasetMappingService.successTransaction(newTransactionId);
            datasetMappingService.postTransformDatasetMappingOperations(link.getDatasetId(), link.getBranch(), newTransactionId, linkRequestDTO.getBuildId());

            DatasetModel datasetModel = datasetRepository.getReferenceById(link.getDatasetId());

            BranchModel branchModel1 = new BranchModel();
            branchModel1.setId(link.getDatasetId() + link.getBranch());
            branchModel1.setDatasetId(link.getDatasetId());
            branchModel1.setBranch(link.getBranch());
            branchModel1.setType(ResourceSubtype.LIVEDATASET);
            branchModel1.setCreatedBy(userId);
            branchModel1.setCreatedAt(new Date());

            branchRepository.save(branchModel1);

            Set<BranchModel> newBranches = datasetModel.getBranches();
            newBranches.add(branchModel1);
            datasetRepository.save(datasetModel);
        } else {
            PipelineModel model = new PipelineModel();
            model.sourceDataset = link.getSourceId();
            model.targetDataset = link.getDatasetId();
            model.sourceBranch = link.getBranch();
            model.targetBranch = link.getBranch();
            model.repositoryId = null;
            model.repositoryBranch = null;
            model.scriptPath = null;
            model.buildId = null;
            model.status = BuildStatus.ACTIVE;
            model.type = source.getType(); // FIXME TODO WARNING
            model.setCreatedBy(userId);
            model.setUpdatedBy(userId);
            pipelineRepository.saveAndFlush(model);
        }


        if (!source.isDirectLoad()) { // This is for agent
            System.out.println("Going in Agent Upload");
            // update config Status
            updateConfig(source.getAgentId());
        }
        return linkRepository.save(link);
    }

    public List<ColumnDTO> getLiveLinkSchema(UUID id) throws Exception {
        Link link = linkRepository.findById(id).orElseThrow();

        Source source = findSourceById(link.getSourceId());

        if (!source.isDirectLoad()) {
            throw new BadRequestException("Not a live link");
        }
        DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(source.getSourceConfig());

        Dataset<Row> sqlDf = sparkDataService.getDbmsTableSparkDF(link.getScript(), databaseSourceConfig, 0);
        StructType schema = sqlDf.schema();
        List<ColumnDTO> colList = new ArrayList<>(schema.fields().length);
        Arrays.stream(schema.fields()).forEach(element -> colList.add(ColumnDTO.builder().headerName(element.name()).field(element.name()).type(element.dataType().typeName()).build()));

        return colList;
    }


    public void updateConfig(List<UUID> agentIdList) {
        for (UUID agentId : agentIdList) {
            ConnectConfig connectConfig = new ConnectConfig();
            connectConfig.setAgentId(agentId);
            connectConfig.setVersion(UUID.randomUUID());
            connectConfig.setUpdatedAt(new Date());
            connectConfigRepository.save(connectConfig);
        }
    }

    public void deleteLinkById(UUID id) {
        if (!this.existsById(id)) {
            throw new NoSuchElementException(MessageFormat.format("link by id {0} does not exist.", id));
        }

        linkRepository.deleteById(id);
        resourceService.deleteById(id);

        pipelineRepository.deleteByTargetDatasetAndTargetBranch(id, null);
    }

    @NotNull
    public Link updateLink(LinkRequestDTO link, UUID userId) {
        if (!linkRepository.existsById(link.getId())) {
            throw new NoSuchElementException(MessageFormat.format("Link with Id {0} does not exist", link.getId()));
        }

        if (link.getDatasetId() != null && (!datasetRepository.existsById(link.getDatasetId()))) {
            throw new NoSuchElementException(MessageFormat.format("Dataset with Id {0} does not exist", link.getDatasetId()));
        }

        if (link.getSourceId() != null && (!existsSourceById(link.getSourceId()))) {
            throw new NoSuchElementException(MessageFormat.format("Source with Id {0} does not exist", link.getSourceId()));
        }

        if (link.getParent() != null && (!resourceService.existsById(link.getParent()))) {
            throw new NoSuchElementException(MessageFormat.format("Parent with Id {0} does not exist", link.getParent()));
        }

        Source source = findSourceById(link.getSourceId());
        Link linkExisting = linkRepository.getReferenceById(link.getId());
        // don't need jdbcType if unlimited -1
        if (Objects.equals(source.getType(), "jdbc")) {
            // don't need jdbcType if unlimited -1
            String processedQuery = jdbcService.processQuery(link.getScript(), null, -1);

            System.out.println("Query : " + processedQuery.toLowerCase());
            if (!processedQuery.toLowerCase().startsWith(SELECT)) {
                throw new LinkException("Error : Only select queries are allowed.");
            }
        } else if (Objects.equals(source.getType(), "SHAREPOINT")) {
            SharepointLinkConfig sharepointLinkConfig = sharepointLinkConfigRepository.getReferenceById(linkExisting.getLinkConfigId());
            if (link.getFileId() != null) {
                sharepointLinkConfig.setFileId(link.getFileId());
            }
            if (link.getFileType() != null) {
                sharepointLinkConfig.setFileType(link.getFileType());
            }
            if (link.getSheetName() != null) {
                sharepointLinkConfig.setSheetName(link.getSheetName());
            }
            sharepointLinkConfigRepository.save(sharepointLinkConfig);
        } else if (Objects.equals(source.getType(), "FOLDER")) {
            FolderLinkConfig folderLinkConfig = folderLinkConfigRepository.getReferenceById(linkExisting.getLinkConfigId());
            if (link.getSubFolder() != null) {
                folderLinkConfig.setSubFolder(link.getSubFolder());
            }

            folderLinkConfigRepository.save(folderLinkConfig);
        } else if (Objects.equals(source.getType(), "rest")) {
            Webhook webhook = webhookRepository.getReferenceById(linkExisting.getLinkConfigId());
            if (link.getRequests() != null) {
                webhook.setRequests(link.getRequests());
            }
            if (link.getResponseParam() != null) {
                webhook.setResponseParam(link.getResponseParam());
            }
            if (link.getCsvPreprocessing() != null) {
                CsvPreprocessingDTO csvPreprocessingDTO = link.getCsvPreprocessing();

                CsvPreprocessingModel csvPreprocessingModel = webhook.getCsvPreprocessing();
                if (csvPreprocessingModel == null) {
                    csvPreprocessingModel = new CsvPreprocessingModel();
                }
                csvPreprocessingModel.setHeader(csvPreprocessingDTO.getHeader());
                csvPreprocessingModel.setFirstLineAsHeader(csvPreprocessingDTO.isFirstLineAsHeader());
                csvPreprocessingModel.setQuote(csvPreprocessingDTO.getQuote());
                csvPreprocessingModel.setEscape(csvPreprocessingDTO.getEscape());
                csvPreprocessingModel.setLineDelimiter(csvPreprocessingDTO.getLineDelimiter());
                csvPreprocessingModel.setCustomLineDelimiter(csvPreprocessingDTO.getCustomLineDelimiter());
                csvPreprocessingModel.setCustomHeader(csvPreprocessingDTO.getCustomHeader());
                csvPreprocessingModel.setFieldDelimiter(csvPreprocessingDTO.getFieldDelimiter());
                csvPreprocessingModel.setCustomFieldDelimiter(csvPreprocessingDTO.getCustomFieldDelimiter());
                csvPreprocessingModel.setTrimSpace(csvPreprocessingDTO.getTrimSpace());
                csvPreprocessingModel.setReplaceInvalidChars(csvPreprocessingDTO.getReplaceInvalidChars());
                csvPreprocessingModel.setTimeFormat(csvPreprocessingDTO.getTimeFormat());
                csvPreprocessingModel.setCustomTimeFormat(csvPreprocessingDTO.getCustomTimeFormat());
                csvPreprocessingModel.setDateFormat(csvPreprocessingDTO.getDateFormat());
                csvPreprocessingModel.setCustomDateFormat(csvPreprocessingDTO.getCustomDateFormat());
                csvPreprocessingModel.setTimestampFormat(csvPreprocessingDTO.getTimestampFormat());
                csvPreprocessingModel.setCustomTimestampFormat(csvPreprocessingDTO.getCustomTimestampFormat());

                webhook.setCsvPreprocessing(csvPreprocessingModel);
            }

            webhookRepository.save(webhook);
        }

        linkExisting.copyNonNullProperties(link);

        ResourceModel folder = resourceService.findById(link.getId()).orElseThrow();
        folder.copyNonNullProperties(link);

        Link link1 = linkRepository.save(linkExisting);
        resourceService.save(folder);

        // update config Status
        updateConfig(source.getAgentId());
        return link1;
    }

    @NotNull
    public Map<String, Object> existsDatasetLinkCheck(UUID datasetId, String branch) {
        HashMap<String, Object> response = new HashMap<>();

        if (Objects.equals(datasetId.toString(), "undefined") || Objects.equals(branch, "undefined")) {
            throw new UnsupportedOperationException("Not valid datasetId or branch");
        }

        if (linkRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            response.put("status", true);
            response.put("link", linkRepository.findByDatasetIdAndBranch(datasetId, branch).orElseThrow());
        } else {
            response.put("status", false);
        }
        return response;
    }

    public BuildLog getBuildLog(UUID id, UUID userId) throws Exception {
        UUID buildId = UUID.randomUUID();

        Link link = this.findById(id);
        link.setBuild(new Date());
        link.setBuildId(buildId);
        linkRepository.save(link);

        Source source = findSourceById(link.getSourceId());
        BuildLog buildLog = buildLogService.initialBuildLogSetupWithSockets(buildId, id, userId, BuildTrigger.CONNECT, BuildLaunchedBy.MANUAL, null, link.getBranch());

        if (source.isDirectLoad()) {
            uploadService.DirectLoad(link.getId(), userId, buildId, BuildTrigger.CONNECT, BuildLaunchedBy.MANUAL);
        } else {
            // TO DO : handle buildBySource for this case, ask rakesh
            // update config Status
            updateConfig(source.getAgentId());
        }
        return buildLog;
    }

    public Dataset<Row> previewFolder(Link link, Source source) throws Exception {
        FolderSourceConfig folderSourceConfig = folderSourceConfigService.findById(source.getSourceConfig());
        String sourcePath = folderSourceConfig.getPath();
        // Check if sourcePath ends with a slash and remove it
        if (sourcePath.endsWith("/")) {
            sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        }
        String subFolder = link.getScript();
        if (!subFolder.startsWith("/")) {
            subFolder = "/" + subFolder;
        }
        String sourceFolderPath = sourcePath + subFolder;

        return sparkDataService.processFilesToSparkDf(sourceFolderPath);
    }

    public Dataset<Row> previewSharepoint(Link link, Source source) throws Exception {
        SharePointSourceConfig sharePointSourceConfig = sharepointConfigService.findById(source.getSourceConfig());
//        String sourcePath = sharePointSourceConfig.getPath();
        // Check if sourcePath ends with a slash and remove it
//        if (sourcePath.endsWith("/")) {
//            sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
//        }
//        String subFolder = link.getScript();
//        if (!subFolder.startsWith("/")) {
//            subFolder = "/" + subFolder;
//        }
//        String sourceFolderPath = sourcePath + subFolder;

//        return sparkDataService.processFilesToSparkDf(sourceFolderPath);
        return null;
    }

    public Dataset<Row> previewWebhook(Link link, List<RestAPIRequest> requests, String responseParam, CsvPreprocessingDTO csvPreprocessing, UUID userId) throws Exception {
        List<WebhookCallData> result = webhookService.executeWebhook(requests, link.getId(), userId);
        if (result.isEmpty()) {
            throw new Exception("No results found for link " + link.getLinkConfigId());
        }

        WebhookCallData callData = result.get(result.size() - 1);
        String contentType = webhookService.getContentTypeFromResponseHeaders(callData.getResponseHeaders());

        if (contentType.equals("text/csv")) {
            return sparkDataService.csvToDataFrame(callData.getResponseBody(), csvPreprocessing);
        } else {
            String filteredBody = webhookService.processResponseParamForJson(responseParam, callData.getResponseBody());
            if (filteredBody == null || filteredBody.equals("null") || filteredBody.isEmpty()) {
                throw new Exception("Particular key/index is missing from JSON data");
            }
            return sparkDataService.jsonToDataFrame(filteredBody);
        }
    }

    @NotNull
    public Map<String, Object> getPreview(UUID id, HashMap<String, String> query, List<RestAPIRequest> requests, String responseParam, CsvPreprocessingDTO csvPreprocessing, boolean isSource, UUID userId) {
        UUID sourceId = null;
        Link link = null;
        if (isSource) {
            sourceId = id;
        } else {
            link = linkRepository.findById(id).orElseThrow();
            sourceId = link.getSourceId();
        }
        HashMap<String, Object> message = new HashMap<>();

        Source source = findSourceById(sourceId);
        try {
            Dataset<Row> df = null;
            StructType schema = null;
            if (!source.isDirectLoad()) {
                throw new LinkException("This is not a live link.");
            } else if (Objects.equals(source.getType(), "FOLDER")) {
                df = previewFolder(link, source);
                schema = df.schema();
            } else if (Objects.equals(source.getType(), "SHAREPOINT")) {
                df = previewSharepoint(link, source);
                schema = df.schema();
            } else if (Objects.equals(source.getType(), "jdbc")) {
                DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(source.getSourceConfig());
                if (JdbcUtils.isValidDQLQuery(query.get("query"), databaseSourceConfig.getDbmsType())) {
                    throw new LinkException("Only valid DQL's are allowed.");
                }

                df = sparkDataService.getDbmsTableSparkDF(query.get("query"), databaseSourceConfig, ConnectConfigs.PREVIEW_LIMIT);
                schema = df.schema();
            } else if (Objects.equals(source.getType(), "rest")) {
                df = previewWebhook(link, requests, responseParam, csvPreprocessing, userId);
                schema = df.schema();
            }

            List<ColumnDTO> colList = new ArrayList<>(schema.fields().length);
            Arrays.stream(schema.fields()).forEach(element -> colList.add(ColumnDTO.builder().headerName(element.name()).field(element.name()).type(element.dataType().typeName()).build()));
            List<Object> rows = SparkService.getDfRows(df);
            message.put("status", "SUCCESS");
            message.put("message", "Data was fetched successfully.");
            message.put("rows", rows);
            message.put("cols", colList);
        } catch (Exception e) {
            message.put("status", "FAILED");
            message.put("message", e.getMessage());
            message.put("data", e.getMessage());
        }
        return message;
    }
}
