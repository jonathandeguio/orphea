import React from "react";
import "./ResizablePane.scss";
import { DragHandleVerticalIcon } from "assets/icons/boslerActionIcons";

const CollapserHandler = ({ primaryPanelRef }: { primaryPanelRef: any }) => {
  const handleToggleCollapse = () => {
    if (primaryPanelRef.current) {
      if (!primaryPanelRef.current.isCollapsed()) {
        primaryPanelRef.current.collapse();
      } else {
        primaryPanelRef.current.expand();
      }
    }
  };

  // useEffect(() => {
  //   const handleResize = () => {
  //     const isSmallScreen = window.matchMedia("(max-width: 768px)").matches;
  //     if (primaryPanelRef.current) {
  //       if (isSmallScreen) {
  //         primaryPanelRef.current.collapse();
  //         setCollapsed(true);
  //       } else {
  //         primaryPanelRef.current.expand();
  //         setCollapsed(false);
  //       }
  //     }
  //   };

  //   handleResize();

  //   window.addEventListener("resize", handleResize);

  //   return () => {
  //     window.removeEventListener("resize", handleResize);
  //   };
  // }, []);

  return (
    <div className="resizablePane-collapser-box">
      <button
        className="resizablePane-collapser-box-button"
        onClick={() => {
          handleToggleCollapse();
        }}
      >
        <DragHandleVerticalIcon />
        {/* {!collapsed ? <DoubleChevronLeftIcon /> : <DoubleChevronRightIcon />} */}
      </button>
    </div>
  );
};

export { CollapserHandler };
