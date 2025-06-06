import { Radio } from "antd";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { MappingConstants } from "./APIConnector.constants";
import styles from "./APIConnector.module.scss";
interface IProps {
  extractOption: string;
  setExtractOption: any;
  setFinalKey: any;
  finalKey: string;
}

const OverlayExtractOption = ({
  extractOption,
  setExtractOption,
  setFinalKey,
  finalKey,
}: IProps) => {
  console.log(">> SELECTED OPTION : ", extractOption);
  return (
    <>
      <div>
        <label className={styles.optionsPanel}>
          <Radio
            type="radio"
            checked={extractOption === MappingConstants.completeResponse}
            onChange={(e) => {
              e.stopPropagation();
              e.preventDefault();
              setExtractOption(MappingConstants.completeResponse);
            }}
          />
          {getLanguageLabel("completeResponse")}
        </label>
        <p className={styles.optionDescription}>
          {getLanguageLabel("addCompleteResponse")}
        </p>
      </div>
      <div>
        <label className={styles.optionsPanel}>
          <Radio
            type="radio"
            checked={extractOption === MappingConstants.key}
            onChange={(e) => {
              e.stopPropagation();
              e.preventDefault();
              setExtractOption(MappingConstants.key);
            }}
          />
          {getLanguageLabel("extractByKey")}
        </label>
        <p className={styles.optionDescription}>
          {getLanguageLabel("extractValueByKeyFromDictionaryResponse")}
        </p>
      </div>
      <div>
        <label className={styles.optionsPanel}>
          <Radio
            type="radio"
            checked={extractOption === MappingConstants.index}
            onChange={() => setExtractOption(MappingConstants.index)}
          />
          {getLanguageLabel("extractByIndex")}
        </label>
        <p className={styles.optionDescription}>
          {getLanguageLabel("extractValueByIndexInArrayResponse")}
        </p>
      </div>
      {extractOption && extractOption !== MappingConstants.completeResponse && (
        <div className={styles.nestedKeyInput}>
          <BoslerInput
            value={finalKey}
            onChange={(e) => setFinalKey(e.target.value)}
            placeholder={`${getLanguageLabel("enter")} ${
              extractOption === MappingConstants.index
                ? getLanguageLabel("index")
                : getLanguageLabel("key")
            }`}
          />
        </div>
      )}
    </>
  );
};

export default OverlayExtractOption;
