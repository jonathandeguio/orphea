import {
  Avatar,
  Col,
  Divider,
  Row,
  Skeleton,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { AddIcon, SearchIcon } from "assets/icons/boslerActionIcons";
import { GroupsIcon, KeyIcon } from "assets/icons/boslerInterfaceIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import GlobalSearch from "helpers/GlobalSearch";
import React, { useState } from "react";

import { RequestAccessModal } from "Apps/AccessManager/RequestAccessModal";
import classNames from "classnames";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { useToggleState } from "hooks/useToggleState";
import { useNavigate } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";
import styles from "../../Users.module.scss";
import { Group } from "../Group";
import { GROUP_TYPE_NAME } from "../Groups.utils";
import { CreateNewGroupModal } from "./CreateNewGroupModal";
import { DeleteGroupModal } from "./DeleteGroupModal";

interface IProps {
  groups: Group[];
  loading: boolean;
  isSystemGroup: boolean;
  isGroupCreationAllowed: boolean;
}

const { Title, Text } = Typography;

export const GroupTab = ({
  groups,
  loading,
  isSystemGroup,
  isGroupCreationAllowed,
}: IProps) => {
  const [filteredData, setFilteredData] = useState();
  const [isCreateNewGroupModalOpen, setIsCreateNewGroupModalOpen] =
    useState(false);
  const [deleteGroupModalDetails, setDeleteGroupModalDetails] = useState({
    id: "",
    name: "",
    isOpen: false,
  });
  const [
    isRequestAccessModalOpen,
    openRequestAccessModal,
    closeRequestAccessModal,
  ] = useToggleState(false);
  const navigate = useNavigate();

  const columns = [
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("groupName").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "name",
      sorter: (a: $TSFixMe, b: $TSFixMe) => a.name.localeCompare(b.name),
      width: "30%",
      render: (text: $TSFixMe, record: $TSFixMe) => {
        return (
          <div
            style={{
              display: "flex",
              marginRight: "1ev",
              alignItems: "center",
            }}
          >
            <Avatar
              style={{
                height: "32px",
                width: "32px",
                border: "1px solid #ccc",
                marginRight: "5px",
              }}
              icon={<GroupsIcon />}
              src={record.profileImage}
            />
            <div
              className="pop-over-item"
              onClick={() =>
                navigate(`/portal/settings/groups/${record.id}/manageGroup`)
              }
            >
              <>{text}</>
            </div>
          </div>
        );
      },
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("description").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "description",

      render: (text: $TSFixMe, record: $TSFixMe) => (
        <Row justify="space-between">
          <Col>{text}</Col>
          <Col>
            {!isSystemGroup && (
              <BoslerButton
                icononly
                onClick={() => {
                  setDeleteGroupModalDetails({
                    id: record.id,
                    name: record.name,
                    isOpen: true,
                  });
                }}
                style={{ color: "var(--bosler-intent-danger)" }}
                icon={<TrashIcon color={"var(--bosler-intent-danger)"} />}
                minimal
                intent="dangerous"
              >
                {getLanguageLabel("delete")}
              </BoslerButton>
            )}
          </Col>
        </Row>
      ),
    },
  ];

  return (
    <>
      <div className="settings-center-block">
        <Row justify="space-between" align={"middle"}>
          <Col>
            <Title level={3}>
              {isSystemGroup
                ? GROUP_TYPE_NAME.SYSTEM
                : GROUP_TYPE_NAME.RESOURCE}{" "}
              {getLanguageLabel("groups")}
              {groups && `(${groups.length})`}
            </Title>
            <Text type="secondary">{getLanguageLabel("groupMsg")}</Text>
          </Col>
          <Col>
            <Row>
              {!isSystemGroup && isGroupCreationAllowed && (
                <Col>
                  <Tooltip
                    placement="top"
                    title={getLanguageLabel("createNewGroup")}
                  >
                    <BoslerButton
                      icon={<AddIcon />}
                      intent="action"
                      onClick={() => setIsCreateNewGroupModalOpen(true)}
                    >
                      {getLanguageLabel("newGroup")}
                    </BoslerButton>
                  </Tooltip>
                </Col>
              )}

              {isSystemGroup && (
                <Col>
                  <BoslerButton
                    icon={<KeyIcon />}
                    intent="primary"
                    onClick={openRequestAccessModal}
                  >
                    {getLanguageLabel("requestAccess")}
                  </BoslerButton>
                </Col>
              )}
            </Row>
          </Col>
        </Row>
        <Divider />

        <BoslerInput
          placeholder={getLanguageLabel("searchGroupsTable")}
          allowClear
          onChange={(e) => {
            setFilteredData(GlobalSearch(e.target.value, groups, columns));
          }}
          suffix={<SearchIcon />}
        />
        {loading ? (
          <Skeleton
            active
            avatar
            paragraph={{ rows: 20 }}
            className={styles.listItem}
          />
        ) : (
          <Table
            columns={columns}
            dataSource={filteredData || groups}
            pagination={false}
            scroll={{ x: true, y: "100%" }}
            loading={loading}
            size="middle"
          />
        )}
      </div>

      <CreateNewGroupModal
        isOpen={isCreateNewGroupModalOpen}
        setIsOpen={setIsCreateNewGroupModalOpen}
      />

      <DeleteGroupModal
        isOpen={deleteGroupModalDetails.isOpen}
        id={deleteGroupModalDetails.id}
        name={deleteGroupModalDetails.name}
        closeModal={() =>
          setDeleteGroupModalDetails({
            ...deleteGroupModalDetails,
            isOpen: false,
          })
        }
      />
      {isRequestAccessModalOpen && (
        <RequestAccessModal
          isOpen={isRequestAccessModalOpen}
          handleClose={closeRequestAccessModal}
        />
      )}
    </>
  );
};
