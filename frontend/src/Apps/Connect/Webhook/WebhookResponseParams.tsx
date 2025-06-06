import { MappingConstants } from "Apps/APIConnector/APIConnector.constants";
import OverlayExtractOption from "Apps/APIConnector/OverlayExtractOption";
import React, { useEffect, useState } from "react";
interface IProps {
  restPreviewResponseParam: string;
  setRestPreviewResponseParam: any;
}

const getDefaultValue = (str: string) => {
  const match = str.match(/\[(.*?)\]/);
  console.log(">> MATCH : ", match);
  return match ? match[1] : "";
};

const getDefaultExtractOption = (str: string) => {
  if (str.includes("@completeresponse")) {
    return MappingConstants.completeResponse;
  } else if (str.includes("@key")) {
    return MappingConstants.key;
  } else if (str.includes("@index")) {
    return MappingConstants.index;
  }
  return MappingConstants.completeResponse;
};

const WebhookResponseParams = ({
  restPreviewResponseParam,
  setRestPreviewResponseParam,
}: IProps) => {
  const [extractOption, setExtractOption] = useState(
    getDefaultExtractOption(restPreviewResponseParam)
  );
  const [finalKey, setFinalKey] = useState<string>(
    getDefaultValue(restPreviewResponseParam)
  );

  useEffect(() => {
    console.log("EXTRACT OPTION : ", extractOption);
    if (extractOption === MappingConstants.key) {
      setRestPreviewResponseParam(`@key[${finalKey}]`);
    } else if (extractOption === MappingConstants.completeResponse) {
      setRestPreviewResponseParam("@completeresponse");
    } else if (extractOption === MappingConstants.index) {
      setRestPreviewResponseParam(`@index[${finalKey}]`);
    }
  }, [finalKey, extractOption]);

  console.log(
    ">> Default :",
    restPreviewResponseParam,
    extractOption,
    finalKey
  );

  return (
    <OverlayExtractOption
      extractOption={extractOption}
      setExtractOption={setExtractOption}
      setFinalKey={setFinalKey}
      finalKey={finalKey}
    />
  );
};

export default WebhookResponseParams;
