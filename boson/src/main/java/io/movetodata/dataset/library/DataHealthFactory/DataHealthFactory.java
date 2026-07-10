package io.movetodata.dataset.library.DataHealthFactory;

import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataHealthFactory {
    private static List<IDataHealth> dataHealthList;
    private final Map<DataHealthTypeEnum, IDataHealth> dataHealthMap = new HashMap<>(4);

    public DataHealthFactory(List<IDataHealth> dataHealthList) {
        DataHealthFactory.dataHealthList = dataHealthList;
    }

    @PostConstruct
    void init() {
        for (IDataHealth dataHealth : dataHealthList) {
            dataHealthMap.put(dataHealth.getDataHealthType(), dataHealth);
        }
    }

    public IDataHealth getDataHealthService(DataHealthTypeEnum dataHealthType) throws Exception {
        if (dataHealthMap.containsKey(dataHealthType)) return dataHealthMap.get(dataHealthType);
        throw new Exception("Data Health type not found");
    }
}
