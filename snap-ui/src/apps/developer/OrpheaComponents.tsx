import { Input, Select, Typography } from "antd";
import React from "react";
import ButtonComponentDisplay from "components/ButtonComponent/ButtonComponentDisplay";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";


const { Title } = Typography;

const MoveToDataComponents = () => {
  return (
    <>
      <Title>Platform Components</Title>
      <Title level={3}>Button</Title>
      <ButtonComponentDisplay />

      <Title level={3}>Input Component</Title>
      <MoveToDataInput placeholder={"hello"} />
      <Input.Password></Input.Password>
      <Input.TextArea></Input.TextArea>
      <Select></Select>
    </>
  );
};

export default MoveToDataComponents;
