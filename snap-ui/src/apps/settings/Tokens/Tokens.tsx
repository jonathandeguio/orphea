import { Col, Divider, Row, Switch, Table, Typography } from "antd";

import { AddIcon, SearchIcon } from "assets/icons/boslerActionIcons";
import CreateNewTokenModal from "apps/settings/Tokens/CreateNewTokenModal";

import React, { useEffect, useState } from "react";
import { getLanguageLabel, globalSearch } from "utils/utilities";
import BoslerLoader from "components/boslerLoader/BoslerLoader";
import { fetchAllTokensAPI } from "../apis";
import BoslerInput from "components/InputComponent/BoslerInput";
import BoslerButton from "components/ButtonComponent/BoslerButton";

const { Text, Title } = Typography;

const Tokens = () => {
  const [tokens, setTokens] = useState([]);
  const [FilteredData, setFilteredData] = useState();
  const [loading, setLoading] = useState(true);
  const [isCreateNewTokenModalOpen, setIsCreateNewTokenModalOpen] =
    useState(false);

  useEffect(() => {
    fetchAllTokensAPI().then(({ data }) => {
      setTokens(data);
      setLoading(false);
    });
  }, []);

  interface DataItem {
    key: string;
    status: boolean;
    name: string;
    createdAt: Date;
    expiration: Date;
  }

  const columns: Array<{
    title: string;
    dataIndex: string;
    align?: string;
    width?: string;
    render?: (value: any, record: DataItem) => React.ReactNode;
    sorter?: (a: DataItem, b: DataItem) => number;
  }> = [
    {
      title: getLanguageLabel("status"),
      dataIndex: "status",
      align: "center",
      width: "10%",
      render: (status: boolean) => <Switch defaultChecked={status} />,
      sorter: (a: DataItem, b: DataItem) => (a.status ? 1 : -1),
    },
    {
      title: getLanguageLabel("label"),
      dataIndex: "name",
      width: "30%",
      sorter: (a: DataItem, b: DataItem) => a.name.localeCompare(b.name),
    },
    {
      title: getLanguageLabel("createdOn"),
      dataIndex: "createdAt",
      render: (createdAt: Date) =>
        new Intl.DateTimeFormat("en-US", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        }).format(createdAt),
      width: "30%",
      sorter: (a: DataItem, b: DataItem) =>
        a.createdAt.getTime() - b.createdAt.getTime(),
    },
    {
      title: getLanguageLabel("expiringOn"),
      dataIndex: "expiration",
      render: (expiration: Date) =>
        new Intl.DateTimeFormat("en-US", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        }).format(expiration),
      sorter: (a: DataItem, b: DataItem) =>
        a.expiration.getTime() - b.expiration.getTime(),
      width: "30%",
    },
  ];

  return loading ? (
    <div className="settings-center-block">
      <BoslerLoader />
    </div>
  ) : (
    <div className="settings-center-block">
      <Row justify="space-between">
        <Col>
          <Title level={3}>{getLanguageLabel("tokens")}</Title>

          <Text type="secondary">{getLanguageLabel("tokenMsg")}</Text>
        </Col>
        <Col>
          <BoslerButton
            icon={<AddIcon />}
            intent="action"
            onClick={() => setIsCreateNewTokenModalOpen(true)}
          >
            {" "}
            {getLanguageLabel("newToken")}{" "}
          </BoslerButton>
        </Col>
      </Row>
      <Divider />
      <BoslerInput
        placeholder={getLanguageLabel("searchTokenTable")}
        allowClear
        onChange={(e) => {
          setFilteredData(globalSearch(e.target.value, tokens, columns));
        }}
        suffix={<SearchIcon />}
      />
      <Table
        // @ts-expect-error TS(2322): Type '({ title: string; dataIndex: string; align: ... Remove this comment to see the full error message
        columns={columns}
        dataSource={FilteredData !== undefined ? FilteredData : tokens}
        pagination={false}
        // scroll={{ y: 320 }}
      />
      <CreateNewTokenModal
        isOpen={isCreateNewTokenModalOpen}
        setIsOpen={setIsCreateNewTokenModalOpen}
        setTokens={setTokens}
      />
    </div>
  );
};

export default Tokens;
