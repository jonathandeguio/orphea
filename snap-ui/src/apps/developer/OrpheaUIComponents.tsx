import { Switch, Typography } from "antd";
import { HistogramIcon } from "assets/icons/orpheaChartIcons";
import { PostgresIcon } from "assets/icons/orpheaExternalIcons";
import React, { useState } from "react";
import OrpheaSwitch from "components/OrpheaSwitch/OrpheaSwitch";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaModal from "components/OrpheaModalContainer";
import OrpheaHeader from "components/Header/OrpheaHeader";
import OrpheaSelectableCard from "components/OrpheaSelectableCard";

const { Title } = Typography;

const OrpheaComponents = () => {
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
        <OrpheaSwitch
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
        <OrpheaSwitch
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
      <Title level={3}>Orphea Model Container</Title>
      <OrpheaButton onClick={showModal}>Open Modal</OrpheaButton>

      <OrpheaModal
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
            <OrpheaButton> Submit</OrpheaButton>
          </>
        }
      >
        This is the body
      </OrpheaModal>
      <Title level={3}>Orphea Header</Title>
      <OrpheaHeader heading="heading" description="subheading" />
      <Title level={3}>Orphea Selectable Card</Title>
      <OrpheaSelectableCard
        icon={<PostgresIcon />}
        heading={"Hello"}
        information={"information"}
      />
      <br />
    </>
  );
};

export default OrpheaComponents;
