package io.orphea.kitab.library.services;

import io.orphea.kitab.library.enums.ResourceType;

import java.util.UUID;

public interface IMetaService {
    ResourceType getResourceType();

    Object getMetaData(UUID id);
}
