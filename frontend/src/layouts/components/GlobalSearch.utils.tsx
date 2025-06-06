import { Col, MenuProps, Row, Typography } from "antd";
import { IResource } from "global";
import React from "react";
const { Text } = Typography;

type MenuItem = Required<MenuProps>["items"][number];

function getItem(
  label: React.ReactNode,
  key?: React.Key | null,
  icon?: React.ReactNode,
  children?: MenuItem[],
  expandIcon?: React.ReactNode,
  onClick?: any
): MenuItem {
  return {
    label,
    key,
    icon,
    children,
    expandIcon,
    onClick,
  } as MenuItem;
}
export const bolserApplications: string[] = [
  "projects",
  "builds",
  "recentlyViewed",
  "favourites",
  "updatedByYou",
  "createdByYou",
  "schedules",
  "conect",
  "agent",
  "source",
  "link",
  "settings",
  "profile",
  "preferences",
  "notifications",
  "tokens",
  "users",
  "groups",
  "tags",
  "loginActivity",
  "changePassword",
  "sso",
];

export const removeDuplicateResources = (
  prevResources: IResource[],
  data: IResource[]
) => {
  const newResources = data.filter(
    (item: any) => !prevResources?.some((existing) => existing.id === item.id)
  );
  return prevResources?.concat(newResources);
};
