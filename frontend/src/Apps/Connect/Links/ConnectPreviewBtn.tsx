import { Popover } from "antd";
import { AutoModeIcon, StopIcon } from "assets/icons/boslerActionIcons";
import { updateBottomBarItemState } from "common/components/BoslerLayout/bottomBarSlice";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";
import React, { useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import createCancelablePromise from "utils/createCancelablePromise";
import { encodeToBase64, getLanguageLabel, isDefined } from "utils/utilities";
import { webhookExecution } from "../../../redux/webhookSlice";
import { PreviewAPI } from "../Connect.api";
import { executeWebhookAPI } from "../Webhook/Webhook.api";
import { convertWebhookRequestsFieldsToString } from "../Webhook/Webhook.utils";
import { ILink } from "./Link.types";

interface IConnectPreviewBtn {
  linkId: string;
  linkDetails: ILink;
  form: any;
}

const ConnectPreviewBtn = ({
  linkId,
  linkDetails,
  form,
}: IConnectPreviewBtn) => {
  const dispatch = useDispatch();
  const [previewActive, setPreviewActive] = useState<boolean>(false);
  const [restPreviewResponseParam, setRestPreviewResponseParam] =
    useState<String>("@completeresponse");
  const { previewSource, querySource } = useSelector(
    (state: RootState) => state.sourceOps
  );
  const cancelRef = useRef<any>(null);

  const executeWebhook = () => {
    if (!linkDetails.id) return;
    dispatch(
      webhookExecution({
        executionLoading: true,
        executionResult: undefined,
        executionError: false,
      })
    );
    executeWebhookAPI(
      linkId,
      convertWebhookRequestsFieldsToString(form.getFieldValue("requests"))
    )
      .then(({ data }) => {
        dispatch(
          webhookExecution({
            executionLoading: false,
            executionResult: data,
            executionError: false,
          })
        );
      })
      .catch((error) => {
        dispatch(
          webhookExecution({
            executionLoading: false,
            executionResult: undefined,
            executionError: error.message,
          })
        );
      });
  };

  const handleAbort = () => {
    if (cancelRef.current) {
      cancelRef.current();
      dispatch(
        updateBottomBarItemState({
          id: "datasetPreviewPanel",
          props: {
            loading: false,
            data: null,
          },
        })
      );
    }
  };
  const handlePreview = (code: string) => {
    setPreviewActive(true);
    dispatch(
      updateBottomBarItemState({
        id: "datasetPreviewPanel",
        openPane: true,
        props: {
          loading: true,
        },
      })
    );

    if (isDefined(linkId)) {
      let body: any = {
        query: { query: encodeToBase64(code) },
      };
      if (linkDetails.type == "rest") {
        body = {
          query: { query: encodeToBase64(code) },
          requests: convertWebhookRequestsFieldsToString(
            form.getFieldValue("requests")
          ),
          responseParam: form.getFieldValue("responseParam"),
          csvPreprocessing: form.getFieldValue("csvPreprocessing"),
        };
        executeWebhook();
      }

      const { promise, cancel } = createCancelablePromise((cancelToken: any) =>
        PreviewAPI(linkId, body, cancelToken)
      );
      cancelRef.current = cancel;
      promise
        .then(({ data }: any) => {
          dispatch(
            updateBottomBarItemState({
              id: "datasetPreviewPanel",
              props: {
                loading: false,
                data: data,
              },
              // tabs: [
              //   {
              //     label: "Request",
              //     paneKey: "request",
              //     closable: false,
              //     children: <Request,
              //   },
              //   {
              //     label: "Response",
              //     paneKey: "Response",
              //     closable: false,
              //     children: <>Request</>,
              //   },
              //   {
              //     label: "More Info",
              //     paneKey: "More Info",
              //     closable: false,
              //     children: <>Request</>,
              //   },
              // ],
            })
          );
        })
        .catch(() => {})
        .finally(() => {
          setPreviewActive(false);
        });
    }
  };

  const getIntent = () => {
    if (previewActive) {
      return "none";
    } else {
      return "action";
    }
  };

  useEffectOnlyOnDependencyUpdate(() => {
    if (previewSource && previewSource.sourceId && previewSource.code) {
      handlePreview(previewSource.code);
    }
  }, [previewSource]);

  const getTitle = () => {
    if (previewActive) {
      return (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <div>{getLanguageLabel("pleaseWait")}</div>
          <div>
            <BoslerButton
              icon={<StopIcon />}
              intent="dangerous"
              onClick={handleAbort}
            >
              {getLanguageLabel("abort")}
            </BoslerButton>
          </div>
        </div>
      );
    } else {
      return <>Click here to run a preview</>;
    }
  };

  if (linkDetails.type == "SHAREPOINT") {
    return <></>;
  }

  return (
    <Popover title={getTitle()} placement="bottom">
      <BoslerButton
        intent={getIntent()}
        icon={<AutoModeIcon />}
        onClick={() => handlePreview(querySource.code)}
        loading={previewActive}
      >
        {getLanguageLabel("preview")}
      </BoslerButton>
    </Popover>
  );
};

export default ConnectPreviewBtn;
