import { Form } from "antd";
import { TableIcon } from "assets/icons/boslerTableIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import React from "react";
import {
  DataHealthTypeEnum,
  IDataHealthCheck,
  IDataHealthDTO,
} from "../DataHealth.types";

const JobStatus = ({ form, handleSave }: IDataHealthCheck) => {
  return (
    <div>
      <BoslerHeader
        icon={<TableIcon />}
        heading={"Job Status"}
        description="Checks the status of most recent job of a dataset"
      />
      <Form
        form={form}
        onKeyPress={(e) => {
          if (e.key === "Enter" && !e.shiftKey) {
            form.submit();
          }
        }}
        onFinish={(values) => {
          const dataHealthDTO: IDataHealthDTO = {
            rule: values.rule,
            notes: values.notes,
            dataHealthType: DataHealthTypeEnum.JOBSTATUS,
          };

          handleSave(dataHealthDTO);
        }}
      >
        <div className="BoslerHeader1">{"Rule"}</div>
        <Form.Item name="rule">
          {
            "This check passes when status of the most recent job of the dataset."
          }
        </Form.Item>
        <div className="BoslerHeader1">{"Notes"}</div>
        <Form.Item
          name="notes"
          rules={[
            {
              required: false,
            },
          ]}
        >
          <BoslerInput />
        </Form.Item>
      </Form>
    </div>
  );
};

export default JobStatus;
