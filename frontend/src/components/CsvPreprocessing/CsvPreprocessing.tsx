import { Form, Input, Select } from "antd";
import FormItemContainer from "components/CommonUI/FormItemContainer";
import React, { useEffect } from "react";
import { getLanguageLabel } from "utils/utilities";
import {
  getDateFormatOptions,
  getEscapeOptions,
  getFieldDelimiterOptions,
  getFirstLineAsHeaderOptions,
  getHeaderOptions,
  getInitialValues,
  getLineDelimiterOptions,
  getQuoteOptions,
  getReplaceInvalidCharsOptions,
  getTimeFormatOptions,
  getTimestampFormatOptions,
  getTrimSpaceOptions,
} from "./CsvPreprocessing.constants";
import { ICsvPreprocessing } from "./CsvPreprocessing.types";
interface IProps {
  initialValues?: ICsvPreprocessing | undefined;
  onValuesChange: (
    values: ICsvPreprocessing,
    allValues: ICsvPreprocessing
  ) => void;
}

const CsvPreprocessing = ({ initialValues, onValuesChange }: IProps) => {
  const [form] = Form.useForm();
  const header = Form.useWatch(["header"], form);
  const fieldDelimiter = Form.useWatch(["fieldDelimiter"], form);
  const dateFormat = Form.useWatch(["dateFormat"], form);
  const timeFormat = Form.useWatch(["timeFormat"], form);
  const timestampFormat = Form.useWatch(["timestampFormat"], form);
  const escape = Form.useWatch(["escape"], form);
  const lineDelimiter = Form.useWatch(["lineDelimiter"], form);

  useEffect(() => {
    form.setFieldsValue(initialValues);
  }, [initialValues]);

  return (
    <div>
      <Form
        initialValues={initialValues ?? getInitialValues()}
        form={form}
        onValuesChange={onValuesChange}
        variant="filled"
      >
        <div className="BoslerHeader1">Csv Processing</div>
        <div>
          <FormItemContainer info={"Header"} label={getLanguageLabel("header")}>
            <Form.Item name="firstLineAsHeader">
              <Select options={getFirstLineAsHeaderOptions()} />
            </Form.Item>
          </FormItemContainer>
          <FormItemContainer info={"Skip lines"} label={"Skip lines"}>
            <Form.Item name="header">
              <Select options={getHeaderOptions()} />
            </Form.Item>
            {header == "CUSTOM" && (
              <Form.Item name="customHeader">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer info={"Field Delimiter"} label={"Field Delimiter"}>
            <Form.Item name="fieldDelimiter">
              <Select options={getFieldDelimiterOptions()} />
            </Form.Item>
            {fieldDelimiter == "CUSTOM" && (
              <Form.Item name="customFieldDelimiter">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer info={"Line Delimiter"} label={"Line Delimiter"}>
            <Form.Item name="lineDelimiter">
              <Select options={getLineDelimiterOptions()} />
            </Form.Item>
            {lineDelimiter == "CUSTOM" && (
              <Form.Item name="customLineDelimiter">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer info={"Quote Character"} label={"Quote Character"}>
            <Form.Item name="quote">
              <Select options={getQuoteOptions()} />
            </Form.Item>
          </FormItemContainer>
          <FormItemContainer
            info={"Escape Character"}
            label={"Escape Character"}
          >
            <Form.Item name="escape">
              <Select options={getEscapeOptions()} />
            </Form.Item>
            {escape == "CUSTOM" && (
              <Form.Item name="customEscape">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer info={"Trim extra space"} label={"Trim Space"}>
            <Form.Item name="trimSpace">
              <Select options={getTrimSpaceOptions()} />
            </Form.Item>
          </FormItemContainer>

          <FormItemContainer
            info={"Header"}
            label={"Replace Invalid Characters"}
          >
            <Form.Item name="replaceInvalidChars">
              <Select options={getReplaceInvalidCharsOptions()} />
            </Form.Item>
          </FormItemContainer>

          <FormItemContainer info={"Date Format"} label={"Date Format"}>
            <Form.Item name="dateFormat">
              <Select options={getDateFormatOptions()} />
            </Form.Item>
            {dateFormat == "CUSTOM" && (
              <Form.Item name="customDateFormat">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer info={"Time Format"} label={"Time Format"}>
            <Form.Item name="timeFormat">
              <Select options={getTimeFormatOptions()} />
            </Form.Item>
            {timeFormat == "CUSTOM" && (
              <Form.Item name="customTimeFormat">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>

          <FormItemContainer
            info={"Timestamp format"}
            label={"Timestamp Format"}
          >
            <Form.Item name="timestampFormat">
              <Select options={getTimestampFormatOptions()} />
            </Form.Item>
            {timestampFormat == "CUSTOM" && (
              <Form.Item name="customTimestampFormat">
                <Input />
              </Form.Item>
            )}
          </FormItemContainer>
        </div>
      </Form>
    </div>
  );
};

export default CsvPreprocessing;
