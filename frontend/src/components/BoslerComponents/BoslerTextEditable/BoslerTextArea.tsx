import React, { useEffect, useRef } from "react";
import "./BoslerTextArea.scss";
import { isDefined } from "utils/utilities";
import { Form } from "antd";

export const BoslerTextArea = ({
  placeholder,
  inputHandler,
  onChange,
  form,
  fieldName,
}: any) => {
  const value = Form.useWatch(fieldName, form) ?? "";
  const contentRef = useRef<HTMLDivElement>(null);

  const saveCursorPosition = () => {
    const selection = window.getSelection();
    if (!selection?.rangeCount || !contentRef.current) return null;

    const range = selection.getRangeAt(0);
    const preCaretRange = range.cloneRange();
    preCaretRange.selectNodeContents(contentRef.current);
    preCaretRange.setEnd(range.startContainer, range.startOffset);

    // Traverse through all child nodes to calculate the position
    let charCount = 0;

    const traverseNodes = (node: any) => {
      if (node === range.startContainer) {
        // Add the offset if this is the node containing the cursor
        charCount += range.startOffset;
        return true; // Stop traversal
      }

      if (node.nodeType === Node.TEXT_NODE) {
        charCount += node.textContent?.length || 0;
      } else {
        // Recursively traverse child nodes
        for (let child of node.childNodes) {
          if (traverseNodes(child)) return true;
        }
      }
      return false;
    };

    traverseNodes(contentRef.current);
    return charCount;
  };
  const restoreCursorPosition = (cursorPosition: any) => {
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
          return true; // Stop traversal
        }
        charCount += nodeLength;
      } else {
        // Recursively traverse child nodes
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
      // Fallback: Place cursor at the end
      const range = document.createRange();
      range.selectNodeContents(contentRef.current);
      range.collapse(false);
      selection.removeAllRanges();
      selection.addRange(range);
    }
  };

  const handleTextInput = (text: string) => {
    const cursorPosition = saveCursorPosition();
    onChange(text);
    // Replace regex for @key[...] and @index[...] and capture alphanumeric content inside brackets
    const replacedText = text.replace(
      /@(key|index)\[([a-zA-Z0-9 _-]+)\]|@completeresponse/g,
      (match, type, alphanumericString) => {
        let label = "";

        if (type === "key") {
          label = `Key->${alphanumericString}`;
        } else if (type === "index") {
          label = `Index->${alphanumericString}`;
        } else if (match === "@completeresponse") {
          label = "Complete Response";
        }

        return `<span class="overlayLabel" title="${label}">${match}</span>`;
      }
    );
    if (contentRef.current && cursorPosition !== null) {
      contentRef.current.innerHTML = replacedText;
      restoreCursorPosition(cursorPosition);
    }
  };
  const handleInput = (e: React.FormEvent<HTMLDivElement>) => {
    const text = e.currentTarget.innerText;
    handleTextInput(text);
    inputHandler(e, saveCursorPosition());
  };

  useEffect(() => {
    if (isDefined(contentRef.current)) {
      handleTextInput(value);
    }
  }, [value, contentRef]);

  return (
    <div
      ref={contentRef}
      className="bosler-text-area"
      contentEditable
      onInput={handleInput}
      role="textbox"
      aria-placeholder={placeholder}
      suppressContentEditableWarning={true}
    ></div>
  );
};
