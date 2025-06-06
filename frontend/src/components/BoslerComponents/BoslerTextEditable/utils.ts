export const saveCursorPosition = (contentRef: any) => {
  try {
    const selection = window.getSelection();
    if (!selection?.rangeCount || !contentRef.current) return null;

    const range = selection.getRangeAt(0);
    const preCaretRange = range.cloneRange();
    preCaretRange.selectNodeContents(contentRef.current);
    preCaretRange.setEnd(range.startContainer, range.startOffset);

    let charCount = 0;

    const traverseNodes = (node: any) => {
      if (node === range.startContainer) {
        charCount += range.startOffset;
        return true;
      }

      if (node.nodeType === Node.TEXT_NODE) {
        charCount += node.textContent?.length || 0;
      } else {
        for (let child of node.childNodes) {
          if (traverseNodes(child)) return true;
        }
      }
      return false;
    };

    traverseNodes(contentRef.current);
    return contentRef.current.innerHTML.length - charCount;
  } catch (error) {}
  return 0;
};
export const restoreCursorPosition = (cursorPosition: any, contentRef: any) => {
  try {
    cursorPosition = contentRef.current.innerHTML - cursorPosition;
    const selection = window.getSelection();
    if (!selection || !contentRef.current) return;

    let charCount = 0;
    let nodeToRestore = null;
    let offset = 0;

    const traverseNodes = (node: any) => {
      if (node.nodeType === Node.TEXT_NODE) {
        const nodeLength = node.textContent?.length || 0;
        if (charCount + nodeLength >= cursorPosition) {
          nodeToRestore = node;
          offset = cursorPosition - charCount;
          return true;
        }
        charCount += nodeLength;
      } else {
        for (let child of node.childNodes) {
          if (traverseNodes(child)) return true;
        }
      }
      return false;
    };

    traverseNodes(contentRef.current);

    if (nodeToRestore) {
      const range = document.createRange();
      range.setStart(nodeToRestore, offset);
      range.collapse(true);
      selection.removeAllRanges();
      selection.addRange(range);
    } else {
      const range = document.createRange();
      range.selectNodeContents(contentRef.current);
      range.collapse(false);
      selection.removeAllRanges();
      selection.addRange(range);
    }
  } catch (error) {}
};
