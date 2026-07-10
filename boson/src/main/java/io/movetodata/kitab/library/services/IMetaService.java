package io.movetodata.kitab.library.services;

import io.movetodata.kitab.library.enums.ResourceType;

import java.util.UUID;

public interface IMetaService {
    ResourceType getResourceType();

    Object getMetaData(UUID id);
}
