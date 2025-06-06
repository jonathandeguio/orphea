package io.bosler.connect.library.Serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import io.bosler.connect.library.models.*;
import io.bosler.connect.library.repository.RestSourceConfigRepository;
import io.bosler.connect.library.services.DatabaseSourceConfigService;
import io.bosler.connect.library.services.FolderSourceConfigService;
import io.bosler.connect.library.services.SharepointConfigService;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.services.ResourceService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SourceConfigSerializer<T> extends JsonSerializer<T> {
    private final DatabaseSourceConfigService databaseSourceConfigService;
    private final FolderSourceConfigService folderSourceConfigService;
    private final SharepointConfigService sharepointConfigService;
    private final ResourceService resourceService;
    private final RestSourceConfigRepository restSourceConfigRepository;

    public SourceConfigSerializer(DatabaseSourceConfigService databaseSourceConfigService,
                                  FolderSourceConfigService folderSourceConfigService,
                                  RestSourceConfigRepository restSourceConfigRepository,
                                  SharepointConfigService sharepointConfigService,
                                  ResourceService resourceService) {
        this.databaseSourceConfigService = databaseSourceConfigService;
        this.folderSourceConfigService = folderSourceConfigService;
        this.resourceService = resourceService;
        this.sharepointConfigService = sharepointConfigService;
        this.restSourceConfigRepository = restSourceConfigRepository;
    }


    @Override
    public void serialize(T bean, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            JavaType javaType = serializers.constructType(bean.getClass());
            BeanDescription beanDesc = serializers.getConfig().introspect(javaType);
            JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(serializers, javaType, beanDesc);
            // this is basically your 'writeAllFields()'-method:
            serializer.unwrappingSerializer(null).serialize(bean, gen, serializers);

            UUID id = (UUID) bean.getClass().getDeclaredMethod("getId").invoke(bean);
            ResourceModel model = resourceService.findById(id).orElseThrow();
            gen.writeStringField("name", model.getName());
            gen.writeStringField("parent", model.getParent().toString());
            gen.writeStringField("project", model.getProject().toString());
            gen.writeStringField("createdAt", model.getCreatedAt().toString());
            gen.writeStringField("createdBy", model.getCreatedBy().toString());
            gen.writeStringField("updatedAt", model.getUpdatedAt().toString());
            gen.writeStringField("updatedBy", model.getUpdatedBy().toString());

            UUID sourceConfigId = (UUID) bean.getClass().getDeclaredMethod("getSourceConfig").invoke(bean);
            String type = (String) bean.getClass().getDeclaredMethod("getType").invoke(bean);

            if (Objects.equals(type.toLowerCase(), "jdbc")) {
                DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceConfigId);
                gen.writeStringField("dbmsType", String.valueOf(databaseSourceConfig.getDbmsType()));
                gen.writeStringField("username", databaseSourceConfig.getUsername());
//                gen.writeStringField("password", databaseSourceConfig.getPassword());
                gen.writeStringField("database", databaseSourceConfig.getDatabase());
                gen.writeStringField("server", databaseSourceConfig.getServer());
                gen.writeStringField("port", String.valueOf(databaseSourceConfig.getPort()));

                gen.writeStringField("authType", String.valueOf(databaseSourceConfig.getAuthType()));
                gen.writeStringField("privateKey", String.valueOf(databaseSourceConfig.getPrivateKey()));
                gen.writeStringField("privateKeyPassPhrase", String.valueOf(databaseSourceConfig.getPrivateKeyPassPhrase()));
                gen.writeStringField("userRole", String.valueOf(databaseSourceConfig.getUserRole()));

                gen.writeStringField("warehouse", String.valueOf(databaseSourceConfig.getWarehouse()));
                gen.writeStringField("schema", String.valueOf(databaseSourceConfig.getSchema()));

            } else if (Objects.equals(type.toLowerCase(), "sharepoint")) {
                SharePointSourceConfig sharePointSourceConfig = sharepointConfigService.findById(sourceConfigId);
                gen.writeStringField("clientId", sharePointSourceConfig.getClientId());
                gen.writeStringField("clientSecret", sharePointSourceConfig.getClientSecret());
                gen.writeStringField("tenantId", sharePointSourceConfig.getTenantId());
                gen.writeStringField("url", sharePointSourceConfig.getUrl());
//                gen.writeStringField("server", sharePointSourceConfig.getServer());
//                gen.writeStringField("port", String.valueOf(sharePointSourceConfig.getPort()));

            } else if (Objects.equals(type.toLowerCase(), "rest")) {
                RestAPISourceConfig restAPISourceConfig = restSourceConfigRepository.findById(sourceConfigId).get();
                List<RestAPISourceDomain> domains = restAPISourceConfig.getDomains();
                gen.writeObjectField("domains", domains);
            } else {
                FolderSourceConfig folderSourceConfig = folderSourceConfigService.findById(sourceConfigId);
                gen.writeStringField("path", folderSourceConfig.getPath());
            }

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        gen.writeEndObject();
    }
}
