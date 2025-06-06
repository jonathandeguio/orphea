import { DragEndEvent } from "@dnd-kit/core";
import { arrayMove } from "@dnd-kit/sortable";
import APIConnector from "Apps/APIConnector/APIConnector";
import SortableWithDrag, {
  TSortableWithDragItem,
} from "common/components/SortableWithDrag/SortableWithDrag";
import React, { useEffect, useState } from "react";
import { ISourceConfig } from "../Sources/Source";
interface IProps {
  fields: any;
  move: any;
  source: ISourceConfig;
  remove: any;
  form: any;
}

const WebhookSortableAPIs = ({
  fields,
  move,
  source,
  remove,
  form,
}: IProps) => {
  const [items, setItems] = useState<TSortableWithDragItem[]>([]);

  const handleDragEnd = ({ active, over }: DragEndEvent) => {
    if (active.id !== over?.id) {
      const activeIndex = items.findIndex(
        (i: { id: string }) => i.id == active.id
      );
      const overIndex = items.findIndex(
        (i: { id: string }) => i.id == over?.id
      );
      const newItems: TSortableWithDragItem[] = arrayMove(
        items,
        activeIndex,
        overIndex
      );

      move(activeIndex, overIndex);
      setItems(newItems);
    }
  };

  useEffect(() => {
    const _items: TSortableWithDragItem[] = [];
    fields.map((field: any, index: number) => {
      _items.push({
        id: field.name,
        children: (
          <div className={"--width100"}>
            <APIConnector
              apiIndex={index}
              source={source}
              outerName={field.name}
              removeRequest={remove}
              form={form}
            />
          </div>
        ),
      });
    });

    setItems(_items);
  }, [fields]);

  return <SortableWithDrag items={items} handleDragEnd={handleDragEnd} />;
};

export default WebhookSortableAPIs;
