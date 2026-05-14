import { Dropdown, Modal, Popover, Typography } from "antd";
import Avatars from "components/Avatars/Avatars";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import Comments from "components/Comments/Comments.view";
import { BoslerInfoPopover } from "components/CommonUI/BoslerInfoPopover/BoslerInfoPopover.view";

import DeleteModal from "components/Modals/DeleteModal";
import CustomBreadCrumb from "components/Nav/Manage/breadCrumb";
import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";

import { getLanguageLabel, isDefined } from "utils/utilities";
import { MoreMenuIcon } from "../../../assets/icons/boslerActionIcons";
import { GraphIcon } from "../../../assets/icons/boslerChartIcons";
import { EditIcon } from "../../../assets/icons/boslerEditorIcons";
import { TrashIcon } from "../../../assets/icons/boslerMiscellaneousIcons";
import { PopOutIcon } from "../../../assets/icons/boslerNavigationIcon";
import SourcesTargets from "../../../helpers/SourcesTargets";
import {
    deleteSource,
    listSources,
} from "../../../redux/actions/sourceActions";
import { ThunkAppDispatch } from "../../../redux/types/store";
import SourceModal from "./SourceModal.view";
import { useUserHook } from "hooks/useUsers";
import { User } from "global";

const { EditText } = require("react-edit-text");
const { Text } = Typography;
const { confirm } = Modal;

const SourceHeader = ({
  sourceDetails,
  setSourceDetails,
  parent,
  updateSource,
}: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const navigate = useNavigate();
  const [createdBy, setCreatedBy] = useState<User>();
  const [updatedBy, setUpdatedBy] = useState<User>();

  const [showPanel, setShowPanel] = useState(false);

  const [isUpdateSourceModalOpen, setIsUpdateSourceModalOpen] = useState(false);

  const [deleteServiceDetails, setDeleteServiceDetails] = useState({
    modalView: false,
    id: null,
    name: "",
    disabled: true,
  });

  const handleDelete = (resourceId: string) => {
    dispatch(deleteSource(resourceId)).then((data: $TSFixMe) => {
      navigate(`/portal/connect/`);
      dispatch(listSources());
    });
  };

  useUserHook(sourceDetails?.createdBy, {
    resolveCallback: (data) => {
      setCreatedBy(data);
    },
  });
  useUserHook(sourceDetails?.updatedBy, {
    resolveCallback: (data) => {
      setUpdatedBy(data);
    },
  });

  // useEffect(() => {
  //   if (sourceDetails != undefined) {
  //     if (isDefined(sourceDetails.createdBy)) {
  //       fetchUserDetailsAPI(sourceDetails.createdBy).then(({ data }) => {
  //         setCreatedBy(data);
  //       });
  //     }
  //     if (isDefined(sourceDetails.updatedBy)) {
  //       fetchUserDetailsAPI(sourceDetails.updatedBy).then(({ data }) => {
  //         setUpdatedBy(data);
  //       });
  //     }
  //   }
  // }, [sourceDetails]);

  return (
    <div
      className="connect-container-header"
      onMouseEnter={() => setShowPanel(true)}
      onMouseLeave={() => setShowPanel(false)}
    >
      <CustomBreadCrumb />

      <div className="connect-container-header-btns">
        <BoslerInfoPopover id={sourceDetails.id} type={sourceDetails.type} />
        <Comments id={sourceDetails.id} />
        <Avatars link={`/topic/${sourceDetails.id}`} />

        <Popover
          title={
            <Link to={`/portal/bezier/${sourceDetails.id}/master`}>
              <div
                className="text-and-icon-center"
                style={{
                  justifyContent: "space-between",
                  width: "100%",
                  color: "var(--bosler-font-color-muted)",
                }}
              >
                {getLanguageLabel("dataLineage")}
                <PopOutIcon />
              </div>
            </Link>
          }
          content={
            <>
              <SourcesTargets id={sourceDetails.id} branch={"master"} />
            </>
          }
          placement="bottom"
          overlayStyle={{ width: "20rem" }}
          // trigger={"click"}
        >
          <Link to={`/portal/bezier/${sourceDetails.id}/master`}>
            <BoslerButton
              icon={<GraphIcon />}
              icononly={true}
              minimal
              trimicononlypadding
            ></BoslerButton>
          </Link>
        </Popover>
        {/* Need a button here */}
        <Dropdown
          menu={{
            items: [
              {
                label: (
                  <>
                    <div
                      className="text-and-icon-center"
                      onClick={() => setIsUpdateSourceModalOpen(true)}
                      style={{
                        width: "100%",
                      }}
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
                      onClick={() =>
                        setDeleteServiceDetails({
                          ...deleteServiceDetails,
                          modalView: true,
                          name: sourceDetails.name,
                          id: sourceDetails.id,
                        })
                      }
                      className="text-and-icon-center"
                      style={{
                        color: "var(--bosler-intent-danger)",
                      }}
                    >
                      <TrashIcon color={"var(--bosler-intent-danger)"} />
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
      </div>

      <SourceModal
        isVisible={isUpdateSourceModalOpen}
        setIsVisible={setIsUpdateSourceModalOpen}
        defaultParent={sourceDetails.id}
        updateDetails={{
          sourceDetails: sourceDetails,
          parent: parent,
          updateSource: updateSource,
        }}
      />

      <DeleteModal
        deleteServiceDetails={deleteServiceDetails}
        setDeleteServiceDetails={setDeleteServiceDetails}
        handleDelete={handleDelete}
      />
    </div>
  );
};

export default SourceHeader;
