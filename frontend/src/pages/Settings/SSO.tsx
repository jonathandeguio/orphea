import { Col, Divider, Dropdown, Row, Table, Typography } from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { AddIcon, MoreMenuIcon } from "../../assets/icons/boslerActionIcons";
import { EditIcon } from "../../assets/icons/boslerEditorIcons";
import { TrashIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import BoslerButton from "../../components/BoslerComponents/ButtonComponent/BoslerButton";

import { getSSODetails } from "../../redux/actions/authActions";

import { AddUserIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { getLanguageLabel, openNotification } from "utils/utilities";
import { ThunkAppDispatch } from "../../redux/types/store";
const { Text, Title } = Typography;

const SSO = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { ssoDetails, loading } = useSelector(
    (state) => (state as $TSFixMe).sso
  );
  const initialSSODetails = {
    id: "",
    name: "",
    description: "",
    providerName: "",
    clientId: "",
    clientSecret: "",
  };
  const [newSSODetails, setNewSSODetails] = useState({ ...initialSSODetails });
  const [createView, setCreateView] = useState(false);
  const [deleteModal, setDeleteModal] = useState(false);
  const [editModal, setEditModal] = useState(false);
  const [editSSO, setEditSSO] = useState({ ...initialSSODetails });
  const [deleteSSO, setDeleteSSO] = useState({ ...initialSSODetails });
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );

  const handleCreateCancel = () => {
    setCreateView(false);
  };

  const handleDeleteCancel = () => {
    setDeleteModal(false);
  };

  const handleEditCancel = () => {
    setEditModal(false);
  };

  const handleCreateOk = async () => {
    if (
      newSSODetails.name == "" ||
      newSSODetails.description == "" ||
      newSSODetails.providerName == "" ||
      newSSODetails.clientId == "" ||
      newSSODetails.clientSecret == ""
    ) {
      openNotification(
        "Details Incomplete",
        "Please enter the complete details",
        "warning"
      );
      return;
    }
    try {
      await axios.post(
        `/passport/oath2/registrations`,
        JSON.stringify({
          ...newSSODetails,
        })
      );
    } catch (error) {
      openNotification(" Unsuccessful Attempt. Try Again", " ", "error");
    }
    setCreateView(false);
    setNewSSODetails({ ...initialSSODetails });
    dispatch(getSSODetails());
  };

  const handleDelete = async (id: string) => {
    try {
      await axios.delete(`/passport/oath2/registrations/${id}`);
    } catch (error) {
      openNotification(" Unsuccessful Attempt. Try Again", " ", "error");
    }
    setDeleteModal(false);
    setDeleteSSO({ ...initialSSODetails });
    dispatch(getSSODetails());
  };

  const handleEdit = async (id: string) => {
    try {
      await axios.put(
        `/passport/oath2/registrations/${id}`,
        JSON.stringify({
          ...editSSO,
        })
      );
    } catch (error) {
      openNotification(" Unsuccessful Attempt. Try Again", " ", "error");
    }
    setEditModal(false);
    setEditSSO({ ...initialSSODetails });
    dispatch(getSSODetails());
  };

  const ssoColumns = [
    {
      title: getLanguageLabel("name"),
      dataIndex: "name",
      key: "name",
      width: "30%",
    },
    {
      title: getLanguageLabel("description"),
      dataIndex: "description",
      key: "description",
      render: (text: any, record: any) => (
        <>
          <Row justify="space-between">
            <Col>{text}</Col>
            {platformAdmin ? (
              <Col>
                <Dropdown
                  menu={{
                    items: [
                      {
                        label: (
                          <>
                            <div
                              onClick={() => {
                                setEditSSO({ ...record });
                                setEditModal(true);
                              }}
                              className="text-and-icon-center"
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
                                setDeleteSSO({ ...record });
                                setDeleteModal(true);
                              }}
                              className="text-and-icon-center"
                            >
                              <TrashIcon
                                color={"var(--movetodata-intent-danger)"}
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
            ) : (
              ""
            )}
          </Row>
        </>
      ),
    },
  ];

  useEffect(() => {
    dispatch(getSSODetails());
  }, []);

  const createSampleData = async () => {
    try {
      await axios.get(`/kitab/sampleData`);
    } catch (error) {
      openNotification("unable to get sample Data", " ", "error");
    }
  };

  return (
    <>
      {/* create sso provider */}
      <BoslerModal
        headingIcon={<AddUserIcon />}
        heading={getLanguageLabel("addNewSSODetails")}
        open={createView}
        onCancel={handleCreateCancel}
        onOk={handleCreateOk}
        footerButtonArea={
          <BoslerButton
            icon={<TickIcon />}
            intent="action"
            key="submit"
            onClick={handleCreateOk}
          >
            {getLanguageLabel("create")}
          </BoslerButton>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("name")}</div>

        <BoslerInput
          onChange={(e) =>
            setNewSSODetails({
              ...newSSODetails,
              name: e.target.value,
            })
          }
          value={newSSODetails.name}
          name="Uname"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <BoslerInput
          onChange={(e) =>
            setNewSSODetails({
              ...newSSODetails,
              description: e.target.value,
            })
          }
          value={newSSODetails.description}
          name="description"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("providerName")}</div>
        <BoslerInput
          onChange={(e) =>
            setNewSSODetails({
              ...newSSODetails,
              providerName: e.target.value,
            })
          }
          value={newSSODetails.providerName}
          name="providerName"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("clientId")}</div>
        <BoslerInput
          onChange={(e) =>
            setNewSSODetails({
              ...newSSODetails,
              clientId: e.target.value,
            })
          }
          value={newSSODetails.clientId}
          name="clientId"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("clientSecret")}</div>
        <BoslerInput
          onChange={(e) =>
            setNewSSODetails({
              ...newSSODetails,
              clientSecret: e.target.value,
            })
          }
          value={newSSODetails.clientSecret}
          name="clientSecret"
          required
        />
      </BoslerModal>

      {/* are you sure you want to delete modal */}

      <BoslerModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => handleDelete(deleteSSO.id)}
        footerButtonArea={
          // <Button
          //   className="interactive"
          //   key="back"
          //   onClick={handleDeleteCancel}
          // >
          //   <CrossIcon /> {getLanguageLabel("cancel")}
          // </Button>,
          <BoslerButton
            key="submit"
            onClick={() => handleDelete(deleteSSO.id)}
            intent="dangerous"
            icon={<TrashIcon />}
          >
            {getLanguageLabel("delete")}
          </BoslerButton>
        }
      >
        {deleteSSO.name} SSO{" "}
      </BoslerModal>

      <BoslerModal
        headingIcon={<EditIcon />}
        heading={`${getLanguageLabel("edit")} SSO`}
        open={editModal}
        onCancel={handleEditCancel}
        // onOk={handleEdit}
        footerButtonArea={
          // <Button className="interactive" key="back" onClick={handleEditCancel}>
          //   <CrossIcon /> {getLanguageLabel("cancel")}
          // </Button>,
          <BoslerButton
            icon={<TickIcon />}
            onClick={() => handleEdit(editSSO.id)}
          >
            {getLanguageLabel("edit")}
          </BoslerButton>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("name")}</div>

        <BoslerInput
          onChange={(e) =>
            setEditSSO({
              ...editSSO,
              name: e.target.value,
            })
          }
          value={editSSO.name}
          name="Uname"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <BoslerInput
          onChange={(e) =>
            setEditSSO({
              ...editSSO,
              description: e.target.value,
            })
          }
          value={editSSO.description}
          name="description"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("providerName")}</div>
        <BoslerInput
          onChange={(e) =>
            setEditSSO({
              ...editSSO,
              providerName: e.target.value,
            })
          }
          value={editSSO.providerName}
          name="providerName"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("clientId")}</div>
        <BoslerInput
          onChange={(e) =>
            setEditSSO({
              ...editSSO,
              clientId: e.target.value,
            })
          }
          value={editSSO.clientId}
          name="clientId"
          required
        />
        <div className="BoslerHeader1">{getLanguageLabel("clientSecret")}</div>
        <BoslerInput
          onChange={(e) =>
            setEditSSO({
              ...editSSO,
              clientSecret: e.target.value,
            })
          }
          value={editSSO.clientSecret}
          name="clientSecret"
          required
        />
      </BoslerModal>

      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>SSO</Title>
              <Text type="secondary">
                {getLanguageLabel("setupSingleSignOn")} (SSO)
              </Text>
            </Col>
            <Col>
              {platformAdmin ? (
                <BoslerButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => setCreateView(true)}
                >
                  {" "}
                  {getLanguageLabel("addSSOProvider")}{" "}
                </BoslerButton>
              ) : (
                ""
              )}
            </Col>
          </Row>
          <Divider />
        </p>
        <Table
          loading={loading}
          dataSource={ssoDetails}
          columns={ssoColumns}
          pagination={false}
          className="interactive"
        />
      </div>
    </>
  );
};

export default SSO;
