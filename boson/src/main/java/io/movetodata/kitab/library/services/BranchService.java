package io.movetodata.kitab.library.services;

import io.movetodata.build.library.dto.SourceDataset;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchService {
    public final DatasetRepository datasetRepository;
    public final BranchRepository branchRepository;
    private final SchemaRepository schemaRepository;

    public Optional<BranchModel> findBranchModelByDatasetIdAndBranch(UUID datasetId, String branch) {
        return branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);
    }

    public List<BranchModel> findAllBranchModelByDatasetId(UUID datasetId) {
        return branchRepository.findAllBranchModelByDatasetId(datasetId);
    }

    public void delete(BranchModel model) {
        branchRepository.delete(model);
    }

    public BranchModel getBranchModel(UUID datasetId, String branch) throws ResourceNotFoundException {
//        DatasetModel datasetModel = datasetRepository.getReferenceById(datasetId);
//        Set<BranchModel> branches = datasetModel.getBranches();
//        if (branches == null) {
//            throw new ResourceNotFoundException("Branches doesn't exist");
//        }
        // Here finding the branch model, as set takes O(n) to find, but the branches of a dataset will be very limited so no problem
        // Didn't use hashmap because maybe in SB3.0 we face problem.
//        BranchModel branchModel = branches.parallelStream().filter((branchModel -> branchModel.getBranch().equals(branch))).findFirst().orElse(null);
//        return
        Optional<BranchModel> branchModelOptional = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);
        if (branchModelOptional.isPresent()) {
            return branchModelOptional.get();
        } else {
            throw new ResourceNotFoundException("Branch doesn't exist");
        }
    }

    @Transactional
    public void resolveBranch(UUID datasetId, String branch, UUID repositoryId, UUID buildId, UUID userId) {
        DatasetModel datasetModel = datasetRepository.getReferenceById(datasetId);

        try {
            BranchModel branchModel = getBranchModel(datasetId, branch);
            BranchModel oldBranchModel = branchRepository.getReferenceById(branchModel.getId());
            oldBranchModel.setUpdatedAt(new Date());
            oldBranchModel.setUpdatedBy(userId);
            oldBranchModel.setBuildId(String.valueOf(buildId));

            branchRepository.save(oldBranchModel);
        } catch (ResourceNotFoundException e) {
            BranchModel newBranch = new BranchModel();
            newBranch.setBranch(branch);
            newBranch.setCreatedBy(userId);
            newBranch.setUpdatedBy(userId);
            newBranch.setId(datasetId + branch);
            newBranch.setDatasetId(datasetId);
            newBranch.setRepositoryId(repositoryId);
            newBranch.setType(ResourceSubtype.BUILDDATASET);
            newBranch.setBuildId(String.valueOf(buildId));

            branchRepository.save(newBranch);

            Set<BranchModel> newBranches = datasetModel.getBranches();
            newBranches.add(newBranch);

            datasetModel.setBranches(newBranches);

            datasetRepository.save(datasetModel);
        }
    }

    public List<String> getBranchTypes(List<SourceDataset> sources) {
        List<String> branchTypes = new ArrayList<>();
        for (SourceDataset source : sources) {
            try {
                BranchModel branchModel = getBranchModel(UUID.fromString(source.getSource()), source.getBranch());
                branchTypes.add(String.valueOf(branchModel.getType()));
            } catch (Exception e) {
                branchTypes.add(String.valueOf(null));
            }
        }

        return branchTypes;
    }

    public List<String> getEncoding(List<SourceDataset> sources, List<UUID> transactionIds) {
        List<String> sourcesEncoding = new ArrayList<>();

        int index = 0;

        for (SourceDataset source : sources) {

            String DEFAULT_ENCODING = "UTF-8";
            try {
//                BranchModel branchModel = getBranchModel(source, branch);
//                if (branchModel != null && !Objects.equals(branchModel.getEncoding(), "none")) {
//                    sourcesEncoding.add(branchModel.getEncoding());
//                }

                SchemaModel schemaModel = schemaRepository.findByDatasetIdAndBranchAndTransactionIdAndStatus(UUID.fromString(source.getSource()), source.getBranch(), transactionIds.get(index), "active");

                if (schemaModel != null && !Objects.equals(schemaModel.getCustomSchema().getEncoding(), "none")) {

                    if (schemaModel.getCustomSchema().getEncoding() == null) {
                        sourcesEncoding.add("UTF-8");
                    } else {
                        sourcesEncoding.add(schemaModel.getCustomSchema().getEncoding());
                    }
                } else {
                    sourcesEncoding.add("none");
                }

            } catch (ResourceNotFoundException e) {
                sourcesEncoding.add(DEFAULT_ENCODING);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            index = index + 1;
        }

        return sourcesEncoding;
    }

    public boolean isBranchPresent(UUID datasetId, String branch) {
        DatasetModel datasetModel = datasetRepository.getReferenceById(datasetId);

        Set<BranchModel> branches = datasetModel.getBranches();
        // Here finding the branch model, as set takes O(n) to find, but the branches of a dataset will be very limited so no problem
        // Didn't use hashmap because maybe in SB3.0 we face problem.
        BranchModel branchModel = null;
        for (BranchModel datasetBranch : branches) {
            if (datasetBranch.getBranch().equals(branch)) {
                branchModel = datasetBranch;
                break;
            }
        }

        return branchModel != null;
    }

}
