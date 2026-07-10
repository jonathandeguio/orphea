import type { Meta } from "@storybook/react";
import MoveToDataButton from "./MoveToDataButton";

const meta = {
  component: MoveToDataButton,
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

export { MoveToDataButton };
