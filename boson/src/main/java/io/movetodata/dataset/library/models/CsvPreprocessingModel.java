package io.movetodata.dataset.library.models;

import io.movetodata.dataset.library.enums.CsvPreprocessing.*;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "csv_preprocessing")
public class CsvPreprocessingModel {
    boolean firstLineAsHeader = true;
    String customHeader;
    String customFieldDelimiter;
    String customLineDelimiter;
    String customDateFormat;
    String customTimeFormat;
    String customTimestampFormat;
    String customEscape;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Enumerated(EnumType.STRING)
    private CsvHeaderOptionsEnum header = CsvHeaderOptionsEnum.NONE;
    @Enumerated(EnumType.STRING)
    private CsvDelimiterOptionsEnum fieldDelimiter = CsvDelimiterOptionsEnum.COMMA;
    @Enumerated(EnumType.STRING)
    private CsvLineDelimiterOptionsEnum lineDelimiter = CsvLineDelimiterOptionsEnum.NEWLINE;
    @Enumerated(EnumType.STRING)
    private CsvTrimSpaceOptionsEnum trimSpace = CsvTrimSpaceOptionsEnum.TRUE;
    @Enumerated(EnumType.STRING)
    private CsvReplaceInvalidCharsEnum replaceInvalidChars = CsvReplaceInvalidCharsEnum.TRUE;
    @Enumerated(EnumType.STRING)
    private CsvDateEnum dateFormat = CsvDateEnum.AUTO;
    @Enumerated(EnumType.STRING)
    private CsvTimeEnum timeFormat = CsvTimeEnum.AUTO;
    @Enumerated(EnumType.STRING)
    private CsvTimestampEnum timestampFormat = CsvTimestampEnum.AUTO;

    @Enumerated(EnumType.STRING)
    private CsvQuoteOptionsEnum quote;

    @Enumerated(EnumType.STRING)
    private CsvEscapeOptionsEnum escape = CsvEscapeOptionsEnum.DOUBLE_QUOTE;
}
