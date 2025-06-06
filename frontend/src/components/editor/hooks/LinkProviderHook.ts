import { getResourceApi } from "Apps/explorer/explorer.api";
import { useEffect, useState } from "react";
import { IsUUID, openNotification } from "utils/utilities";
import { getIdByPathAPI } from "../editor.api";
import { Monaco } from "@monaco-editor/react";
import { BASE_URL } from "Authentication/constants";
import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { replaceResolvedPathsToUuid } from "../editor.utils";
import { DATASET } from "components/Builds/Builds.constants";

interface ILinkProviderHook {
  monaco: Monaco;
  editor: any;
  languageSelector: string;
}

const typeToLanguageMap: { [key: string]: string } = {
  [ResourceSubTypeEnum.PY]: "python",
  [ResourceSubTypeEnum.SQL]: "sql",
};

export const useLinkProviderHook = ({
  monaco,
  editor,
  languageSelector,
}: ILinkProviderHook) => {
  async function getIdByPath(path: any) {
    try {
      const { data } = await getIdByPathAPI(path);
      if (data.Type === "DATASET") {
        window
          ?.open(
            BASE_URL + "/portal/kitab/dataset/" + data.Message + "/master",
            "_blank"
          )
          ?.focus();
      } else {
        openNotification(
          "Invalid",
          "Not a valid Dataset path " + path,
          "warning"
        );
      }
    } catch (error) {
      openNotification(
        "Invalid",
        "Not a valid Dataset path " + path,
        "warning"
      );
    }
  }

  async function linkProviderHelper(matches: any[]) {
    let res = new Array(matches.length);

    await Promise.all(
      matches.map(async (match: any, index: number) => {
        if (IsUUID(match.matches[0])) {
          res[index] = await getResourceApi(match.matches[0]).then(
            ({ data }) => {
              return data.type === "DATASET";
            }
          );
        } else {
          try {
            const { data } = await getIdByPathAPI(match.matches[0]);
            if (data.Status && data.Type === "DATASET") {
              replaceResolvedPathsToUuid(match, data.Message, editor);
            }
            res[index] = data.Status;
          } catch (error) {
            res[index] = false;
          }
        }
      })
    );

    return res;
  }

  const monacoLinkProvider = async (model: any, token: any) => {
    const matches = model.findMatches(
      `\/Projects\/[^"\`]*|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}`,
      true,
      true,
      true,
      null,
      true
    );

    let res: { links: any[] } = { links: [] };

    await linkProviderHelper(matches).then((result) => {
      res.links = matches.filter((_v: any, index: any) => result[index]);
    });

    // res.links.forEach(match => replaceResolvedPathsToUuid(match,))

    return res;
  };

  const registerMonacoLinkProvider = (
    monaco: Monaco,
    languageSelector: string
  ) => {
    if (monaco) {
      const disposableLinkProvider = monaco.languages.registerLinkProvider(
        languageSelector,
        {
          provideLinks: monacoLinkProvider,
          resolveLink: (link: any) => {
            if (IsUUID(link.matches[0])) {
              window
                ?.open(
                  BASE_URL +
                    "/portal/kitab/dataset/" +
                    link.matches[0] +
                    "/master",
                  "_blank"
                )
                ?.focus();
            } else {
              getIdByPath(link.matches[0]);
            }
            return link;
          },
        }
      );

      return disposableLinkProvider;
    }
    return null;
  };

  useEffect(() => {
    const disposableLinkprovider: any = registerMonacoLinkProvider(
      monaco,
      typeToLanguageMap[languageSelector]
    );

    return () => {
      disposableLinkprovider?.dispose();
    };
  }, [monaco, editor]);
};
