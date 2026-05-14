import React, { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { listSourceLinks } from "../../../redux/actions/sourceActions";

import {
  encodeToBase64,
  getLanguageLabel,
  isDefined,
  notEmpty,
} from "utils/utilities";
import { RootState, ThunkAppDispatch } from "../../../redux/types/store";

import BoslerLoader from "components/boslerLoader";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { CollapserHandler } from "../../../components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import {
  getConnectElementAPI,
  getParentAPI,
  previewSourceAPI,
} from "../Connect.api";

import LinkTable2 from "../Links/LinkTable.view";

import { BottomBarLayout } from "common/components/BoslerLayout/BottomBarLayout";
import {
  initBottomBar,
  updateBottomBarItemState,
} from "common/components/BoslerLayout/bottomBarSlice";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";
import { initialSourceDetails } from "./Source.constants";
import { getSourceBottombarItems } from "./Source.utils";
import SourceHeader from "./SourceHeader.view";
import SourceInfoPanel from "./SourceInfoPanel";
import SourceTree from "./SourceTree";

type TBoslerSwitch = "dataBrowser" | "info";

const SourceDetails = () => {
  const primaryPanelRef = useRef<any>(null);
  const { id } = useParams();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [selectedDataSourceSwitch, setSelectedDataSourceSwitch] =
    useState<TBoslerSwitch>("dataBrowser");

  const { sourceLinks, loading } = useSelector(
    (state) => (state as $TSFixMe).sourceLinksList
  );
  const { previewSource } = useSelector((state: RootState) => state.sourceOps);
  const [parent, setParent] = useState({ name: "", id: "" });
  const [source, setSource] = useState({ ...initialSourceDetails });

  const getSource = () => {
    getConnectElementAPI(id as string, "source").then(({ data }) => {
      setSource(data);
      getParentAPI(data.parent).then(({ data: data1 }) => {
        setParent(data1);
      });
    });
  };

  const handlePreview = (code: string) => {
    dispatch(
      updateBottomBarItemState({
        id: "datasetPreviewPanel",
        openPane: true,
        props: {
          loading: true,
        },
      })
    );

    if (isDefined(source.id)) {
      const body = {
        query: encodeToBase64(code),
      };
      previewSourceAPI(source.id, body).then(({ data }: any) => {
        dispatch(
          updateBottomBarItemState({
            id: "datasetPreviewPanel",
            props: {
              loading: false,
              data: data,
            },
          })
        );
      });
    }
  };

  useEffect(() => {
    getSource();
    dispatch(listSourceLinks(id));
  }, []);

  useEffect(() => {
    if (notEmpty(source.id)) {
      dispatch(
        initBottomBar({
          leftItems: getSourceBottombarItems(source),
        })
      );
    }
  }, [source]);

  useEffectOnlyOnDependencyUpdate(() => {
    if (previewSource && previewSource.sourceId && previewSource.code) {
      handlePreview(previewSource.code);
    }
  }, [previewSource]);

  if (!source || source.id == "") {
    return <BoslerLoader />;
  }

  return (
    <BottomBarLayout>
      <div className="connect-container">
        <SourceHeader
          sourceDetails={source}
          setSourceDetails={setSource}
          parent={parent}
          updateSource={getSource}
        />
        <PanelGroup direction={"horizontal"}>
          <Panel collapsible={true} defaultSize={25} ref={primaryPanelRef}>
            <BoslerSwitch
              items={[
                {
                  label: getLanguageLabel("dataBrowser"),
                  value: "dataBrowser",
                  children: source && <SourceTree sourceId={source.id} />,
                },
                {
                  label: getLanguageLabel("info"),
                  value: "info",
                  children: (
                    <SourceInfoPanel source={source} getSource={getSource} />
                  ),
                },
              ]}
              value={selectedDataSourceSwitch}
              onChange={(newSwitch: TBoslerSwitch) => {
                setSelectedDataSourceSwitch(newSwitch);
              }}
            />
          </Panel>
          <PanelResizeHandle className="resizablePane-collapser">
            <CollapserHandler primaryPanelRef={primaryPanelRef} />
          </PanelResizeHandle>
          <Panel>
            <div style={{ padding: "20px" }}>
              <LinkTable2 tableList={sourceLinks} loading={loading} />
            </div>
          </Panel>
        </PanelGroup>
      </div>
    </BottomBarLayout>
  );
};
export default SourceDetails;
