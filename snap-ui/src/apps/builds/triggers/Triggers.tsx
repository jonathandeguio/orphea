import React, { useEffect, useState } from "react";
import { Col, Divider, Row, Typography, Tooltip } from "antd";
import { useDispatch, useSelector } from "react-redux";
import MoveToDataButton from "../../../components/ButtonComponent/MoveToDataButton";
import MoveToDataModal from "../../../components/MoveToDataModalContainer";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import CreateNewTriggerModal from "./CreateNewTriggerModal";
import MoveToDataLoader from "../../../components/movetodataLoader";
import TriggersTable from "./TriggersTable";
import { AddIcon } from "../../../assets/icons/movetodataActionIcons"; // Pour AddIcon
import { TrashIcon } from "../../../assets/icons/movetodataMiscellaneousIcons"; // Pour TrashIcon
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

  if (loading) return <MoveToDataLoader />;

  return (
    <>
      <MoveToDataModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteTriggerHandler()}
        footerButtonArea={
          <MoveToDataButton
            icon={<TrashIcon />}
            onClick={() => deleteTriggerHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </MoveToDataButton>
        }
      >
        {deleteTriggerDetails.name}
      </MoveToDataModal>

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
                Here are movetodata build triggers to build images.
              </Text>
              <Filters onFilterUpdate={handleFilterUpdate} />
            </Col>
            <Col>
              <Tooltip placement="top" title={"Create New Trigger"}>
                <MoveToDataButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => {
                    setIsCreateNewTriggerModalOpen(true);
                  }}
                >
                  New Trigger
                </MoveToDataButton>
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
