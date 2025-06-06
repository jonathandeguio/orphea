import React from "react";
import { List } from "antd";
import { SelectNodeIcon } from "assets/icons/boslerActionIcons";
import styles from "../CreateTags.module.scss";

interface ICategortyListProps {
  categories: string[];
  handleCategoryClick: (category: string) => void;
}
const CategoryListComp: React.FC<ICategortyListProps> = ({categories,handleCategoryClick}) => {
  return (
    <List
      dataSource={categories}
      renderItem={(category, index) => (
        <List.Item
          key={"category" + category}
          onClick={() => handleCategoryClick(category)}
          className={styles.listItem}
        >
          <div className={`--flex-row --flex-gap20`}>
            <SelectNodeIcon />
            {category}
          </div>
        </List.Item>
      )}
    />
  );
};
export default CategoryListComp;
