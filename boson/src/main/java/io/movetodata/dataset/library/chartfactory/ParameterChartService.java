package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.kepler.library.models.ParametersModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ParameterChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), "noProcess", new ArrayList<>(), -1, locale);

        if (chartDataRequest.getParameters().isEmpty())
            return new ChartResponse();

        ChartResponse chartResponse = ChartResponse.builder().rows(0).cachedData(false).request(chartDataRequest).build();
        HashMap<String, Object> chartData = new HashMap<>();


        for (ParametersModel parameter : chartDataRequest.getParameters()) {
            HashMap<String, Object> columnData = new HashMap<>();
            String label = parameter.getLabel();
            String column = parameter.getColumn();

            if (label != null && column != null) {
                chartResponse.setRows(1);

                DataType columnType = sparkDataMain.schema().apply(column).dataType();

                columnData.put("type", columnType);
                columnData.put("label", label);
                columnData.put("column", column);

                if (columnType.equals(DataTypes.StringType)) {
                    List<String> values = new ArrayList<>();
                    for (Row row : sparkDataMain.select(column).distinct().collectAsList()) {
                        values.add((String) row.get(0));
                    }
                    columnData.put("values", values);
                } else {
                    Dataset<Row> df = sparkDataMain.agg(functions.min(sparkDataMain.col(column)).alias("min"), functions.max(sparkDataMain.col(column)).alias("max"));
                    Object max = df.select("max").collectAsList().get(0).get(0);
                    Object min = df.select("min").collectAsList().get(0).get(0);

                    columnData.put("min", min);
                    columnData.put("max", max);
                }

                chartData.put(column, columnData);
            }
        }

        chartResponse.setData(chartData);
        return chartResponse;
    }

    @Override
    public String getChartServiceType() {
        return "parameterChart";
    }
}
