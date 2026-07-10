import { MenuProps, Segmented, Select, Switch, Typography } from "antd";
import React, { useEffect, useState } from "react";
import "../ButtonComponent/ButtonComponentDisplay.scss";
import MoveToDataButton from "./MoveToDataButton";
import { SearchIcon } from "assets/icons/movetodataActionIcons";
import { ArrowTopRightIcon } from "assets/icons/movetodataNavigationIcon";

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
const MoveToDataComponentDisplay = () => {
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

  let buttonString = "<MoveToDataButton icon={<SearchIcon />} ";
  let buttonStringWithActionIcon = `<MoveToDataButton icon={<SearchIcon />} actionIcon={<ArrowTopRightIcon />}
  onClick={(e: any) => {
    
  }}
  onClickActionIcon={(e: any) => {
    e.stopPropagation();
    
  }}`;
  let buttonStringWithMenuItems =
    "<MoveToDataButton icon={<SearchIcon />} menuItems={items} ";

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

  buttonString += "> Primary Button </MoveToDataButton>";
  buttonStringWithActionIcon += "> Primary Button </MoveToDataButton>";
  buttonStringWithMenuItems += "> Primary Button </MoveToDataButton>";

  return (
    <div className="movetodataComponents-buttonDisplay">
      <div className="movetodataComponents-buttonDisplay-preview">
        <div className="movetodataComponents-buttonDisplay-preview-screen">
          <MoveToDataButton icon={<SearchIcon />} {...btnProps}>
            Primary
          </MoveToDataButton>
          <MoveToDataButton
            icon={<SearchIcon />}
            {...btnProps}
            actionIcon={<ArrowTopRightIcon />}
            onClick={(e: any) => {}}
            onClickActionIcon={(e: any) => {
              e.stopPropagation();
            }}
          >
            Primary Button
          </MoveToDataButton>

          <MoveToDataButton icon={<SearchIcon />} menuItems={items} {...btnProps}>
            Primary Button
          </MoveToDataButton>
        </div>
        <Text
          code
          copyable
          className="movetodataComponents-buttonDisplay-preview-code"
        >
          {buttonString}
        </Text>

        <Text
          code
          copyable
          className="movetodataComponents-buttonDisplay-preview-code"
        >
          {buttonStringWithActionIcon}
        </Text>

        <Text
          code
          copyable
          className="movetodataComponents-buttonDisplay-preview-code"
        >
          {buttonStringWithMenuItems}
        </Text>
      </div>
      <div className="movetodataComponents-buttonDisplay-controller">
        <Title level={5}>Props</Title>
        <div className="movetodataComponents-buttonDisplay-controller-items">
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
        <div className="movetodataComponents-buttonDisplay-controller-items">
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
        <div className="movetodataComponents-buttonDisplay-controller-items">
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

export default MoveToDataComponentDisplay;
