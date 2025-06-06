import React from "react";

import { Col, Row, theme, Tooltip, Typography } from "antd";
import { AddIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import ProjectButton from "components/buttons/ProjectButton";
import { getLanguageLabel } from "utils/utilities";
import { IResourceFilters } from "../interfaces/Project";
import styles from "../Project.module.scss";
import FilterMenu from "./FilterMenu";
import {
  SortAlphaIcon,
  SortNumericAscHrizontalIcon,
  SortNumericDescHrizontalIcon,
  SortReverseAlphaIcon,
} from "assets/icons/boslerSortIcons";
import {
  RESOURCE_FILTER_FIELDS,
  RESOURCE_SORT_BY_TYPE,
  RESOURCE_SORT_DIRECTION,
} from "../utils/Projects.utils";
import classNames from "classnames";
import { SELECTED_SORT_BY_COLOR } from "../Project.constants";

const { Title, Text } = Typography;

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
  const isSortingByName = filters.sortBy == RESOURCE_SORT_BY_TYPE.NAME;
  const isSortingByCreatedAt =
    filters.sortBy == RESOURCE_SORT_BY_TYPE.CREATED_AT;
  const isSortDesc = filters.sortDirection == RESOURCE_SORT_DIRECTION.DESC;

  const handleChangeSortDirection = (isCurrentSortByButtonClick: boolean) => {
    updateFilters(
      RESOURCE_FILTER_FIELDS.SORT_DIRECTION,
      isSortDesc || isCurrentSortByButtonClick
        ? RESOURCE_SORT_DIRECTION.ASC
        : RESOURCE_SORT_DIRECTION.DESC
    );
  };

  const handleSortNameClick = () => {
    if (!isSortingByName)
      updateFilters(RESOURCE_FILTER_FIELDS.SORT_BY, RESOURCE_SORT_BY_TYPE.NAME);

    handleChangeSortDirection(!isSortingByName);
  };

  const handleSortCreatedAtClick = () => {
    if (!isSortingByCreatedAt)
      updateFilters(
        RESOURCE_FILTER_FIELDS.SORT_BY,
        RESOURCE_SORT_BY_TYPE.CREATED_AT
      );

    handleChangeSortDirection(!isSortingByCreatedAt);
  };

  return (
    <>
      <div className={styles.header}>
        <Row justify={"space-between"} align={"middle"}>
          <Col>
            <Title level={3}>{getLanguageLabel("projects")}</Title>
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
        <br />
        <FilterMenu
          filters={filters}
          updateFilters={updateFilters}
          resetFilters={resetFilters}
        />
      </div>

      <Row
        justify="space-around"
        align="middle"
        gutter={[16, 16]}
        className={styles.tableHeader}
      >
        <Col span={10}>
          <Row align={"middle"} gutter={[8, 8]}>
            <Col>
              <Text
                type="secondary"
                strong
                className={classNames(styles.tableHeaderItem)}
              >
                {getLanguageLabel("name").toUpperCase()}
              </Text>
            </Col>
            <Col>
              <BoslerButton
                minimal
                icononly
                intent="none"
                trimicononlypadding
                borderless
                size="small"
                iconColor={isSortingByName && SELECTED_SORT_BY_COLOR}
                icon={
                  isSortingByName && isSortDesc ? (
                    <SortReverseAlphaIcon size={11} />
                  ) : (
                    <SortAlphaIcon size={11} />
                  )
                }
                onClick={handleSortNameClick}
              >
                {getLanguageLabel("sort")} : {RESOURCE_SORT_BY_TYPE.NAME}
              </BoslerButton>
            </Col>
          </Row>
        </Col>
        <Col span={8}>
          <Text type={"secondary"} className={styles.tableHeaderItem} strong>
            {getLanguageLabel("access").toUpperCase()}
          </Text>
        </Col>
        <Col span={6}>
          <Row align={"middle"} gutter={[8, 8]}>
            <Col>
              <Text type="secondary" className={styles.tableHeaderItem} strong>
                {getLanguageLabel("info").toUpperCase()}
              </Text>
            </Col>
            <Col>
              <BoslerButton
                minimal
                icononly
                intent="none"
                trimicononlypadding
                borderless
                size="small"
                iconColor={isSortingByCreatedAt && SELECTED_SORT_BY_COLOR}
                icon={
                  isSortingByCreatedAt && isSortDesc ? (
                    <SortNumericDescHrizontalIcon size={11} />
                  ) : (
                    <SortNumericAscHrizontalIcon size={11} />
                  )
                }
                onClick={handleSortCreatedAtClick}
              >
                {getLanguageLabel("sort")} : {RESOURCE_SORT_BY_TYPE.CREATED_AT}
              </BoslerButton>
            </Col>
          </Row>
        </Col>
      </Row>
    </>
  );
};

export default ProjectPageHeader;
