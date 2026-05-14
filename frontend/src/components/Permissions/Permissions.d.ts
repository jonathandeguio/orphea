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
