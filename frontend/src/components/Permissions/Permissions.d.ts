interface UserPermissions {
  owner: string;
  editor: string;
  viewer: string;
}

interface PermissionMapping {
  id: string;
  identity: User | Group;
  resourceId: string;
  roleId: string;
  inherited: boolean;
}

interface IRole {
  delete: boolean;
  id: string;
  name: string;
  read: boolean;
  status: string;
  write: boolean;
}
