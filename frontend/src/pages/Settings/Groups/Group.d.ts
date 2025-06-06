import { CHANGE_META_DATA_TYPES } from "./Groups.utils";

interface Group {
  id: string;
  name: string;
  description: string;
  status: string;
  owners: User[];
  managers: User[];
  members: User[];
  createdBy: User;
  createdAt: number;
  updatedBy: User;
  updatedAt: number;
}

export type TChangeMetaData = keyof typeof CHANGE_META_DATA_TYPES;
