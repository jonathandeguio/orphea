export const downloadSampleUserFile = () => {
  const fileName = "SampleUsersData.csv";
  const url = process.env.PUBLIC_URL + "/" + fileName;
  const a = document.createElement("a");
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};
