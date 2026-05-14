export interface IBuildFilters {
  searchText: string | undefined;
  status: string[];
  trigger: string[];
  rangeFrom: string;
  rangeTo: string;
  finishRangeFrom: string;
  finishRangeTo: string;
  showMyBuildsOnly: boolean;
  branch: string;
  startedBy: string;
}
