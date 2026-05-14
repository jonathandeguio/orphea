package io.orphea.dataset.library.chartfactory;

import io.orphea.dataset.library.DTOs.ChartResponse;
import io.orphea.dataset.library.services.SparkDataService;
import io.orphea.dataset.requests.ChartDataRequest;
import io.orphea.dataset.requests.ChartSeriesRequest;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.spark.sql.functions.col;

@Service
public class WordCloudChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        if(chartDataRequest.getSeries().isEmpty())
            return new ChartResponse();

        ChartSeriesRequest seriesRequest = chartDataRequest.getSeries().get(0);

        Column[] colArray = chartDataRequest.getDimensions().stream().map(functions::col).toArray(Column[]::new);

        RelationalGroupedDataset groupedDataset = sparkDataMain.groupBy(colArray);

        List<ChartSeriesRequest> chartSeriesRequests = new ArrayList<>();
        chartSeriesRequests.add(seriesRequest);
        if(Objects.nonNull(chartDataRequest.getCustomAggregateFunction()) && Objects.nonNull(chartDataRequest.getCustomColumnName()) ) {
            ChartSeriesRequest chartSeriesRequest = new ChartSeriesRequest();
            chartSeriesRequest.setAggregate(chartDataRequest.getCustomAggregateFunction());
            chartSeriesRequest.setColumnName(chartDataRequest.getCustomColumnName());

            chartSeriesRequests.add(chartSeriesRequest);
        }
        sparkDataMain = this.performAggregate(groupedDataset, chartSeriesRequests);
        sparkDataMain = this.sortDf(sparkDataMain, false, chartDataRequest);
        long totalCount = sparkDataMain.count();
        boolean flagTrimmedData = sparkDataMain.count() > chartDataRequest.getRowLimit();
        sparkDataMain = sparkDataMain.limit(chartDataRequest.getRowLimit());

        chartDataRequest.setXAxis(null);
        Object seriesData = this.convertToAxisData(sparkDataMain, chartDataRequest, seriesRequest, chartDataRequest.getDimensions());

        return ChartResponse.builder()
                .data(seriesData)
                .rows(sparkDataMain.count())
                .totalRows(totalCount)
                .trimmedData(flagTrimmedData)
                .request(chartDataRequest)
                .build();
    }

    @Override
    public String getChartServiceType() {
        return "wordCloudChart";
    }
}
