package io.orphea.dataset.library.chartfactory;

import io.orphea.dataset.library.DTOs.ChartResponse;
import io.orphea.dataset.library.services.SparkDataService;
import io.orphea.dataset.requests.ChartDataRequest;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapChartService implements IChartService{
    @Autowired
    SparkDataService sparkDataService;

    @Override
    public String getChartServiceType() {
        return "mapChart";
    }

    @Override
    public ChartResponse getChartData(ChartDataRequest chartDataRequest, String locale) throws Exception {
        Dataset<Row> sparkDataMain = sparkDataService.getFilteredSparkDataframe(chartDataRequest.getDatasetId(), chartDataRequest.getBranch(), String.valueOf(chartDataRequest.getTransactionId()), chartDataRequest.getXAxisTimeGrain(), chartDataRequest.getFilter(), -1, locale);

        if (chartDataRequest.getLongitude() == null || chartDataRequest.getLatitude() == null) {
            return new ChartResponse();
        }

        return ChartResponse.builder()
                .data(this.convertToMapData(sparkDataMain))
                .rows(sparkDataMain.count())
                .request(chartDataRequest).build();
    }

    private JSONArray convertToMapData(Dataset<Row> sparkData) {
        List<String> cols = Arrays.stream(sparkData.schema().fields()).map(f -> f.name()).collect(Collectors.toList());

        Seq<String> colSeq = JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq();

        JSONArray resultantDataArray = new JSONArray();
        for (Row r : sparkData.collectAsList()) {
            resultantDataArray.add(new JSONObject(JavaConverters.mapAsJavaMapConverter(r.getValuesMap(colSeq)).asJava()));
        }
        return resultantDataArray;
    }

}
