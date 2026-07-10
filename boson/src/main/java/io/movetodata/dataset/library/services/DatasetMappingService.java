package io.movetodata.dataset.library.services;


import com.esotericsoftware.minlog.Log;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.library.dto.SourceDataset;
import io.movetodata.build.library.enums.WriteModeEnum;
import io.movetodata.build.library.repository.BuildSpecificationsRepository;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.enums.DatasetMappingEnums;
import io.movetodata.dataset.library.models.DatasetMappingModel;
import io.movetodata.dataset.library.models.DatasetMappingTransactionModel;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.dataset.library.repository.DatasetMappingTransactionRepository;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.kitab.library.services.DatasetWritingTransactionService;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.DeletionInBackingFS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetMappingService {
    private final DatasetMappingRepository datasetMappingRepository;
    private final DatasetMappingTransactionRepository datasetMappingTransactionRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final SchemaRepository schemaRepository;
    private final DeletionInBackingFS deletionInBackingFS;
    private final PlatformConfigService platformConfigService;
    private final DatasetWritingTransactionService datasetWritingTransactionService;

    public UUID getCurrentTransaction(UUID datasetId, String branch) {
        return datasetMappingRepository.findById(new DatasetMappingKey(datasetId, branch)).get().getCurrentTransaction();
    }

    /*
        Transaction Graph Details

        Python
        Transaction process integrated with buildLog
        Create : On resolve target, inside write_dataframe
        Complete : Success on postTransform Checkpoint log
                 : Failed on failing to write, via funnel update log
                 : Abort on build abort

        Sql
        Create :
        Complete : Success on
                 : Failed on
                 : Abort on

        Connect
        Create :
        Complete : Success on
                 : Failed on
                 : Abort on

        Upload
        Transaction process integrated with buildLog
        Create : uploadCsv
        Complete : Success on | handled there itself
                 : Failed on | handled there itself

        Live Data
        Transaction process independent
        Create: createLink
        Complete : Success | handled there itself
                 : Failed | no case required for failing there

     */

    // Creates a new Transaction but doesn't assign as the current one
    @Transactional
    public UUID createTransaction(UUID datasetId, String branch, UUID userId, BuildLaunchedBy launchedBy, UUID buildId, WriteModeEnum writeMode) {
        DatasetMappingKey key = new DatasetMappingKey(datasetId, branch);

        DatasetMappingModel datasetMappingModel;

        if (datasetMappingRepository.findById(key).isPresent()) {
            datasetMappingModel = getDatasetMapping(datasetId, branch).get();
        } else {
            datasetMappingModel = new DatasetMappingModel();
            datasetMappingModel.setDatasetId(datasetId);
            datasetMappingModel.setBranch(branch);
        }
        // Valid Date processing
        Set<LocalDate> validDates = datasetMappingModel.getValidDates();
        validDates.add(LocalDate.now());
        datasetMappingModel.setValidDates(validDates);

        // Dataset Transaction Part
        DatasetMappingTransactionModel datasetMappingTransactionModel = new DatasetMappingTransactionModel();
        datasetMappingTransactionModel.setDatasetId(datasetId);
        datasetMappingTransactionModel.setBranch(branch);
        datasetMappingTransactionModel.setWriteMode(writeMode);
        datasetMappingTransactionModel.setBuildStatus(BuildStatus.ACTIVE);
        datasetMappingTransactionModel.setBuildId(buildId);
        datasetMappingTransactionModel.setLaunchedBy(launchedBy);
        datasetMappingTransactionModel.setCreatedAt(new Date());
        datasetMappingTransactionModel.setLocalDate(LocalDate.now());
        datasetMappingTransactionModel.setCreatedBy(userId);
        datasetMappingTransactionRepository.save(datasetMappingTransactionModel);

        UUID newTransactionId = datasetMappingTransactionModel.getId();
//        datasetMappingModel.setCurrentTransaction(newTransactionId);
        datasetMappingRepository.save(datasetMappingModel);

        return newTransactionId;
    }


    // Assign a already existing transaction as current transaction for the dataset
    @Transactional
    public void assignTransactionAsCurrentTransaction(UUID datasetId, String branch, UUID transactionId, UUID buildId) throws Exception {
        DatasetMappingTransactionModel transactionModel = getTransaction(transactionId);
        if (transactionModel.getBuildStatus() != BuildStatus.SUCCESS) {
            log.error("Trying to assign non success transaction {} as current transaction for build :{}", transactionId, buildId);
            return;
        }
        DatasetMappingModel datasetMappingModel = getDatasetMapping(datasetId, branch).get();
        datasetMappingModel.setCurrentTransaction(transactionId);
        datasetMappingModel.setCurrentBuildId(buildId);
        datasetMappingRepository.save(datasetMappingModel);
    }

    @Transactional
    public void handleTransactionsQueue(UUID datasetId, String branch) {
        Log.info(">>>>> INSIDE HANDLE QUEUE");
        DatasetMappingModel datasetMappingModel = getDatasetMapping(datasetId, branch).get();

        Integer queueSize = datasetMappingModel.getDatasetHistory();

        if (Objects.equals(datasetMappingModel.getHistoryStoreType(), DatasetMappingEnums.PLATFORM)) {
            queueSize = platformConfigRepository.findByName("platformConfig").orElseThrow().getDatasetHistory();
        }
        Log.info(">>>>>>> Queue Size : " + queueSize);
        List<DatasetMappingTransactionModel> transactions = datasetMappingTransactionRepository.findByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);

        while (transactions.size() > queueSize) {
            int _toDeleteIndex = transactions.size() - 1;
            UUID transactionId = transactions.get(_toDeleteIndex).getId();
            log.info(">>>>>> TSIZE " + transactions.size() + " QUEUE SIZE  : " + queueSize);
            log.info(">>>>>> Handling : " + transactionId);
            datasetMappingTransactionRepository.deleteById(transactionId);
            datasetMappingTransactionRepository.flush();

            Log.info(">>>>> BEFORE CLEAN UP : " + transactionId);
            try {
                cleanUpDatasetTransactionRelatedThings(datasetId, branch, transactionId);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            transactions.remove(_toDeleteIndex);
        }

        Log.info(">>>>> Setting final queue : " + transactions.size());
        datasetMappingRepository.save(datasetMappingModel);
        Log.info(">>>>> DONE ");
    }

    @Transactional
    public void cleanUpDatasetTransactionRelatedThings(UUID datasetId, String branch, UUID transactionId) {
        try {
            if (buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId)) {
                buildSpecificationsRepository.delete(buildSpecificationsRepository.findByTransactionId(transactionId));
                Log.info(">>>>> BUILD SPEC CLEANED ");
            }

            if (schemaRepository.existsByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId)) {

                try {
                    schemaRepository.deleteAllByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId);
                    log.info("Schema Deleted");
                } catch (Exception e) {
                    log.error("Schema deletion failed : " + e.getMessage());
                }
            }

            // Build Transactions are for dataset and branch
            // Postgres sync gets modified in postTransform
            // Schedules are also for dataset and branch
            deletionInBackingFS.deleteDatasetFilesWrapperBasedOnStorageType("dataset", datasetId, transactionId);
            Log.info(">>>>> Physcial CLEANED ");
        } catch (Exception e) {
            Log.info(">>> Exception : ", e);
        }
    }

    @Transactional
    public void postTransformDatasetMappingOperations(UUID datasetId, String branch, UUID transactionId, UUID buildId) throws Exception {
        Log.info(">>>>> Inside Dataset Mapping post");
        assignTransactionAsCurrentTransaction(datasetId, branch, transactionId, buildId);
        Log.info(">>>>> Assign Success");
        handleTransactionsQueue(datasetId, branch);
    }

    @Transactional
    public void statsCalculation(DatasetMappingTransactionModel transaction) {
        DatasetMappingModel datasetMappingModel = datasetMappingRepository.findById(new DatasetMappingKey(transaction.getDatasetId(), transaction.getBranch())).get();


        switch (transaction.getBuildStatus()) {
            case ABORTED:
                datasetMappingModel.setTotalAborted(datasetMappingModel.getTotalAborted() + 1L);
                break;
            case SUCCESS:
                datasetMappingModel.setTotalSuccessful(datasetMappingModel.getTotalSuccessful() + 1L);
                break;
            case FAILED:
                datasetMappingModel.setTotalFailed(datasetMappingModel.getTotalFailed() + 1L);
                break;
        }

        long elapsedTime = transaction.getFinishedAt().getTime() - transaction.getCreatedAt().getTime();
        long oldCount = datasetMappingModel.getTotalCount();

        // Time related calculations
        datasetMappingModel.setTotalCount(oldCount + 1L);
        datasetMappingModel.setLowestTime(Math.min(datasetMappingModel.getLowestTime(), elapsedTime));
        datasetMappingModel.setHighestTime(Math.max(datasetMappingModel.getHighestTime(), elapsedTime));
        datasetMappingModel.setMeanTime((datasetMappingModel.getMeanTime() * oldCount + elapsedTime) / (oldCount + 1L));
        datasetMappingModel.setMeanTime((datasetMappingModel.getMeanTime() * oldCount + elapsedTime) / (oldCount + 1L));

        datasetMappingRepository.save(datasetMappingModel);
    }

    @Transactional
    public void successTransaction(UUID transactionId) throws Exception {
        updateTransactionStatus(transactionId, BuildStatus.SUCCESS);
    }

    @Transactional
    public void failedTransaction(UUID transactionId) throws Exception {
        updateTransactionStatus(transactionId, BuildStatus.FAILED);
    }

    @Transactional
    public void abortTransaction(UUID transactionId) throws Exception {
        updateTransactionStatus(transactionId, BuildStatus.ABORTED);
    }

    @Transactional
    public void forceClosureOfTransaction(UUID transactionId, UUID userId) throws Exception {
        DatasetMappingTransactionModel transactionModel = getTransaction(transactionId);
        if (transactionModel.getBuildStatus() == BuildStatus.ACTIVE) {
            failedTransaction(transactionId);
            datasetWritingTransactionService.abortTransaction(transactionModel.getDatasetId(), transactionModel.getBranch(), userId);
        }
    }

    @Transactional
    public void updateTransactionStatus(UUID transactionId, BuildStatus status) throws Exception {
        DatasetMappingTransactionModel transaction = getTransaction(transactionId);
        transaction.setBuildStatus(status);
        // Setting Finished Time
        switch (status) {
            case ABORTED:
            case SUCCESS:
            case FAILED:
                transaction.setFinishedAt(new Date());
                statsCalculation(transaction);
        }

        datasetMappingTransactionRepository.save(transaction);
    }

    public DatasetMappingTransactionModel getTransaction(UUID transactionId) throws Exception {
        Optional<DatasetMappingTransactionModel> optionalTransaction = datasetMappingTransactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            throw new Exception("Transaction not found : " + transactionId);
        }
        return optionalTransaction.get();
    }

    @Transactional
    public Optional<DatasetMappingModel> getDatasetMapping(UUID datasetId, String branch) {
        DatasetMappingKey datasetMappingKey = new DatasetMappingKey(datasetId, branch);

        return datasetMappingRepository.findById(datasetMappingKey);
    }

    @Transactional
    public List<UUID> getDatasetsTransactions(List<SourceDataset> sources) {
        List<UUID> transactions = new ArrayList<>();
        String defaultBranch = platformConfigService.getPlatformConfig().getDefaultBranch();

        for (SourceDataset source : sources) {
            UUID datasetId = UUID.fromString(source.getSource());
            String branch = source.getBranch();
            if (branch == null) {
                branch = defaultBranch;
            }

            System.out.println(source);
            System.out.println(branch);

            transactions.add(getDatasetMapping(datasetId, branch).get().getCurrentTransaction());
        }

        return transactions;
    }

    public List<String> getTransactions(UUID datasetId, String branch, UUID transactionId) throws Exception {
        List<String> transactions = new ArrayList<>();
        DatasetMappingTransactionModel datasetMappingTransactionModel = getTransaction(transactionId);
        if (datasetMappingTransactionModel.getWriteMode().equals(WriteModeEnum.SNAPSHOT)) {
            transactions.add(String.valueOf(transactionId));
        } else if (datasetMappingTransactionModel.getWriteMode().equals(WriteModeEnum.APPEND)) {
            transactions.addAll(datasetMappingTransactionRepository.findTransactionIdsUpToFirstSnapshotOrAll(datasetId, branch, transactionId));
        }

        return transactions;
    }

}