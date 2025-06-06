import { IProject } from "Apps/explorer/explorer";
import { Col, List, Row, Tooltip } from "antd";
import { RefreshIcon } from "assets/icons/boslerActionIcons";
import { ProjectIcon } from "assets/icons/boslerDataIcons";
import {
  AddUserIcon,
  CalendarIcon,
  KeyIcon,
  UserIcon,
} from "assets/icons/boslerInterfaceIcons";
import BoslerAvatarGroup from "components/BoslerComponents/BoslerAvatar/BoslerAvatarGroup";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerUserPopover from "components/UserPopover/userpopover";
import React from "react";
import {
  getLanguageLabel,
  getTimeDisplay,
  timeConverter,
} from "utils/utilities";
import styles from "../Project.module.scss";

interface IProps {
  project: IProject;
  setSelectedProject: any;
  openPermissionsModal: any;
  openRequestAccessModal: any;
}

const ProjectPageItem = ({
  project,
  setSelectedProject,
  openPermissionsModal,
  openRequestAccessModal,
}: IProps) => {
  return (
    <Row justify={"start"} gutter={[16, 16]}>
      <Col flex="auto" span={10}>
        <List.Item.Meta
          className={styles.listItemMeta}
          avatar={<ProjectIcon size={22} />}
          title={project.name}
          description={project.description}
          style={{
            // alignItems: "center",
            marginBlockEnd: "6px",
          }}
        ></List.Item.Meta>
      </Col>

      <Col flex="auto" span={8}>
        <div className={styles.listExtra}>
          {project.hasAccess ? (
            <Tooltip
              placement="left"
              title={getLanguageLabel("addPeopleToFolder")}
            >
              <BoslerButton
                outlined
                size={"small"}
                intent="none"
                icon={<AddUserIcon />}
                onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                  e.preventDefault();
                  e.stopPropagation();
                  setSelectedProject(project);
                  openPermissionsModal();
                }}
              >
                {getLanguageLabel("manageAccess")}
              </BoslerButton>
            </Tooltip>
          ) : (
            <BoslerButton
              outlined
              size={"small"}
              icon={<KeyIcon />}
              onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                e.preventDefault();
                e.stopPropagation();
                setSelectedProject(project);
                openRequestAccessModal();
              }}
            >
              {getLanguageLabel("requestAccess")}
            </BoslerButton>
          )}

          <Row gutter={[8, 8]} align={"middle"}>
            <Col>{getLanguageLabel("team")} :</Col>
            <Col>
              <BoslerAvatarGroup userIds={project.team} />
            </Col>
          </Row>
        </div>
      </Col>

      <Col className={styles.extraInfo} span={6}>
        <Row justify={"start"}>
          <UserIcon />
          <div className={styles.createdBy}>
            <BoslerUserPopover id={project.createdBy} />
          </div>
          <CalendarIcon />
          <Tooltip title={timeConverter(project.createdAt)}>
            {getTimeDisplay(project.createdAt)}
          </Tooltip>
        </Row>
        {project.updatedBy && (
          <Row justify={"start"}>
            <RefreshIcon />
            <div className={styles.createdBy}>
              <BoslerUserPopover id={project.updatedBy} />
            </div>
            <CalendarIcon />
            <Tooltip title={timeConverter(project.updatedAt)}>
              {getTimeDisplay(project.updatedAt)}
            </Tooltip>
          </Row>
        )}
      </Col>
    </Row>
  );
};

export default ProjectPageItem;
