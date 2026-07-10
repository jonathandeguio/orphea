package io.movetodata.dataset.library.services;

import com.fasterxml.jackson.core.type.TypeReference;
import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.library.dto.SourceDataset;
import io.movetodata.build.library.services.FunnelUtils;
import io.movetodata.build.shyne.models.ResourcePathOrId;
import io.movetodata.connect.library.enums.SourceTypeEnum;
import io.movetodata.connect.library.services.JdbcUtils;
import io.movetodata.dataset.library.DTOs.ColumnDTO;
import io.movetodata.dataset.library.DTOs.PreviewDatasetBySqlDTO;
import io.movetodata.dataset.library.models.DatasetPreviewQueriesModel;
import io.movetodata.dataset.library.repository.DatasetPreviewQueriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.build.shyne.Utils.SQLTransformUtils.jacksonStringParser;
import static io.movetodata.build.shyne.Utils.SQLTransformUtils.resolveDatasets;
import static io.movetodata.sharedutils.Utils.decodeBase64;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetPreviewQueries {
    private final DatasetPreviewQueriesRepository datasetPreviewQueriesRepository;
    private final FunnelUtils funnelUtils;
    private final SparkSession sparkSession;
    private final SparkDataService sparkDataService;

    @Transactional
    public void saveWithLimit(DatasetPreviewQueriesModel previewQueriesModel) {
        datasetPreviewQueriesRepository.save(previewQueriesModel);

        List<DatasetPreviewQueriesModel> datasetList = datasetPreviewQueriesRepository
                .findByDatasetIdOrderByCreatedAtDesc(previewQueriesModel.getDatasetId());

        if (datasetList.size() > 10) {
            List<DatasetPreviewQueriesModel> recordsToDelete = datasetList.subList(10, datasetList.size());
            datasetPreviewQueriesRepository.deleteAll(recordsToDelete);
        }
    }

    public List<DatasetPreviewQueriesModel> getPreviewQueriesHistory(UUID datasetId) {
        return datasetPreviewQueriesRepository.findByDatasetIdOrderByCreatedAtDesc(datasetId);
    }

    @Transactional
    public Map<String, Object> previewDatasetBySql(PreviewDatasetBySqlDTO previewDatasetBySqlDTO, UUID userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String query = decodeBase64(previewDatasetBySqlDTO.getQuery());
        if (!JdbcUtils.isValidDQLQuery(query, SourceTypeEnum.SPARKSQL)) {
            throw new Exception("Only valid DQL's are allowed.");
        }

        BufferedReader reader = new BufferedReader(new StringReader(query));
        StringBuilder content = new StringBuilder();
        Map<String, List<ResourcePathOrId>> resolveTargetDatasetMap;
        List<ResourcePathOrId> sourcesOriginal;
        resolveTargetDatasetMap = resolveDatasets(reader, content, System.getenv("TEMP_PATH"), BuildType.PREVIEW);
        reader.close();

        sourcesOriginal = resolveTargetDatasetMap.get("sources");
        if (sourcesOriginal.isEmpty()) {
            throw new Exception("No dataset specified to preview dataset.");
        }
        String tempView = sourcesOriginal.get(0).getViewName();
        List<SourceDataset> sources = funnelUtils.verifyFunnelSources(List.of(new SourceDataset(sourcesOriginal.get(0).getResource(), previewDatasetBySqlDTO.getBranch())));
        if (sourcesOriginal.isEmpty()) {
            throw new Exception("Invalid dataset specified to preview dataset.");
        }
        SourceDataset source = sources.get(0);
        Dataset<Row> df = sparkDataService.getSparkDF(UUID.fromString(source.getSource()), source.getBranch(), UUID.fromString("00000000-0000-0000-0000-000000000000"), -100);
        df.createOrReplaceTempView(tempView);
        Dataset<Row> resultDf = null;
        try {
            resultDf = sparkSession.sql(content.toString());
        } catch (Exception e) {
            throw new Exception("Error while running SQL " + e);
        }

        StructType schema = resultDf.schema();
        List<ColumnDTO> columnDTOList = new ArrayList<>();

        for (StructField structField : schema.fields()) {
            columnDTOList.add(new ColumnDTO(structField.name(), structField.name(), structField.dataType().typeName()));
        }
        List<Map<String, Object>> parsedData = resultDf.limit(1000).toJSON().collectAsList().stream().map(e -> {
            try {
                return jacksonStringParser(e, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());

        sparkSession.catalog().dropTempView(tempView);

        // Maintaining history of queries ran
        DatasetPreviewQueriesModel previewQueriesModel = new DatasetPreviewQueriesModel();
        previewQueriesModel.setQuery(previewDatasetBySqlDTO.getQuery());
        previewQueriesModel.setUserId(userId);
        previewQueriesModel.setDatasetId(previewDatasetBySqlDTO.getDatasetId());
        previewQueriesModel.setBranch(previewDatasetBySqlDTO.getBranch());
        previewQueriesModel.setTransactionId(previewDatasetBySqlDTO.getTransactionId());
        previewQueriesModel.setCreatedAt(new Date());
        previewQueriesModel.setUpdatedAt(new Date());
        previewQueriesModel.setCreatedBy(userId);
        previewQueriesModel.setUpdatedBy(userId);

        saveWithLimit(previewQueriesModel);

        result.put("data", parsedData);
        result.put("schema", columnDTOList);

        return result;
    }

}
