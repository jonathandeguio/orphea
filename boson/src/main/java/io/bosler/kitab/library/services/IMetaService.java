package io.bosler.kitab.library.services;

import io.bosler.kitab.library.enums.ResourceType;

import java.util.UUID;

public interface IMetaService {
    ResourceType getResourceType();

    Object getMetaData(UUID id);
}
