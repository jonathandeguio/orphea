export enum LayoutViewEnum {
  COMFORTABLE = "COMFORTABLE",
  COMPACT = "COMPACT",
}

export const preventFilterLossOnReNavigate = (
  currentPath: string,
  pathToNavigate: string
) => {
  return currentPath == pathToNavigate;
};
