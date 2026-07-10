package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TreeMapChartService  implements IChartService {
    @Autowired
    SparkDataService sparkDataService;
    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        if(chartDataRequest.getDimensions().isEmpty())
            return ChartResponse.builder()
                    .rows(0)
                    .request(chartDataRequest)
                    .build();

        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        if(chartDataRequest.getSeries().isEmpty())
            return new ChartResponse();

        ChartSeriesRequest seriesRequest = chartDataRequest.getSeries().get(0);

        // Group by columns
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
        boolean flagTrimmedData =  sparkDataMain.count() > chartDataRequest.getRowLimit();
        long totalCount = sparkDataMain.count();
        sparkDataMain = sparkDataMain.limit(chartDataRequest.getRowLimit());

        Object seriesData = this.convertToTabularData(sparkDataMain);

        return ChartResponse.builder()
                .data(seriesData)
                .rows(sparkDataMain.count())
                .totalRows(totalCount)
                .stats(this.generateColumnName(seriesRequest.getColumnName(), seriesRequest.getAggregate()))
                .trimmedData(flagTrimmedData)
                .request(chartDataRequest)
                .build();
    }

    @Override
    public String getChartServiceType() {
        return "treeMapChart";
    }
}
