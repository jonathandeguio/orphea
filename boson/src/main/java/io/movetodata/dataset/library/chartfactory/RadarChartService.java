package io.movetodata.dataset.library.chartfactory;

import com.google.common.collect.ImmutableMap;
import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RadarChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        if (Objects.isNull(chartDataRequest.getSeries()) || chartDataRequest.getSeries().isEmpty() || Objects.isNull(chartDataRequest.getDimensions()) || chartDataRequest.getDimensions().isEmpty())
            return new ChartResponse();

        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        Column[] colArray = chartDataRequest.getDimensions().stream()
                .filter(column -> Objects.nonNull(column) && !column.isEmpty())
                .map(functions::col).toArray(Column[]::new);

        RelationalGroupedDataset groupedDataset = sparkDataMain.groupBy(colArray);

        List<ChartSeriesRequest> chartSeriesRequests = new ArrayList<>(chartDataRequest.getSeries());
        if(Objects.nonNull(chartDataRequest.getCustomAggregateFunction()) && Objects.nonNull(chartDataRequest.getCustomColumnName()) ) {
            ChartSeriesRequest chartSeriesRequest = new ChartSeriesRequest();
            chartSeriesRequest.setAggregate(chartDataRequest.getCustomAggregateFunction());
            chartSeriesRequest.setColumnName(chartDataRequest.getCustomColumnName());

            chartSeriesRequests.add(chartSeriesRequest);
        }

        Dataset<Row> sparkData = this.performAggregate(groupedDataset, chartSeriesRequests);
        sparkData = this.sortDf(sparkData, false, chartDataRequest);

        boolean flagTrimmedData = sparkData.count() > chartDataRequest.getRowLimit();
        long totalRows = sparkData.count();
        sparkData = sparkData.limit(chartDataRequest.getRowLimit());

        List<Map<String, Object>> seriesList = new ArrayList<>();

        for(ChartSeriesRequest seriesRequest : chartDataRequest.getSeries()) {
            Map<String, Object> seriesDataMap = new HashMap<>();

            Object seriesData = this.convertToAxisData(sparkData, chartDataRequest, seriesRequest, chartDataRequest.getDimensions());
            seriesDataMap.put(this.generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate()), seriesData);

            seriesList.add(Map.of("seriesData", seriesDataMap, "id", seriesRequest.getId(), "groupBy", chartDataRequest.getDimensions(), "seriesName", seriesRequest.getSeriesName()));
        }

        List<String> statsColumns = chartDataRequest.getSeries().stream()
                .filter(s -> !s.getAggregate().equals("none"))
                .map(s -> this.generateColumnName(s.getColumnName(), s.getAggregate())).collect(Collectors.toList());

        return ChartResponse.builder()
                .data(Map.of("series", seriesList))
                .rows(sparkData.count())
                .totalRows(totalRows)
                .request(chartDataRequest)
                .trimmedData(flagTrimmedData)
                .stats(this.getColumnStats(sparkData, statsColumns))
                .build();
    }

    @Override
    public String getChartServiceType() {
        return "radarChart";
    }
}
