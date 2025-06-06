import { GitChangeType } from "./types";

export const getClassNameByChange = (change: GitChangeType) => {
  switch (change) {
    case "ADD":
      return "--add";
    case "MODIFY":
      return "--modify";
    case "DELETE":
      return "--delete";
    case "RENAME":
      return "--rename";
    case "COPY":
      return "--copy";
  }
};
