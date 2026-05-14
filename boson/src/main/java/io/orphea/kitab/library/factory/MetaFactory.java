package io.orphea.kitab.library.factory;

import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.services.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetaFactory {
    private final List<IMetaService> metaServices;

    Map<ResourceType, IMetaService> metaServiceMap;

    @Autowired
    public MetaFactory(List<IMetaService> metaServices) {
        this.metaServices = metaServices;
    }

    @PostConstruct
    public void init() {
        metaServiceMap = new HashMap<>();
        for (IMetaService metaService : metaServices) {
            metaServiceMap.put(metaService.getResourceType(), metaService);
        }
    }

    public IMetaService getMetaService(ResourceType resourceType) {
        return metaServiceMap.get(resourceType);
    }
}
