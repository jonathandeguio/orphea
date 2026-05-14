package io.orphea.dataset.library.services.Spark;

import io.orphea.dataset.library.DTOs.CsvPreprocessingDTO;
import io.orphea.dataset.library.enums.CsvPreprocessing.*;
import io.orphea.dataset.library.models.CustomSchemaModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.nio.charset.Charset;
import java.util.HashMap;

@Slf4j
@Service
@AllArgsConstructor
public class CsvPreprocessingService {
    private final SparkSession sparkSession;

    private HashMap<String, Object> getDefaultOptions() {
        HashMap<String, Object> defaultOptions = new HashMap<>();
        defaultOptions.put("header", true);
        defaultOptions.put("format", "csv");
        defaultOptions.put("delimiter", ",");
        defaultOptions.put("quote", "\"");
        defaultOptions.put("escape", "\"");
        defaultOptions.put("multiline", true);
        defaultOptions.put("ignoreLeadingWhiteSpace", true);
        defaultOptions.put("encoding", Charset.defaultCharset().name());

        return defaultOptions;
    }

    private Dataset<String> processSkippingRows(Dataset<String> csvData, CsvHeaderOptionsEnum header, String customValue) {
        int skipRows;
        if (header.equals(CsvHeaderOptionsEnum.SKIPFIRSTLINE)) {
            skipRows = 1;
        } else if (header.equals(CsvHeaderOptionsEnum.CUSTOM)) {
            skipRows = Integer.parseInt(customValue);
        } else {
            skipRows = 0;
        }

        if (skipRows <= 0) {
            return csvData;
        }

        JavaRDD<String> filteredRDD = csvData
                .javaRDD()
                .zipWithIndex()
                .filter((Tuple2<String, Long> tuple) -> tuple._2 >= skipRows) // Skip the specified number of rows
                .map(Tuple2::_1); // Extract the original row (tuple._1)

        return sparkSession.createDataset(filteredRDD.rdd(), Encoders.STRING());
    }

    private void processQuote(CsvQuoteOptionsEnum quote, HashMap<String, Object> options) {
        if (quote.equals(CsvQuoteOptionsEnum.SINGLE_QUOTE)) {
            options.put("quote", "'");
        } else if (quote.equals(CsvQuoteOptionsEnum.DOUBLE_QUOTE)) {
            options.put("quote", "\"");
        } else if (quote.equals(CsvQuoteOptionsEnum.NONE)) {
            options.put("quote", "");
        } else {
            throw new IllegalArgumentException("Unsupported csv quote option: " + quote);
        }
    }

    private String getQuote(CsvQuoteOptionsEnum quote) {
        if (quote.equals(CsvQuoteOptionsEnum.SINGLE_QUOTE)) {
            return "'";
        } else if (quote.equals(CsvQuoteOptionsEnum.DOUBLE_QUOTE)) {
            return "\"";
        } else if (quote.equals(CsvQuoteOptionsEnum.NONE)) {
            return "";
        } else {
            throw new IllegalArgumentException("Unsupported csv quote option: " + quote);
        }
    }

    private void processEscape(CsvEscapeOptionsEnum escape, String customValue, HashMap<String, Object> options) {
        if (escape.equals(CsvEscapeOptionsEnum.CUSTOM)) {
            options.put("escape", customValue);
        } else if (escape.equals(CsvEscapeOptionsEnum.BACKSLASH)) {
            options.put("escape", "\\");
        } else if (escape.equals(CsvEscapeOptionsEnum.DOUBLE_QUOTE)) {
            options.put("escape", "\"");
        } else if (escape.equals(CsvEscapeOptionsEnum.NONE)) {
            options.put("escape", "");
        } else {
            throw new IllegalArgumentException("Unsupported csv escape option: " + escape);
        }
    }

    private String getEscape(CsvEscapeOptionsEnum escape, String customValue) {
        if (escape.equals(CsvEscapeOptionsEnum.CUSTOM)) {
            return customValue;
        } else if (escape.equals(CsvEscapeOptionsEnum.BACKSLASH)) {
            return "\\";
        } else if (escape.equals(CsvEscapeOptionsEnum.DOUBLE_QUOTE)) {
            return "\"";
        } else if (escape.equals(CsvEscapeOptionsEnum.NONE)) {
            return "";
        } else {
            throw new IllegalArgumentException("Unsupported csv escape option: " + escape);
        }
    }

