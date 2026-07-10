package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import io.movetodata.passport.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.spark.sql.functions.call_udf;
import static org.apache.spark.sql.functions.col;

@Service
@Slf4j
public class AbstractChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;

    // Defining the defaultSeriesCustomize object with necessary fields
    static final Map<String, Object> defaultSeriesCustomize = new HashMap<>();
    static {
        defaultSeriesCustomize.put("symbol", "circle");
        defaultSeriesCustomize.put("symbolSize", 10);
        defaultSeriesCustomize.put("lineChartStyle", "linear");
        defaultSeriesCustomize.put("stackedBars", true);
        defaultSeriesCustomize.put("stackedLine", false);
        defaultSeriesCustomize.put("showLabel", false);
        defaultSeriesCustomize.put("labelPosition", "top");
        defaultSeriesCustomize.put("labelRotate", 0);
        defaultSeriesCustomize.put("labelFontSize", 14);
        defaultSeriesCustomize.put("labelFontWeight", 400);
        defaultSeriesCustomize.put("labelPrecision", 2);
        defaultSeriesCustomize.put("labelMode", "auto");
        defaultSeriesCustomize.put("labelScale", "K");
    }

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        JSONObject data = new JSONObject();

        // Group by columns
        ArrayList<Column> cols = new ArrayList<>();

        if (Objects.nonNull(chartDataRequest.getXAxis())) {
            cols.add(col(chartDataRequest.getXAxis()));
        }

        if (Objects.nonNull(chartDataRequest.getDimensions())) {
            cols.addAll(chartDataRequest.getDimensions().stream().map(functions::col).collect(Collectors.toList()));
        }

        Column[] colArray = cols.toArray(new Column[0]);

        Dataset<Row> sparkData = sparkDataMain;

        String xAxisType = sparkData.select(col(chartDataRequest.getXAxis())).schema().fields()[0].dataType().typeName();
        boolean xAxisIsTimeColumn = Stream.of("timestamp", "date").collect(Collectors.toList()).contains(xAxisType);

        if (xAxisIsTimeColumn) {
            sparkData = sparkData.withColumn(chartDataRequest.getXAxis()+"timestamp", col(chartDataRequest.getXAxis()));

            Column dateStrCol = null;
            if (xAxisType.equals("timestamp")) {
                dateStrCol = call_udf("myDateFormat", col(chartDataRequest.getXAxis()));
            } else if (xAxisType.equals("date")) {
                dateStrCol = call_udf("myDateFormat2", col(chartDataRequest.getXAxis()));
            }

            sparkData = sparkData.withColumn(chartDataRequest.getXAxis(), dateStrCol);
        }


        RelationalGroupedDataset groupedDataset = sparkData.groupBy(colArray);

        List<ChartSeriesRequest> chartSeriesRequests = new ArrayList<>(chartDataRequest.getSeries());
        if(Objects.nonNull(chartDataRequest.getCustomAggregateFunction()) && Objects.nonNull(chartDataRequest.getCustomColumnName()) ) {
            ChartSeriesRequest chartSeriesRequest = new ChartSeriesRequest();
            chartSeriesRequest.setAggregate(chartDataRequest.getCustomAggregateFunction());
            chartSeriesRequest.setColumnName(chartDataRequest.getCustomColumnName());

            chartSeriesRequests.add(chartSeriesRequest);
        }

        if(xAxisIsTimeColumn) {
            ChartSeriesRequest csr = new ChartSeriesRequest();
            csr.setAggregate("first");
            csr.setColumnName(chartDataRequest.getXAxis()+"timestamp");

            chartSeriesRequests.add(csr);
            sparkData = this.performAggregate(groupedDataset, chartSeriesRequests);
        } else {
            sparkData = this.performAggregate(groupedDataset, chartSeriesRequests);
        }
        sparkData = this.sortDf(sparkData, xAxisIsTimeColumn, chartDataRequest);

        long totalRows = sparkData.count();
        boolean flagTrimmedData = sparkData.count() > chartDataRequest.getRowLimit();
        sparkData = sparkData.limit(chartDataRequest.getRowLimit());
        long rows = sparkData.count();

        // X Axis Data
        if (Objects.nonNull(chartDataRequest.getXAxis())) {


            List<Object> xAxisData = new ArrayList<>();
            Set<Object> hSet = new HashSet<>();

            for(Row r: sparkData.select(chartDataRequest.getXAxis()).collectAsList()) {
                Object val = r.getAs(chartDataRequest.getXAxis());

                if(!hSet.contains(val)) {
                    hSet.add(val);
                    xAxisData.add(val);
                }
            }
            data.put("xAxisData", xAxisData); //.subList(0, Math.min(5000 + 1, xAxisData.size())));
        } else {
            data.put("xAxisData", new ArrayList<>());
        }

        JSONArray seriesVal = new JSONArray();
        for (ChartSeriesRequest seriesRequest : chartDataRequest.getSeries()) {
            if(Objects.isNull(seriesRequest.getColumnName()) || seriesRequest.getColumnName().isEmpty()) continue;


            Object seriesData = this.convertToAxisData(sparkData, chartDataRequest, seriesRequest, chartDataRequest.getDimensions());

            JSONObject series = new JSONObject();
            series.put("seriesData", seriesData);
            series.put("axisName", this.generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate()));
            series.put("groupBy", chartDataRequest.getDimensions());
            series.put("name", seriesRequest.getSeriesName());
            series.put("id", seriesRequest.getId());
            series.put("seriesId", seriesRequest.getSeriesId());
            series.put("type", seriesRequest.getSeriesType());
            series.put("index", seriesRequest.getSeriesIndex());
            series.put("reversed", seriesRequest.getReversed());
            seriesVal.add(series);
        }


        data.put("series", seriesVal);

        ChartResponse chartResponse = ChartResponse.builder()
                .data(data)
                .rows(rows)
                .trimmedData(flagTrimmedData)
                .totalRows(totalRows)
                .request(chartDataRequest)
                .build();

        // Convert JSONObject to string
        String jsonString = data.toString();

        // Get the length of the string in bytes
        int sizeInBytes = jsonString.getBytes().length;

        // Define the size limit (5MB)
        final int FIVE_MB_IN_BYTES = 5 * 1024 * 1024;

        // Check if the JSONObject size exceeds 5MB
        if (sizeInBytes > FIVE_MB_IN_BYTES) {
            log.error("The JSONObject size exceeds 5MB.");
        } else {
            log.error("The JSONObject size is within the 5MB limit.");
        }

        return chartResponse;
    }


    public static List<Map<String, Object>> syncCustomizeSeries(List<Map<String, Object>> querySeries, List<Map<String, Object>> customizeSeries) {
        Map<String, Map<String, Object>> customizeSeriesObject = new HashMap<>();

        if (customizeSeries != null) {
            for (Map<String, Object> cSeries : customizeSeries) {
                customizeSeriesObject.put((String) cSeries.get("id"), cSeries);
            }
        }

        List<Map<String, Object>> syncedCustomize = new ArrayList<>();
        if (querySeries != null) {
            for (Map<String, Object> qSeries : querySeries) {
                String id = (String) qSeries.get("id");
                if (id != null && customizeSeriesObject.containsKey(id)) {
                    Map<String, Object> combinedSeries = new HashMap<>(defaultSeriesCustomize);
                    combinedSeries.putAll(customizeSeriesObject.get(id));
                    combinedSeries.put("id", id);
                    combinedSeries.put("seriesName", qSeries.get("seriesName"));
                    combinedSeries.put("seriesType", qSeries.get("seriesType"));
                    syncedCustomize.add(combinedSeries);
                } else {
                    Map<String, Object> combinedSeries = new HashMap<>(defaultSeriesCustomize);
                    combinedSeries.put("id", id);
                    combinedSeries.put("seriesName", qSeries.get("seriesName"));
                    combinedSeries.put("seriesType", qSeries.get("seriesType"));
                    syncedCustomize.add(combinedSeries);
                }
            }
        }

        return syncedCustomize;
    }

    @Override
    public String getChartServiceType() {
        return "default";
    }
}
