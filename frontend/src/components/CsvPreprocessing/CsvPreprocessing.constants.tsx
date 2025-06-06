import {
  CsvDateEnum,
  CsvDelimiterOptionsEnum,
  CsvEscapeOptionsEnum,
  CsvHeaderOptionsEnum,
  CsvLineDelimiterOptionsEnum,
  CsvQuoteOptionsEnum,
  CsvReplaceInvalidCharsEnum,
  CsvTimeEnum,
  CsvTimestampEnum,
  CsvTrimSpaceOptionsEnum,
} from "./CsvPreprocessing.types";

export function getInitialValues() {
  return {
    firstLineAsHeader: true,
    header: CsvHeaderOptionsEnum.NONE,
    fieldDelimiter: CsvDelimiterOptionsEnum.COMMA,
    lineDelimiter: CsvLineDelimiterOptionsEnum.NEWLINE,
    trimSpace: CsvTrimSpaceOptionsEnum.TRUE,
    replaceInvalidChars: CsvReplaceInvalidCharsEnum.TRUE,
    dateFormat: CsvDateEnum.AUTO,
    timeFormat: CsvTimeEnum.AUTO,
    timestampFormat: CsvTimestampEnum.AUTO,
    quote: CsvQuoteOptionsEnum.DOUBLE_QUOTE,
    escape: CsvEscapeOptionsEnum.DOUBLE_QUOTE,
  };
}

export const getHeaderOptions = () => {
  return [
    { label: "Don't skip", value: CsvHeaderOptionsEnum.NONE },
    {
      label: "Skip first line",
      value: CsvHeaderOptionsEnum.SKIPFIRSTLINE,
    },
    {
      label: "Custom",
      value: CsvHeaderOptionsEnum.CUSTOM,
    },
  ];
};

export const getFirstLineAsHeaderOptions = () => {
  return [
    { label: "True", value: true },
    {
      label: "False",
      value: false,
    },
  ];
};

export const getFieldDelimiterOptions = () => {
  return [
    { label: "None", value: CsvDelimiterOptionsEnum.NONE },
    {
      label: "Comma",
      value: CsvDelimiterOptionsEnum.COMMA,
    },
    {
      label: "Tab",
      value: CsvDelimiterOptionsEnum.TAB,
    },
    {
      label: "Semicolon",
      value: CsvDelimiterOptionsEnum.SEMICOLON,
    },
    {
      label: "Space",
      value: CsvDelimiterOptionsEnum.SPACE,
    },
    {
      label: "Custom",
      value: CsvDelimiterOptionsEnum.CUSTOM,
    },
  ];
};

export const getLineDelimiterOptions = () => {
  return [
    {
      label: "New Line",
      value: CsvLineDelimiterOptionsEnum.NEWLINE,
    },
    {
      label: "Custom",
      value: CsvLineDelimiterOptionsEnum.CUSTOM,
    },
  ];
};

export const getEscapeOptions = () => {
  return [
    { label: "None", value: CsvEscapeOptionsEnum.NONE },
    {
      label: "Back Slash",
      value: CsvEscapeOptionsEnum.BACKSLASH,
    },
    {
      label: "Double Quote",
      value: CsvEscapeOptionsEnum.DOUBLE_QUOTE,
    },
    {
      label: "Custom",
      value: CsvEscapeOptionsEnum.CUSTOM,
    },
  ];
};

export const getQuoteOptions = () => {
  return [
    { label: "None", value: CsvQuoteOptionsEnum.NONE },
    {
      label: "Single Quote",
      value: CsvQuoteOptionsEnum.SINGLE_QUOTE,
    },
    {
      label: "Double Quote",
      value: CsvQuoteOptionsEnum.DOUBLE_QUOTE,
    },
  ];
};

export const getTrimSpaceOptions = () => {
  return [
    { label: "True", value: CsvTrimSpaceOptionsEnum.TRUE },
    {
      label: "False",
      value: CsvTrimSpaceOptionsEnum.FALSE,
    },
  ];
};

export const getReplaceInvalidCharsOptions = () => {
  return [
    { label: "True", value: CsvReplaceInvalidCharsEnum.TRUE },
    {
      label: "False",
      value: CsvReplaceInvalidCharsEnum.FALSE,
    },
  ];
};

export const getDateFormatOptions = () => {
  return [
    { label: "Auto", value: CsvDateEnum.AUTO },
    // {
    //   label: "Custom",
    //   value: CsvDateEnum.CUSTOM,
    // },
  ];
};

export const getTimeFormatOptions = () => {
  return [
    { label: "Auto", value: CsvTimeEnum.AUTO },
    // {
    //   label: "Custom",
    //   value: CsvTimeEnum.CUSTOM,
    // },
  ];
};

export const getTimestampFormatOptions = () => {
  return [
    { label: "Auto", value: CsvTimestampEnum.AUTO },
    // {
    //   label: "Custom",
    //   value: CsvTimestampEnum.CUSTOM,
    // },
  ];
};
