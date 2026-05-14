import React from "react";

import { Form } from "antd";
import { HierarchyController } from "./HierarchyController";

function DimensionController(props: { Heading?: string; form: any }) {
  return (
    <Form.List name="dimensions">
      {(fields, { add, remove, move }) => {
        return (
          <>
            <div className="query_item">
              <div className="query_item__heading">{props.Heading}</div>

              <div className="query_item__body">
                <HierarchyController
                  form={props.form}
                  add={add}
                  fields={fields}
                  remove={remove}
                  move={move}
                />
              </div>
            </div>
          </>
        );
      }}
    </Form.List>
  );
}

export default DimensionController;
