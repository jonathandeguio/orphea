package io.movetodata.dataset.library.chartfactory;

import io.movetodata.dataset.library.DTOs.ChartResponse;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import io.movetodata.passport.exception.BadRequestException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
public class BigNumberChartService implements IChartService {
    @Autowired
    SparkDataService sparkDataService;
    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        if(Objects.isNull(chartDataRequest.getSeries()) || chartDataRequest.getSeries().isEmpty()) {
            throw new BadRequestException("Expected at least one series");
        }

        ChartSeriesRequest seriesRequest = chartDataRequest.getSeries().get(0);
        if (seriesRequest.getColumnName() == null || seriesRequest.getAggregate() == null) {
            return new ChartResponse();
        }

        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        HashMap<String, String> aggMap = new HashMap<>();
        aggMap.put(seriesRequest.getColumnName(), seriesRequest.getAggregate());
        sparkDataMain = sparkDataMain.agg(aggMap);

        Object bigNumber = sparkDataMain.collectAsList().get(0).getAs(0);

        return ChartResponse.builder()
                .data(bigNumber)
                .rows(sparkDataMain.count())
                .request(chartDataRequest)
                .trimmedData(false).build();
    }

    @Override
    public String getChartServiceType() {
        return "bigNumber";
    }
}
