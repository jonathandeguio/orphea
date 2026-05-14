import { Avatar } from "antd";
import React from "react";
import BoslerAvatar from "./BoslerAvatar";
import { isDefined } from "utils/utilities";
import { GroupProps } from "antd/es/avatar";
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
    <Avatar.Group max={max} {...restProp}>
      {isDefined(userIds) &&
        userIds?.map((userId: string) => <BoslerAvatar userId={userId} />)}
    </Avatar.Group>
  );
};

export default BoslerAvatarGroup;
