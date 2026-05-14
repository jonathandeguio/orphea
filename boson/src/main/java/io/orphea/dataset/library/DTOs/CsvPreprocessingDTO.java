package io.orphea.dataset.library.DTOs;

import io.orphea.dataset.library.enums.CsvPreprocessing.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvPreprocessingDTO {
    boolean firstLineAsHeader;
    CsvHeaderOptionsEnum header;
    String customHeader;
    CsvDelimiterOptionsEnum fieldDelimiter;
    String customFieldDelimiter;
    CsvTrimSpaceOptionsEnum trimSpace;
    CsvReplaceInvalidCharsEnum replaceInvalidChars;
    CsvDateEnum dateFormat;
    String customDateFormat;
    CsvTimeEnum timeFormat;
    String customTimeFormat;
    CsvTimestampEnum timestampFormat;
    String customTimestampFormat;

    CsvQuoteOptionsEnum quote;
    CsvEscapeOptionsEnum escape;
    String customEscape;

    CsvLineDelimiterOptionsEnum lineDelimiter;
    String customLineDelimiter;
}
