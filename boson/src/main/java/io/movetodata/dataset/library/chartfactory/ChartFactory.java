package io.movetodata.dataset.library.chartfactory;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ChartFactory {

    private final List<IChartService> chartServiceList;

    private static Map<String, IChartService> chartServiceMap = new HashMap<>(4);

    public ChartFactory(List<IChartService> chartServiceList) {
        System.out.println(Objects.nonNull(chartServiceList.size()));
        System.out.println(chartServiceList.size());
        this.chartServiceList = chartServiceList;
    }

    @PostConstruct
    void init() {
        for (IChartService chartService : chartServiceList) {
            chartServiceMap.put(chartService.getChartServiceType(), chartService);
        }
    }

    public IChartService getChartService(String chartType) {
        if (chartServiceMap.containsKey(chartType)) return chartServiceMap.get(chartType);
        return chartServiceMap.get("default");
    }
}
