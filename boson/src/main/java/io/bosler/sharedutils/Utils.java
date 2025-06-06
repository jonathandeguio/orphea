package io.bosler.sharedutils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.bosler.dataset.library.DTOs.DatasetFiltersDTO;
import io.bosler.dataset.library.DTOs.FilterDTO;
import io.bosler.kepler.library.models.DatasetFilterModel;
import io.bosler.kepler.library.models.FilterModel;
import io.bosler.kepler.library.repository.FilterModelRepository;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.sharedutils.DTO.DateFormatDTO;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}}?$");

    private Utils() {
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

    public static String getFileResourceSubType(String fileName) {
        if (Objects.nonNull(fileName)) {
            String extension = getExtension(fileName);

            try {
                return ResourceSubtype.valueOf(extension.toUpperCase()).toString();
            } catch (Exception ignored) {
                return ResourceSubtype.FILE.toString();
            }
        } else {
            return ResourceSubtype.NONE.toString();
        }
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

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
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

    public static boolean isMySQLTableNameValid(String str) {
        final String VALID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_$]*$";
        final Pattern pattern = Pattern.compile(VALID_PATTERN);

        return pattern.matcher(str).matches();
    }

    public static boolean isSnowflakeTableNameValid(String str) {
        final String VALID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*$";
        final Pattern pattern = Pattern.compile(VALID_PATTERN);

        return pattern.matcher(str).matches();
    }

    public static Charset detectCharset(byte[] bytes) {

        Set<String> ALLOWED_ENCODINGS = new HashSet<>(Arrays.asList(
                "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"));

        CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);

        CharsetMatch[] matches = detector.detectAll();
        CharsetMatch bestMatch = Arrays.stream(matches)
                .max((match1, match2) -> Integer.compare(match1.getConfidence(), match2.getConfidence()))
                .orElse(null);

        if (bestMatch != null && ALLOWED_ENCODINGS.contains(bestMatch.getName())) {
            return Charset.forName(bestMatch.getName());
        } else {
            return StandardCharsets.UTF_8;
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

    public static DatasetFiltersDTO convertDatasetFilterModelToDatasetFilterDTO(DatasetFilterModel filterModel) {
        List<FilterDTO> ls = new ArrayList<>();

        for (io.bosler.kepler.library.models.FilterModel obj : filterModel.getFilters()) {
            FilterDTO abc = new FilterDTO(obj.getId(), obj.getKey(), obj.getOperator(), obj.getValue());
            ls.add(abc);
        }

        return (new DatasetFiltersDTO(filterModel.getDatasetId(), filterModel.getKey(), filterModel.getColumnName(), filterModel.getColumnType(), filterModel.getLogicalOperator(), ls));
    }

    public static DatasetFilterModel convertDatasetFilterDTOToDatasetFilterModel(DatasetFiltersDTO filtersDTO, FilterModelRepository repo) {
        List<FilterModel> ls = new ArrayList<>();

        for (FilterDTO obj : filtersDTO.getFilters()) {
            FilterModel abc = new FilterModel(obj.getId(), obj.getKey(), obj.getOperator(), obj.getValue());
            ls.add(abc);
            repo.save(abc);
        }

        return (new DatasetFilterModel(filtersDTO.getColumnName(), filtersDTO.getColumnType(), filtersDTO.getKey(), filtersDTO.getDatasetId(), filtersDTO.getLogicalOperator(), ls));
    }

    public static DateFormatDTO detectDateFormat(String dateString) {
        String[] possibleFormats = {
                "dd-MMM-yyyy hh:mm a",
                "yyyy-MM-dd",
                "MM/dd/yyyy",
                "dd-MM-yyyy",
                "MM-dd-yyyy",
                "yyyy/MM/dd",
                "dd/MM/yyyy",
                "MM.dd.yyyy",
                "yyyy.MM.dd",
                "MMM dd, yyyy",
                "dd MMM yyyy",
                "yyyy.MM.dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "EEE, dd MMM yyyy HH:mm:ss Z",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "HH:mm:ss dd-MM-yyyy",
                "yyyy-MM-dd HH:mm",
                "MM/dd/yyyy HH:mm",
                "dd-MM-yyyy HH:mm",
                "HH:mm dd-MM-yyyy",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "dd-MM-yyyy HH:mm:ss.SSS",
                "HH:mm:ss.SSS dd-MM-yyyy",
                "yyyy-MM-dd HH:mm:ss.SSSZ",
                "dd-MM-yyyy HH:mm:ss.SSSZ",
                "HH:mm:ss.SSSZ dd-MM-yyyy",
                "MMM dd, yyyy HH:mm:ss a",
                "yyyy/MM/dd HH:mm:ss a",
                "dd-MM-yyyy HH:mm:ss a",
                "hh:mm a dd-MM-yyyy",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "dd-MMM-yyyy HH:mm:ss",
                "MMMM dd, yyyy",
                "dd MMMM yyyy",
                "E, dd MMM yyyy HH:mm:ss Z",
                "E, dd MMM yyyy HH:mm:ss a",
                "yyyyMMdd",
                "MMddyyyy",
                "ddMMyyyy",
                "ddMMMyyyy",
                "MMyyyy",
                "yyyyMM",
                "yyMMdd",
                "MMMM dd yyyy",
                "dd MMM yyyy HH:mm:ss Z",
                "dd MMM yyyy HH:mm:ss a",
                "dd MMM yyyy HH:mm:ss",
                "dd MMM yyyy HH:mm",
                "dd/MM/yyyy HH:mm:ss a",
                "HH:mm:ss a dd/MM/yyyy",
                "HH:mm dd/MM/yyyy",
                "yyyy.MM.dd'T'HH:mm:ss'Z'",
                "MM/dd/yyyy HH:mm:ss a",
                "dd-MMM-yyyy HH:mm:ss a",
                "MMM dd, yyyy hh:mm:ss a",
                "MMMM dd, yyyy hh:mm:ss a",
                "hh:mm:ss a dd-MMM-yyyy",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'",
                "yyyyMMddHHmmss",
                "dd MMMM yyyy HH:mm:ss",
                "dd MMMM yyyy HH:mm",
                "dd MMM yyyy HH:mm:ss",
                "dd MMM yyyy HH:mm",
                "dd MMMM yyyy",
                "dd MMM yyyy",
                "dd-MM-yyyy HH:mm:ss",
                "dd-MM-yyyy HH:mm",
                "dd-MM-yyyy",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy",
                "dd.MM.yyyy HH:mm:ss",
                "dd.MM.yyyy HH:mm",
                "dd.MM.yyyy",
                "d MMMM yyyy HH:mm:ss",
                "d MMMM yyyy HH:mm",
                "d MMM yyyy HH:mm:ss",
                "d MMM yyyy HH:mm",
                "d MMMM yyyy",
                "d MMM yyyy",
                "d-MM-yyyy HH:mm:ss",
                "d-MM-yyyy HH:mm",
                "d-MM-yyyy",
                "d/MM/yyyy HH:mm:ss",
                "d/MM/yyyy HH:mm",
                "d/MM/yyyy",
                "d.MM.yyyy HH:mm:ss",
                "d.MM.yyyy HH:mm",
                "d.MM.yyyy",
                "dd MMMM yyyy HH:mm:ss a",
                "dd MMMM yyyy HH:mm a",
                "dd MMM yyyy HH:mm:ss a",
                "dd MMM yyyy HH:mm a",
                "dd MMMM yyyy a",
                "dd MMM yyyy a",
                "dd-MM-yyyy HH:mm:ss a",
                "dd-MM-yyyy HH:mm a",
                "dd-MM-yyyy a",
                "dd/MM/yyyy HH:mm:ss a",
                "dd/MM/yyyy HH:mm a",
                "dd/MM/yyyy a",
                "dd.MM.yyyy HH:mm:ss a",
                "dd.MM.yyyy HH:mm a",
                "dd.MM.yyyy a",
                "d MMMM yyyy HH:mm:ss a",
                "d MMMM yyyy HH:mm a",
                "d MMM yyyy HH:mm:ss a",
                "d MMM yyyy HH:mm a",
                "d MMMM yyyy a",
                "d MMM yyyy a",
                "d. MMMM yyyy HH:mm:ss a",
                "d. MMMM yyyy HH:mm a",
                "d. MMM yyyy HH:mm a",
                "d. MMMM yyyy a",
                "d. MMM yyyy a",
                "d-MM-yyyy HH:mm:ss a",
                "d-MM-yyyy HH:mm a",
                "d-MM-yyyy a",
                "d/MM/yyyy HH:mm:ss a",
                "d/MM/yyyy HH:mm a",
                "d/MM/yyyy a",
                "d.MM.yyyy HH:mm:ss a",
                "d.MM.yyyy HH:mm a",
                "d.MM.yyyy a",
                "d. MMM yy HH:mm:ss a",
                "d. MMM yy HH:mm a",
                "d. MMM yy a",
                "d-MM-yyyy HH:mm:ss a",
                "d-MM-yyyy HH:mm a",
                "d-MM-yyyy a",
                "d/MM/yyyy HH:mm:ss a",
                "d/MM/yyyy HH:mm a",
                "d/MM/yyyy a",
                "d.MM.yyyy HH:mm:ss a",
                "d.MM.yyyy HH:mm a",
                "d.MM.yyyy a",
                "d. MMMM yy HH:mm:ss a",
                "d. MMMM yy HH:mm a",
                "d. MMMM yy a",
                "dd/MM/yy HH:mm:ss a",
                "yyyy.MM.dd HH:mm a",
                "dd.MM.yy HH:mm a",
                "d. MMMM yy HH:mm:ss a",
                "d. MMMM yy HH:mm a",
                "d. MMMM yy a",
                "d-MM-yy HH:mm:ss a",
                "d-MM-yy HH:mm a",
                "d-MM-yy a",
                "d/MM/yy HH:mm:ss a",
                "d/MM/yy HH:mm a",
                "d/MM/yy a",
                "d.MM.yy HH:mm:ss a",
                "d.MM.yy HH:mm a",
                "d.MM.yy a",
                "d-MM-yy HH:mm:ss a Z",
                "d-MM-yy HH:mm a Z",
                "d-MM-yy a Z",
                "d/MM/yy HH:mm:ss a Z",
                "d/MM/yy HH:mm a Z",
                "d/MM/yy a Z",
                "d.MM.yy HH:mm:ss a Z",
                "d.MM.yy HH:mm a Z",
                "d.MM.yy a Z",
                "d. MMMM yy HH:mm:ss a Z",
                "d. MMMM yy HH:mm a Z",
                "d. MMMM yy a Z",
                "dd/MM/yy hh:mm:ss a",
                "yyyy.MM.dd HH:mm:ss a",
                "dd.MM.yy hh:mm a",
                "d. MMMM yy HH:mm:ss a Z",
                "d. MMMM yy HH:mm a Z",
                "d. MMMM yy a Z",
                "d-MM-yy hh:mm:ss a Z",
                "d-MM-yy hh:mm a Z",
                "d-MM-yy a Z",
                "d/MM/yy hh:mm:ss a Z",
                "d/MM/yy hh:mm a Z",
                "d/MM/yy a Z",
                "d.MM.yy hh:mm:ss a Z",
                "d.MM.yy hh:mm a Z",
                "d.MM.yy a Z",
                "d. MMMM yy hh:mm:ss a Z",
                "d. MMMM yy",
                "yyyy-dd-MMM hh:mm a"
        };

        Arrays.sort(possibleFormats, Collections.reverseOrder());

        for (Locale locale : Locale.getAvailableLocales()) {
            for (String format : possibleFormats) {
                SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
                sdf.setLenient(false);

                try {
                    Date date = sdf.parse(dateString);
                    if (date != null) {
                        return DateFormatDTO.builder().format(format).locale(locale).build();
                    }
                } catch (Exception e) {
                    // Parsing failed for the current format, try the next one
                }
            }
        }
        return null;
    }

    public static String getCharacterEncoding() {
        // Creating and initializing byte array
        // with some random character say it N

        // Here N = w
        byte[] byteArray = {'w'};

        // Creating an object of inputStream
        InputStream instream
                = new ByteArrayInputStream(byteArray);

        // Now, opening new file input stream reader
        InputStreamReader streamreader
                = new InputStreamReader(instream);

        return streamreader.getEncoding();
    }

    public static String[] convertBackStringifyArr(String input) {
        Gson gson = new Gson();
        return gson.fromJson(input, String[].class);
    }

    public static String removeFileExtenstion(String input) {
        try {
            int lastDotIndex = input.lastIndexOf('.');
            return input.substring(0, lastDotIndex);
        } catch (Exception e) {
            return input;
        }
    }

    public static ResourceSubtype getFileExtensionFromName(String input) {
        try {
            String[] parts = input.split("\\.");
            return ResourceSubtype.valueOf(parts[parts.length - 1].toUpperCase());
        } catch (Exception e) {
            return ResourceSubtype.NONE;
        }
    }

    public static void waitForFileUnlock(Path filePath) {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            filePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            boolean fileUnlocked = false;

            while (!fileUnlocked) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedPath = (Path) event.context();

                    if (changedPath.equals(filePath.getFileName())) {
                        // File has been modified, check if it's still being accessed
                        if (!Files.isReadable(filePath)) {
                            fileUnlocked = true;
                        }
                    }
                }

                key.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static boolean matchesPattern(String pattern, String input) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(input).find();
    }

    public static class LanguageQuality {
        public String language;
        public double quality;

        public LanguageQuality(String language, double quality) {
            this.language = language;
            this.quality = quality;
        }
    }

}
