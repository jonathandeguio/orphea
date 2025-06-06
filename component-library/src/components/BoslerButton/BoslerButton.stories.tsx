import type { Meta } from "@storybook/react";
import BoslerButton from "./BoslerButton";

const meta = {
  component: BoslerButton,
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

export { BoslerButton };
