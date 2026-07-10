import {
  Avatar,
  Col,
  Divider,
  Dropdown,
  Input,
  Row,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllGroups } from "redux/actions/authActions";

import MoveToDataLoader from "components/movetodataLoader";
import React from "react";
import { useNavigate } from "react-router-dom";
import { getLanguageLabel, globalSearch, openNotification } from "utils/utilities";
import {
  AddIcon,
  MoreMenuIcon,
  SearchIcon,
} from "assets/icons/movetodataActionIcons";
import { EditIcon } from "assets/icons/movetodataEditorIcons";
import {
  AddUserIcon,
  GroupsIcon,
} from "assets/icons/movetodataInterfaceIcons";
import { TrashIcon } from "assets/icons/movetodataMiscellaneousIcons";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import { ThunkAppDispatch } from "redux/types/store";
import { createGroupAPI, deleteGroupAPI } from "./Groups/Groups.api";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataSwitch from "components/MoveToDataSwitch/MoveToDataSwitch";

const { TextArea } = Input;

const { Title, Text } = Typography;
const Groups = () => {
  const [FilteredData, setFilteredData] = useState();
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { user: groupAdmin } = useSelector(
    (state) => (state as any).groupAdmin
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { allGroups, loading } = useSelector(
    (state) => (state as $TSFixMe).allGroups
  );
  const [selectedTab, setSelectedTab] = useState<string>("resourceGroups");
  const [deleteServiceDetails, setDeleteServiceDetails] = useState({
    Id: [],
    name: [],
  });
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);

  const defaultGroupDetails = {
    id: "",
    name: "",
    description: "",
    status: "",
    managers: [],
    members: [],
    owners: [],
  };

  const systemGroupNames = [
    "platform-administrators",
    "user-administrators",
    "group-administrators",
    "project-administrators",
    "connect-administrators",
  ];

  const [deleteModal, setDeleteModal] = useState(false);
  const [deleteGroupDetails, setDeleteGroupDetails] = useState({
    ...defaultGroupDetails,
  });

  const [newGroupDetails, setNewGroupDetails] = useState({
    ...defaultGroupDetails,
  });

  const handleClick = (record: $TSFixMe) => {
    navigate(`/portal/settings/groups/${record.id}/manageGroup`);
  };

  const columns = [
    {
      title: getLanguageLabel("groupName"),
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
              onClick={(e) =>
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
      title: getLanguageLabel("description"),
      dataIndex: "description",

      render: (text: $TSFixMe, record: $TSFixMe) => (
        <Row justify="space-between">
          <Col>{text}</Col>
          {
            // isGroupAdmin or isPlatformAdmin
            <Col>
              <Dropdown
                menu={{
                  items: [
                    {
                      label: (
                        <>
                          <div
                            onClick={() => {
                              handleClick(record);
                            }}
                            className="text-and-icon-center"
                            style={{ width: "100%" }}
                          >
                            <EditIcon />
                            {getLanguageLabel("edit")}
                          </div>
                        </>
                      ),
                      key: 0,
                    },
                    {
                      label: (
                        <>
                          <div
                            onClick={() => {
                              setDeleteGroupDetails({ ...record });
                              setDeleteModal(true);
                            }}
                            className="text-and-icon-center"
                            style={{ color: "var(--movetodata-intent-danger)" }}
                          >
                            <TrashIcon color={"var(--movetodata-intent-danger)"} />
                            {getLanguageLabel("delete")}
                          </div>
                        </>
                      ),
                      key: 1,
                    },
                  ],
                }}
                trigger={["click"]}
              >
                <div
                  onClick={(e) => e.preventDefault()}
                  style={{ cursor: "pointer" }}
                >
                  <MoreMenuIcon />
                </div>
              </Dropdown>
            </Col>
          }
        </Row>
      ),
    },
  ];

  function onChange(
    pagination: $TSFixMe,
    filters: $TSFixMe,
    sorter: $TSFixMe,
    extra: $TSFixMe
  ) {
    //
  }

  const handleNew = () => {
    setShowModal(true);
  };
  const handleCancel = () => {
    setShowModal(false);
  };

  const handleCreate = () => {
    if (!newGroupDetails.name && !newGroupDetails.description) {
      openNotification(
        "Details incomplete",
        "Please complete the details",
        "warning"
      );
      return;
    }
    createGroupAPI(newGroupDetails).then(() => {
      setShowModal(false);
      dispatch(getAllGroups());
    });
  };

  useEffect(() => {
    dispatch(getAllGroups());
  }, []);


  let resourceGroups;
  let systemGroups;

  if (allGroups) {
    const resourceGroupsList = allGroups.filter(
      (item: any) => !systemGroupNames.includes(item.name.toLowerCase())
    );

    resourceGroups = (
      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                Resource Groups (
                {resourceGroupsList && resourceGroupsList.length})
              </Title>
              <Text type="secondary">{getLanguageLabel("groupMsg")}</Text>
            </Col>
            <Col>
              {groupAdmin || platformAdmin ? (
                <>
                  <div style={{ display: "flex", justifyContent: "flex-end" }}>
                    <div className="text-and-icon-center">
                      <Tooltip
                        placement="top"
                        title={getLanguageLabel("createNewGroup")}
                      >
                        <MoveToDataButton
                          icon={<AddIcon />}
                          intent="action"
                          onClick={handleNew}
                        >
                          {getLanguageLabel("newGroup")}
                        </MoveToDataButton>
                      </Tooltip>
                    </div>
                  </div>
                </>
              ) : (
                ""
              )}
            </Col>
          </Row>
          <Divider />
        </p>

        <MoveToDataInput
          // size="small"
          placeholder={getLanguageLabel("searchGroupsTable")}
          allowClear
          onChange={(e) => {
            setFilteredData(
              globalSearch(e.target.value, resourceGroupsList, columns)
            );
          }}
          suffix={<SearchIcon />}
        />
        <Table
          columns={columns}
          dataSource={
            FilteredData !== undefined ? FilteredData : resourceGroupsList
          }
          className="interactive"
          pagination={false}
          loading={loading}
          // scroll={{ y: "60vh" }}
        />
      </div>
    );

    const systemGroupsList = allGroups.filter((item: any) =>
      systemGroupNames.includes(item.name.toLowerCase())
    );

    systemGroups = (
      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                System Groups ({systemGroupsList && systemGroupsList.length})
              </Title>
              <Text type="secondary">{getLanguageLabel("groupMsg")}</Text>
            </Col>
          </Row>
          <Divider />
        </p>

        <MoveToDataInput
          // size="small"
          placeholder={getLanguageLabel("searchGroupsTable")}
          allowClear
          onChange={(e) => {
            setFilteredData(
              globalSearch(e.target.value, systemGroupsList, columns)
            );
          }}
          suffix={<SearchIcon />}
        />
        <Table
          columns={columns}
          dataSource={
            FilteredData !== undefined ? FilteredData : systemGroupsList
          }
          loading={loading}
          className="interactive"
          pagination={false}
          // scroll={{ y: "60vh" }}
        />
      </div>
    );
  }

  return (
    <>
      {loading ? (
        <>
          <MoveToDataLoader />
        </>
      ) : allGroups && allGroups.length == 0 ? (
        <>
          <div className="settings-center-block">
            You are not part of any groups.
          </div>
        </>
      ) : (
        <>
          <MoveToDataModal
            headingIcon={<AddUserIcon />}
            heading={getLanguageLabel("createNewGroup")}
            open={showModal}
            onCancel={handleCancel}
            footerButtonArea={
              <MoveToDataButton
                icon={<TickIcon />}
                intent="action"
                key="submit"
                onClick={handleCreate}
              >
                {getLanguageLabel("create")}
              </MoveToDataButton>
            }
          >
            <div className="MoveToDataHeader1">{getLanguageLabel("groupName")}</div>

            <MoveToDataInput
              placeholder={newGroupDetails.name}
              name={newGroupDetails.name}
              onChange={(e) =>
                setNewGroupDetails({
                  ...defaultGroupDetails,
                  name: e.target.value,
                })
              }
              required
            />
            <div className="MoveToDataHeader1">
              {getLanguageLabel("description")}
            </div>

            <TextArea
              onChange={(e) =>
                setNewGroupDetails({
                  ...newGroupDetails,
                  description: e.target.value,
                })
              }
              value={newGroupDetails.description}
              name="description"
              required
            />
          </MoveToDataModal>

          {/* MODAL TO CONFIRM DELETE */}
          <MoveToDataModal
            headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
            heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
            open={deleteModal}
            onCancel={() => setDeleteModal(false)}
            footerButtonArea={
              <MoveToDataButton
                icon={<TrashIcon />}
                intent="dangerous"
                onClick={() =>
                  deleteGroupAPI(deleteGroupDetails.id).then(() => {
                    dispatch(getAllGroups());
                    setDeleteModal(false);
                  })
                }
              >
                {getLanguageLabel("delete")}
              </MoveToDataButton>
            }
          >
            {deleteGroupDetails.name} Group
          </MoveToDataModal>

          {/* ---------------------------------- */}

          <MoveToDataSwitch
            items={[
              {
                label: "Resource Groups",
                value: "resourceGroups",
                children: resourceGroups,
              },
              {
                label: "System Groups",
                value: "systemGroups",
                children: systemGroups,
              },
            ]}
            value={selectedTab}
            onChange={(newVal: string) => {
              setSelectedTab(newVal);
            }}
          />
        </>
      )}
    </>
  );
};

export default Groups;
