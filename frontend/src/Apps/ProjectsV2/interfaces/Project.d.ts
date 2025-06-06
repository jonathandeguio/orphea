export interface IResourceFilters {
  searchText: string | undefined;
  sortBy: string | undefined;
  sortDirection: TRESOURCE_SORT_DIRECTION;
  permissions: string[] | undefined;
}
