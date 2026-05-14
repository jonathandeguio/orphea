import type { Meta } from "@storybook/react";
import OrpheaButton from "./OrpheaButton";

const meta = {
  component: OrpheaButton,
  parameters: {
    layout: "centered",
  },
  argTypes: {
    color: {
      control: "select",
    },
    onClick: {
      control: {
        disable: true,
      },
    },
  },
  args: {
    children: "button",
    color: "primary",
    size: "medium",
  },
} satisfies Meta<any>;

export default meta;

export { OrpheaButton };
