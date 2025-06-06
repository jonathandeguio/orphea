import React, { useState } from "react";
import { Dropdown, Button, Form, Input, Space, message } from "antd";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { AddIcon, CrossIcon, SaveIcon } from "assets/icons/boslerActionIcons";
import { getLanguageLabel } from "utils/utilities";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { saveCategoryAPI } from "../CreateTags.api";
import styles from "../CreateTags.module.scss";

interface NewCategoryProps {
  onSaveCategory: () => void;
}
const NewCategory: React.FC<NewCategoryProps> = ({ onSaveCategory }) => {
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm();

  const handleSave = async (values: { name: string; description: string }) => {
    try {
      console.log("Saving category:", values);
      saveCategoryAPI(values.name, values.description).then((res) => {
        onSaveCategory();
      });
      form.resetFields();
      setVisible(false);
    } catch (error) {
      console.error("Failed to save category!");
    }
  };

  const handleCancel = () => {
    form.resetFields();
    setVisible(false);
  };

  return (
    <Dropdown
      open={visible}
      onOpenChange={setVisible}
      dropdownRender={() => (
        <div className={styles.dropdownContent}>
          <Form
            form={form}
            onFinish={handleSave}
            layout="vertical"
            style={{ width: "300px" }}
          >
            {/* Name Input */}
            <Form.Item
              name="name"
              rules={[
                { required: true, message: "Please enter a category name!" },
              ]}
            >
              <BoslerInput
                autofocus
                placeholder={getLanguageLabel("enterCategoryName")}
              />
            </Form.Item>

            <Form.Item
              name="description"
              rules={[
                {
                  required: true,
                  message: "Please enter a category description!",
                },
              ]}
            >
              <BoslerInput
                placeholder={getLanguageLabel("enterCategoryDescription")}
              />
            </Form.Item>

            <Space className={styles.spaceContainer}>
              <BoslerButton
                onClick={handleCancel}
                icononly
                icon={<CrossIcon />}
                minimal
              >
                Cancel
              </BoslerButton>
              <BoslerButton
                icon={<SaveIcon />}
                intent="primary"
                htmlType="submit"
              >
                Save
              </BoslerButton>
            </Space>
          </Form>
        </div>
      )}
    >
      <BoslerButton icon={<AddIcon />} intent="action">
        {getLanguageLabel("newCategory")}
      </BoslerButton>
    </Dropdown>
  );
};

export default NewCategory;