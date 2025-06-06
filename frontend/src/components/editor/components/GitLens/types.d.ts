export type GitChangeType = "ADD" | "MODIFY" | "DELETE" | "RENAME" | "COPY";

export interface IGitCommitDetails {
  changeType: GitChangeType;
  oldPath: string;
  content: string;
  newPath: string;
}

export interface IGitLog {
  id: string;
  username: string;
  email: string;
  body: string;
  message: string;
  commitTime: number;
}

export interface IGitLens {
  path: string;
}
