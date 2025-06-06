import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React, { useEffect, useState } from "react";
import { getLanguageLabel } from "utils/utilities";
import {
  abortDatasetWritingTransactionAPI,
  abortDatasetWritingTransactionForWholeBuildAPI,
} from "../Builds.api";
interface IProps {
  text: string;
  datasetId?: string;
  branch?: string;
  buildId: string;
}

const ALREADY_ACTIVE_TEXT = "There is a transaction already active";
const ABORT_TRANSACTION_BTN = "ABORT_TRANSACTION_BTN";

const BuildTableDatasetWritingTransactionActive = ({
  text,
  datasetId,
  branch,
  buildId,
}: IProps) => {
  const [showAbortTransactionBtn, setShowAbortTransactionBtn] =
    useState<boolean>(false);

  const performCheck = (text: string) => {
    if (text == ALREADY_ACTIVE_TEXT) {
      setShowAbortTransactionBtn(true);
    }
  };

  const handleTransactionAborting = (
    buildId: string,
    datasetId?: string,
    branch?: string
  ) => {
    if (datasetId && branch) {
      abortDatasetWritingTransactionAPI(datasetId, branch)
        .then(() => {
          (window as any).makeButtonTemporarySuccess(
            ABORT_TRANSACTION_BTN,
            300000
          );
        })
        .catch(() => {
          (window as any).makeButtonTemporaryFailure(
            ABORT_TRANSACTION_BTN,
            300000
          );
        })
        .finally(() => {});
    } else {
      abortDatasetWritingTransactionForWholeBuildAPI(buildId)
        .then(() => {
          (window as any).makeButtonTemporarySuccess(
            ABORT_TRANSACTION_BTN,
            300000
          );
        })
        .catch(() => {
          (window as any).makeButtonTemporaryFailure(
            ABORT_TRANSACTION_BTN,
            300000
          );
        })
        .finally(() => {});
    }
  };

  useEffect(() => {
    performCheck(text);
  }, [text]);

  if (!showAbortTransactionBtn) {
    return <></>;
  }
  console.log("DATASET ID : ", datasetId);
  return (
    <div>
      <BoslerButton
        intent="action"
        onClick={() => handleTransactionAborting(buildId, datasetId, branch)}
        id={ABORT_TRANSACTION_BTN}
      >
        {getLanguageLabel("abortTransaction")}
      </BoslerButton>
    </div>
  );
};

export default BuildTableDatasetWritingTransactionActive;
