import {
  Avatar,
  Col,
  Divider,
  Dropdown,
  Row,
  Skeleton,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import BoslerUserPopover from "../../components/UserPopover/userpopover";
import UserButton from "../../components/buttons/Userbutton";
import GlobalSearch from "../../helpers/GlobalSearch";
import { getAllUserDetails } from "../../redux/actions/userActions";

import React from "react";
import { useNavigate } from "react-router-dom";
import {
  getLanguageLabel,
  getTimeDisplay,
  isDefined,
  openNotification,
} from "utils/utilities";
import { MoreMenuIcon, SearchIcon } from "../../assets/icons/boslerActionIcons";
import { EditIcon } from "../../assets/icons/boslerEditorIcons";
import { deleteUser } from "../../redux/actions/authActions";
import { ThunkAppDispatch } from "../../redux/types/store";

import classNames from "classnames";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader";
import {
  InfoIcon,
  TrashIcon,
} from "../../assets/icons/boslerMiscellaneousIcons";
import styles from "./Users.module.scss";

const { Title, Text } = Typography;

const Users = () => {
  const [FilteredData, setFilteredData] = useState();
  const { allusers } = useSelector(
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

  // const [userAdmin, setUserAdmin]= useState();
  const navigate = useNavigate();

  const deleteUserHandler = () => {
    setDeleteModal(false);
    try {
      dispatch(deleteUser(deleteUserDetails.id)).then(() => {
        dispatch(getAllUserDetails());
      });
    } catch (error) {
      openNotification("Unable to Delete User", "User not found", "error");
    }
  };

  const columns = [
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("userName").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "username",
      // fixed:"left"as unknown as "left",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.username.localeCompare(b.username),
      width: "20%",
      render: (text: $TSFixMe, row: $TSFixMe) => (
        <>
          <BoslerUserPopover record={row}>
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
          </BoslerUserPopover>
        </>
      ),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("givenName").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "givenName",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.givenName.localeCompare(b.givenName),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("familyName").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "familyName",
      sorter: (a: $TSFixMe, b: $TSFixMe) =>
        a.familyName.localeCompare(b.familyName),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("location").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "location",
      sorter: (a: $TSFixMe, b: $TSFixMe) => {
        let x = isDefined(a.location) ? a.location : "";
        let y = isDefined(b.location) ? b.location : "";

        return x.localeCompare(y);
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
          {getLanguageLabel("email").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "email",
      sorter: (a: $TSFixMe, b: $TSFixMe) => a.email.localeCompare(b.email),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("lastLogin").toUpperCase()}{" "}
        </Text>
      ),
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
                                style={{ color: "var(--bosler-intent-danger)" }}
                              >
                                <TrashIcon
                                  color={"var(--bosler-intent-danger)"}
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

  return (
    <>
      <BoslerModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteUserHandler()}
        footerButtonArea={
          <BoslerButton
            icon={<TrashIcon />}
            onClick={() => deleteUserHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </BoslerButton>
        }
      >
        {deleteUserDetails.name}
      </BoslerModal>

      {/* -------- */}

      {!allusers || allusers === "" ? (
        <>
          <div className="settings-center-block">
            <Skeleton
              active
              avatar
              paragraph={{ rows: 20 }}
              className={styles.listItem}
            />
          </div>
        </>
      ) : (
        <>
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
                  {platformAdmin || userAdmin ? (
                    <div className="text-and-icon-center">
                      <Tooltip
                        placement="top"
                        title={getLanguageLabel("createANewUser")}
                      >
                        <UserButton
                          title={getLanguageLabel("createANewUser")}
                        />
                      </Tooltip>
                    </div>
                  ) : (
                    ""
                  )}
                </Col>
              </Row>

              <Divider />
            </p>

            <BoslerInput
              // size="small"
              placeholder={getLanguageLabel("searchUsersTable")}
              allowClear
              onChange={(e) => {
                setFilteredData(
                  GlobalSearch(e.target.value, allusers, columns)
                );
              }}
              suffix={<SearchIcon />}
            />
            <Table
              size="middle"
              columns={columns}
              dataSource={FilteredData !== undefined ? FilteredData : allusers}
              onChange={onChange}
              className="interactive"
              pagination={false}
              scroll={{ x: true, y: "100%" }}
            />
          </div>
        </>
      )}
    </>
  );
};

export default Users;
