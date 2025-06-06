import React, { useEffect, useState } from "react";
import { useParams } from "react-router";

import { BOSLER_TOKEN } from "Authentication/constants";
import BoslerLoader from "components/boslerLoader";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import "../editor.scss";

interface IProps {
  pane: any;
}

const JupyterNotebook = ({ pane }: IProps) => {
  const { id: repoId } = useParams();
  const [notebookPath, setNotebookPath] = useState<string>();
  const { user } = useSelector((state: RootState) => state.userDetails);

  useEffect(() => {
    if (user && pane && repoId) {
      const token = document.cookie
  .split('; ')
  .find(row => row.startsWith('bAT='))
  ?.split('=')[1];
      setNotebookPath(
        process.env.REACT_APP_BASE_URL +
          "/api/jupyter/lab/tree/" +
          user.id +
          "/" +
          repoId +
          "/" +
          pane?.path +
          "?token=" +
          token
      );
    }
  }, [user, pane, repoId]);

  if (!notebookPath) {
    return <BoslerLoader />;
  }

  return (
    <iframe height="100%" width="100%" src={notebookPath} style={{border: "none"}}>
      JupyterNotebook
    </iframe>
  );
};

export default JupyterNotebook;
