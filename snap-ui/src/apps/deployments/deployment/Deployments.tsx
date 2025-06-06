import { Col, Divider, Row, Tooltip, Typography } from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import React from "react";
import {
  getLanguageLabel,
  globalSearch,
  openNotification,
} from "utils/utilities";
import { AddIcon, SearchIcon } from "assets/icons/boslerActionIcons";

import { ThunkAppDispatch } from "redux/types/store";

import BoslerLoader from "components/boslerLoader";
import BoslerModal from "components/BoslerModalContainer";
import BoslerButton from "components/ButtonComponent/BoslerButton";
import BoslerInput from "components/InputComponent/BoslerInput";
import { deleteDeploymentAPI } from "../apis";
import CreateNewDeploymentModal from "./CreateNewDeploymentModal";
import DeploymentsTable from "./DeployementsTable";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import { getAllDeploymentDetails } from "redux/actions/DeploymentActions";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader"; // Import the new component

const { Title, Text } = Typography;

const Deployments = () => {
  const [FilteredData, setFilteredData] = useState();

  // UseSelector to fetch deployment details from redux store
  const { allDeployments, loading } = useSelector(
    (state: any) => state.allDeploymentDetails
  );

  const dispatch = useDispatch<ThunkAppDispatch>();

  const [deleteModal, setDeleteModal] = useState(false);
  const [deleteDeploymentDetails, setDeleteDeploymentDetails] = useState({
    name: "",
    id: "",
  });
  const [isCreateNewDeploymentModalOpen, setIsCreateNewDeploymentModalOpen] =
    useState(false);

  const deleteDeploymentHandler = () => {
    setDeleteModal(false);

    deleteDeploymentAPI(deleteDeploymentDetails.id)
      .then(() => {
        dispatch(getAllDeploymentDetails());
      })
      .catch(() => {
        openNotification(
          "Unable to Delete Deployment",
          "Deployment not found",
          "error"
        );
      });
  };

  const handleDeleteCancel = () => {
    setDeleteModal(false);
  };

  // Use useEffect to fetch deployments only if they don't exist
  useEffect(() => {
    dispatch(getAllDeploymentDetails());
  }, []);

  useEffect(() => {
    return () => {
      let favicon = document.querySelector('link[rel="icon"]') as any;
      favicon.href = getDefaultFavicon();
    };
  }, [allDeployments]);

  if (loading) return <BoslerLoader />;

  return (
    <>
      <BoslerModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteDeploymentHandler()}
        footerButtonArea={
          <BoslerButton
            icon={<TrashIcon />}
            onClick={() => deleteDeploymentHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </BoslerButton>
        }
      >
        {deleteDeploymentDetails.name}
      </BoslerModal>

      <CreateNewDeploymentModal
        isOpen={isCreateNewDeploymentModalOpen}
        setIsOpen={setIsCreateNewDeploymentModalOpen}
      />

      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                Deployments ({allDeployments && allDeployments.length})
              </Title>
              <Text type="secondary">Here are bosler deployments.</Text>
            </Col>
            <Col>
              <Tooltip placement="top" title={"Create New Deployment"}>
                <BoslerButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => {
                    setIsCreateNewDeploymentModalOpen(true);
                  }}
                >
                  New Deployment
                </BoslerButton>
              </Tooltip>
            </Col>
          </Row>

          <Divider />
        </p>

        <BoslerInput
          placeholder={getLanguageLabel("search")}
          allowClear
          onChange={(e: any) => {
            setFilteredData(globalSearch(e.target.value, allDeployments, []));
          }}
          suffix={<SearchIcon />}
        />

        <DeploymentsTable
          dataSource={
            FilteredData !== undefined ? FilteredData : allDeployments
          }
          setDeleteDeploymentDetails={setDeleteDeploymentDetails}
          setDeleteModal={setDeleteModal}
        />
      </div>
    </>
  );
};

export default Deployments;
