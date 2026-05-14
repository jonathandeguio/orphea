import React from "react";

import { Col, Divider, Row, theme, Tooltip, Typography } from "antd";
import { AddIcon } from "assets/icons/boslerActionIcons";
import { AllProjectsIcon } from "assets/icons/boslerDataIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import ProjectButton from "components/buttons/ProjectButton";
import { getLanguageLabel } from "utils/utilities";
import styles from "../Project.module.scss";
import { IResourceFilters } from "../interfaces/Project";
import FilterMenu from "./FilterMenu";

const { Title } = Typography;
const { useToken } = theme;

interface IProps {
  resurfaceProjectsToPage0: () => {};
  allowProjectCreation: boolean;
  filters: IResourceFilters;
  updateFilters: any;
  resetFilters: () => void;
}

const ProjectPageHeader = ({
  allowProjectCreation,
  resurfaceProjectsToPage0,
  filters,
  updateFilters,
  resetFilters,
}: IProps) => {
  return (
    <>
      <div className={styles.header}>
        <Row justify={"space-between"} align={"middle"}>
          <Col>
            <Title level={3}>
              <AllProjectsIcon size={20} /> {getLanguageLabel("projects")}
            </Title>
          </Col>
          <Col>
            <ProjectButton successCallback={resurfaceProjectsToPage0}>
              <Tooltip
                title={
                  allowProjectCreation
                    ? getLanguageLabel("createNewProject")
                    : getLanguageLabel("noAccessToCreateProjects")
                }
              >
                <BoslerButton
                  icon={<AddIcon />}
                  intent={allowProjectCreation ? "success" : "none"}
                  disabled={!allowProjectCreation}
                >
                  {getLanguageLabel("newProject")}
                </BoslerButton>
              </Tooltip>
            </ProjectButton>
          </Col>
        </Row>
        <Divider />
        <FilterMenu
          filters={filters}
          updateFilters={updateFilters}
          resetFilters={resetFilters}
        />
      </div>
    </>
  );
};

export default ProjectPageHeader;
