import React, { useEffect, useState } from "react";
import { Col, Divider, Row, Typography, Tooltip } from "antd";
import { useDispatch, useSelector } from "react-redux";
import OrpheaButton from "../../../components/ButtonComponent/OrpheaButton";
import OrpheaModal from "../../../components/OrpheaModalContainer";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import CreateNewTriggerModal from "./CreateNewTriggerModal";
import OrpheaLoader from "../../../components/orpheaLoader";
import TriggersTable from "./TriggersTable";
import { AddIcon } from "../../../assets/icons/orpheaActionIcons"; // Pour AddIcon
import { TrashIcon } from "../../../assets/icons/orpheaMiscellaneousIcons"; // Pour TrashIcon
import { getLanguageLabel, openNotification } from "utils/utilities";
import { deleteTriggerAPI, runTrigger, userById } from "../apis";
import Filters from "./Filters";

const { Title, Text } = Typography;

const Triggers = () => {
  const { allTriggers, loading } = useSelector(
    (state) => (state as any).allTriggerDetails
  );
  const dispatch = useDispatch();

  const [deleteModal, setDeleteModal] = useState(false);
  const [deleteTriggerDetails, setDeleteTriggerDetails] = useState({
    name: "",
    id: "",
  });
  const [isCreateNewTriggerModalOpen, setIsCreateNewTriggerModalOpen] =
    useState(false);
  const [filteredTriggers, setFilteredTriggers] = useState(allTriggers);

  useEffect(() => {
    dispatch(getAllTriggerDetails() as any);
  }, [dispatch]);

  // useEffect(() => {
  //   if (allTriggers) {
  //     setFilteredTriggers(allTriggers);
  //   }
  // }, [allTriggers]);

  useEffect(() => {}, [filteredTriggers]);

  const deleteTriggerHandler = () => {
    setDeleteModal(false);

    deleteTriggerAPI(deleteTriggerDetails.id)
      .then(() => {
        dispatch(getAllTriggerDetails() as any);
      })
      .catch(() => {
        openNotification(
          "Unable to Delete Trigger",
          "Trigger not found",
          "error"
        );
      });
  };

  const handleDeleteCancel = () => {
    setDeleteModal(false);
  };

  const handleFilterUpdate = (filteredData: any) => {
    setFilteredTriggers(filteredData);
  };

  if (loading) return <OrpheaLoader />;

  return (
    <>
      <OrpheaModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteTriggerHandler()}
        footerButtonArea={
          <OrpheaButton
            icon={<TrashIcon />}
            onClick={() => deleteTriggerHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </OrpheaButton>
        }
      >
        {deleteTriggerDetails.name}
      </OrpheaModal>

      <CreateNewTriggerModal
        isOpen={isCreateNewTriggerModalOpen}
        setIsOpen={setIsCreateNewTriggerModalOpen}
      />

      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                Build Triggers ({allTriggers && allTriggers.length})
              </Title>
              <Text type="secondary">
                Here are orphea build triggers to build images.
              </Text>
              <Filters onFilterUpdate={handleFilterUpdate} />
            </Col>
            <Col>
              <Tooltip placement="top" title={"Create New Trigger"}>
                <OrpheaButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => {
                    setIsCreateNewTriggerModalOpen(true);
                  }}
                >
                  New Trigger
                </OrpheaButton>
              </Tooltip>
            </Col>
          </Row>

          <Divider />
        </p>
        <TriggersTable allTriggers={filteredTriggers} />
      </div>
    </>
  );
};

export default Triggers;
