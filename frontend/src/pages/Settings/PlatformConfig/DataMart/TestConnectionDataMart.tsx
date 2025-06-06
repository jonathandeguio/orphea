import React, { useEffect, useState } from "react";
import { FormInstance } from "antd";
import { initialSourceDetails } from "Apps/Connect/Sources/Source.constants";
import { Buffer } from "buffer";
import { testDBConnectionAPI } from "Apps/Connect/Connect.api";
import { SourceTypeEnum } from "Apps/Connect/Enums/SourceTypeEnum";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { ArrowRightIcon } from "assets/icons/boslerNavigationIcon";

interface ITestConnectionProps {
  form: FormInstance;
  outerName: number;
}

const TEST_CONNECTION_BTN_ID = "TEST-CONNECTION-BTN";
const isSuccess = (data: any) => {
  if (data.status == "SUCCESS" || data.status == true) {
    return true;
  } else {
    return false;
  }
};

export const TestConnectionDataMart: React.FC<ITestConnectionProps> = ({
  form,
  outerName,
}) => {
  const [testConnection, setTestConnection] = useState<any>();

  const handleTestConnection = async () => {
    const latestValues = await getData();

    if (latestValues) {
      if (latestValues.type === SourceTypeEnum.JDBC) {
        try {
          const { data } = await testDBConnectionAPI(
            JSON.stringify(latestValues)
          );
          setTestConnection(data);
        } catch (error) {}
      }
    }
  };

  const getData = () => {
    const newSourceDetails = {
      ...initialSourceDetails,
      dbmsType: "POSTGRES",
      type: "jdbc",
      username: form.getFieldValue(["dataMartModels", outerName, "username"]),
      password: Buffer.from(
        form.getFieldValue(["dataMartModels", outerName, "password"]),
        "ascii"
      ).toString("base64"),
      server: form.getFieldValue(["dataMartModels", outerName, "server"]),
      port: form.getFieldValue(["dataMartModels", outerName, "port"]),
      database: form.getFieldValue(["dataMartModels", outerName, "database"]),
    };

    return newSourceDetails;
  };

  return (
    <>
      <BoslerButton
        icon={<ArrowRightIcon />}
        intent={
          testConnection
            ? isSuccess(testConnection)
              ? "success"
              : "dangerous"
            : "action"
        }
        onClick={handleTestConnection}
        id={TEST_CONNECTION_BTN_ID}
      >
        {testConnection
          ? testConnection.status == "SUCCESS" || testConnection.status == true
            ? "Re-Test Connection"
            : "Re-Test Connection"
          : "Test Connection"}
      </BoslerButton>
      {testConnection?.status && <div>{testConnection.message}</div>}
    </>
  );
};
