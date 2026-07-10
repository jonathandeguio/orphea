package io.movetodata.controller;

import io.movetodata.library.models.ColumnsModel;
import io.movetodata.library.models.ConditionsModel;
import io.movetodata.library.models.FilterModel;
import io.movetodata.library.models.SparkResults;
import io.movetodata.utils.ApiCaller;
import io.movetodata.utils.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.apache.spark.sql.functions.asc;
import static org.apache.spark.sql.functions.desc;

@Service
@RequiredArgsConstructor
public class DatasetController {

    public void getDataset(UUID datasetId, String branch, FilterModel filterModel, UUID resultId) throws Exception {

        try {
            Dataset<Row> dfTotal;
            dfTotal = SparkUtils.getSparkDF(datasetId, branch, -1);

//            Dataset<Row> df;
            Dataset<Row> df = dfTotal;

            if (filterModel.getColumns() != null) { // if columns null pass it as it is
                for (ColumnsModel columnsModel1 : filterModel.getColumns()) {
                    if (Objects.equals(columnsModel1.getType(), "string")) {  // TODO : other use cases
                        System.out.println("entering string");
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).like(columnsModel1.getValue()));
//                        dfTotal.show();
                    } else if (Objects.equals(columnsModel1.getType(), "integer") || Objects.equals(columnsModel1.getType(), "double")) {

                        if (Objects.equals(columnsModel1.getExpression(), "gt")) {
                            System.out.println("going in integer greater than");
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).gt(Integer.parseInt(columnsModel1.getValue()))
                            );

                        } else if (Objects.equals(columnsModel1.getExpression(), "lt")) {
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).lt(Integer.parseInt(columnsModel1.getValue()))
                            );

                        } else if (Objects.equals(columnsModel1.getExpression(), "eq")) {
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).equalTo(Integer.parseInt(columnsModel1.getValue()))
                            );
                        }

                    } else if (Objects.equals(columnsModel1.getType(), "boolean")) {
                        // TODO : Not tested
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).equalTo(columnsModel1.getValue()));
                    } else if (Objects.equals(columnsModel1.getType(), "date")) {
                        // TODO : Not tested
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).equalTo(columnsModel1.getValue()));
                    }

                }
            }

            if (filterModel.getConditions() != null) { // if columns null pass it as it is
                for (ConditionsModel conditionsModel : filterModel.getConditions()) {
                    if (Objects.equals(conditionsModel.getConditionType(), "join")) {
//                        System.out.println(" joining .... sksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllll");
                        Dataset<Row> dfJoin = SparkUtils.getSparkDF(conditionsModel.getDataset(), branch, 300); // TODO branch in future

                        dfTotal = dfTotal.join(dfJoin,
                                dfTotal.col(conditionsModel.getWhere().getSourceColumn())
                                        .equalTo(dfJoin.col(conditionsModel.getWhere().getDestinationColumn())),
                                "inner");

                    }
                    if (Objects.equals(conditionsModel.getConditionType(), "union")) {
//                        System.out.println(" unioning .... sksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllll");
                        Dataset<Row> dfUnion = SparkUtils.getSparkDF(conditionsModel.getDataset(), branch, 300); // TODO branch in future

                        dfTotal = dfTotal.unionAll(dfUnion).distinct();

                    }
                }
            }

            // Example:
            /*
            dataframe1.join(dataframe2,
                dataframe2.col("id_device").equalTo(dataframe1.col("id_device")).
                        and(dataframe2.col("id_vehicule").equalTo(dataframe1.col("id_vehicule"))).
                        and(dataframe2.col("tracking_time").lt(dataframe1.col("tracking_time"))).
                        and(dataframe1.col("diffDate").lt(3888))
                )
                        .orderBy(dataframe2.col("tracking_time").desc())
             */

            if (filterModel.getSort() != null) {
                if (Objects.equals(filterModel.getSort().getDirection(), "desc")) {
                    dfTotal = dfTotal.sort(desc(filterModel.getSort().getColumn()));
                }
                if (Objects.equals(filterModel.getSort().getDirection(), "asc")) {
                    dfTotal = dfTotal.sort(asc(filterModel.getSort().getColumn()));
//                    dfTotal.show();
                }
            }

//        check if word is not null then set numerical to null
//        if word is null and numerical is not null set word to null
//        if less than and equalto are provided then get values
//        and if greater than provided then get values
//        and if equal to provided get values
//        check if rows exists
//        set limit for the rows with number
//        and area to get
//        if 0 get first 500 if 1 get last 1000 and if null get all.

            if (filterModel.getRows() != null) {
                if (filterModel.getRows().getAmount() == 0) {
                    dfTotal = dfTotal.limit(500);
                } else {
                    dfTotal = dfTotal.limit(filterModel.getRows().getAmount());
                }
            }

            df = dfTotal.limit(500);


//            return new ResponseEntity<>(df.toJSON().collectAsList().toString(), HttpStatus.OK);
//        return df.toJSON().collectAsList().toString();

            Map<String, Object> result = new HashMap<>();

            result.put("result", df.toJSON().collectAsList().toString());

            SparkResults sparkResults = new SparkResults();
            sparkResults.setId(resultId);
            sparkResults.setResults(result);

            // call API to send results
            Map<String, String> performed = ApiCaller.callApi("/api/dataset/sparkResults", "POST", System.getenv("ACCESS_TOKEN"), sparkResults, "json");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Something gone wrong " + e.getMessage());
        }
    }
}
