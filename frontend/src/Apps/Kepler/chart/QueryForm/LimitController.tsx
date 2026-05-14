import { InputNumber } from "antd";
import React, { useCallback } from "react";
import { useDispatch } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel, makeDebounceFunction } from "utils/utilities";
import { updateQuery } from "../../../../redux/actions/keplerActions";

export const LimitController = ({ form }: { form: any }) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const deb = useCallback(
    makeDebounceFunction((e: number) => {
      form.setFieldValue("rowLimit", e);
      dispatch(updateQuery({ rowLimit: e }));
    }, 800),

    [form]
  );

  return (
    <>
      <div style={{ marginTop: "1rem", marginLeft: "2rem" }} className="query_item__heading">
        <strong>{getLanguageLabel("limitRows")}</strong>
      </div>
      <div style={{ marginLeft: "2rem" }}>
      <InputNumber
        defaultValue={form.getFieldValue("rowLimit")}
        onChange={deb}
        min={0}
        max={50000}
        style={{ width: "100%" }}
      />
      </div>
    </>
  );
};
