package io.bosler.sharedutils.Serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.services.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Service
@Slf4j
public class ResourceSerializer<T> extends JsonSerializer<T> {
    private final ResourceService resourceService;
    private static final Logger logger = LoggerFactory.getLogger(ResourceSerializer.class);

    @Autowired
    public ResourceSerializer(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public void serialize(T bean, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            JavaType javaType = serializers.constructType(bean.getClass());
            BeanDescription beanDesc = serializers.getConfig().introspect(javaType);
            JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanOrAddOnSerializer(serializers, javaType, beanDesc, false);
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

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error(e.getMessage());
        }

        gen.writeEndObject();
    }
}
