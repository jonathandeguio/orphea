export enum CsvHeaderOptionsEnum {
  NONE = "NONE",
  SKIPFIRSTLINE = "SKIPFIRSTLINE",
  CUSTOM = "CUSTOM",
}

export enum CsvEscapeOptionsEnum {
  NONE = "NONE",
  BACKSLASH = "BACKSLASH",
  DOUBLE_QUOTE = "DOUBLE_QUOTE",
  CUSTOM = "CUSTOM",
}

export enum CsvQuoteOptionsEnum {
  NONE = "NONE",
  SINGLE_QUOTE = "SINGLE_QUOTE",
  DOUBLE_QUOTE = "DOUBLE_QUOTE",
}

export enum CsvDelimiterOptionsEnum {
  NONE = "NONE",
  COMMA = "COMMA",
  TAB = "TAB",
  SEMICOLON = "SEMICOLON",
  SPACE = "SPACE",
  CUSTOM = "CUSTOM",
}

export enum CsvLineDelimiterOptionsEnum {
  NEWLINE = "NEWLINE",
  CUSTOM = "CUSTOM",
}

export enum CsvTrimSpaceOptionsEnum {
  TRUE = "TRUE",
  FALSE = "FALSE",
}

export enum CsvReplaceInvalidCharsEnum {
  TRUE = "TRUE",
  FALSE = "FALSE",
}

export enum CsvDateEnum {
  AUTO = "AUTO",
  CUSTOM = "CUSTOM",
}

export enum CsvTimeEnum {
  AUTO = "AUTO",
  CUSTOM = "CUSTOM",
}

export enum CsvTimestampEnum {
  AUTO = "AUTO",
  CUSTOM = "CUSTOM",
}

export interface ICsvPreprocessing {
  firstLineAsHeader: boolean;
  header: keyof typeof CsvHeaderOptionsEnum;
  customHeader?: string;
  fieldDelimiter: keyof typeof CsvDelimiterOptionsEnum;
  customFieldDelimiter?: string;
  lineDelimiter: keyof typeof CsvLineDelimiterOptionsEnum;
  customLineDelimiter?: string;
  trimSpace: keyof typeof CsvTrimSpaceOptionsEnum;
  replaceInvalidChars: keyof typeof CsvReplaceInvalidCharsEnum;
  dateFormat: keyof typeof CsvDateEnum;
  customDateFormat?: string;
  timeFormat: keyof typeof CsvTimeEnum;
  customTimeFormat?: string;
  timestampFormat: keyof typeof CsvTimestampEnum;
  customTimestampFormat?: string;
  quote: keyof typeof CsvQuoteOptionsEnum;
  escape: keyof typeof CsvEscapeOptionsEnum;
  customEscape?: string;
}
