import { Col, Divider, Dropdown, Row, Table, Typography } from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { AddIcon, MoreMenuIcon } from "assets/icons/orpheaActionIcons";
import { EditIcon } from "assets/icons/orpheaEditorIcons";
import { TrashIcon } from "assets/icons/orpheaMiscellaneousIcons";
import { TickIcon } from "assets/icons/orpheaNavigationIcon";

import { AddUserIcon } from "assets/icons/orpheaInterfaceIcons";
import { getLanguageLabel, openNotification } from "utils/utilities";
import { fetchAllSSODetailsAPI } from "./apis";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import OrpheaModal from "components/OrpheaModalContainer/OrpheaModal";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
const { Text, Title } = Typography;

const SSO = () => {
  const [allSSODetails, setAllSSODetails] = useState([]);
  const [loading, setLoading] = useState(true);
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
    fetchAllSSODetailsAPI().then(({ data }) => {
      setAllSSODetails(data);
    });
  };

  const handleDelete = async (id: string) => {
    try {
      await axios.delete(`/passport/oath2/registrations/${id}`);
    } catch (error) {
      openNotification(" Unsuccessful Attempt. Try Again", " ", "error");
    }
    setDeleteModal(false);
    setDeleteSSO({ ...initialSSODetails });
    fetchAllSSODetailsAPI().then(({ data }) => {
      setAllSSODetails(data);
    });
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
    fetchAllSSODetailsAPI().then(({ data }) => {
      setAllSSODetails(data);
    });
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
            ) : (
              ""
            )}
          </Row>
        </>
      ),
    },
  ];

  useEffect(() => {
    fetchAllSSODetailsAPI().then(({ data }) => {
      setAllSSODetails(data);
      setLoading(false);
    });
  }, []);

  return (
    <>
      {/* create sso provider */}
      <OrpheaModal
        headingIcon={<AddUserIcon />}
        heading={getLanguageLabel("addNewSSODetails")}
        open={createView}
        onCancel={handleCreateCancel}
        onOk={handleCreateOk}
        footerButtonArea={
          <OrpheaButton
            icon={<TickIcon />}
            intent="action"
            key="submit"
            onClick={handleCreateOk}
          >
            {getLanguageLabel("create")}
          </OrpheaButton>
        }
      >
        <div className="OrpheaHeader1">{getLanguageLabel("name")}</div>

        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("description")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("providerName")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("clientId")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("clientSecret")}</div>
        <OrpheaInput
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
      </OrpheaModal>

      {/* are you sure you want to delete modal */}

      <OrpheaModal
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
          <OrpheaButton
            key="submit"
            onClick={() => handleDelete(deleteSSO.id)}
            intent="dangerous"
            icon={<TrashIcon />}
          >
            {getLanguageLabel("delete")}
          </OrpheaButton>
        }
      >
        {deleteSSO.name} SSO{" "}
      </OrpheaModal>

      <OrpheaModal
        headingIcon={<EditIcon />}
        heading={`${getLanguageLabel("edit")} SSO`}
        open={editModal}
        onCancel={handleEditCancel}
        // onOk={handleEdit}
        footerButtonArea={
          // <Button className="interactive" key="back" onClick={handleEditCancel}>
          //   <CrossIcon /> {getLanguageLabel("cancel")}
          // </Button>,
          <OrpheaButton
            icon={<TickIcon />}
            onClick={() => handleEdit(editSSO.id)}
          >
            {getLanguageLabel("edit")}
          </OrpheaButton>
        }
      >
        <div className="OrpheaHeader1">{getLanguageLabel("name")}</div>

        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("description")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("providerName")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("clientId")}</div>
        <OrpheaInput
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
        <div className="OrpheaHeader1">{getLanguageLabel("clientSecret")}</div>
        <OrpheaInput
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
      </OrpheaModal>

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
                <OrpheaButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => setCreateView(true)}
                >
                  {" "}
                  {getLanguageLabel("addSSOProvider")}{" "}
                </OrpheaButton>
              ) : (
                ""
              )}
            </Col>
          </Row>
          <Divider />
        </p>
        <Table
          loading={loading}
          dataSource={allSSODetails}
          columns={ssoColumns}
          pagination={false}
          className="interactive"
        />
      </div>
    </>
  );
};

export default SSO;
