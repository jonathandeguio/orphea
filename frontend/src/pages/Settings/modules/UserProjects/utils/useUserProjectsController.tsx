import React, { useEffect, useState } from "react";
import { getLanguageLabel, isEmpty } from "utils/utilities";
import { getAllUserProjectsAPI } from "./userProjects.utils";
import { Col, Row } from "antd";
import { ROLES } from "Apps/AccessManager/RequestAccessModal/RequestAccessModal.utils";
import { IUserProjects } from "../UserProjects.interfaces";
import { ProjectIcon } from "assets/icons/boslerDataIcons";
import classNames from "classnames";
import { useNavigate } from "react-router";

export const useUserProjectsController = ({ userId }: { userId: string }) => {
  const [loading, setLoading] = useState(false);
  const [projectsWithUserRole, setProjectsWithUserRole] = useState([]);
  const [isCurrentUserAdmin, setIsCurrentUserAdmin] = useState(false);
  const navigate = useNavigate();
  const isListEmpty = projectsWithUserRole && isEmpty(projectsWithUserRole);

  const userProjectColumns = [
    {
      title: getLanguageLabel("name"),
      dataIndex: "name",
      key: "name",
      render: (
        projectName: string,
        project: IUserProjects.IProjectWithUserRole
      ) => (
        <Row
          justify={"start"}
          className={classNames("pop-over-item")}
          onClick={() =>
            navigate(
              `/portal/kitab/folder/${project.id}?activeId=${project.id}`
            )
          }
          gutter={[8, 8]}
        >
          <Col>
            <ProjectIcon />
          </Col>
          <Col>{projectName}</Col>
        </Row>
      ),
    },
    {
      title: getLanguageLabel("description"),
      dataIndex: "description",
      key: "description",
    },
    {
      title: "Role",
      dataIndex: "userRole",
      key: "userRole",
      render: (role: IRole) => {
        return (
          <Row
            gutter={[8, 0]}
            align={"bottom"}
            className="text-and-icon-center"
          >
            <Col>{ROLES[role.name.toUpperCase()].icon}</Col>
            <Col>{ROLES[role.name.toUpperCase()].name}</Col>
          </Row>
        );
      },
    },
  ];

  const resurfaceRequests = async () => {
    const { data } = await getAllUserProjectsAPI(userId);
    setProjectsWithUserRole(data.projectList);
    setIsCurrentUserAdmin(data.projectOrPlatformAdmin);
    setLoading(false);
  };

  useEffect(() => {
    resurfaceRequests();
  }, []);

  return {
    isCurrentUserAdmin,
    projectsWithUserRole,
    loading,
    isListEmpty,
    userProjectColumns,
  };
};
