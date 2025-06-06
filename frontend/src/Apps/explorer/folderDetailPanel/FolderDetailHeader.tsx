import { Col, Popover, Row, Tooltip } from "antd";
import { SearchIcon } from "assets/icons/boslerActionIcons";
import {
  CollectionIcon,
  SidePanelIcon,
  UnorderedListIcon,
} from "assets/icons/boslerInterfaceIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import Avatars from "components/Avatars/Avatars";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { updateUserDataAPI } from "components/CommandPalette/CommandPalette.api";
import Comments from "components/Comments/Comments.view";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import NewButton from "components/buttons/NewButton";
import { useFileExplorerService } from "hooks/useFileExplorerService";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router";
import { getLanguageLabel, isDefined, notEmpty } from "utils/utilities";
import { updateUserDetails } from "../../../redux/actions/userActions";
import { ThunkAppDispatch } from "../../../redux/types/store";
import { Breadcrumb } from "../BreadCrumb";
import { permanentDelete } from "../explorer.api";
import { specialIds } from "../explorer.constants";
import { ResourceType } from "../explorer.utils";

interface Props {
  listView?: boolean;
  setListView?: React.Dispatch<React.SetStateAction<boolean>>;
  setGlobalFilter?: any;
  activeId?: string;
  onClickBreadCrumb?: (id: string) => void;
  type?: ResourceType[];
  setIsSidePanelOpen?: any;
  isEditable: boolean;
}

export const FolderDetailHeader: React.FC<Props> = ({
  listView,
  setListView,
  isEditable,
  setGlobalFilter,
  onClickBreadCrumb,
  activeId,
  type,
  setIsSidePanelOpen,
}) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const [trashModal, setTrashModal] = useState(false);
  const [modalProps, setModalProps] = useState<any>({
    open: false,
    closeable: true,
    onCancel: <></>,
    heading: "",
    headingIcon: <></>,
    footerButtonArea: <></>,
    children: <></>,
  });

  const { rehydrateRecycleBin } = useFileExplorerService();

  const { id } = useParams();

  return (
    <div className="explorer-header">
      {notEmpty(activeId) && (
        <Breadcrumb onClick={onClickBreadCrumb} id={activeId} />
      )}

      <div style={{ marginLeft: "auto" }}>
        <Row gutter={16} align="middle">
          <Col>
            {activeId == "RECYCLE_BIN" && (
              <BoslerButton
                icon={<TrashIcon />}
                onClick={() => {
                  if (notEmpty(id)) {
                    setTrashModal(true);
                  }
                }}
                intent="dangerous"
              >
                {getLanguageLabel("empty")}
              </BoslerButton>
            )}
          </Col>
          <Col>
            <Popover
              content={
                <BoslerInput
                  size="small"
                  placeholder={getLanguageLabel("searchInFolder")}
                  allowClear
                  onChange={(e) => {
                    setGlobalFilter?.(e.target.value);
                  }}
                  suffix={<SearchIcon />}
                />
              }
            >
              <SearchIcon />
            </Popover>
          </Col>
          <Col>
            {setListView && (
              <Tooltip
                title={
                  listView ? getLanguageLabel("grid") : getLanguageLabel("rows")
                }
                placement="bottom"
              >
                <BoslerButton
                  onClick={() =>
                    setListView((state: boolean) => {
                      updateUserDataAPI({
                        ...user,
                        folderListView: !state,
                      }).then(({ data }) => {
                        dispatch(updateUserDetails(data));
                      });
                      return !state;
                    })
                  }
                  icon={
                    listView ? (
                      <CollectionIcon />
                    ) : (
                      <UnorderedListIcon size={20} />
                    )
                  }
                  icononly
                  trimicononlypadding
                  minimal
                ></BoslerButton>
              </Tooltip>
            )}
          </Col>

          <Col>
            {isDefined(activeId) && !specialIds.includes(activeId) && (
              <>
                <Comments id={activeId} />
              </>
            )}
          </Col>

          <Col>
            {notEmpty(id) && (
              <>
                <Avatars link={`/topic/${id}`} />
              </>
            )}
          </Col>
          <Col>
            <NewButton type={type} parent={activeId} />
          </Col>
          <Col>
            {isEditable && setIsSidePanelOpen && (
              <Tooltip title={getLanguageLabel("details")} placement="bottom">
                <BoslerButton
                  onClick={() =>
                    setIsSidePanelOpen((state: boolean) => {
                      updateUserDataAPI({
                        ...user,
                        sidePanelOpen: !state,
                      }).then(({ data }) => {
                        dispatch(updateUserDetails(data));
                      });
                      return !state;
                    })
                  }
                  icon={<SidePanelIcon />}
                  icononly
                  minimal
                  trimicononlypadding
                />
              </Tooltip>
            )}
          </Col>
        </Row>
      </div>
      <BoslerModal
        destroyOnClose
        open={trashModal}
        onCancel={() => {
          setTrashModal(false);
        }}
        heading={getLanguageLabel("permanentDelete")}
        headingIcon={<TrashIcon />}
        footerButtonArea={
          <BoslerButton
            intent="dangerous"
            onClick={() => {
              if (notEmpty(id)) {
                permanentDelete(id)
                  .then(() => {
                    rehydrateRecycleBin(id);
                    // openNotification(
                    //   "Trash Emptied Successfully",
                    //   "",
                    //   "success"
                    // );
                  })
                  .catch(({ response }) => {
                    // openNotification(
                    //   response.data.error,
                    //   response.data.description,
                    //   "error"
                    // );
                  })
                  .finally(() => {
                    setModalProps({
                      open: false,
                      heading: "",
                      headingIcon: <TrashIcon />,
                      footerButtonArea: <></>,
                      children: <></>,
                    });
                  });
              }
              setTrashModal(false);
            }}
            disabled={true}
          >
            {getLanguageLabel("empty")}
          </BoslerButton>
        }
      >
        {getLanguageLabel("areYouSureYouWantToDeleteThis?")}
      </BoslerModal>
    </div>
  );
};
