import { IProject } from "Apps/explorer/explorer";
import { List, Skeleton } from "antd";
import { ProjectIcon } from "assets/icons/boslerDataIcons";
import { ContextMenu, MenuItem } from "common/components/ContextMenu";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import NoData from "components/CommonUI/NoData";
import React, { Dispatch, SetStateAction } from "react";
import { getLanguageLabel, isDefined } from "utils/utilities";
import styles from "../Project.module.scss";
import { IResourceFilters } from "../interfaces/Project";
import { RESOURCE_FILTER_FIELDS } from "../utils/Projects.utils";
import ProjectPageItem from "./ProjectPageItem";

interface IProps {
  updateFilters: any;
  filters: IResourceFilters;
  openRequestAccessModal: () => void;
  openPermissionsModal: () => void;
  setSelectedProject: any;
  navigator: any;
  projects: IProject[] | undefined;
  isEmpty: boolean | undefined;
  lastElementRef: (node: HTMLDivElement | null) => void;
  loading: boolean;
  isLoadingMore: boolean;
  contextMenuItems: MenuItem[];
  contextMenuStore: any;
  setContextMenuId: Dispatch<SetStateAction<string | undefined>>;
}

const ProjectPageBody = ({
  openRequestAccessModal,
  filters,
  updateFilters,
  openPermissionsModal,
  setSelectedProject,
  navigator,
  projects,
  isEmpty,
  lastElementRef,
  loading,
  isLoadingMore,
  contextMenuItems,
  contextMenuStore,
  setContextMenuId,
}: IProps) => {
  return (
    <>
      <List
        size="small"
        split
        itemLayout="vertical"
        className={styles.list}
        dataSource={projects}
        renderItem={(project: IProject, index) => (
          <Skeleton
            loading={loading}
            active
            avatar
            paragraph={{ rows: 2 }}
            className={styles.listItem}
          >
            {!loading && (
              <List.Item
                key={project.id}
                className={styles.listItem}
                onClick={() => {
                  if (project.hasAccess) navigator(project.id);
                  else {
                    setSelectedProject(project);
                    openRequestAccessModal();
                  }
                }}
                onContextMenu={(e: React.MouseEvent<HTMLDivElement>) => {
                  e.preventDefault();
                  setContextMenuId(project?.id);
                  contextMenuStore.displayContextMenu(e.pageX, e.pageY);
                }}
                {...(projects && index == projects.length - 1
                  ? { ref: lastElementRef }
                  : {})}
              >
                <ProjectPageItem
                  project={project}
                  setSelectedProject={setSelectedProject}
                  openPermissionsModal={openPermissionsModal}
                  openRequestAccessModal={openRequestAccessModal}
                />
              </List.Item>
            )}
          </Skeleton>
        )}
      >
        {isEmpty && (
          <div className={styles.noData}>
            <NoData
              icon={<ProjectIcon size={"30px"} />}
              heading="No Projects Yet"
              subHeading={getLanguageLabel("noPermissionWarning")} //"Start by adding a new project to get things rolling!"
            />
            <div className={styles.allProjectsButton}>
              &nbsp;&nbsp;
              {isDefined(filters.permissions) && (
                <>
                  <BoslerButton
                    intent="none"
                    onClick={() => {
                      updateFilters(
                        RESOURCE_FILTER_FIELDS.PERMISSIONS,
                        undefined
                      );
                    }}
                  >
                    {getLanguageLabel("clickToViewProjects")}
                  </BoslerButton>
                </>
              )}
            </div>
          </div>
        )}
        {isLoadingMore &&
          Array.from({ length: 10 }, (_, index) => (
            <Skeleton
              key={index}
              active
              avatar
              paragraph={{ rows: 2 }}
              className={styles.listItem}
            />
          ))}

        <ContextMenu items={contextMenuItems} {...contextMenuStore} />
      </List>
    </>
  );
};

export default ProjectPageBody;
