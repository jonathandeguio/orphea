import { Form, FormInstance } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { AddIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import NoData from "components/CommonUI/NoData";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { ISourceConfig } from "../Sources/Source";
import styles from "./Webhook.module.scss";
import { IWebhook } from "./Webhook.types";
import WebhookSortableAPIs from "./WebhookSortableAPIs";

interface IProps {
  form: FormInstance;
  webhook: IWebhook;
  source: ISourceConfig;
  setNoChanges: any;
}

const WebhookAPIs = ({ webhook, source, form, setNoChanges }: IProps) => {
  const initialValues = {
    method: "GET",
    apiTitle: getLanguageLabel("apiRequest"),
    domainId: source?.domains?.[0]?.id,
    path: "",
    queryParams: [],
    headers: [],
    bodyType: "NONE",
    formData: [],
    rawBody: "",
  };

  return (
    <Form
      layout="vertical"
      initialValues={webhook}
      form={form}
      className={styles.webhookContainer}
      onValuesChange={() => {
        setNoChanges(false);
      }}
    >
      <div className={styles.webhookContainer_upper}>
        <Form.List name="requests">
          {(fields, { add, remove, move }) => (
            <>
              <h3 style={{ marginLeft: "20px" }}>
                {getLanguageLabel("apiConnector")}
              </h3>
              <div className={styles.requestsContainer}>
                <WebhookSortableAPIs
                  fields={fields}
                  move={move}
                  source={source}
                  remove={remove}
                  form={form}
                />
                {(!form.getFieldValue("requests") ||
                  form.getFieldValue("requests")?.length == 0) && (
                  <NoData
                    icon={<SearchEmptyState />}
                    heading={getLanguageLabel("addRequestToGetStarted")}
                  />
                )}
                <div className={styles.addRequest}>
                  <Form.Item>
                    <BoslerButton
                      onClick={() => add(initialValues)}
                      icon={<AddIcon />}
                      intent="primary"
                      outlined
                    >
                      {getLanguageLabel("addRequest")}
                    </BoslerButton>
                  </Form.Item>
                </div>
              </div>
            </>
          )}
        </Form.List>
      </div>
    </Form>
  );
};

export default WebhookAPIs;
