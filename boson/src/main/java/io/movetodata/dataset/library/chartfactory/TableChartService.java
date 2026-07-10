package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

@Service
public class TableChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;

    @Override
    public String getChartServiceType() {
        return "table";
    }

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        JSONObject tableData = new JSONObject();

        chartDataRequest.setSeries(chartDataRequest.getSeries().stream().filter(seriesRequest -> seriesRequest.getColumnName() != null && seriesRequest.getAggregate() != null).collect(Collectors.toList()));

        ArrayList<String> aggregateCols = new ArrayList<>();
        ArrayList<Column> groupByCols = new ArrayList<>();
        ArrayList<String> cols = new ArrayList<>();
        HashMap<String, String> aggMap = new HashMap<>();


       for (ChartSeriesRequest seriesRequest : chartDataRequest.getSeries()) {
            if (seriesRequest.getColumnName() != null && seriesRequest.getAggregate() != null) {
                if (!seriesRequest.getAggregate().equals("none")) {
                    aggregateCols.add(generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate()));
                    aggMap.put(seriesRequest.getColumnName(), seriesRequest.getAggregate());
                    cols.add(generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate()));
                } else {
                    groupByCols.add(col(seriesRequest.getColumnName()));
                    cols.add(seriesRequest.getColumnName());
                }
            }
        }

        if (!aggregateCols.isEmpty()) {
            Column[] colArray = groupByCols.toArray(new Column[0]);
            RelationalGroupedDataset groupedDataset = sparkDataMain.groupBy(colArray);
            sparkDataMain = this.performAggregate(groupedDataset, chartDataRequest.getSeries());
        } else {
            ArrayList<Column> colList = new ArrayList<>();
            for (String c : cols)
                colList.add(col(c));
            Column[] colArray = colList.toArray(new Column[0]);
            sparkDataMain = sparkDataMain.select(colArray);
        }

        boolean flagTrimmedData = sparkDataMain.count() > chartDataRequest.getRowLimit();

        List<Column> sortColumns = new ArrayList<>();
        for (ChartSeriesRequest seriesRequest : chartDataRequest.getSeries()) {
            if(Objects.nonNull(seriesRequest.getSort()) && !seriesRequest.getSort().isEmpty()) {
                sortColumns.add("desc".equals(seriesRequest.getSort()) ? col(generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate())).desc(): col(generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate())).asc());
            }
        }
        sparkDataMain = sparkDataMain.sort(sortColumns.toArray(Column[]::new));
        long totalCount = sparkDataMain.count();
        sparkDataMain = sparkDataMain.limit(chartDataRequest.getRowLimit());

        // collect data
        JSONArray resultantDataArray = new JSONArray();
        Seq<String> colSeq = JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq();
        for (Row r : sparkDataMain.collectAsList()) {
            Map<String, Object> mapObj = JavaConverters.mapAsJavaMapConverter(r.getValuesMap(colSeq)).asJava();

            JSONObject obj = new JSONObject();
            mapObj.keySet().forEach(key -> obj.put(key, String.valueOf(mapObj.get(key))));
            resultantDataArray.add(obj);
        }

        // Generate Columns
        JSONArray columns = new JSONArray();
        org.apache.spark.sql.types.StructField[] fields = sparkDataMain.schema().fields();
        for (int i = 0; i < fields.length; i++) {

            JSONObject colObj = new JSONObject();
            colObj.put("name", fields[i].name());
            colObj.put("type", fields[i].dataType().typeName());

            if(!fields[i].dataType().equals(DataTypes.DateType) && !fields[i].dataType().equals(DataTypes.StringType) && !fields[i].dataType().equals(DataTypes.BooleanType)) {
                Dataset<Row> columnData = sparkDataMain.agg(max(fields[i].name()), min(fields[i].name()), avg(fields[i].name()), sum(fields[i].name()));
                if (aggregateCols.contains(fields[i].name())) {
                    for (Row r : columnData.collectAsList()) {
                        colObj.put("max", r.get(0));
                        colObj.put("min", r.get(1));
                        colObj.put("avg", r.get(2));
                        colObj.put("sum", r.get(3));
                    }
                }
            }

            columns.add(colObj);
        }
        // --------------

        tableData.put("columns", columns);
        tableData.put("tableData", resultantDataArray);
        tableData.put("aggregateCols", aggregateCols);

        return ChartResponse.builder()
                .data(tableData)
                .request(chartDataRequest)
                .totalRows(totalCount)
                .trimmedData(flagTrimmedData)
                .rows(sparkDataMain.count()).build();
    }
}
