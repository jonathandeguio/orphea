export type SortOrder = "asc" | "dsc";
export type SortOrderBy = "createdAt" | "updatedAt";
export interface ISuggestedChartsFilters {
  searchText: string | null;
  createdAtFrom: Date | null;
  createdAtTo: Date | null;
  updatedAtFrom: Date | null;
  updatedAtTo: Date | null;
  createdBy: string[] | null;
}
