import { Editor, Monaco } from "@monaco-editor/react";
import { RunIcon, SyncIcon } from "assets/icons/boslerActionIcons";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { useFileSelectorOverlayHook } from "components/editor/hooks/FileSelectorOverlayHook";
import { useUuidOverlayProvider } from "components/editor/hooks/UuidOverlayProviderHook";
import React, { useRef, useState } from "react";
import { useSelector } from "react-redux";
import {
  copyToClipboard,
  getLanguageLabel,
  isCurrentConfigThemeDark,
  isDefined,
} from "utils/utilities";
import styles from "./DatasetSql.module.scss";

interface IProps {
  handleQueryExecution: (query: string) => void;
  datasetId: string;
}

const DatasetSqlEditor = ({ handleQueryExecution, datasetId }: IProps) => {
  const DEFAULT_QUERY = "SELECT * FROM `" + datasetId + "` \nLIMIT 1000;";
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const [query, setQuery] = useState<string>(DEFAULT_QUERY);
  const editorContainerRef = useRef<HTMLDivElement | null>(null);
  const [editor, setEditor] = useState<any>();
  const [monaco, setMonaco] = useState<Monaco | undefined>();

  // REGISTER UUID OVERLAY PROVIDER
  useUuidOverlayProvider({
    editor,
    monaco,
    fontSize: 12,
    languageSelector: "SQL",
  });

  // REGISTER FILE SELECTOR OVERLAY
  useFileSelectorOverlayHook({ monaco, editor, editorContainerRef });

  return (
    <div className={styles.container} ref={editorContainerRef}>
      <div className={styles.header}>
        <BoslerButton
          intent="dangerous"
          onClick={() => setQuery(DEFAULT_QUERY)}
          icon={<SyncIcon />}
          minimal
          icononly
        >
          {getLanguageLabel("reset")}
        </BoslerButton>
        <BoslerButton
          intent="action"
          onClick={() => copyToClipboard(query)}
          icon={<CopyIcon />}
          minimal
          icononly
        >
          {getLanguageLabel("copy")}
        </BoslerButton>
        <BoslerButton
          intent="action"
          onClick={() => handleQueryExecution(query)}
          actionIcon={<RunIcon />}
          size="small"
          minimal
        >
          {getLanguageLabel("execute")}
        </BoslerButton>
      </div>
      <Editor
        height={"50vh"}
        defaultLanguage="sql"
        theme={isCurrentConfigThemeDark(user) ? "vs-dark" : "light"}
        onChange={(value, event) => {
          setQuery(value ? value : "");
        }}
        value={query}
        beforeMount={(monaco) => {
          setMonaco(monaco);
        }}
        onMount={(editor, monaco) => {
          setEditor(editor);
        }}
        options={{
          minimap: {
            enabled: false,
          },
          fontSize:
            isDefined(user.preferences) && isDefined(user.preferences.fontSize)
              ? user.preferences.fontSize
              : "16",
          lineHeight: 19,
          lineNumbersMinChars: 2,

          fontFamily:
            '"IBM Plex Mono", "Courier New", Courier, monospace, "Droid Sans Mono", "monospace", monospace',
        }}
      />
    </div>
  );
};

export default React.memo(DatasetSqlEditor);
