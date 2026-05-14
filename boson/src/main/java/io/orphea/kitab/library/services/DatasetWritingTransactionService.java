package io.orphea.kitab.library.services;

import io.orphea.build.BobEnums.BuildTrigger;
import io.orphea.build.library.models.BuildLog;
import io.orphea.build.library.models.DatasetBranchPair;
import io.orphea.build.library.models.SocketMessage;
import io.orphea.build.library.repository.BuildLogRepository;
import io.orphea.kitab.library.enums.TransactionStatus;
import io.orphea.kitab.library.models.TransactionModel;
import io.orphea.kitab.library.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class DatasetWritingTransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private BuildLogRepository buildLogRepository;

    @Transactional
    public void startTransaction(UUID datasetId, String branch, UUID userId, UUID buildId) throws Exception {
        TransactionModel transactionModel = null;

        if (transactionRepository.existsTransactionModelByDatasetIdAndBranch(datasetId, branch)) {
            transactionModel = transactionRepository.findTransactionModelByDatasetIdAndBranch(datasetId, branch);
            if (transactionModel.getStatus().equals(TransactionStatus.ACTIVE)) {
                throw new Exception("There is a transaction already active");
            }

            transactionModel.setStatus(TransactionStatus.ACTIVE);
            transactionModel.setTrigger(BuildTrigger.CONNECT);
            transactionModel.setBuildId(String.valueOf(buildId));
            transactionModel.setCreatedBy(userId);
            transactionModel.setCreatedAt(new Date());
            transactionModel.setFinishedAt(null);
            transactionModel.setFinishedBy(null);

        } else {
            transactionModel = new TransactionModel();
            transactionModel.setBranch(branch);
            transactionModel.setDatasetId(datasetId);
            transactionModel.setTrigger(BuildTrigger.CONNECT);
            transactionModel.setStatus(TransactionStatus.ACTIVE);
            transactionModel.setCreatedBy(userId);
            transactionModel.setCreatedAt(new Date());
            transactionModel.setBuildId(String.valueOf(buildId));

        }

        transactionRepository.save(transactionModel);
        
        BuildLog buildLog = buildLogRepository.findById(buildId).get();
        Set<DatasetBranchPair> datasetBranchPairs = buildLog.getDatasetBranchPair();
        datasetBranchPairs.add(new DatasetBranchPair(datasetId, branch));
        buildLog.setDatasetBranchPair(datasetBranchPairs);
        buildLogRepository.save(buildLog);

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, textMessage);
    }

    @Transactional
    public void endTransaction(UUID datasetId, String branch, UUID userId) throws Exception {
        TransactionModel transactionModel = null;

        if (transactionRepository.existsTransactionModelByDatasetIdAndBranch(datasetId, branch)) {
            transactionModel = transactionRepository.findTransactionModelByDatasetIdAndBranch(datasetId, branch);
            if (transactionModel.getStatus().equals(TransactionStatus.ACTIVE)) {
                transactionModel.setStatus(TransactionStatus.COMPLETED);
                transactionModel.setFinishedBy(userId);
                transactionModel.setFinishedAt(new Date());
            }
        } else {
            throw new Exception("No transaction ever existed");
        }

        transactionRepository.save(transactionModel);

        // Sending to socket
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage(String.valueOf(TransactionStatus.COMPLETED));

        template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, socketMessage);
    }

    @Transactional
    public void abortTransaction(UUID datasetId, String branch, UUID userId) {
        TransactionModel transactionModel = null;

        if (transactionRepository.existsTransactionModelByDatasetIdAndBranch(datasetId, branch)) {
            transactionModel = transactionRepository.findTransactionModelByDatasetIdAndBranch(datasetId, branch);
            if (transactionModel.getStatus().equals(TransactionStatus.ACTIVE)) {
                transactionModel.setStatus(TransactionStatus.ABORTED);
                transactionModel.setFinishedBy(userId);
                transactionModel.setFinishedAt(new Date());
            }
            transactionRepository.save(transactionModel);
        } else {
            log.warn("No transaction ever existed");
        }

        // Sending to socket
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage(String.valueOf(TransactionStatus.ABORTED));

        template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, socketMessage);
    }

    @Transactional
    public void abortTransactionForBuild(UUID buildId, UUID userId) {
        Set<DatasetBranchPair> datasets = buildLogRepository.findById(buildId).get().getDatasetBranchPair();
        for (DatasetBranchPair datasetBranchPair : datasets) {
            abortTransaction(datasetBranchPair.getDatasetId(), datasetBranchPair.getBranch(), userId);
        }
    }

    public boolean isActiveTransaction(UUID datasetId, String branch) {
        TransactionModel transactionModel = transactionRepository
                .findTransactionModelByDatasetIdAndBranch(datasetId, branch);

        return transactionModel.getStatus().equals(TransactionStatus.ACTIVE);
    }

    @Transactional
    public void verifyAndDeleteTransaction(UUID datasetId, String branch) {
        TransactionModel transactionModel = transactionRepository
                .findTransactionModelByDatasetIdAndBranch(datasetId, branch);

        if (transactionModel.getStatus().equals(TransactionStatus.ACTIVE)) {
            throw new UnsupportedOperationException("There is a transaction already active. Hence can't delete");
        }

        transactionRepository.delete(transactionModel);
    }
}
