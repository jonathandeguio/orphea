import {
  DndContext,
  KeyboardSensor,
  MouseSensor as LibMouseSensor,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import type { MouseEvent } from "react";
import React from "react";
import SortableItem from "./SortableItem";
export type TSortableWithDragItem = {
  id: string;
  children: React.ReactElement;
};
interface IProps {
  items: TSortableWithDragItem[];
  handleDragEnd: any;
}

interface DraggableNodeProps extends React.HTMLAttributes<HTMLDivElement> {
  id: string;
}

import { TouchSensor as LibTouchSensor } from "@dnd-kit/core";
import { TouchEvent } from "react";

// Block DnD event propagation if element have "data-no-dnd" attribute
const handler = ({ nativeEvent: event }: MouseEvent | TouchEvent) => {
  let cur = event.target as HTMLElement;

  while (cur) {
    if (cur.dataset && cur.dataset.noDnd) {
      return false;
    }
    cur = cur.parentElement as HTMLElement;
  }

  return true;
};

export class MouseSensor extends LibMouseSensor {
  static activators = [
    { eventName: "onMouseDown", handler },
  ] as (typeof LibMouseSensor)["activators"];
}

export class TouchSensor extends LibTouchSensor {
  static activators = [
    { eventName: "onTouchStart", handler },
  ] as (typeof LibTouchSensor)["activators"];
}

const DraggableNode = ({ className, ...props }: DraggableNodeProps) => {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable({
      id: props["id"],
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
const SortableWithDrag = ({ items, handleDragEnd }: IProps) => {
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  //   const handleDragEnd = ({ active, over }: DragEndEvent) => {
  //     if (active.id !== over?.id) {
  //       setSortableWithDragItems((prev: any[]) => {
  //         const activeIndex = prev.findIndex((i) => i.id === active.id);
  //         const overIndex = prev.findIndex((i) => i.id === over?.id);

  //         return arrayMove(prev, activeIndex, overIndex);
  //       });
  //     }
  //   };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
    >
      <SortableContext
        items={(items ? items : []).map((i: TSortableWithDragItem) => i.id)}
        strategy={verticalListSortingStrategy}
      >
        {items.map((item) => {
          return (
            <SortableItem key={item.id} id={item.id}>
              {item.children}
            </SortableItem>
          );
        })}
      </SortableContext>
    </DndContext>
  );
};

export default SortableWithDrag;
