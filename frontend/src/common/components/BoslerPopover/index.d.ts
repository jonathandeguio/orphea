interface IBoslerPopover {
  children: JSX.Element | string;
  popover: JSX.Element;
  position?: "top" | "right" | "bottom" | "left";
  align?: "start" | "middle" | "end";
  style?: React.CSSProperties;
  trigger?: "hover" | "click";
  displayPopover?: boolean;
}

interface IPopoverBody {
  children: JSX.Element | string;
  style?: React.CSSProperties;
  className?: string;
  popoverContentRef: React.RefObject<HTMLDivElement>;
}
