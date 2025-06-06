import { MenuProps, Segmented, Select, Switch, Typography } from "antd";
import React, { useEffect, useState } from "react";
import { SearchIcon } from "../../../assets/icons/boslerActionIcons";
import { PopOutIcon } from "../../../assets/icons/boslerNavigationIcon";
import "../ButtonComponent/ButtonComponentDisplay.scss";
import BoslerButton from "./BoslerButton";

const { Title, Text } = Typography;

const items: MenuProps["items"] = [
  {
    key: "1",
    label: (
      <a
        target="_blank"
        rel="noopener noreferrer"
        href="https://www.antgroup.com"
      >
        1st menu item
      </a>
    ),
  },
  {
    key: "2",
    label: (
      <a
        target="_blank"
        rel="noopener noreferrer"
        href="https://www.aliyun.com"
      >
        2nd menu item
      </a>
    ),
  },
  {
    key: "3",
    label: (
      <a
        target="_blank"
        rel="noopener noreferrer"
        href="https://www.luohanacademy.com"
      >
        3rd menu item
      </a>
    ),
  },
];
const BoslerComponentDisplay = () => {
  const DEFAULT_STATE = {};
  const [btnProps, setBtnProps] = useState<any>(DEFAULT_STATE);

  const SwitchProp = ({ name }: { name: string }) => {
    const key = name.toLowerCase();
    const isChecked = btnProps.hasOwnProperty(key) ? btnProps[key] : false;
    return (
      <div style={{ display: "flex", gap: "1rem" }}>
        <Switch
          checked={isChecked}
          onChange={(checked: boolean) => {
            setBtnProps({ ...btnProps, ...{ [key]: checked } });
          }}
        />
        <Text>{name}</Text>
      </div>
    );
  };

  useEffect(() => {}, [btnProps]);

  let buttonString = "<BoslerButton icon={<SearchIcon />} ";
  let buttonStringWithActionIcon = `<BoslerButton icon={<SearchIcon />} actionIcon={<PopOutIcon />}
  onClick={(e: any) => {
    
  }}
  onClickActionIcon={(e: any) => {
    e.stopPropagation();
    
  }}`;
  let buttonStringWithMenuItems =
    "<BoslerButton icon={<SearchIcon />} menuItems={items} ";

  for (const prop in btnProps) {
    if (
      btnProps[prop] != false &&
      btnProps[prop] != "middle" &&
      btnProps[prop] != "center"
    ) {
      if (btnProps[prop] == true) {
        buttonString += `${prop}={${btnProps[prop]}} `;
        buttonStringWithActionIcon += `${prop}={${btnProps[prop]}} `;
        buttonStringWithMenuItems += `${prop}={${btnProps[prop]}} `;
      } else {
        buttonString += `${prop}="${btnProps[prop]}" `;
        buttonStringWithActionIcon += `${prop}="${btnProps[prop]}" `;
        buttonStringWithMenuItems += `${prop}="${btnProps[prop]}" `;
      }
    }
  }

  buttonString += "> Primary Button </BoslerButton>";
  buttonStringWithActionIcon += "> Primary Button </BoslerButton>";
  buttonStringWithMenuItems += "> Primary Button </BoslerButton>";

  return (
    <div className="boslerComponents-buttonDisplay">
      <div className="boslerComponents-buttonDisplay-preview">
        <div className="boslerComponents-buttonDisplay-preview-screen">
          <BoslerButton icon={<SearchIcon />} {...btnProps}>
            Primary
          </BoslerButton>
          <BoslerButton
            icon={<SearchIcon />}
            {...btnProps}
            actionIcon={<PopOutIcon />}
            onClick={(e: any) => {}}
            onClickActionIcon={(e: any) => {
              e.stopPropagation();
            }}
          >
            Primary Button
          </BoslerButton>

          <BoslerButton icon={<SearchIcon />} menuItems={items} {...btnProps}>
            Primary Button
          </BoslerButton>
        </div>
        <Text
          code
          copyable
          className="boslerComponents-buttonDisplay-preview-code"
        >
          {buttonString}
        </Text>

        <Text
          code
          copyable
          className="boslerComponents-buttonDisplay-preview-code"
        >
          {buttonStringWithActionIcon}
        </Text>

        <Text
          code
          copyable
          className="boslerComponents-buttonDisplay-preview-code"
        >
          {buttonStringWithMenuItems}
        </Text>
      </div>
      <div className="boslerComponents-buttonDisplay-controller">
        <Title level={5}>Props</Title>
        <div className="boslerComponents-buttonDisplay-controller-items">
          {/* <SwitchProp name="Active" /> */}
          <SwitchProp name="Disabled" />
          <SwitchProp name="Loading" />
          <SwitchProp name="Minimal" />
          <SwitchProp name="Outlined" />
          <SwitchProp name="Dashed" />
          <SwitchProp name="Borderless" />
          <SwitchProp name="Fill" />
          <SwitchProp name="IconOnly" />
          <SwitchProp name="TrimIconOnlyPadding" />
        </div>
        <Title level={5}>Align Text</Title>
        <div className="boslerComponents-buttonDisplay-controller-items">
          <Segmented
            options={["left", "center", "right"]}
            value={
              btnProps.hasOwnProperty("alignText")
                ? btnProps["alignText"]
                : "center"
            }
            onChange={(value) =>
              setBtnProps({ ...btnProps, ...{ alignText: value } })
            }
          />
        </div>
        <Title level={5}>Size</Title>
        <div className="boslerComponents-buttonDisplay-controller-items">
          <Segmented
            options={["small", "middle", "large"]}
            value={
              btnProps.hasOwnProperty("size") ? btnProps["size"] : "middle"
            }
            onChange={(value) =>
              setBtnProps({ ...btnProps, ...{ size: value } })
            }
          />
        </div>
        <Title level={5}>Intent</Title>
        <Select
          defaultValue="none"
          style={{ width: 120 }}
          onChange={(value) => {
            setBtnProps({ ...btnProps, ...{ intent: value } });
          }}
          options={[
            { value: "none", label: "none" },
            { value: "primary", label: "primary" },
            { value: "action", label: "action" },
            { value: "success", label: "success" },
            { value: "dangerous", label: "dangerous" },
            { value: "warning", label: "warning" },
          ]}
        />
      </div>
    </div>
  );
};

export default BoslerComponentDisplay;
