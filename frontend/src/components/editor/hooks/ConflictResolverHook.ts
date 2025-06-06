import { Monaco } from "@monaco-editor/react";
import React, { useEffect } from "react";
import { isDefined } from "utils/utilities";
import { useHighlightRangeHook } from "./HighlightRangeHook";

interface IUseCodeLensProviderHook {
  monaco: Monaco;
  editor: any;
}

export const useCodeLensProviderHook = ({
  monaco,
  editor,
}: IUseCodeLensProviderHook) => {
  const provideCodeLens = () => {
    const model = editor.getModel();
    const codeLensDisposablearr: any[] = [];

    const changesPattern =
      /<<<<<<< HEAD[\s\S\n]*?=======[\s\S\n]*?>>>>>>>[\s\S]*?/g;

    const matchesArr = model.findMatches(
      changesPattern.source,
      true,
      true,
      true,
      null,
      true
    );

    matchesArr.map((match: any) => {
      const startLine = match.range.startLineNumber;
      const range = {
        startLineNumber: startLine,
        startColumn: 1,
        endLineNumber: startLine + 1,
        endColumn: 1,
      };

      const currentChangesPattern = /<<<<<<< HEAD[\s\S\n]*?=======/g;
      const incomingChangesPattern = /=======[\s\S\n]*?>>>>>>>[\s\S]*?/g;

      const currentMatchesArr = model
        .findMatches(currentChangesPattern.source, true, true, true, null, true)
        .find((iMatch: any) => match.range.containsRange(iMatch.range));
      const incomingMatchesArr = model
        .findMatches(
          incomingChangesPattern.source,
          true,
          true,
          true,
          null,
          true
        )
        .find((iMatch: any) => match.range.containsRange(iMatch.range));

      const resolveCodeLens = (model: any, codeLens: any, token: any) => {
        return codeLens;
      };
      const acceptCurrentChangeLensProvider = {
        provideCodeLenses: function (model: any, token: any) {
          return {
            lenses: [
              {
                range,
                id: "acceptCurrentChange" + startLine,
                command: {
                  id: "acceptCurrentAction",
                  title: "Accept Current Change",
                  arguments: [
                    currentMatchesArr.range,
                    incomingMatchesArr.range,
                  ],
                },
              },
            ],
            dispose: () => {},
          };
        },
        resolveCodeLens,
      };
      const acceptIncomingChangeLensProvider = {
        provideCodeLenses: function (model: any, token: any) {
          return {
            lenses: [
              {
                range,
                id: "acceptIncomingChange" + startLine,
                command: {
                  id: "acceptIncomingAction",
                  title: "Accept Incoming Change",
                  arguments: [
                    currentMatchesArr.range,
                    incomingMatchesArr.range,
                  ],
                },
              },
            ],
            dispose: () => {},
          };
        },
        resolveCodeLens,
      };
      const acceptBothChangeLensProvider = {
        provideCodeLenses: function (model: any, token: any) {
          return {
            lenses: [
              {
                range,
                id: "acceptBothChange" + startLine,
                command: {
                  id: "acceptBothAction",
                  title: "Accept Both Changes",
                  arguments: [
                    currentMatchesArr.range,
                    incomingMatchesArr.range,
                  ],
                },
              },
            ],
            dispose: () => {},
          };
        },
        resolveCodeLens,
      };

      codeLensDisposablearr.push(
        monaco.languages.registerCodeLensProvider(
          editor.getModel().getLanguageId(),
          acceptBothChangeLensProvider
        )
      );
      codeLensDisposablearr.push(
        monaco.languages.registerCodeLensProvider(
          editor.getModel().getLanguageId(),
          acceptIncomingChangeLensProvider
        )
      );
      codeLensDisposablearr.push(
        monaco.languages.registerCodeLensProvider(
          editor.getModel().getLanguageId(),
          acceptCurrentChangeLensProvider
        )
      );
    });
    // Register the command for handling Code Lens actions
    monaco.editor.registerCommand(
      "acceptIncomingAction",
      (editorA: any, currentChangesRange: any, incomingChangesRange: any) => {
        editor.executeEdits(
          new Date().toISOString(),
          [
            {
              identifier: new Date().toISOString(),
              range: currentChangesRange,
              text: "",
            },
            {
              identifier: new Date().toISOString(),
              range: {
                startLineNumber: incomingChangesRange.endLineNumber,
                startColumn: 0,
                endLineNumber: incomingChangesRange.endLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
          ],
          true
        );
      }
    );
    // Register the command for handling Code Lens actions
    monaco.editor.registerCommand(
      "acceptCurrentAction",
      (editorA: any, currentChangesRange: any, incomingChangesRange: any) => {
        editor.executeEdits(
          new Date().toISOString(),
          [
            {
              identifier: new Date().toISOString(),
              range: {
                ...incomingChangesRange,
                endLineNumber: incomingChangesRange.endLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
            {
              identifier: new Date().toISOString(),
              range: {
                startLineNumber: currentChangesRange.startLineNumber,
                startColumn: 0,
                endLineNumber: currentChangesRange.startLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
          ],
          true
        );
      }
    );
    // Register the command for handling Code Lens actions
    monaco.editor.registerCommand(
      "acceptBothAction",
      (editorA: any, currentChangesRange: any, incomingChangesRange: any) => {
        editor.executeEdits(
          new Date().toISOString(),
          [
            {
              identifier: new Date().toISOString(),
              range: {
                startLineNumber: currentChangesRange.startLineNumber,
                startColumn: 0,
                endLineNumber: currentChangesRange.startLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
            {
              identifier: new Date().toISOString(),
              range: {
                startLineNumber: currentChangesRange.endLineNumber,
                startColumn: 0,
                endLineNumber: currentChangesRange.endLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
            {
              identifier: new Date().toISOString(),
              range: {
                startLineNumber: incomingChangesRange.endLineNumber,
                startColumn: 0,
                endLineNumber: incomingChangesRange.endLineNumber + 1,
                endColumn: 0,
              },
              text: "",
            },
          ],
          true
        );
      }
    );
    return codeLensDisposablearr;
  };
  useEffect(() => {
    let disposableChangeHandler: any;
    if (!isDefined(editor)) return;

    let codeLensDisposablearr = provideCodeLens();

    disposableChangeHandler = editor.onDidChangeModelContent((e: any) => {
      codeLensDisposablearr?.forEach((codeLensDisposable) =>
        codeLensDisposable.dispose()
      );
      codeLensDisposablearr = provideCodeLens();
    });

    return () => {
      disposableChangeHandler?.dispose();
      codeLensDisposablearr?.forEach((codeLensDisposable) =>
        codeLensDisposable.dispose()
      );
    };
  }, [monaco, editor]); // Depend on monaco, editor, and searchPattern
};
