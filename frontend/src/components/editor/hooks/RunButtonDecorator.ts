import { Monaco } from "@monaco-editor/react";
import { useEffect, useMemo, useState } from "react";
import { isDefined } from "utils/utilities";
import { findMatchingPositions } from "../editor.utils";

interface IuseRunButtonDecorator {
  editor: any;
  monaco: Monaco;
  setDoesScriptHasDecorator: any;
  paneId: any;
  activeId: any;
}

export const useRunButtonDecorator = ({
  editor,
  monaco,
  setDoesScriptHasDecorator,
}: IuseRunButtonDecorator) => {
  const [buildRunWidgetLineSet, setBuildRunWidgetLineSet] = useState<{
    [key: string]: any;
  }>({});

  const regex = /@funnel/g;

  const attachButtons = useMemo(
    () => () => {
      const matches = findMatchingPositions(editor, regex);

      // const matchDict: { [key: string]: any } = {};

      setDoesScriptHasDecorator(matches.length !== 0);
      // matches.forEach((match) => (matchDict[match.startLineNumber] = match));

      // Object.keys(buildRunWidgetLineSet).forEach((element) => {
      //   if (!matchDict.hasOwnProperty(element)) {
      //     editor.removeGlyphMarginWidget(
      //       getBuildRunWidget(editor, monaco, buildRunWidgetLineSet[element])
      //     );
      //   }
      // });

      // matches.forEach((match) => {
      //   setDoesScriptHasDecorator(true);
      //   if (!buildRunWidgetLineSet.hasOwnProperty(match.startLineNumber)) {
      //     editor.addGlyphMarginWidget(
      //       getBuildRunWidget(editor, monaco, match)
      //     );
      //   }
      // });

      // setBuildRunWidgetLineSet(matchDict);
    },
    [editor, monaco, setDoesScriptHasDecorator]
  );

  useEffect(() => {
    if (isDefined(editor)) {
      attachButtons();
      editor.getModel().onDidChangeContent((event: any) => {
        attachButtons();
      });
    }
  }, [editor, monaco, setDoesScriptHasDecorator]);
};
