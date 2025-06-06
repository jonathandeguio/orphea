import React, { useState, useEffect, useRef } from "react";
import {
  Form,
  Divider,
  Typography,
  Row,
  Col,
  Tag,
  ColorPicker,
  Popover,
  Tooltip,
} from "antd";
import styles from "./CreateTags.module.scss";
import { getLanguageLabel } from "utils/utilities";
const { Title, Text } = Typography;
import {
  CollapserHandler,
  ResponsivePanel,
} from "components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { SaveIcon, SearchIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import {
  deleteTagAPI,
  fetchAllCategoryNamesAPI,
  fetchCategoryDetailsAndTagsAPI,
  saveTagAPI,
  updateTagAPI,
} from "./CreateTags.api";
import TagsTableHeader from "./components/TagsTableHeader";
import NewCategory from "./components/NewCategory";
import CategoryDetails from "./components/CategoryDetails";
import { SingleChevronLeftIcon } from "assets/icons/boslerNavigationIcon";
import { DEAFAULT_COLORS } from "pages/Settings/tags/CreateTags.constants";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import UserInfo from "./components/UserInfo";
import NoData from "components/CommonUI/NoData";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import CategoryListComp from "./components/CategoryList";

const CreateTags: React.FC = () => {
  const [form] = Form.useForm();

  const [selectedTag, setSelectedTag] = useState<string | undefined>(undefined);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [categoryDetails, setCategoryDetails] = useState<TCategoryDetails>();
  const [tags, setTags] = useState<TTag[]>([]);
  const [categoriesList, setCategoriesList] = useState<string[]>([]);
  const [searchValue, setSearchValue] = useState<string | undefined>("");

  const primaryPanelRef = useRef<any>(null);

  const getCategoryNames = async () => {
    try {
      fetchAllCategoryNamesAPI().then((res) => {
        setCategoriesList(res.data);
      });
    } catch (error) {
      console.error("Something went wrong", " ", "error");
    }
  };

  const handleCategoryClick = (category: string) => {
    setSelectedCategory(category);
    fetchCategoryDetailsAndTagsAPI(category).then((res) => {
      setCategoryDetails(res.data);
      setTags(res.data.tags);
      form.setFieldsValue({ tags: res.data.tags });
    });
  };

  const handleAddTag = () => {
    const newTag: TTag = {
      color: "",
      name: "",
      description: "",
      createdAt: new Date().toISOString(),
      createdBy: "System",
      updatedAt: null,
      updatedBy: null,
    };
    setTags((prevTags) => {
      const updatedTags = [...prevTags, newTag];
      form.setFieldsValue({ tags: updatedTags });
      return updatedTags;
    });
  };

  const handleBack = () => {
    setSelectedCategory(null);
    setCategoryDetails(undefined);
    form.setFieldsValue({ tags: [] });
  };

  const saveOrUpdateTag = async (
    tag: TTag,
    selectedTag: string,
    saveButtonId: string
  ) => {
    if (selectedTag === "new") {
      try {
        saveTagAPI(
          tag.name,
          tag.description,
          tag.color,
          categoryDetails?.id as string
        ).then((res) => {
          (window as any).makeButtonTemporarySuccess(saveButtonId);
          setSelectedTag(undefined);
        });
      } catch (error) {
        console.error("Error saving tag:", error);
      }
    } else {
      updateTagAPI(tag.name, tag.description, tag.color, selectedTag).then(
        (res) => {
          (window as any).makeButtonTemporarySuccess(saveButtonId);
          setSelectedTag(undefined);
        }
      );
    }
  };

  const handleUpdatedCategories = () => {
    handleBack();
    getCategoryNames();
  };

  const handleDeleteTag = (tagId: string) => {
    deleteTagAPI(tagId);
  };

  const handleSearch = (searchValue: string) => {
    setSearchValue(searchValue);
  };

  const handleTagFieldChange = (
    fieldName: string,
    name: number,
    value: string
  ) => {
    const currentTags = form.getFieldValue("tags") || {};

    if (selectedTag !== "new") {
      setSelectedTag(form.getFieldValue("tags")[name]?.id);
    }

    form.setFieldsValue({
      tags: {
        ...currentTags,
        [name]: {
          ...(currentTags[name] || {}),
          [fieldName]: value,
        },
      },
    });
  };

  const handleTagColorChange = (
    color: any,
    form: any,
    name: number,
    selectedTag: string,
    setSelectedTag: (tagId: string) => void
  ) => {
    form.setFieldsValue({
      tags: form.getFieldValue("tags").map((tag: any, idx: number) =>
        idx === name
          ? {
              ...tag,
              color: color.toHexString(),
            }
          : tag
      ),
    });

    if (selectedTag !== "new") {
      setSelectedTag(form.getFieldValue("tags")[name]?.id);
    }
  };

  const handleTagClick = (
    color: string,
    form: any,
    name: number,
    selectedTag: string,
    setSelectedTag: (tagId: string) => void
  ) => {
    form.setFieldsValue({
      tags: form
        .getFieldValue("tags")
        .map((tag: any, idx: number) =>
          idx === name ? { ...tag, color } : tag
        ),
    });

    if (selectedTag !== "new") {
      setSelectedTag(form.getFieldValue("tags")[name]?.id);
    }
  };

  const filteredCategories =
    searchValue == undefined
      ? categoriesList
      : categoriesList.filter((category) =>
          category.toLowerCase().includes(searchValue?.toLowerCase())
        );

  useEffect(() => {
    getCategoryNames();
  }, []);

  return (
    <div className="settings-center-block">
      <Row justify="space-between">
        <Col>
          <Title level={3}>{getLanguageLabel("tags")}</Title>
          <Text type="secondary">
            {getLanguageLabel("tagListAndCategories")}
          </Text>
        </Col>
      </Row>
      <Divider />
      <div className="--flex-row-space-between --mb15">
        <BoslerInput
          className="search-input"
          placeholder={getLanguageLabel("searchCategories")}
          allowClear
          suffix={<SearchIcon />}
          onChange={(e) => handleSearch(e.target.value)}
        />
        <NewCategory onSaveCategory={getCategoryNames} />
      </div>
      <div className={styles.panelGroupWrapper}>
        <PanelGroup direction={"horizontal"}>
          <ResponsivePanel defaultSize={25} primaryPanelRef={primaryPanelRef}>
            <div
              className={`${styles.categoryListHeader} ${
                selectedCategory === null
                  ? "--flex-center"
                  : "--flex-space-between"
              }`}
            >
              {selectedCategory === null ? (
                <div className="categories">
                  <strong>{getLanguageLabel("categories")}</strong>
                </div>
              ) : (
                <>
                  <div className="grabbable" onClick={handleBack}>
                    <SingleChevronLeftIcon />
                  </div>
                  <div style={{ textAlign: "center", flexGrow: 1 }}>
                    <strong>{getLanguageLabel("details")}</strong>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <UserInfo categoryDetails={categoryDetails}></UserInfo>
                  </div>
                </>
              )}
            </div>
            <Divider className={styles.categoryListDivider} />
            <div className={`${styles.categoryList} --p10`}>
              {selectedCategory === null ? (
                <CategoryListComp
                  categories={filteredCategories}
                  handleCategoryClick={handleCategoryClick}
                />
              ) : (
                <CategoryDetails
                  onClickAddNewTag={handleAddTag}
                  categoryDetails={categoryDetails}
                  getCategoryNames={getCategoryNames}
                  handleback={handleBack}
                  setSelectedTag={setSelectedTag}
                />
              )}
            </div>
          </ResponsivePanel>
          <PanelResizeHandle className="resizablePane-collapser">
            <CollapserHandler primaryPanelRef={primaryPanelRef} />
          </PanelResizeHandle>
          <Panel>
            <Form form={form} layout="vertical" initialValues={{ tags }}>
              <Form.Item className={styles.tagsTableHeader}>
                <TagsTableHeader />
              </Form.Item>

              <Divider className={styles.tableDivider} />
              <div className="--pd10">
                {selectedCategory === null || tags.length === 0 ? (
                  <div style={{ marginTop: "140px" }}>
                    <NoData
                      heading={getLanguageLabel("noTags")}
                      icon={<SearchEmptyState size="90px" />}
                    ></NoData>
                  </div>
                ) : (
                  <Form.List name="tags">
                    {(fields, { remove }) => (
                      <>
                        {fields.map(({ key, name, ...restField }, index) => (
                          <div key={name} className={styles.tagTableRow}>
                            <Form.Item
                              {...restField}
                              name={[name, "name"]}
                              validateTrigger="onBlur"
                              rules={[
                                {
                                  required: true,
                                  message: "Tag name is required",
                                },
                              ]}
                              style={{ width: "25%" }}
                            >
                              <div style={{ width: "75%" }}>
                                <Tooltip title="click here to edit">
                                  <BoslerInput
                                    editText
                                    placeholder={getLanguageLabel(
                                      "enterTagName"
                                    )}
                                    defaultValue={
                                      categoryDetails?.tags[name]?.name
                                    }
                                    onChange={(e) =>
                                      handleTagFieldChange(
                                        "name",
                                        name,
                                        e.target.value
                                      )
                                    }
                                  />
                                </Tooltip>
                              </div>
                            </Form.Item>
                            <Form.Item
                              {...restField}
                              name={[name, "description"]}
                              validateTrigger="onBlur"
                              rules={[
                                {
                                  required: true,
                                  message: "Tag description is required",
                                },
                              ]}
                              style={{ width: "40%", paddingRight: "0px" }}
                            >
                              <div style={{ width: "75%" }}>
                                <Tooltip title="click here to edit">
                                  <BoslerInput
                                    editText
                                    placeholder={getLanguageLabel(
                                      "enterTagDescription"
                                    )}
                                    defaultValue={
                                      categoryDetails?.tags[name]?.description
                                    }
                                    onChange={(e) =>
                                      handleTagFieldChange(
                                        "description",
                                        name,
                                        e.target.value
                                      )
                                    }
                                  />
                                </Tooltip>
                              </div>
                            </Form.Item>
                            <Form.Item
                              {...restField}
                              name={[name, "color"]}
                              rules={[
                                {
                                  required: true,
                                  message: "Tag color is required",
                                },
                              ]}
                              style={{ width: "24%" }}
                            >
                              <div className={`--flex-row --flex-gap2`}>
                                <div
                                  className={styles.tagColorDiv}
                                  style={{
                                    backgroundColor: `${
                                      form.getFieldValue("tags")[name]?.color ||
                                      categoryDetails?.tags[name]?.color ||
                                      "red"
                                    }`,
                                  }}
                                ></div>
                                <Popover
                                  placement="topRight"
                                  content={
                                    <div className={styles.tagColorPopOver}>
                                      <div
                                        className={`--flex-row --flex-center`}
                                      >
                                        <span>Select Color:</span>
                                        <div className="--pd10">
                                          <ColorPicker
                                            value={
                                              form.getFieldValue("tags")[name]
                                                ?.color ||
                                              categoryDetails?.tags[name]
                                                ?.color ||
                                              "red"
                                            }
                                            onChange={(color) =>
                                              handleTagColorChange(
                                                color,
                                                form,
                                                name,
                                                selectedTag as string,
                                                setSelectedTag
                                              )
                                            }
                                          />
                                        </div>
                                      </div>
                                      <div>
                                        {DEAFAULT_COLORS.map((color) => (
                                          <Tag
                                            className={`grabbable --mb5`}
                                            aria-setsize={20}
                                            key={color}
                                            color={color}
                                            onClick={() =>
                                              handleTagClick(
                                                color,
                                                form,
                                                name,
                                                selectedTag as string,
                                                setSelectedTag
                                              )
                                            }
                                          >
                                            {color}
                                          </Tag>
                                        ))}
                                      </div>
                                    </div>
                                  }
                                  trigger="click"
                                >
                                  <div>
                                    <Tooltip title={getLanguageLabel("edit")}>
                                      <EditIcon />
                                    </Tooltip>
                                  </div>
                                  {/* <BoslerButton
                                    minimal
                                    icononly
                                    icon={<EditIcon />}
                                  >
                                    {getLanguageLabel("edit")}
                                  </BoslerButton> */}
                                </Popover>
                              </div>
                            </Form.Item>

                            <Form.Item>
                              <div className={`--flex-row --flex-end`}>
                                <BoslerButton
                                  id={"TagSaveButton" + name}
                                  intent="primary"
                                  icononly
                                  minimal
                                  icon={<SaveIcon />}
                                  onClick={async () => {
                                    try {
                                      await form.validateFields([
                                        ["tags", index, "name"],
                                        ["tags", index, "description"],
                                      ]);
                                      saveOrUpdateTag(
                                        form.getFieldValue("tags")[name],
                                        selectedTag as string,
                                        "TagSaveButton" + name
                                      );
                                      console.log("Save successful!");
                                    } catch (error) {
                                      console.error(
                                        "Validation failed:",
                                        error
                                      );
                                    }
                                  }}
                                >
                                  {getLanguageLabel("save")}
                                </BoslerButton>
                                <BoslerButton
                                  icon={<TrashIcon />}
                                  icononly
                                  minimal
                                  intent="dangerous"
                                  onClick={() => {
                                    handleDeleteTag(
                                      form.getFieldValue("tags")[name].id
                                    );
                                    remove(name);
                                  }}
                                >
                                  {getLanguageLabel("delete")}
                                </BoslerButton>
                              </div>
                            </Form.Item>
                          </div>
                        ))}
                      </>
                    )}
                  </Form.List>
                )}
              </div>
            </Form>
          </Panel>
        </PanelGroup>
      </div>
    </div>
  );
};

export default CreateTags;
