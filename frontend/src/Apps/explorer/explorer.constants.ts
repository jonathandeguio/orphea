export const specialIds = [
  "CREATED_BY_YOU",
  "UPDATED_BY_YOU",
  "RECYCLE_BIN",
  "RECENTLY_VIEWED",
  "FAVOURITES",
];

export const gitStatusClasses: { [key: string]: string } = {
  clean: "git-clean",
  removed: "git-removed",
  modified: "git-modified",
  uncommittedChanges: "git-uncommitted",
  added: "git-added",
  changed: "git-changed",
  conflicting: "git-conflicting",
  ignoredNotInIndex: "git-ignored",
  missing: "git-missing",
  untracked: "git-untracked",
  untrackedFolders: "git-untracked-folder",
};
