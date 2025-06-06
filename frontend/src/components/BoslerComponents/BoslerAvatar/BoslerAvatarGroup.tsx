import { Avatar } from "antd";
import { GroupProps } from "antd/es/avatar";
import React from "react";
import { isDefined } from "utils/utilities";
import BoslerAvatar from "./BoslerAvatar";
import { DEFAULT_MAX_GROUP_PROP } from "./BoslerAvatar.constants";

interface IProps extends GroupProps {
  userIds: string[];
}

const BoslerAvatarGroup = ({
  userIds,
  max = DEFAULT_MAX_GROUP_PROP,
  ...restProp
}: IProps) => {
  return (
    <Avatar.Group max={max} {...restProp} size={"small"}>
      {isDefined(userIds) &&
        userIds?.map((userId: string) => <BoslerAvatar userId={userId} />)}
    </Avatar.Group>
  );
};

export default BoslerAvatarGroup;
