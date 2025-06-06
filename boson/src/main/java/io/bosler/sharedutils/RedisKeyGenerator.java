package io.bosler.sharedutils;

import io.bosler.dataset.requests.ChartDataRequest;

import java.util.UUID;

public class RedisKeyGenerator {
    public static String chart(ChartDataRequest request) {
        return "chartData" + request.getDatasetId() + request.getBranch() + request.getChartUUID();
    }

    public static String preview(UUID previewId) {
        return "previewResult" + previewId;
    }
}
