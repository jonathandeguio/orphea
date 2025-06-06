import { Form, Popover } from "antd";
import React from "react";
import { generateKey } from "utils/utilities";
import FiltersAddPopoverContent, {
  TCondition,
} from "./FilterAddPopoverContent";
import { TDatasetColumn } from "./Filters.view";

const FilterAddPopover = ({
  currentFilters,
  setCurrentFilters,
  filter,
  children,
  fieldOptions,
  editMode,
  open,
  setOpen,
}: {
  currentFilters: any[];
  setCurrentFilters: any;
  filter?: any;
  children?: any;
  fieldOptions: TDatasetColumn[];
  editMode: boolean;
  open?: boolean;
  setOpen?: any;
}) => {
  const [form] = Form.useForm();
  const defaultFormValues = {
    field: filter ? filter.field : undefined,
    conditionCase:
      filter && filter.conditionCase
        ? filter.conditionCase
        : [
            {
              operator: undefined,
              value: undefined,
              key: `${"Condition"}_${new Date().getTime()}`,
            },
          ],
    logicalOperator:
      filter && filter.logicalOperator ? filter.logicalOperator : "AND",
  };
  const newUniqueId = generateKey("filter");

  const getValidConditions = (
    field: TDatasetColumn,
    conditionCase: TCondition[]
  ) => {
    const validConditions: TCondition[] = [];

    if (!conditionCase) return validConditions;
    //
    conditionCase.map((condition: TCondition) => {
      if (field && field.type == "boolean" && condition.value) {
        validConditions.push(condition);
      } else if (
        field &&
        condition.operator &&
        ["exists", "doesNotExist"].includes(condition.operator)
      ) {
        validConditions.push(condition);
      } else if (field && condition.operator && condition.value) {
        validConditions.push(condition);
      }
    });
    return validConditions;
  };

  const onFilterApply = () => {
    const field = form.getFieldValue("field");
    const conditionCase = form.getFieldValue("conditionCase");
    const logicalOperator = form.getFieldValue("logicalOperator");

    const validConditions = getValidConditions(field, conditionCase);
    const isEmptyConditionCase = validConditions.length == 0 ? true : false;

    const emptyCondition = !field || isEmptyConditionCase;

    if (emptyCondition) {
      form.resetFields();
      setOpen(undefined);
      return;
    }

    let newFilters = undefined;

    if (filter) {
      newFilters = [...currentFilters].map((current) => {
        if (current.key == filter.key) {
          return {
            field: field,
            conditionCase: validConditions,
            logicalOperator: logicalOperator,
            key: newUniqueId,
          };
        }
        return current;
      });
    } else {
      newFilters = [...currentFilters];
      newFilters.push({
        field: field,
        conditionCase: validConditions,
        logicalOperator: logicalOperator,
        key: newUniqueId,
      });

      form.resetFields();
    }

    setCurrentFilters(newFilters);
    setOpen(false);
  };

  return (
    <Form form={form} layout="vertical" initialValues={defaultFormValues}>
      <Popover
        content={
          <FiltersAddPopoverContent
            editMode={editMode}
            fieldOptions={fieldOptions}
            form={form}
            onFilterApply={onFilterApply}
          />
        }
        trigger="click"
        onOpenChange={(visible: boolean) => {
          setOpen(visible);
        }}
        destroyTooltipOnHide={true}
        open={open}
      >
        {children}
      </Popover>
    </Form>
  );
};
export default FilterAddPopover;
