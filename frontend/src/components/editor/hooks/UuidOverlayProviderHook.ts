import { Monaco } from "@monaco-editor/react";
import { useEffect, useMemo } from "react";
import { isDefined } from "../../../utils/utilities";
import { getUuidOverlayWidget } from "../editor.utils";

interface IUuidOverlayProviderHook {
  editor: any;
  monaco: Monaco;
  languageSelector: string;
  fontSize: number;
}

function difference<T>(setA: Set<T>, setB: Set<T>) {
  const _difference = new Set(setA);
  for (const elem of setB) {
    _difference.delete(elem);
  }
  return _difference;
}

export const useUuidOverlayProvider = ({
  editor,
  monaco,
  fontSize,
  languageSelector,
}: IUuidOverlayProviderHook) => {
  const overlays: { [key: string]: any } = useMemo(() => ({}), []);

  const updateOverlays = (matches: any[]) => {
    const matchesMap: { [key: string]: string } = {};
    matches.forEach((match) => {
      matchesMap[JSON.stringify(match.range)] = match.matches[0];
    });

    const existingOverlays = new Set(Object.keys(overlays));
    const incomingOverlays = new Set(
      matches.map((match) => JSON.stringify(match.range))
    );

    const deletedOverlays = difference(existingOverlays, incomingOverlays);
    const newOverlays = difference(incomingOverlays, existingOverlays);

    deletedOverlays.forEach((deleted) => {
      delete overlays[deleted];
    });

    newOverlays.forEach((newOverlay) => {
      const range = JSON.parse(newOverlay);

      const uuidOverlay = getUuidOverlayWidget(
        editor,
        monaco,
        matchesMap[newOverlay],
        range
      );

      editor.addContentWidget(uuidOverlay);
      overlays[newOverlay] = matchesMap[newOverlay];
    });
  };

  useEffect(() => {
    if (isDefined(editor) && isDefined(editor.getModel())) {
      const matchesArr = editor
        .getModel()
        .findMatches(
          `[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}`,
          true,
          true,
          true,
          null,
          true
        );

      updateOverlays(matchesArr);
      editor.getModel().onDidChangeContent((e: any) => {
        const matchesArr = editor
          .getModel()
          .findMatches(
            `[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}`,
            true,
            true,
            true,
            null,
            true
          );

        updateOverlays(matchesArr);
      });
    }
  }, [editor]);
};
