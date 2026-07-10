package io.movetodata.sharedutils.Serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.factory.MetaFactory;
import io.movetodata.kitab.library.services.IMetaService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ResourceModelSerializer<T> extends JsonSerializer<T> {
    private static final Logger logger = LoggerFactory.getLogger(ResourceModelSerializer.class);
    private final MetaFactory metaFactory;

    @Autowired
    public ResourceModelSerializer(MetaFactory metaFactory) {
        this.metaFactory = metaFactory;
    }

    @Override
    public void serialize(T bean, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            JavaType javaType = serializers.constructType(bean.getClass());
            BeanDescription beanDesc = serializers.getConfig().introspect(javaType);
            JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanOrAddOnSerializer(serializers, javaType, beanDesc, false);
            serializer.unwrappingSerializer(null).serialize(bean, gen, serializers);

            ResourceType resourceType = (ResourceType) bean.getClass().getDeclaredMethod("getType").invoke(bean);
            UUID id = (UUID) bean.getClass().getDeclaredMethod("getId").invoke(bean);

            IMetaService iMetaService = metaFactory.getMetaService(resourceType);
            if (Objects.nonNull(iMetaService)) {
                gen.writeObjectField("metaData", iMetaService.getMetaData(id));
            } else {
                gen.writeObjectField("metaData", null);
            }

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            logger.error(e.getMessage());
        }

        gen.writeEndObject();
    }
}