    private void processHeader(CsvHeaderOptionsEnum header, String customValue, HashMap<String, Object> options) {
        if (header.equals(CsvHeaderOptionsEnum.NONE)) {
            options.put("skipRows", 0);
        } else if (header.equals(CsvHeaderOptionsEnum.SKIPFIRSTLINE)) {
            options.put("skipRows", 1);
        } else if (header.equals(CsvHeaderOptionsEnum.CUSTOM)) {
            options.put("skipRows", Integer.parseInt(customValue));
        } else {
            throw new IllegalArgumentException("Unsupported csv header option: " + header);
        }
    }

    private void processFirstLineAsHeader(boolean header, HashMap<String, Object> options) {
        options.put("header", header);
    }

    private void processTrimSpace(CsvTrimSpaceOptionsEnum trim, HashMap<String, Object> options) {
        if (trim.equals(CsvTrimSpaceOptionsEnum.TRUE)) {
            options.put("ignoreLeadingWhiteSpace", true);
        } else if (trim.equals(CsvTrimSpaceOptionsEnum.FALSE)) {
            options.put("ignoreLeadingWhiteSpace", false);
        } else {
            throw new IllegalArgumentException("Unsupported csv trim space option: " + trim);
        }
    }

    private boolean getTrimSpace(CsvTrimSpaceOptionsEnum trim) {
        if (trim.equals(CsvTrimSpaceOptionsEnum.TRUE)) {
            return true;
        } else if (trim.equals(CsvTrimSpaceOptionsEnum.FALSE)) {
            return false;
        } else {
            throw new IllegalArgumentException("Unsupported csv trim space option: " + trim);
        }
    }

    private void processLineDelimiter(CsvLineDelimiterOptionsEnum delimiter, String customValue, HashMap<String, Object> options) {
        if (delimiter.equals(CsvLineDelimiterOptionsEnum.NEWLINE)) {
            options.put("lineSep", "\n");
        } else if (delimiter.equals(CsvLineDelimiterOptionsEnum.CUSTOM)) {
            if (customValue == null || customValue.isEmpty()) {
                throw new IllegalArgumentException("Custom line delimiter value cannot be null or empty");
            }
            options.put("delimiter", customValue);
        } else {
            throw new IllegalArgumentException("Unsupported csv line delimiter option: " + delimiter);
        }
    }

    private String getLineDelimiter(CsvLineDelimiterOptionsEnum delimiter, String customValue) {
        if (delimiter.equals(CsvLineDelimiterOptionsEnum.NEWLINE)) {
            return "\n";
        } else if (delimiter.equals(CsvLineDelimiterOptionsEnum.CUSTOM)) {
            if (customValue == null || customValue.isEmpty()) {
                throw new IllegalArgumentException("Custom line delimiter value cannot be null or empty");
            }
            return customValue;
        } else {
            throw new IllegalArgumentException("Unsupported csv line delimiter option: " + delimiter);
        }
    }

