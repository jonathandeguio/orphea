import { Divider } from "Apps/LexicalEditor/plugins/ToolbarPlugin";
import { AddIcon, CrossIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React, { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { getLanguageLabel } from "utils/utilities";
import { MappingConstants } from "./APIConnector.constants";
import styles from "./APIConnector.module.scss";
import OverlayExtractOption from "./OverlayExtractOption";
import { IMappingTypeOverlayProps } from "./types";

export const MappingTypeOverlay = ({
  form,
  position,
  setOverlayVisible,
  overlayFieldValue,
  overlayAtPos,
}: IMappingTypeOverlayProps) => {
  const overlayRef = useRef<HTMLDivElement | null>(null);
  const [selectedType, setSelectedType] = useState(
    getLanguageLabel("parentCall")
  );
  const [extractOption, setExtractOption] = useState("");
  const [finalKey, setFinalKey] = useState("");
  const [isInputValid, setIsInputValid] = useState(true);

  const extractData = () => {
    const value = form.getFieldValue(overlayFieldValue);
    if (overlayAtPos !== null) {
      const beforeAt = value.slice(0, overlayAtPos);
      const afterAt = value.slice(overlayAtPos + 1);
      console.log(">> VALUES : ", beforeAt, afterAt, overlayAtPos);

      if (extractOption === MappingConstants.key) {
        form.setFieldValue(
          overlayFieldValue,
          beforeAt + `@key[${finalKey}]` + afterAt
        );
      } else if (extractOption === MappingConstants.completeResponse) {
        form.setFieldValue(
          overlayFieldValue,
          beforeAt + "@completeresponse" + afterAt
        );
      } else if (extractOption === MappingConstants.index) {
        form.setFieldValue(
          overlayFieldValue,
          beforeAt + `@index[${finalKey}]` + afterAt
        );
      }

      setOverlayVisible(false);
    }
  };

  useEffect(() => {
    setIsInputValid(
      extractOption === MappingConstants.completeResponse ||
        finalKey.trim().length > 0
    );
  }, [finalKey, extractOption]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        overlayRef.current &&
        !overlayRef.current.contains(event.target as Node)
      ) {
        setOverlayVisible(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [setOverlayVisible]);

  return createPortal(
    <div
      ref={overlayRef}
      className={styles.overlayContainer}
      style={{ top: position.top + 80, left: position.left + 15 }}
    >
      <div className={styles.mappingTypeSelection}>
        <div className="BoslerHeader1">
          <strong>{getLanguageLabel("mappingType")}</strong>
        </div>
        <div className={styles.typeList}>
          <div onClick={() => setSelectedType(getLanguageLabel("parentCall"))}>
            <strong>{getLanguageLabel("parentCall")}</strong>
          </div>
        </div>
      </div>

      {selectedType === getLanguageLabel("parentCall") && (
        <div className={"--pl20"}>
          <div className="BoslerHeader1">
            <strong>{selectedType}</strong>
          </div>
          <Divider />
          <OverlayExtractOption
            extractOption={extractOption}
            setExtractOption={setExtractOption}
            setFinalKey={setFinalKey}
            finalKey={finalKey}
          />
          <div className={styles.buttonContainer}>
            <BoslerButton
              icon={<AddIcon />}
              intent={isInputValid ? "action" : "none"} // Disable button intent if input is invalid
              onClick={extractData}
              disabled={!isInputValid} // Disable click event if input is invalid
            >
              {getLanguageLabel("addNewTag")}
            </BoslerButton>
            <BoslerButton
              icon={<CrossIcon />}
              onClick={() => setOverlayVisible(false)}
            >
              {getLanguageLabel("close")}
            </BoslerButton>
          </div>
        </div>
      )}
    </div>,
    document.body
  );
};
