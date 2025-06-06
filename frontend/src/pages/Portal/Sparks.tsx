import { useEffect } from "react";
import Iframe from "react-iframe";
import { useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { isDefined } from "utils/utilities";

//
const BASE_URL = process.env.REACT_APP_BASE_URL;

const Sparks = () => {
  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  const { id } = useParams();
  useEffect(() => {
    document.title = "Sparks";

    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Bosler";
    };
  }, []);

  const url_history = `${BASE_URL}/bosler-shs/history/${id}/jobs/`;

  return (
    // @ts-expect-error TS(2322): Type '{ children: string; src: string; className: ... Remove this comment to see the full error message
    <Iframe src={url_history} className="iframesparks">
      {" "}
    </Iframe>
  );
};

export default Sparks;
