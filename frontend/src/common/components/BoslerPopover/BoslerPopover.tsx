import { useOutsideClickHandler } from "hooks/useOutsideClickHandler";
import React, { useRef, useState } from "react";
import ReactDOM from "react-dom";
import "./BoslerPopover.scss";

const PopoverBody: React.FC<IPopoverBody> = ({
  children,
  style,
  popoverContentRef,
}) => {
  return ReactDOM.createPortal(
    <div ref={popoverContentRef} style={style} className="popover-content">
      {children}
    </div>,
    document.body
  );
};

export const BoslerPopover: React.FC<IBoslerPopover> = ({
  position = "right",
  align = "start",
  trigger = "hover",
  style = {},
  displayPopover = true,
  popover,
  children,
}) => {
  const popoverRef = useRef<HTMLDivElement>(null);
  const popoverContentRef = useRef<HTMLDivElement>(null);
  const [popoverStyle, setPopoverStyle] = useState({});

  const [showPopover, setShowPopover] = useState(false);
  useOutsideClickHandler(
    () => setShowPopover(false),
    [popoverRef, popoverContentRef]
  );

  const handleMouseEnter = () => {
    setShowPopover(true);
    if (popoverRef.current) {
      const targetRect = popoverRef.current.getBoundingClientRect();
      setPopoverStyle({
        top: targetRect.top,
        left: targetRect.left + targetRect.width + 10,
      });
    }
  };

  const handleMouseLeave = () => setShowPopover(false);

  return (
    <div
      ref={popoverRef}
      className="popover"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      {children}
      {showPopover && displayPopover && (
        <PopoverBody popoverContentRef={popoverContentRef} style={popoverStyle}>
          {popover}
        </PopoverBody>
      )}
    </div>
  );
};
