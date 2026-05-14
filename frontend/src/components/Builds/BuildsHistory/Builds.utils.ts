import { User } from "global";
import { getYesterdayDate } from "utils/utilities";
import { IBuildFilters } from "./BuildFilters";

export const getAllUsersIdsList = (users: User[]) => {
  let userIdsList: string[] = [];
  users.forEach((user: User) => {
    userIdsList.push(user.id);
  });
  return userIdsList;
};

export const getDefaultBuildFilters = (currentUserId: string) => {
  return {
    searchText: undefined,
    status: [],
    trigger: [],
    rangeFrom: getYesterdayDate(),
    rangeTo: undefined,
    finishRangeFrom: undefined,
    finishRangeTo: undefined,
    showMyBuildsOnly: false,
    branch: "1",
    startedBy: [currentUserId],
  } as unknown as IBuildFilters;
};
