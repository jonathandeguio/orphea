package io.bosler.connect.library.Serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import io.bosler.connect.library.models.FolderLinkConfig;
import io.bosler.connect.library.models.SharepointLinkConfig;
import io.bosler.connect.library.models.Webhook;
import io.bosler.connect.library.services.FolderLinkConfigService;
import io.bosler.connect.library.services.SharepointLinkConfigService;
import io.bosler.connect.library.services.WebhookService;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.services.ResourceService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

@Service
public class LinkConfigSerializer<T> extends JsonSerializer<T> {
    private final FolderLinkConfigService folderLinkConfigService;
    private final SharepointLinkConfigService sharepointLinkConfigService;
    private final WebhookService webhookService;
    private final ResourceService resourceService;

    public LinkConfigSerializer(FolderLinkConfigService folderLinkConfigService, SharepointLinkConfigService sharepointLinkConfigService, ResourceService resourceService, WebhookService webhookService) {
        this.folderLinkConfigService = folderLinkConfigService;
        this.sharepointLinkConfigService = sharepointLinkConfigService;
        this.resourceService = resourceService;
        this.webhookService = webhookService;
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

            UUID linkConfigId = (UUID) bean.getClass().getDeclaredMethod("getLinkConfigId").invoke(bean);
            String type = (String) bean.getClass().getDeclaredMethod("getType").invoke(bean);

            if (linkConfigId != null) {
                if (Objects.equals(type.toLowerCase(), "sharepoint")) {
                    SharepointLinkConfig sharepointLinkConfig = sharepointLinkConfigService.findById(linkConfigId);
                    gen.writeStringField("fileId", sharepointLinkConfig.getFileId());
                    gen.writeStringField("fileType", String.valueOf(sharepointLinkConfig.getFileType()));
                    gen.writeStringField("sheetName", sharepointLinkConfig.getSheetName());

                } else if (Objects.equals(type.toLowerCase(), "folder")) {
                    FolderLinkConfig folderLinkConfig = folderLinkConfigService.findById(linkConfigId);
                    gen.writeStringField("subFolder", folderLinkConfig.getSubFolder());
                } else if (Objects.equals(type, "rest")) {
                    Webhook webhook = webhookService.findById(linkConfigId);
                    gen.writeObjectField("requests", webhook.getRequests());
                    gen.writeObjectField("responseParam", webhook.getResponseParam());
                    gen.writeObjectField("csvPreprocessing", webhook.getCsvPreprocessing());
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        gen.writeEndObject();
    }
}
