import { BULK_USER_CREATION_SAMPLE_FILE_NAME } from "./BulkUserCreation.constants";

export const downloadSampleUserFile = () => {
  const url =
    process.env.PUBLIC_URL + "/" + BULK_USER_CREATION_SAMPLE_FILE_NAME;
  const a = document.createElement("a");
  a.href = url;
  a.download = BULK_USER_CREATION_SAMPLE_FILE_NAME;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};
