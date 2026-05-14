import { Input, Select, Typography } from "antd";
import React from "react";
import ButtonComponentDisplay from "components/ButtonComponent/ButtonComponentDisplay";
import OrpheaInput from "components/InputComponent/OrpheaInput";


const { Title } = Typography;

const OrpheaComponents = () => {
  return (
    <>
      <Title>Platform Components</Title>
      <Title level={3}>Button</Title>
      <ButtonComponentDisplay />

      <Title level={3}>Input Component</Title>
      <OrpheaInput placeholder={"hello"} />
      <Input.Password></Input.Password>
      <Input.TextArea></Input.TextArea>
      <Select></Select>
    </>
  );
};

export default OrpheaComponents;
