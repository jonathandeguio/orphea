import { Switch, Typography } from "antd";
import { HistogramIcon } from "assets/icons/movetodataChartIcons";
import { PostgresIcon } from "assets/icons/movetodataExternalIcons";
import React, { useState } from "react";
import MoveToDataSwitch from "components/MoveToDataSwitch/MoveToDataSwitch";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataHeader from "components/Header/MoveToDataHeader";
import MoveToDataSelectableCard from "components/MoveToDataSelectableCard";

const { Title } = Typography;

const MoveToDataComponents = () => {
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
        <MoveToDataSwitch
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
        <MoveToDataSwitch
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
      <MoveToDataButton onClick={showModal}>Open Modal</MoveToDataButton>

      <MoveToDataModal
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
            <MoveToDataButton> Submit</MoveToDataButton>
          </>
        }
      >
        This is the body
      </MoveToDataModal>
      <Title level={3}>MoveToData Header</Title>
      <MoveToDataHeader heading="heading" description="subheading" />
      <Title level={3}>MoveToData Selectable Card</Title>
      <MoveToDataSelectableCard
        icon={<PostgresIcon />}
        heading={"Hello"}
        information={"information"}
      />
      <br />
    </>
  );
};

export default MoveToDataComponents;
