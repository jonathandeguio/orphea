import { TChangeMetaData } from "./Group";

export const CHANGE_META_DATA_TYPES = {
  NAME: "NAME",
  DESCRIPTION: "DESCRIPTION",
};

export const GROUP_TYPE_NAME = {
  SYSTEM: "System",
  RESOURCE: "Resource",
};

export const ADMINISTRATORS_GROUPS = [
  "platform-administrators",
  "group-administrators",
  "user-administrators",
  "project-administrators",
  "connect-administrators",
];

export const getChangeMetaDataPayload = (
  id: string,
  type: TChangeMetaData,
  value: string
) => {
  switch (type) {
    case CHANGE_META_DATA_TYPES.NAME:
      return { id, name: value };
    case CHANGE_META_DATA_TYPES.DESCRIPTION:
      return { id, description: value };
  }
};
