import axios from "axios";
/**
 * fetches All Permissions Roles
 */
export const fetchAllRolesAPI = () => {
  return axios.get(`/passport/roles/all`);
};

/**
 * fetches  Permission mapping for particular resouceId
 * @param resourceId
 */
export const fetchPermissionMappingAPI = (resourceId: string) => {
  return axios.get(
    `/passport/permissionsMapping/getByResourceId/${resourceId}`
  );
};

/**
 * create a new Permission mapping for list of users with resourceId and role
 * @param body
 */
export const createPermissionMappingAPI = (body: any) => {
  return axios.post(`/passport/permissionsMapping/create`, body);
};

/**
 * Updates a  list of permissions Mapping
 * @param updateList
 */
export const updatePermissionsMappingAPI = (updateList: any) => {
  return axios.post(`/passport/permissionsMapping/update`, updateList);
};

/**
 * fetches  Permission mapping for particular resouceId
 * @param resourceId
 */
export const deletePermissionsMappingAPI = (permissionId: string) => {
  return axios.delete(
    `/passport/permissionsMapping/deleteById/${permissionId}`
  );
};

/**
 * fetches  Permission mapping for particular resouceId
 * @param resourceId
 */
export const fetchUserToResourcePermissionsAPI = (resourceId: string) => {
  return axios.get(`/passport/users/userResourcePermissions/${resourceId}`);
};