    private void processDelimiter(CsvDelimiterOptionsEnum delimiter, String customValue, HashMap<String, Object> options) {
        if (delimiter.equals(CsvDelimiterOptionsEnum.COMMA)) {
            options.put("delimiter", ",");
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.TAB)) {
            options.put("delimiter", "\t");
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.SEMICOLON)) {
            options.put("delimiter", ";");
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.SPACE)) {
            options.put("delimiter", " ");
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.NONE)) {
            options.put("delimiter", "");
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.CUSTOM)) {
            if (customValue == null || customValue.isEmpty()) {
                throw new IllegalArgumentException("Custom delimiter value cannot be null or empty");
            }
            options.put("delimiter", customValue);
        } else {
            throw new IllegalArgumentException("Unsupported csv delimiter option: " + delimiter);
        }
    }

    private String getDelimiter(CsvDelimiterOptionsEnum delimiter, String customValue) {
        if (delimiter.equals(CsvDelimiterOptionsEnum.COMMA)) {
            return ",";
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.TAB)) {
            return "\t";
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.SEMICOLON)) {
            return ";";
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.SPACE)) {
            return " ";
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.NONE)) {
            return "";
        } else if (delimiter.equals(CsvDelimiterOptionsEnum.CUSTOM)) {
            if (customValue == null || customValue.isEmpty()) {
                throw new IllegalArgumentException("Custom delimiter value cannot be null or empty");
            }
            return customValue;
        } else {
            throw new IllegalArgumentException("Unsupported csv delimiter option: " + delimiter);
        }
    }

    private void processReplaceInvalidChars(CsvReplaceInvalidCharsEnum invalidChars, HashMap<String, Object> options) {
        if (invalidChars.equals(CsvReplaceInvalidCharsEnum.TRUE)) {
            options.put("mode", "PERMISSIVE");
        } else if (invalidChars.equals(CsvReplaceInvalidCharsEnum.FALSE)) {
            options.put("mode", "PERMISSIVE");
        } else {
            throw new IllegalArgumentException("Unsupported replaceInvalidChars option: " + invalidChars);
        }
    }

    private String getReplaceInvalidChars(CsvReplaceInvalidCharsEnum invalidChars) {
        if (invalidChars.equals(CsvReplaceInvalidCharsEnum.TRUE)) {
            return "PERMISSIVE";
        } else if (invalidChars.equals(CsvReplaceInvalidCharsEnum.FALSE)) {
            return "PERMISSIVE";
        } else {
            throw new IllegalArgumentException("Unsupported replaceInvalidChars option: " + invalidChars);
        }
    }

    private void processTime(CsvTimeEnum time, String customValue, HashMap<String, Object> options) {
        if (time.equals(CsvTimeEnum.AUTO)) {
            // Handled by default
        } else if (time.equals(CsvTimeEnum.CUSTOM)) {
            // Handle value
        } else {
            throw new IllegalArgumentException("Unsupported time option: " + time);
        }

    }

    private void processDate(CsvDateEnum date, String customValue, HashMap<String, Object> options) {
        if (date.equals(CsvDateEnum.AUTO)) {
            // Handled by default
        } else if (date.equals(CsvDateEnum.CUSTOM)) {
            // Handle value
        } else {
            throw new IllegalArgumentException("Unsupported date option: " + date);
        }

    }

    private void processTimestamp(CsvTimestampEnum timestamp, String customValue, HashMap<String, Object> options) {
        if (timestamp.equals(CsvTimestampEnum.AUTO)) {
            // Handled by default
        } else if (timestamp.equals(CsvTimestampEnum.CUSTOM)) {
            // Handle value
        } else {
            throw new IllegalArgumentException("Unsupported timestamp option: " + timestamp);
        }

    }

    public void getCustomSchemaFromCsvPreprocessing(CustomSchemaModel customSchema, CsvPreprocessingDTO csvPreprocessingModel) {
        customSchema.setHeader(csvPreprocessingModel.isFirstLineAsHeader());
        customSchema.setFieldDelimiter(getDelimiter(csvPreprocessingModel.getFieldDelimiter(), csvPreprocessingModel.getCustomFieldDelimiter()));
        customSchema.setEscapeCharacter(getEscape(csvPreprocessingModel.getEscape(), csvPreprocessingModel.getCustomEscape()));
        customSchema.setQuote(getQuote(csvPreprocessingModel.getQuote()));
        customSchema.setLineDelimiter(getLineDelimiter(csvPreprocessingModel.getLineDelimiter(), csvPreprocessingModel.getCustomLineDelimiter()));
    }

    public Dataset<Row> getProcessedCsv(Dataset<String> csvData, CsvPreprocessingDTO csvPreprocessing) {
        HashMap<String, Object> options = getDefaultOptions();
        csvData = processSkippingRows(csvData, csvPreprocessing.getHeader(), csvPreprocessing.getCustomHeader());
        processFirstLineAsHeader(csvPreprocessing.isFirstLineAsHeader(), options);
        processQuote(csvPreprocessing.getQuote(), options);
        processEscape(csvPreprocessing.getEscape(), csvPreprocessing.getCustomEscape(), options);
        processHeader(csvPreprocessing.getHeader(), csvPreprocessing.getCustomHeader(), options);
        processTrimSpace(csvPreprocessing.getTrimSpace(), options);
        processDelimiter(csvPreprocessing.getFieldDelimiter(), csvPreprocessing.getCustomFieldDelimiter(), options);
        processReplaceInvalidChars(csvPreprocessing.getReplaceInvalidChars(), options);
        processTime(csvPreprocessing.getTimeFormat(), csvPreprocessing.getCustomTimeFormat(), options);
        processDate(csvPreprocessing.getDateFormat(), csvPreprocessing.getCustomDateFormat(), options);
        processTimestamp(csvPreprocessing.getTimestampFormat(), csvPreprocessing.getCustomTimestampFormat(), options);

        org.apache.spark.sql.DataFrameReader reader = sparkSession.read();

        for (HashMap.Entry<String, Object> entry : options.entrySet()) {
            reader.option(entry.getKey(), entry.getValue().toString());
        }

        return reader.format("csv").csv(csvData);
    }
}
