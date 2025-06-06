import { Input, Select, Typography } from "antd";
import React from "react";
import ButtonComponentDisplay from "components/ButtonComponent/ButtonComponentDisplay";
import BoslerInput from "components/InputComponent/BoslerInput";


const { Title } = Typography;

const BoslerComponents = () => {
  return (
    <>
      <Title>Platform Components</Title>
      <Title level={3}>Button</Title>
      <ButtonComponentDisplay />

      <Title level={3}>Input Component</Title>
      <BoslerInput placeholder={"hello"} />
      <Input.Password></Input.Password>
      <Input.TextArea></Input.TextArea>
      <Select></Select>
    </>
  );
};

export default BoslerComponents;
