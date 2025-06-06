import React, { useEffect } from "react";
import { Button, Form, Input, Popover, Space, Typography, message } from "antd";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { AddIcon, SaveIcon } from "assets/icons/boslerActionIcons";
import { getLanguageLabel, getTimeDisplay } from "utils/utilities";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { deleteCategoryAPI, updateCategoryAPI } from "../CreateTags.api";
import {TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import styles from "../CreateTags.module.scss";

const { Text } = Typography;

interface ICategoryDetailsProps {
  onClickAddNewTag: () => void;
  categoryDetails: TCategoryDetails | undefined;
  getCategoryNames:() => void;
  handleback: () => void;
  setSelectedTag: (value: string) => void;
}

const CategoryDetails: React.FC<ICategoryDetailsProps> = ({
  onClickAddNewTag,
  categoryDetails,
  getCategoryNames,
  handleback,
  setSelectedTag,
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (categoryDetails) {
      form.setFieldsValue({
        categoryName: categoryDetails.name,
        categoryDescription: categoryDetails.description,
      });
    }
  }, [categoryDetails, form]);

  const handleSave = (values: {
    categoryName: string;
    categoryDescription: string;
  }) => {
    console.log("Saved values:", values);
    updateCategoryAPI(
      values.categoryName,
      values.categoryDescription,
      categoryDetails?.id as string
    ).then((res)=>{
      (window as any).makeButtonTemporarySuccess(categoryDetails?.id);
      getCategoryNames();
    })
  };

  const handleDelete = () => {
    deleteCategoryAPI(categoryDetails?.id as string).then((res) => {
      handleback();
      getCategoryNames();
    });
    form.resetFields();
  };

  const handleAddTagClick = () => {
    setSelectedTag("new");
    onClickAddNewTag();
  };

  return (
    <div className="--p5">
      <Form
        form={form}
        onFinish={handleSave}
        layout="vertical"
        className="--width100 --height100"
      >
        <Form.Item>
          <div>
            <BoslerButton icon={<AddIcon />} onClick={handleAddTagClick} fill>
              {getLanguageLabel("addNewTag")}
            </BoslerButton>
          </div>
        </Form.Item>
        <Form.Item
          label={<Text className={styles.categoryLabels}>NAME</Text>}
          name="categoryName"
          rules={[
            { required: true, message: "Please enter the category name!" },
          ]}
        >
          <BoslerInput />
        </Form.Item>

        <Form.Item
          label={<Text className={styles.categoryLabels}>DESCRIPTION</Text>}
          name="categoryDescription"
          rules={[
            {
              required: true,
              message: "Please enter the category description!",
            },
          ]}
        >
          <BoslerInput />
        </Form.Item>

        <div className={styles.categoryDetailsActionButtons}>
          <BoslerButton
            minimal
            icononly
            icon={<TrashIcon />}
            onClick={handleDelete}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </BoslerButton>
          <BoslerButton id={categoryDetails?.id} icon={<SaveIcon />} onClick={() => form.submit()}>
          {getLanguageLabel("update")}
          </BoslerButton>
        </div>
      </Form>
    </div>
  );
};

export default CategoryDetails;