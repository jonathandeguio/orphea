export const downloadBlobFile = (
  file: Blob,
  name: string,
  extension: string
) => {
  const url = URL.createObjectURL(file);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${name.split(".")[0]}.${extension}`;
  document.body.appendChild(link);
  link.click();
  URL.revokeObjectURL(url);
  document.body.removeChild(link);
};

export const getPath = (linksPath: any) => {
  if (linksPath) {
    let path = "/Projects";
    linksPath.map((cur: $TSFixMe) => (path = path + `/${cur.name}`));
    return path;
  }
  return "/";
};

export const checkAvailableFileType = (type: string) => {
  if (
    type.toLowerCase() == "docx" ||
    type.toLowerCase() == "doc" ||
    type.toLowerCase() == "ppt" ||
    type.toLowerCase() == "pptx"
  )
    return false;
  return true;
};
