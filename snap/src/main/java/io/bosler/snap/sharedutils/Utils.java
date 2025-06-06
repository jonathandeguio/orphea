package io.bosler.snap.sharedutils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}}?$");

    private Utils() {
    }

    public static class LanguageQuality {
        public String language;
        public double quality;

        public LanguageQuality(String language, double quality) {
            this.language = language;
            this.quality = quality;
        }
    }

    public static String removeFileExtenstion(String input) {
        String[] parts = input.split("\\.");

        int lastDotIndex = input.lastIndexOf('.');
        return input.substring(0, lastDotIndex);
    }
    public static List<LanguageQuality> parseAcceptLanguage(String header) {
        List<LanguageQuality> languages = new ArrayList<>();

        // Split the header by commas to get individual language-sets
        String[] languageSets = header.split(",");

        for (String languageSet : languageSets) {
            // Trim spaces and split by semicolon to get language and optional quality factor
            String[] parts = languageSet.trim().split(";");

            String language = parts[0].trim();
            double quality = 1.0; // Default quality is 1.0 if not specified

            if (parts.length > 1 && parts[1].startsWith("q=")) {
                try {
                    quality = Double.parseDouble(parts[1].substring(2).trim());
                } catch (NumberFormatException e) {
                    // If parsing fails, fall back to default quality
                    quality = 1.0;
                }
            }

            languages.add(new LanguageQuality(language, quality));
        }

        // Sort the list by quality in descending order
        Collections.sort(languages, new Comparator<LanguageQuality>() {
            @Override
            public int compare(LanguageQuality o1, LanguageQuality o2) {
                return Double.compare(o2.quality, o1.quality);
            }
        });

        return languages;
    }


    public static <T> T jacksonStringParser(@NotNull String json, TypeReference<T> typeReference) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        return objectMapper.readValue(json, typeReference);
    }

    public static String getFileNameExtensionTrimmed(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }


    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        return Arrays.stream(pds).parallel().map(FeatureDescriptor::getName).filter(name -> Objects.isNull(src.getPropertyValue(name))).toArray(String[]::new);
    }

    public static boolean isBase64(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String decodeBase64(String base64Query) {
        return new String(Base64.getDecoder().decode(base64Query));
    }

    public static byte[] decodeBase64Tobytes(String str) {
        return Base64.getDecoder().decode(str);
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String removeLineBreaks(String query) {
        return query.replaceAll("[\\r\\n]", " ");
    }


    public static boolean isValidUuid(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    public static boolean isValidUuid2(String input) {
        // Implement your UUID validation logic here
        // Example: Check if the input is a valid UUID
        return input.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    public static boolean isLikelyPath(String input) {
        return input.contains("/") || input.contains("\\") || input.startsWith("/");
    }

    public static boolean isPostgresTableNameValid(String str) {
        final String VALID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9 _-]*$";
        final Pattern pattern = Pattern.compile(VALID_PATTERN);

        return pattern.matcher(str).matches();
    }

    public static boolean isDevelopment() {
        if (System.getenv("ENVIRONMENT") == null) {
            return false;

        } else {
            return System.getenv("ENVIRONMENT").equals("development");
        }
    }



    public static String normalizeName(String name) {
        String normalizedFileName = name;

        // Remove any leading or trailing underscores
        normalizedFileName = normalizedFileName.replaceAll("^_+|_+$", "");

        // Remove the characters you want to block
        normalizedFileName = normalizedFileName.replaceAll("[/\\\\:;%$&^#£@?~,*+\"<>|'`]", "");

        return normalizedFileName;
    }


    public static Pageable getPageRequest(Integer page, Integer size, List<String> sort) {
        List<Sort.Order> orderList = new ArrayList<>();

        if (sort != null) {
            for (String sortString : sort) {
                if (sortString.contains(",")) {
                    String[] sortWithDir = sortString.split(",");
                    if (sortWithDir.length > 1) {
                        String sortField = sortWithDir[0];
                        String sortDir = sortWithDir[1];

                        Sort.Direction direction = Sort.Direction.fromString(sortDir);
                        orderList.add(new Sort.Order(direction, sortField));
                    }
                }
            }
        }
        if (page != null && size != null)
            return PageRequest.of(page, size, Sort.by(orderList));

        return PageRequest.of(0, 250, Sort.by(orderList));
    }

    public static Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();

        // Set the time to 12:00:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

}
