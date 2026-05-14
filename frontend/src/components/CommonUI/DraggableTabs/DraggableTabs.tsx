import { DndContext, PointerSensor, useSensor } from "@dnd-kit/core";
import {
  SortableContext,
  horizontalListSortingStrategy,
  useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Tabs, TabsProps } from "antd";
import React from "react";

interface IProps extends TabsProps {
  isDraggable: boolean;
  onDragEnd?: any;
}
interface DraggableTabPaneProps extends React.HTMLAttributes<HTMLDivElement> {
  "data-node-key": string;
}

const DraggableTabNode = ({ className, ...props }: DraggableTabPaneProps) => {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable({
      id: props["data-node-key"],
    });

  const style: React.CSSProperties = {
    ...props.style,
    transform: CSS.Transform.toString(transform && { ...transform, scaleX: 1 }),
    transition,
    cursor: "move",
  };

  return React.cloneElement(props.children as React.ReactElement, {
    ref: setNodeRef,
    style,
    ...attributes,
    ...listeners,
  });
};
const DraggableTabs = (props: IProps) => {
  const sensor = useSensor(PointerSensor, {
    activationConstraint: { distance: 10 },
  });
  return (
    <Tabs
      {...props}
      renderTabBar={(tabBarProps, DefaultTabBar) => {
        return props.isDraggable ? (
          <DndContext sensors={[sensor]} onDragEnd={props.onDragEnd}>
            <SortableContext
              items={(props.items ? props.items : []).map(
                (i: { key: any }) => i.key
              )}
              strategy={horizontalListSortingStrategy}
            >
              <DefaultTabBar {...tabBarProps}>
                {(node) => (
                  <DraggableTabNode {...node.props} key={node.key}>
                    {node}
                  </DraggableTabNode>
                )}
              </DefaultTabBar>
            </SortableContext>
          </DndContext>
        ) : (
          <DefaultTabBar {...tabBarProps} />
        );
      }}
    />
  );
};

export default DraggableTabs;
