import { Flex } from "antd";
import React, { Dispatch, SetStateAction } from "react";
import { AccessRequestAction } from "../Action";
import { AccessRequestSummary } from "../Summary";
import { IAccessRequest } from "Apps/AccessManager/AccessManager";
import styles from "../AccessRequest.module.scss";

interface IProps {
  accessRequest: IAccessRequest;
  setAccessRequest: Dispatch<SetStateAction<IAccessRequest | undefined>>;
}

export const ActionRequestContent = ({
  accessRequest,
  setAccessRequest,
}: IProps) => {
  return (
    <Flex gap="middle" vertical className={styles.container}>
      <AccessRequestSummary accessRequest={accessRequest} />
      <AccessRequestAction
        accessRequest={accessRequest}
        setAccessRequest={setAccessRequest}
      />
    </Flex>
  );
};
