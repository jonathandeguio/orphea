export interface Resource {
  id: string;
  workspace: any;
  size: number;
  name: string;
  description: string | null;
  type: string;
  subType: string;
  status: string;
  children: Resource[];
  createdBy: User;
  createdAt: number;
  updatedBy: User;
  updatedAt: number;
  metaData?: any;
}

export interface IProject extends Resource {
  hasAccess: boolean;
  team: string[];
}
