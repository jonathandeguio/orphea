import axios from "axios";

export const fetchAllCategoryNamesAPI = () => {
  return axios.get(`/docket/category/categoryNames`);
};

export const fetchCategoryDetailsAndTagsAPI = (categoryName: string) => {
  return axios.get(
    `/docket/category/categoryDetailsAndTags/${categoryName}`
  );
};

export const saveCategoryAPI = (
  categoryName: string,
  categoryDescription: string
) => {
  const jsonData = {
    name: categoryName,
    description: categoryDescription,
    enabled: true,
  };
  return axios.post(`/docket/category/create`, jsonData);
};

export const saveTagAPI = (
  tagName: string,
  tagDescription: string,
  tagColor: string,
  categoryId: string
) => {
  const jsonData = {
    name: tagName,
    description: tagDescription,
    color: tagColor,
    tagsCategoryId: categoryId,
  };
  return axios.post(`/docket/tag/create`, jsonData);
};
export const deleteCategoryAPI = (categoryId: string) => {
  return axios.get(`/docket/category/delete/${categoryId}`);
};

export const updateCategoryAPI = (
  categoryName: string,
  categoryDescription: string,
  categoryId: string
) => {
  const jsonData = {
    name: categoryName,
    description: categoryDescription,
    enabled: true,
  };
  return axios.put(`/docket/category/update/${categoryId}`, jsonData);
};

export const deleteTagAPI = (tagId: string) => {
  return axios.get(`/docket/tag/delete/${tagId}`);
};

export const updateTagAPI = (
  tagName: string,
  tagDescription: string,
  tagColor: string,
  tagId: string
) => {
  const jsonData = {
    name: tagName,
    description: tagDescription,
    color: tagColor,
  };
  return axios.put(`/docket/tag/update/${tagId}`, jsonData);
};
