import { FormInstance } from "antd";

export interface IFormvalues {
  queryParams: any[];
  url: any;
  method?: any;
  headers?: any;
  body?: any;
  formData?: any;
}

export interface IMappingTypeOverlayProps {
  form: FormInstance;
  position: { top: number; left: number };
  setOverlayVisible: (visible: boolean) => void;
  overlayFieldValue: any;
  overlayAtPos: any;
  // setValue: (value: string) => void;
  // setReadOnlyDisplayValue: (displayValue: string) => void;
}
