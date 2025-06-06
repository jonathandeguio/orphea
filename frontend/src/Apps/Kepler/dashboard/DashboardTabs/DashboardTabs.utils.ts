export const confirmTabDeletion = (
  e: React.MouseEvent<HTMLElement> | undefined
) => {};

export const cancelTabDeletion = (
  e: React.MouseEvent<HTMLElement> | undefined
) => {
  if (e) {
    e.stopPropagation();
  }
};
