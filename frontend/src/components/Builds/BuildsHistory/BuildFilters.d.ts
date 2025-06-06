import { JDBCSourceTypeEnum } from "Apps/Connect/Enums/JDBCSourceTypeEnum";

export interface IBuildFilters {
  searchText: string | undefined;
  status: string[];
  trigger: string[];
  sourceType: JDBCSourceTypeEnum[];
  rangeFrom: string;
  rangeTo: string;
  finishRangeFrom: string;
  finishRangeTo: string;
  showMyBuildsOnly: boolean;
  branch: string;
  startedBy: string;
}
