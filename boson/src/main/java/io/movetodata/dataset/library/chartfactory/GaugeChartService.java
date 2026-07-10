package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import io.movetodata.passport.exception.BadRequestException;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class GaugeChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;
    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        if(Objects.isNull(chartDataRequest.getSeries()) || chartDataRequest.getSeries().isEmpty()) {
            throw new BadRequestException("Expected at least one series");
        }

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

        Object seriesData = this.convertToDimensionData(sparkData, chartDataRequest, true);

        Dataset<Row> data = this.performAggregate(sparkDataMain.groupBy(), chartDataRequest.getSeries());
        return ChartResponse.builder()
                .data(seriesData)
                .trimmedData(flagTrimmedData)
                .request(chartDataRequest)
                .rows(sparkData.count())
                .totalRows(totalRows)
                .stats(this.convertToDimensionData(data, chartDataRequest, false)).build();
    }

    @Override
    public String getChartServiceType() {
        return "gaugeChart";
    }
}
