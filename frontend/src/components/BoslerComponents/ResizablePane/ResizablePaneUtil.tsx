import React, { useEffect, RefObject } from "react";
import { DragHandleVerticalIcon } from "../../../assets/icons/boslerActionIcons";
import "./ResizablePane.scss";
import { Panel, ImperativePanelHandle } from "react-resizable-panels";
import { mobileWidth } from "../../../common/responsiveSize.constants";

const CollapserHandler = ({
  primaryPanelRef,
  alignButton = "right",
}: {
  primaryPanelRef: any;
  alignButton?: string;
}) => {
  const handleToggleCollapse = () => {
    if (primaryPanelRef.current) {
      if (!primaryPanelRef.current.isCollapsed()) {
        primaryPanelRef.current.collapse();
      } else {
        primaryPanelRef.current.expand();
      }
    }
  };

  return (
    <div className="resizablePane-collapser-box">
      <button
        className={`resizablePane-collapser-box-button resizablePane-collapser-box-button-${alignButton}`}
        onClick={() => {
          handleToggleCollapse();
        }}
      >
        <DragHandleVerticalIcon />
      </button>
    </div>
  );
};

interface IProps {
  children: React.ReactNode;
  defaultSize: number;
  primaryPanelRef: RefObject<ImperativePanelHandle>;
  style?: React.CSSProperties;
}

const ResponsivePanel: React.FC<IProps> = ({
  children,
  defaultSize,
  primaryPanelRef,
  style,
}) => {
  useEffect(() => {
    const handleResize = () => {
      if (primaryPanelRef.current) {
        if (window.innerWidth <= mobileWidth) {
          primaryPanelRef.current.resize(100);
          primaryPanelRef.current.collapse();
        } else {
          primaryPanelRef.current.resize(defaultSize);
        }
      }
    };

    window.addEventListener("resize", handleResize);
    handleResize();

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return (
    <Panel
      collapsible={true}
      defaultSize={defaultSize}
      ref={primaryPanelRef}
      style={style}
    >
      {children}
    </Panel>
  );
};

export { CollapserHandler, ResponsivePanel };
