import { Switch, Typography } from "antd";
import { HistogramIcon } from "assets/icons/boslerChartIcons";
import { PostgresIcon } from "assets/icons/boslerExternalIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import BoslerSelectableCard from "components/CommonUI/BoslerSelectableCard";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import React, { useState } from "react";

const { Title } = Typography;

const BoslerComponents = () => {
  const [activeTab, setActiveTab] = useState<string>("allHistory");
  const [isModalOpen, setIsModalOpen] = useState(false);

  const showModal = () => {
    setIsModalOpen(true);
  };

  const handleOk = () => {
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };
  return (
    <>
      <Title>UI Components</Title>
      <Title level={3}>Switch Component</Title>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "2rem",
        }}
      >
        <>
          Small
          <Switch size="small"></Switch>
        </>
        <>
          Medium
          <Switch></Switch>
        </>
      </div>

      <Title level={3}>Rich Switch</Title>
      <div
        style={{
          width: "400px",
          display: "flex",
          flexDirection: "column",
          gap: "20px",
        }}
      >
        <BoslerSwitch
          items={[
            { label: "All history", value: "allHistory", children: "" },
            {
              label: "Versions only",
              value: "versionsOnly",
              children: "",
            },
          ]}
          value={activeTab}
          onChange={(value) => setActiveTab(value)}
          padding="4px"
        />
        <BoslerSwitch
          items={[
            {
              label: "All history",
              value: "allHistory",
              children: "",
              icon: <HistogramIcon />,
            },
            {
              label: "Versions only",
              value: "versionsOnly",
              children: "",
            },
          ]}
          value={activeTab}
          onChange={(value) => setActiveTab(value)}
        />
      </div>
      <Title level={3}>MoveToData Model Container</Title>
      <BoslerButton onClick={showModal}>Open Modal</BoslerButton>

      <BoslerModal
        open={isModalOpen}
        onOk={handleOk}
        onCancel={handleCancel}
        destroyOnClose
        footer={null}
        styles={{
          mask: {
            backgroundColor: "rgba(248, 250, 251, 0.7)",
          },
        }}
        closable={false}
        closeIcon
        heading="Heading2"
        // headingIcon={<AddIcon />}
        // information={<>Infor</>}
        footerExtraText="new footer text"
        footerButtonArea={
          <>
            <BoslerButton> Submit</BoslerButton>
          </>
        }
      >
        This is the body
      </BoslerModal>
      <Title level={3}>MoveToData Header</Title>
      <BoslerHeader heading="heading" description="subheading" />
      <Title level={3}>MoveToData Selectable Card</Title>
      <BoslerSelectableCard
        icon={<PostgresIcon />}
        heading={"Hello"}
        information={"information"}
      />
      <br />
    </>
  );
};

export default BoslerComponents;
