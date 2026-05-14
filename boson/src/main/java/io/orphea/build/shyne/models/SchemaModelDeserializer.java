package io.orphea.build.shyne.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.orphea.dataset.library.models.SchemaModel;

import java.io.IOException;

public class SchemaModelDeserializer extends StdDeserializer<SchemaModel> {

    public SchemaModelDeserializer() {
        this(null);
    }

    public SchemaModelDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SchemaModel deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        // Implement custom deserialization logic here
        // You'll need to manually parse the JsonParser's current token or structure
        // and construct a SchemaModel instance from it.
        JsonNode node = jp.getCodec().readTree(jp);
        // Example of manual parsing:
        // String index = node.get("index").asText();
        // Return a new SchemaModel instance based on parsed data
        return new SchemaModel();
    }
}
