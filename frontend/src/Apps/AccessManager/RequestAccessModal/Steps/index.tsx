import { Steps, Typography } from "antd";
import React, { Dispatch, SetStateAction } from "react";
import { classNames } from "utils/styles";
import styles from "./RequestAccess.module.scss";

const { Step } = Steps;
const { Text } = Typography;

interface IProps {
  current: number;
  setCurrent: Dispatch<SetStateAction<number>>;
}

export const RequestAccessSteps = ({ current, setCurrent }: IProps) => {
  return (
    <Steps
      current={current}
      className={classNames(styles.steps, "--p15")}
      direction="vertical"
      size="small"
    >
      <Step onClick={() => setCurrent(0)} title={<Text>Add Details</Text>} />
      <Step onClick={() => setCurrent(1)} title={<Text>Select Role</Text>} />
      <Step
        onClick={() => setCurrent(2)}
        title={<Text>Add Justification</Text>}
      />
      <Step onClick={() => setCurrent(3)} title={<Text>Review</Text>} />
    </Steps>
  );
};
