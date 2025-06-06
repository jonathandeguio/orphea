import { IRequestAccessReview } from "Apps/AccessManager/AccessManager";
import { Descriptions, DescriptionsProps, Form } from "antd";
import {
  DESCRIPTION_1_COL_RESPONSIVE_SPAN,
  DESCRIPTION_2_COL_RESPONSIVE_SPAN,
} from "pages/Settings/settings.utils";
import React from "react";
import { REQUEST_ACCESS_TYPE, ROLES } from "../RequestAccessModal.utils";

interface IProps {
  accessRequest: IRequestAccessReview;
}

const { Item } = Form;

export const ReviewForm = ({ accessRequest }: IProps) => {
  const isProjectAccess = accessRequest.type == REQUEST_ACCESS_TYPE.PROJECT;
  const items: DescriptionsProps["items"] = [
    {
      label: "Title",
      span: DESCRIPTION_2_COL_RESPONSIVE_SPAN,
      children: accessRequest.title,
    },
    {
      label: "Description",
      span: DESCRIPTION_2_COL_RESPONSIVE_SPAN,
      children: accessRequest.description,
    },
    {
      label: "Type",
      span: DESCRIPTION_2_COL_RESPONSIVE_SPAN,
      children: accessRequest.type,
    },
    {
      label: "Role",
      span: DESCRIPTION_1_COL_RESPONSIVE_SPAN,
      children: isProjectAccess
        ? accessRequest.role
        : ROLES[accessRequest.role].administratorName,
    },
    {
      label: isProjectAccess ? "Project Name" : "Administrator Group",
      span: DESCRIPTION_1_COL_RESPONSIVE_SPAN,
      children: accessRequest.requestTargetName,
    },
    {
      label: "Requesters",
      span: DESCRIPTION_1_COL_RESPONSIVE_SPAN,
      children: accessRequest.requesterNames.join(", "),
    },
  ];
  return (
    <>
      <Descriptions layout="vertical" bordered items={items} />
    </>
  );
};
