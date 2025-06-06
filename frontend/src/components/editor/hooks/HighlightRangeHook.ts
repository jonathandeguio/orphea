import { Monaco } from "@monaco-editor/react";
import React, { useEffect } from "react";
import { isDefined } from "utils/utilities";

interface IUseHighlightRangeHook {
  monaco: Monaco;
  editor: any;
}

export const useHighlightRangeHook = ({
  monaco,
  editor,
}: IUseHighlightRangeHook) => {
  const getDecorations = () => {
    let decorations: any = [];
    const model = editor.getModel();
    const languageId = model.getLanguageId();

    const currentChangesPattern = /<<<<<<< HEAD[\s\S\n]*?=======/g;
    const incomingChangesPattern = /=======[\s\S\n]*?>>>>>>>[\s\S]*?/g;

    const currentChangesMatchesArr = model.findMatches(
      currentChangesPattern.source,
      true,
      true,
      true,
      null,
      true
    );

    currentChangesMatchesArr.map((match: any) => {
      const range = new monaco.Range(
        match.range.startLineNumber + 1,
        match.range.startColumn,
        match.range.endLineNumber - 1,
        match.range.endColumn
      );
      const decoration = {
        range,
        options: {
          isWholeLine: true,
          className: "currentChangesBackground",
          inlineClassNameAffectsLetterSpacing: true,
        },
      };
      const decorationHeader = {
        range: new monaco.Range(
          match.range.startLineNumber,
          match.range.startColumn,
          match.range.startLineNumber,
          match.range.endColumn
        ),
        options: {
          isWholeLine: true,
          className: "currentHeaderBackground",
          inlineClassNameAffectsLetterSpacing: true,
        },
      };
      decorations.push(decoration);
      decorations.push(decorationHeader);
    });

    const incomingChangesMatchesArr = model.findMatches(
      incomingChangesPattern.source,
      true,
      true,
      true,
      null,
      true
    );

    incomingChangesMatchesArr.map((match: any) => {
      const decoration = {
        range: new monaco.Range(
          match.range.startLineNumber + 1,
          match.range.startColumn,
          match.range.endLineNumber - 1,
          match.range.endColumn
        ),
        options: {
          isWholeLine: true,
          className: "incomingChangesBackground",
          inlineClassNameAffectsLetterSpacing: true,
        },
      };
      const decorationHeader = {
        range: new monaco.Range(
          match.range.endLineNumber,
          match.range.startColumn,
          match.range.endLineNumber,
          match.range.endColumn
        ),
        options: {
          isWholeLine: true,
          className: "incomingHeaderBackground",
          inlineClassNameAffectsLetterSpacing: true,
        },
      };
      decorations.push(decoration);
      decorations.push(decorationHeader);
    });
    return decorations;
  };

  useEffect(() => {
    let disposableChangeHandler: any;
    if (!isDefined(editor)) return;
    let decorationIds = editor.deltaDecorations([], getDecorations());

    disposableChangeHandler = editor.onDidChangeModelContent((e: any) => {
      editor.deltaDecorations(decorationIds, []);
      decorationIds = editor.deltaDecorations([], getDecorations());
    });

    return () => {
      editor.deltaDecorations(decorationIds, []);

      disposableChangeHandler?.dispose();
    };
  }, [editor, monaco]);
};
