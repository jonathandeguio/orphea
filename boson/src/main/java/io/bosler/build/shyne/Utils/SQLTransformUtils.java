package io.bosler.build.shyne.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.shyne.models.ResourcePathOrId;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bosler.build.shyne.Utils.Utils.*;

public class SQLTransformUtils {


    public static Map<String, List<ResourcePathOrId>> resolveDatasets(BufferedReader reader, StringBuilder content, String tempPath, BuildType buildType) throws Exception {
        ShyneLogging logger = new ShyneLogging();

        List<ResourcePathOrId> sources = new ArrayList<>();
        List<ResourcePathOrId> targets = new ArrayList<>();
        Pattern commentPattern = Pattern.compile("(?m)^\\s*--.*");

        String line;
        while ((line = reader.readLine()) != null) {

//            line = commentPattern.matcher(line).replaceAll("").replaceFirst("^\\s*", "").trim();  // remove comments

            String targetDatasetPathOrId = getTargetDataset(line); // get Logical path from the line if it is there ... like /Projects/Test/Data/dataset
            if (targetDatasetPathOrId != null) {

                String viewName = "target" + getViewName(targetDatasetPathOrId);

                // below needs adding into SQL otherwise it does not work with sparkSQL
                line = line.replace("`" + targetDatasetPathOrId + "`",
                        "`" + viewName + "`" + "\n USING parquet \n OPTIONS ('path' '" +
                                tempPath + "')");

                targets.add(new ResourcePathOrId(viewName, targetDatasetPathOrId));
            } else {
//                line = commentPattern.matcher(line).replaceAll("");  // remove comments

                Matcher sourceMatcher = getSourceMatcher(line);

                while (sourceMatcher.find()) {

                    String sourceDatasetPathOrId = sourceMatcher.group(1);
                    String viewName = getViewName(sourceDatasetPathOrId);
                    line = line.replace(sourceDatasetPathOrId, viewName);

                    // This is to avoid adding multiple resources from where statements etc
                    boolean resourceExists = false;
                    for (ResourcePathOrId resourcePathOrId : sources) {
                        if (Objects.equals(resourcePathOrId.getResource(), sourceDatasetPathOrId)) {
                            resourceExists = true;
                            break;
                        }
                    }

                    if (!resourceExists) {
                        sources.add(new ResourcePathOrId(viewName, sourceDatasetPathOrId));
                    }
                }
            }

            content.append(line).append("\n");
        }

        Map<String, List<ResourcePathOrId>> result = new HashMap<>();

        result.put("targets", targets);
        result.put("sources", sources);

        return result;
    }

    public static <T> T jacksonStringParser(@NotNull String json, TypeReference<T> typeReference) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        return objectMapper.readValue(json, typeReference);
    }

}
