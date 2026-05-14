import {
  Avatar,
  Col,
  Divider,
  Dropdown,
  Row,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import UserPopOver from "components/UserPopover/userpopover";
import { getAllUserDetails } from "redux/actions/userActions";

import React from "react";
import { useNavigate } from "react-router-dom";
import {
  getLanguageLabel,
  getTimeDisplay, globalSearch,
  openNotification,
} from "utils/utilities";
import { MoreMenuIcon, SearchIcon } from "assets/icons/orpheaActionIcons";
import { EditIcon } from "assets/icons/orpheaEditorIcons";

import { ThunkAppDispatch } from "redux/types/store";

import { AddUserIcon } from "assets/icons/orpheaInterfaceIcons";
import { getDefaultFavicon } from "components/orpheaLoader/FavIconLoader";
import {
  InfoIcon,
  TrashIcon,
} from "assets/icons/orpheaMiscellaneousIcons";
import OrpheaLoader from "components/orpheaLoader";
import CreateNewUserModal from "./CreateNewUserModal";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaModal from "components/OrpheaModalContainer";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import { deleteUserAPI } from "../apis";

const { Title, Text } = Typography;

const Users = () => {
  const [FilteredData, setFilteredData] = useState();
  const { allusers, loading } = useSelector(
    (state) => (state as $TSFixMe).allUserDetails
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const { user: userAdmin } = useSelector((state) => (state as any).userAdmin);
  const dispatch = useDispatch<ThunkAppDispatch>();

  const [deleteModal, setDeleteModal] = useState(false);
  const [deleteUserDetails, setDeleteUserDetails] = useState({
    name: "",
    id: "",
  });
  const [isCreateNewUserModalOpen, setIsCreateNewUserModalOpen] =
    useState(false);

  // const [userAdmin, setUserAdmin]= useState();
  const navigate = useNavigate();

  const deleteUserHandler = () => {
    setDeleteModal(false);

    deleteUserAPI(deleteUserDetails.id)
      .then(() => {
        dispatch(getAllUserDetails());
      })
      .catch(() => {
        openNotification("Unable to Delete User", "User not found", "error");
      });
  };

  const columns = [
    {
      title: getLanguageLabel("userName"),
      dataIndex: "username",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.username.localeCompare(b.username),
      width: "20%",
      render: (text: $TSFixMe, row: $TSFixMe) => (
        <>
          <UserPopOver record={row}>
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
                src={
                  row.profileImage && row.profileImage != ""
                    ? row.profileImage
                    : null
                }
              >
                {row.name ? row.name.charAt(0).toUpperCase() : "B"}
              </Avatar>
              <div
                className="pop-over-item"
                onClick={(e) => navigate(`/portal/settings/user/${row.id}`)}
              >
                {text}
              </div>
            </div>
          </UserPopOver>
        </>
      ),
    },
    {
      title: getLanguageLabel("givenName"),
      dataIndex: "givenName",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.givenName.localeCompare(b.givenName),
    },
    {
      title: getLanguageLabel("familyName"),
      dataIndex: "familyName",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.familyName.localeCompare(b.familyName),
    },
    {
      title: getLanguageLabel("location"),
      dataIndex: "location",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.location.localeCompare(b.location),
    },
    {
      title: getLanguageLabel("email"),
      dataIndex: "email",
      sorter: (a: $TSFixMe, b: $TSFixMe) => a.email.localeCompare(b.email),
    },
    {
      title: getLanguageLabel("lastLogin"),
      dataIndex: "lastLoginAt",
      render: (text: $TSFixMe, record: $TSFixMe) => {
        return (
          <>
            <Row justify="space-between">
              <Col>
                {record.lastLoginAt
                  ? getTimeDisplay(record.lastLoginAt)
                  : "Never"}
              </Col>
              {
                // condition of isUserAdmin or isPlatformAdmin
                <Col>
                  <Dropdown
                    menu={{
                      items: [
                        {
                          label: (
                            <>
                              {platformAdmin || userAdmin ? (
                                <div
                                  onClick={(e) =>
                                    navigate(
                                      `/portal/settings/user/${record.id}`
                                    )
                                  }
                                  className="text-and-icon-center"
                                  style={{ width: "100%" }}
                                >
                                  <EditIcon />
                                  {getLanguageLabel("edit")}
                                </div>
                              ) : (
                                <div
                                  onClick={(e) =>
                                    navigate(
                                      `/portal/settings/user/${record.id}`
                                    )
                                  }
                                  className="text-and-icon-center"
                                  style={{ width: "100%" }}
                                >
                                  <InfoIcon />
                                  {getLanguageLabel("info")}
                                </div>
                              )}
                            </>
                          ),
                          key: 0,
                        },
                        {
                          label: (
                            <>
                              <div
                                onClick={() => {
                                  setDeleteUserDetails({ ...record });
                                  setDeleteModal(true);
                                }}
                                className="text-and-icon-center"
                                style={{ color: "var(--orphea-intent-danger)" }}
                              >
                                <TrashIcon
                                  color={"var(--orphea-intent-danger)"}
                                />
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
          </>
        );
      },
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

  const handleDeleteCancel = () => {
    setDeleteModal(false);
  };

  useEffect(() => {
    dispatch(getAllUserDetails());
  }, []);

  useEffect(() => {
    return () => {
      let favicon = document.querySelector('link[rel="icon"]') as any;
      favicon.href = getDefaultFavicon();
    };
  }, [allusers]);

  if (loading) return <OrpheaLoader />;

  return (
    <>
      <OrpheaModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteUserHandler()}
        footerButtonArea={
          <OrpheaButton
            icon={<TrashIcon />}
            onClick={() => deleteUserHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </OrpheaButton>
        }
      >
        {deleteUserDetails.name}
      </OrpheaModal>

      <CreateNewUserModal
        isOpen={isCreateNewUserModalOpen}
        setIsOpen={setIsCreateNewUserModalOpen}
      />

      <div className="settings-center-block">
        <p>
          {/* <UserIcon size={30} /> */}
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                {getLanguageLabel("users")} ({allusers && allusers.length})
              </Title>
              <Text type="secondary">{getLanguageLabel("users")}</Text>
            </Col>
            <Col>
              {(platformAdmin || userAdmin) && (
                <Tooltip
                  placement="top"
                  title={getLanguageLabel("createANewUser")}
                >
                  <OrpheaButton
                    icon={<AddUserIcon />}
                    intent="action"
                    onClick={() => {
                      setIsCreateNewUserModalOpen(true);
                    }}
                  >
                    {getLanguageLabel("newUser")}
                  </OrpheaButton>
                </Tooltip>
              )}
            </Col>
          </Row>

          <Divider />
        </p>

        <OrpheaInput
          // size="small"
          placeholder={getLanguageLabel("searchUsersTable")}
          allowClear
          onChange={(e) => {
            setFilteredData(globalSearch(e.target.value, allusers, columns));
          }}
          suffix={<SearchIcon />}
        />

        <Table
          columns={columns}
          dataSource={FilteredData !== undefined ? FilteredData : allusers}
          onChange={onChange}
          className="interactive"
          pagination={false}
        />
      </div>
    </>
  );
};

export default Users;
