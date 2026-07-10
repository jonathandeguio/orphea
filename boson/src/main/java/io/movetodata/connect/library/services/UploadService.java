package io.movetodata.connect.library.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.movetodata.build.BobEnums.*;
import io.movetodata.build.library.enums.WriteModeEnum;
import io.movetodata.build.library.services.BuildLogService;
import io.movetodata.build.library.services.BuildService;
import io.movetodata.build.library.services.BuildSpecService;
import io.movetodata.connect.library.DTOs.DirectLoadResourcesDTO;
import io.movetodata.connect.library.enums.SourceAuthTypeEnum;
import io.movetodata.connect.library.models.*;
import io.movetodata.connect.library.repository.LinkRepository;
import io.movetodata.connect.library.repository.SourcesRepository;
import io.movetodata.dataset.library.DTOs.CsvPreprocessingDTO;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.kitab.library.services.DatasetWritingTransactionService;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.BackFsFileUtils;
import io.movetodata.sharedutils.BackingFS;
import io.movetodata.sharedutils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;
import static io.movetodata.sharedutils.Utils.isBase64;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadService {
    private final LinkRepository linkRepository;
    private final SourcesRepository sourcesRepository;
    private final BuildService buildService;
    private final BranchRepository branchRepository;
    private final DatasetRepository datasetRepository;
    private final JDBCService jdbcService;
    private final DatasetMappingService datasetMappingService;
    private final DatabaseSourceConfigService databaseSourceConfigService;
    private final FolderSourceConfigService folderSourceConfigService;
    private final BranchService branchService;
    private final BuildLogService buildLogService;
    private final DatasetWritingTransactionService datasetWritingTransactionService;
    private final ResourceService resourceService;
    private final PlatformConfigService platformConfigService;
    private final BackingFS backingFS;
    private final SparkSession sparkSession;
    private final BackFsFileUtils backFsFileUtils;
    private final SharepointConfigService sharepointConfigService;
    private final SharepointLinkConfigService sharepointLinkConfigService;
    private final WebhookService webhookService;
    private final SparkDataService sparkDataService;
    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private BuildSpecService buildSpecService;

    @Transactional
    public ResourceModel uploadFileWrapper(UUID parentId, MultipartFile file, String fileName, String description, List<String> sheetNames, CsvPreprocessingDTO csvPreprocessingDTO, UUID userId) throws Exception {
        String defaultBranch = platformConfigService.getPlatformConfig().getDefaultBranch();
        ResourceSubtype fileType = BackingFS.getFileType(file);
        String normalizedFileName = Utils.normalizeName(file.getOriginalFilename());

        ResourceSubtype extensionFromName = Utils.getFileExtensionFromName(normalizedFileName);
        UUID buildId = UUID.randomUUID();
        buildLogService.initialBuildLogSetupWithSockets(buildId, null, userId, BuildTrigger.UPLOAD, BuildLaunchedBy.UPLOAD, null, defaultBranch);

        try {
            // RAW FILE CSV OR XLS
            if (fileType.equals(ResourceSubtype.CSV) || fileType.equals(ResourceSubtype.PARQUET) || fileType.equals(ResourceSubtype.JSON) || fileType.equals(ResourceSubtype.XLS) || fileType.equals(ResourceSubtype.XLSX)) {
                if (platformConfigService.isResourceCreationLimitReached(ResourceType.DATASET)) {
                    throw new Exception("Maximum No. of Datasets Creation Limit Reached.");
                }
                ResourceModel resourceModel = resourceService.createDataset(fileName, description, userId, parentId, ResourceSubtype.RAWDATASET);
                UUID newTransactionId = datasetMappingService.createTransaction(resourceModel.getId(), defaultBranch, userId, BuildLaunchedBy.UPLOAD, buildId, WriteModeEnum.SNAPSHOT);

                List<MultipartFile> files1 = new ArrayList<>();
                files1.add(file);
//                if (sheetNames == null) {
//                    sheetNames = excelService.getExcelSheetNames(file);
//                }
                backingFS.UploadCSV(files1, resourceModel.getId(), defaultBranch, "overwrite", ResourceSubtype.RAWDATASET, fileType, userId, buildId, newTransactionId, BuildTrigger.UPLOAD, sheetNames.get(0), csvPreprocessingDTO);
                return resourceModel;
            } else {
                ResourceModel resourceModel = resourceService.newResource(fileName, description, ResourceType.FILE, extensionFromName, userId, parentId);
                backFsFileUtils.UploadFile(file, "file", resourceModel.getId(), "file", "overwrite", false);

                return resourceModel;
            }
        } catch (Exception e) {
            log.error("Upload build failed - full stack trace:", e);
            buildLogService.createBuildLogEntryWithDebug("Build failed", e.getMessage(), BuildStage.FINISHED, FunnelStatus.FAILED, buildId, null);
            return null;
        }
    }

    public DirectLoadResourcesDTO resourceVerification(UUID linkId) throws Exception {
        Optional<Link> linkOptional = linkRepository.findById(linkId);
        if (linkOptional.isEmpty()) {
            throw new Exception("Link not found");
        } else {
            try {
                resourceService.getResourceModel(linkId);
            } catch (Exception e) {
                throw new Exception("Link is in trash!");
            }
        }
        Link link = linkOptional.get();
        Optional<Source> sourceOptional = sourcesRepository.findById(link.getSourceId());
        try {
            resourceService.getResourceModel(link.getDatasetId());
        } catch (Exception e) {
            throw new Exception("Dataset deleted or in trash");
        }


        if (sourceOptional.isEmpty()) {
            throw new Exception("Source not found");
        } else {
            try {
                resourceService.getResourceModel(sourceOptional.get().getId());
            } catch (Exception e) {
                throw new Exception("Source is in trash!");
            }
        }
        Source source = sourceOptional.get();
        return new DirectLoadResourcesDTO(link, source);
    }

    @Async
    public void DirectLoad(UUID linkId, UUID userId, UUID buildId, BuildTrigger trigger, BuildLaunchedBy launchedBy) throws Exception {
        // This direct load is too fast, on frontend this fast sockets are not able to be processed, hence sometimes leading to last log message missing problem
        try {
            Thread.sleep(1000);
            DirectLoadResourcesDTO directLoadResourcesDTO = resourceVerification(linkId);
            Source source = directLoadResourcesDTO.getSource();
            Link link = directLoadResourcesDTO.getLink();

            UUID newTransactionId = datasetMappingService.createTransaction(link.getDatasetId(), link.getBranch(), userId, launchedBy, buildId, link.getWriteMode());
            buildSpecService.createBuildSpec(null, null, null, null, null, link.getDatasetId(), link.getBranch(), newTransactionId, buildId, userId, null, null, link.getWriteMode());

            if (Objects.equals(source.getType(), "FOLDER")) {
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

                File folder = new File(sourceFolderPath);

                File[] listOfFiles = folder.listFiles();
                List<MultipartFile> multipartFiles = new ArrayList<>();
                ResourceSubtype fileType = null;
                for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
                    if (listOfFiles[i].isFile()) {

                        MultipartFile multipartFile = BackFsFileUtils.convertFileToMultipartFile(listOfFiles[i]);
                        fileType = BackingFS.getFileType(multipartFile);
                        multipartFiles.add(multipartFile);
                    }
                }
                assert fileType != null;
                if (fileType.equals(ResourceSubtype.CSV) || fileType.equals(ResourceSubtype.PARQUET) || fileType.equals(ResourceSubtype.JSON)) {
                    backingFS.UploadCSV(multipartFiles, link.getDatasetId(), link.getBranch(), "overwrite", ResourceSubtype.BUILDDATASET, fileType, userId, buildId, newTransactionId, BuildTrigger.CONNECT, null, null);
                }

            } else if (Objects.equals(source.getType(), "jdbc")) {
                // Clean up the files first, before upload
// TO DO : Handle this later on when take care about overwrite
//            if (Objects.equals(links.getSaveMode(), "overwrite")) {
//                backingFS.deleteDatasetFiles("dataset", links.getDatasetId(), links.getBranch());
//                System.out.println("deleting files");
//            }
                ingestDirectDataFromDb(link, buildId, source.getSourceConfig(), userId, newTransactionId, trigger);
            } else if (Objects.equals(source.getType(), "SHAREPOINT")) {
                try {
                    SharepointLinkConfig sharepointLinkConfig = sharepointLinkConfigService.findById(link.getLinkConfigId());
                    ResourceSubtype fileType = sharepointLinkConfig.getFileType();
                    String sharepointItemId = sharepointLinkConfig.getFileId();
                    String sheetName = sharepointLinkConfig.getSheetName();
                    SharePointSourceConfig sharePointSourceConfig = sharepointConfigService.findById(source.getSourceConfig());
                    JsonNode item = SharePointConnectorService.fetchItemData(sharePointSourceConfig.getToken(), sharePointSourceConfig.getSiteId(), sharePointSourceConfig.getDriveId(), sharepointItemId);


                    if (fileType.equals(ResourceSubtype.CSV) || fileType.equals(ResourceSubtype.XLS)) {
                        backingFS.downloadFile(item.get("@microsoft.graph.downloadUrl").asText(), link.getDatasetId(), link.getBranch(), "overwrite", ResourceSubtype.BUILDDATASET, fileType, userId, buildId, newTransactionId, BuildTrigger.CONNECT, sheetName);
                    } else {
                        throw new Exception("File type not supported " + fileType);
                    }
                } catch (Exception e) {
                    throw new Exception("Sharepoint link config related error" + e.getMessage());
                }

            } else if (Objects.equals(source.getType(), "rest")) {
                try {
                    Webhook webhook = webhookService.findById(link.getLinkConfigId());
                    List<WebhookCallData> result = webhookService.executeWebhook(webhook.getRequests(), webhook.getId(), userId);
                    if (result.isEmpty()) {
                        throw new Exception("No results found for link " + link.getLinkConfigId());
                    }

                    WebhookCallData callData = result.get(result.size() - 1);
                    String contentType = webhookService.getContentTypeFromResponseHeaders(callData.getResponseHeaders());
                    ResourceSubtype fileType = ResourceSubtype.JSON;

                    if (contentType.equals("text/csv")) {
                        fileType = ResourceSubtype.CSV;
                    } else {
                        fileType = ResourceSubtype.JSON;
                    }

                    buildLogService.createBuildLogEntry("Writing response to " + link.getDatasetId(), BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
                    Dataset<Row> df = null;
                    CsvPreprocessingDTO csvPreprocessingDTO = new CsvPreprocessingDTO();
                    BeanUtils.copyProperties(webhook.getCsvPreprocessing(), csvPreprocessingDTO);
                    try {
                        datasetWritingTransactionService.startTransaction(link.getDatasetId(), link.getBranch(), userId, buildId);

                        if (contentType.equals("text/csv")) {
                            df = sparkDataService.csvToDataFrame(callData.getResponseBody(), csvPreprocessingDTO);
                            sparkDataService.writeDataFrameAsCsv(df, link.getDatasetId(), newTransactionId);
                        } else {
                            String filteredBody = webhookService.processResponseParamForJson(webhook.getResponseParam(), callData.getResponseBody());
                            if (filteredBody == null || filteredBody.equals("null") || filteredBody.isEmpty()) {
                                throw new Exception("Particular key/index is missing from JSON data");
                            }
                            df = sparkDataService.jsonToDataFrame(filteredBody);
                            sparkDataService.writeDataFrameAsJson(df, link.getDatasetId(), newTransactionId);
                        }

                    } catch (Exception e) {
                        buildLogService.checkpoint(buildId, link.getDatasetId(), BuildStatus.FAILED, newTransactionId);
                        buildLogService.createBuildLogEntryWithDebug("Build failed", e.getMessage(), BuildStage.FINISHED, FunnelStatus.FAILED, buildId, newTransactionId);
                        throw e;
                    } finally {
                        datasetWritingTransactionService.endTransaction(link.getDatasetId(), link.getBranch(), userId);
                    }

                    buildLogService.createBuildLogEntry("Ingesting " + df.count() + " rows " + df.columns().length + " cols ", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
                    handleBranch(link.getDatasetId(), link.getBranch(), fileType, userId);
                    // Handling after branch creation, as schema update require branch
                    if (contentType.equals("text/csv")) {
                        sparkDataService.updateDatasetCustomSchemaForFunnel(link.getDatasetId(), newTransactionId, link.getBranch(), csvPreprocessingDTO);
                    }
                    buildService.postDataIngestion(userId, link, newTransactionId);
                    buildLogService.createBuildLogEntry("Build Finished", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
                } catch (Exception e) {
                    throw new Exception("Webhook related error" + e.getMessage());
                }

            } else {
                throw new Exception("Type is undefined" + source.getType());
            }

        } catch (Exception e) {
            buildLogService.createBuildLogEntryWithDebug("Processing Failed ", e.getMessage(), BuildStage.FINISHED, FunnelStatus.ERROR, buildId, null);
        }
    }

    public void handleBranch(UUID datasetId, String branch, ResourceSubtype branchType, UUID userId) {
        Optional<DatasetModel> optionalDatasetModeldatasetModel = datasetRepository.findById(datasetId);
        if (optionalDatasetModeldatasetModel.isEmpty()) {
            return;
        }
        DatasetModel datasetModel = optionalDatasetModeldatasetModel.get();

        Optional<BranchModel> branchModel = branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch);

        if (branchModel.isEmpty()) {
            BranchModel branchModel1 = new BranchModel();
            branchModel1.setId(datasetId + branch);
            branchModel1.setDatasetId(datasetId);
            branchModel1.setBranch(branch);
            branchModel1.setType(branchType);
            branchModel1.setCreatedBy(userId);
            branchModel1.setCreatedAt(new Date());
            branchRepository.save(branchModel1);

            Set<BranchModel> newBranches = datasetModel.getBranches();
            newBranches.add(branchModel1);
            datasetModel.setBranches(newBranches);
        } else {
            branchModel.get().setType(branchType);
            branchModel.get().setUpdatedAt(new Date());
            branchModel.get().setUpdatedBy(userId);

            branchRepository.save(branchModel.get());
        }


        datasetRepository.save(datasetModel);
        Optional<ResourceModel> optionalResourceModel = resourceService.findById(datasetId);
        if (optionalResourceModel.isEmpty()) {
            return;
        }
        ResourceModel resourceModel = optionalResourceModel.get();

        resourceModel.setUpdatedAt(new Date());
        resourceModel.setUpdatedBy(userId);
        resourceService.save(resourceModel);
    }

    @Transactional
    public void ingestDirectDataFromDb(Link link, UUID buildId, UUID sourceConfig, UUID userId, UUID newTransactionId, BuildTrigger buildTrigger) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceConfig);

        String databaseName = databaseSourceConfig.getDatabase();
        String username = databaseSourceConfig.getUsername();

        String query = link.getScript();
        query = jdbcService.processQuery(query, databaseSourceConfig.getDbmsType(), -1);

        buildLogService.createBuildLogEntry("Processing data from " + databaseSourceConfig.getDbmsType() + " database " + databaseName, BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
        buildLogService.createBuildLogEntry("Importing to dataset " + link.getDatasetId(), BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);


        String password = databaseSourceConfig.getPassword();
        if (isBase64(password)) {
            password = Utils.decodeBase64(password);
        }

        String url = JDBCService.JDBCUrl(databaseSourceConfig);

        try {
            Dataset<Row> jdbcDF = null;

            if (databaseSourceConfig.getAuthType() != null && databaseSourceConfig.getAuthType().equals(SourceAuthTypeEnum.KEYPAIR)) {
                jdbcDF = sparkSession.read()
                        .format("jdbc")
                        .option("url", url)
                        .option("query", query)
                        .option("user", username)
                        .option("privateKey", databaseSourceConfig.getPrivateKey())
                        .option("privateKeyPassphrase", databaseSourceConfig.getPrivateKeyPassPhrase())
                        .option("driver", jdbcService.getDriver(databaseSourceConfig.getDbmsType()))
                        .load();
            } else {
                jdbcDF = sparkSession.read()
                        .format("jdbc")
                        .option("url", url)
                        .option("query", query)
                        .option("user", username)
                        .option("password", password)
                        .option("driver", jdbcService.getDriver(databaseSourceConfig.getDbmsType()))
                        .load();
            }

            datasetWritingTransactionService.startTransaction(link.getDatasetId(), link.getBranch(), userId, buildId);
            try {
                jdbcDF.write().mode("overwrite").parquet(getResourcePath("dataset", link.getDatasetId(), String.valueOf(newTransactionId)));
            } finally {
                datasetWritingTransactionService.endTransaction(link.getDatasetId(), link.getBranch(), userId);
            }

            buildLogService.createBuildLogEntry("Ingesting " + jdbcDF.count() + " rows " + jdbcDF.columns().length + " cols ", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
            handleBranch(link.getDatasetId(), link.getBranch(), ResourceSubtype.PARQUET, userId);
            buildService.postDataIngestion(userId, link, newTransactionId);
            buildLogService.createBuildLogEntry("Build Finished", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
        } catch (Exception e) {
            buildLogService.createBuildLogEntryWithDebug("Error while running query", e.getMessage(), BuildStage.FINISHED, FunnelStatus.ERROR, buildId, newTransactionId);
        }
    }
}
