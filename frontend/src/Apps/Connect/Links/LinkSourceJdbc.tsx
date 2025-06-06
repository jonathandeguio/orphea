import Editor from "@monaco-editor/react";
import { Col, Row, Typography } from "antd";
import { getPreviewStatement } from "components/SimpleTreeViewer/NodeExtraOptions";
import React, { useEffect } from "react";
import {
  getLanguageLabel,
  isCurrentConfigThemeDark,
  isDefined,
} from "utils/utilities";
import SourceTableSelectionTree from "../Sources/SourceTableSelectionTree";
interface IProps {
  sourceId: string;
  code: string | undefined;
  setCode: any;
  user: any;
  defaultQuery: string | undefined;
}

const { Text } = Typography;

const LinkSourceJdbc = ({
  sourceId,
  code,
  setCode,
  user,
  defaultQuery,
}: IProps) => {
  useEffect(() => {
    if (defaultQuery) {
      setCode(defaultQuery);
    }
  }, [defaultQuery]);

  return (
    <Row
      gutter={[16, 16]}
      style={{
        marginTop: "5px",
      }}
    >
      {code ? (
        <>
          <Col span={8} style={{ display: "flex", alignItems: "center" }}>
            <Text>{getLanguageLabel("query")}</Text>
          </Col>
          <Col span={16}>
            <div
              style={{
                border: "1px solid var(--bosler-border-color-default)",
              }}
            >
              <Editor
                height="20vh"
                defaultLanguage="sql"
                theme={isCurrentConfigThemeDark(user) ? "vs-dark" : "light"}
                onChange={(value, event) => {
                  setCode(value as string);
                }}
                value={code}
                options={{
                  minimap: {
                    enabled: false,
                  },
                  fontSize:
                    isDefined(user.preferences) &&
                    isDefined(user.preferences.fontSize)
                      ? user.preferences.fontSize
                      : "16",
                  lineHeight: 19,
                  lineNumbersMinChars: 2,

                  fontFamily:
                    '"IBM Plex Mono", "Courier New", Courier, monospace, "Droid Sans Mono", "monospace", monospace',
                }}
              />
            </div>
          </Col>
        </>
      ) : (
        <>
          {sourceId && (
            <SourceTableSelectionTree
              sourceId={sourceId}
              onTableClick={(node, parent) =>
                setCode(getPreviewStatement(node.name, node.type, parent?.name))
              }
            />
          )}
        </>
      )}
    </Row>
  );
};

export default LinkSourceJdbc;
