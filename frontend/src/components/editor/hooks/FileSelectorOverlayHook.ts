import { Monaco } from "@monaco-editor/react";
import { useEffect, useState } from "react";
import { getOverlayWidget } from "../editor.utils";
import { projectPatternRegex } from "../editor.constants";
import { isDefined } from "utils/utilities";

interface IUseFileSelectorOverlayHook {
  monaco: Monaco;
  editor: any;
  editorContainerRef: React.MutableRefObject<HTMLDivElement | null>;
}

export const useFileSelectorOverlayHook = ({
  monaco,
  editor,
  editorContainerRef,
}: IUseFileSelectorOverlayHook) => {
  const [activeOverlayLine, setActiveOverlayline] = useState<number | null>(
    null
  );

  const changeHandler = (event: any) => {
    let currentLineNumber = event.position.lineNumber;

    if (activeOverlayLine != currentLineNumber) {
      let lineContent = editor.getModel().getLineContent(currentLineNumber);

      // Add a content widget (scrolls inline with text)
      const contentWidget = getOverlayWidget(
        editor,
        lineContent,
        monaco,
        editorContainerRef,
        currentLineNumber,
        setActiveOverlayline
      );

      const matchArray = projectPatternRegex.exec(lineContent);

      if (isDefined(matchArray)) {
        const matchedStartIndex = matchArray.index;
        const matchedEndIndex = matchedStartIndex + matchArray[0].length + 1;

        if (
          event.position.column >= matchedStartIndex &&
          event.position.column <= matchedEndIndex
        ) {
          editor.addContentWidget(contentWidget);
          setActiveOverlayline(currentLineNumber);
        }
      }
    }
  };

  useEffect(() => {
    let disposableChangeHandler: any;

    if (isDefined(editor)) {
      disposableChangeHandler = editor.onDidChangeCursorPosition(changeHandler);
    }

    return () => {
      disposableChangeHandler?.dispose();
    };
  }, [editor, activeOverlayLine]);
};
