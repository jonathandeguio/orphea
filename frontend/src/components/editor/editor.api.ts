import axios, { AxiosResponse } from "axios";
import { encodeToBase64, isBase64 } from "utils/utilities";

/**
 * Fetches the directory tree
 * @param id Repo id
 * @param branch branch
 */
export const getFileContentAPI = (
  id: string,
  branch: string,
  filePath: string,
  detached = false
): Promise<AxiosResponse<any, any>> => {
  if (detached) {
    return axios.get(
      `/fractal/${id}/${branch}/viewFileContent/commit?filePath=${filePath}`
    );
  }
  return axios.get(
    `/fractal/${id}/${branch}/viewFileContent/workingTree?filePath=${filePath}`
  );
};

/**
 * Fetches the directory tree
 * @param id Repo id
 * @param branch branch
 */
export const fetchTree = (
  id: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/fractal/${id}/${branch}/tree`);
};
/**
 * Fetches the directory tree
 * @param id Repo id
 * @param branch branch
 */
export const deleteRepoFile = (
  id: string,
  branch: string,
  filePath: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${id}/${branch}/delete`, {
    filePath: filePath,
  });
};
/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const makeCopy = (
  id: string,
  branch: string,
  filePath: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${id}/${branch}/makeCopy`, {
    filePath: filePath,
  });
};
/**
 * move a file to different folder
 * @param id Repo id
 * @param branch branch
 */
export const moveFileFractalAPI = (
  id: string,
  branch: string,
  source: string,
  destination: string,
  sourceType: string,
  destinationType: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${id}/${branch}/move`, {
    source,
    destination,
    sourceType,
    destinationType,
  });
};
/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const autoSave = (
  id: string,
  branch: string,
  filePath: string,
  fileContent: string
): Promise<AxiosResponse<any, any>> => {
  if (!isBase64(fileContent)) {
    fileContent = encodeToBase64(fileContent);
  }

  return axios.post(`/fractal/${id}/${branch}/autoSave`, [
    {
      filePath: filePath,
      fileContent: fileContent,
    },
  ]);
};

/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const gitBlameApi = (
  id: string,
  branch: string,
  filePath: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${id}/${branch}/gitBlame`, {
    filePath,
  });
};

/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const checkout = (
  repoId: string,
  branch: string,
  commitId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${repoId}/${branch}/checkout/${commitId}`);
};

/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const checkoutBranch = (
  repoId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/fractal/${repoId}/${branch}/checkout`);
};

/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const renameApi = (
  repoId: string,
  branch: string,
  filePath: string,
  newName: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${repoId}/${branch}/rename`, {
    filePath,
    newName,
  });
};
/**
 * makes a copy of the file in the same folder
 * @param id Repo id
 * @param branch branch
 */
export const getIdByPathAPI = (
  path: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/getIdByPath`, {
    path,
  });
};

export const repoBuildAPI = (
  id: string,
  buildTrigger: "PYTHON" | "SQL" | undefined,
  buildType: "PREVIEW" | "DEFAULT",
  activeBranch: string,
  scriptPath: string,
  branchId: string | undefined,
  commitId: string | undefined,
  code: string | undefined,
  rowLimit: number | null
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/build/build/${buildTrigger}`,
    JSON.stringify({
      repositoryId: id,
      branch: activeBranch,
      scriptPath: scriptPath,
      branchId: branchId,
      commitId: commitId,
      code: code,
      buildLanguage: buildTrigger,
      buildType: buildType,
      rowLimit: rowLimit,
    })
  );
};

export const getPreviewDataAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/funnel/previewResult`);
};

export const isValidScript = (path: any) => {
  if (path) {
    const extension = path.split(".").pop();
    return ["py", "r", "sql"].includes(extension);
  }
  return false;
};

export const abortK8PreviewAPI = (
  previewId: string,
  repoId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/funnel/${previewId}/${repoId}/abort`);
};
export const gitCommitDetails = (
  id: string,
  commitId: string | undefined
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/fractal/${id}/commit/${commitId}`);
};
export const gitFileCommitDetails = (
  id: string,
  commitId: string,
  filePath: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/fractal/${id}/file/${commitId}`, {
    params: {
      filePath,
    },
  });
};

export const getPreviewSpecsAPI = (
  id: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  const payload = {
    repositoryId: id,
    branch: branch,
  };
  return axios.post(`/build/previewSpecs`, payload);
};

export const putPreviewSpecsAPI = (
  id: string,
  branch: string,
  rowLimit: number
): Promise<AxiosResponse<any, any>> => {
  const payload = {
    repositoryId: id,
    branch: branch,
    rowLimit: rowLimit,
  };
  return axios.put(`/build/previewSpecs`, payload);
};

export const createRepositoryAPI = (
  templateType: string,
  repoObject: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/fractal/${templateType}/createRepository`, repoObject);
};
