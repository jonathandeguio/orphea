import { ISourceConfig } from "Apps/Connect/Sources/Source";
import { FormInstance } from "antd";
import { copyToClipboard } from "utils/utilities";

interface ICopyAsCurl {
  source: ISourceConfig;
  outerName: number;
  form: FormInstance;
}
const CopyAsCurl = ({ form, outerName, source }: ICopyAsCurl) => {
  const domainDetails = source.domains ? source.domains[outerName] : null;

  if (!domainDetails) {
    console.error(">> domain not found");
    return;
  }

  const request = form.getFieldsValue(true).requests[outerName];
  const { queryParams, headers, method, path, bodyType, rawBody, formData } =
    request;

  const queryString = queryParams
    ?.map(
      (param: { key: string; value: string }) =>
        `${encodeURIComponent(param.key)}=${encodeURIComponent(param.value)}`
    )
    .join("&");

  const headerString = headers
    ?.map(
      (header: { key: string; value: string }) =>
        `-H "${header.key}: ${header.value}"`
    )
    .join(" ");

  // Construct cURL command
  const url = `${domainDetails.protocol}://${domainDetails.domain}:${
    domainDetails.port
  }/${path}${queryString ? "?" + queryString : ""}`;
  let curlCommand = `curl -X ${method.toUpperCase()} "${url}" ${
    headerString || ""
  }`;

  // Handle Body
  switch (bodyType) {
    case "RAW":
      if (rawBody) {
        curlCommand += ` -d '${rawBody}'`;
      }
      break;
    case "FORMDATA":
      console.log(formData);
      if (formData && formData.length > 0) {
        const formDataString = formData
          .map(
            (item: { key: string; value: string }) =>
              `-F "${item.key}=${item.value}"`
          )
          .join(" ");
        curlCommand += ` ${formDataString}`;
      }
      break;
    case "JSON":
      const parsedBody = JSON.parse(rawBody);
      console.log(`>>`, parsedBody);
      curlCommand += ` -d '${JSON.stringify(
        parsedBody
      )}' -H "Content-Type: application/json"`;
      break;
    default:
      console.log(" >> Check body type", bodyType);
  }

  console.log(curlCommand);
  copyToClipboard(curlCommand);
};

export default CopyAsCurl;
