export const getTagCategory = (id: string, categoriesData: any) => {
  let res: string | undefined = undefined;
  categoriesData?.map((ele: any) => {
    if ((ele["tags"] as any).find((tag: any) => tag.id == id))
      res = ele["name"] as string;
  });
  return res;
};

export const removeTags = (
  e: any,
  removedTags: any,
  setRemovedTags: any,
  datasetTags: any
) => {
  let removedTagsArray: string[] = [];
  if (removedTags && removedTags.length != 0)
    removedTagsArray = [...(removedTags as string[])];
  if (datasetTags?.findIndex((element: any) => element.id === e.id) !== -1)
    removedTagsArray.push(e.id);
  setRemovedTags(removedTagsArray);
};

export const applyTags = (
  e: any,
  addedTags: any,
  setEditAddedTags: any,
  setAddedTags: any
) => {
  let addedTagsArray: object[] = [];
  if (addedTags && addedTags.length != 0)
    addedTagsArray = [...(addedTags as object[])];
  if (addedTagsArray.findIndex((element: any) => element.id === e.id) === -1) {
    setEditAddedTags(true);
    addedTagsArray.push(e);
  }
  setAddedTags(addedTagsArray);
};
