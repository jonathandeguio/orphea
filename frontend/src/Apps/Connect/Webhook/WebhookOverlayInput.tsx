import React, { useEffect, useRef, useState } from "react";
import ContentEditable, { ContentEditableEvent } from "react-contenteditable";

interface IProps {
  value?: string;
  onChange?: (value: string) => void;
}
const WebhookOverlayInput: React.FC<IProps> = ({
  value = "",
  onChange = () => {},
}) => {
  // Local state to store the raw value (without highlights)
  const [rawValue, setRawValue] = useState(value);
  const contentRef = useRef<HTMLDivElement>(null);

  // Update rawValue when the external `value` prop changes
  useEffect(() => {
    setRawValue(value);
  }, [value]);

  // Regex patterns to match
  const completeResponsePattern = /@completeresponse/g;
  const keyPattern = /@key\[(.*?)\]/g;
  const indexPattern = /@index\[(.*?)\]/g;

  // Function to replace matched patterns with highlighted HTML and custom text
  const getHighlightedText = (text: string) => {
    const combinedPattern = new RegExp(
      `${completeResponsePattern.source}|${keyPattern.source}|${indexPattern.source}`,
      "g"
    );

    // Replace matches with custom text and highlighted span tags
    return text.replace(combinedPattern, (match) => {
      if (match === "@completeresponse") {
        return `<span class="overlay-highlight">[Complete Response]</span>`;
      } else if (keyPattern.test(match)) {
        const key = match.match(keyPattern)?.[1] || "unknown key";
        return `<span class="overlay-highlight">[Key: ${key}]</span>`;
      } else if (indexPattern.test(match)) {
        const index = match.match(indexPattern)?.[1] || "unknown index";
        return `<span class="overlay-highlight">[Index: ${index}]</span>`;
      }
      return match;
    });
  };

  // Function to save the cursor position
  const saveSelection = () => {
    const selection = window.getSelection();
    if (selection && selection.rangeCount > 0) {
      return selection.getRangeAt(0);
    }
    return null;
  };

  // Function to restore the cursor position
  const restoreSelection = (range: Range | null) => {
    const selection = window.getSelection();
    if (range && selection) {
      selection.removeAllRanges();
      selection.addRange(range);
    }
  };

  // Handle content change
  const handleChange = (event: ContentEditableEvent) => {
    const updatedRawValue = event.target.value;
    setRawValue(updatedRawValue); // Update local raw value
    onChange(updatedRawValue); // Pass updated value to parent
  };

  // Update the displayed HTML content based on the rawValue
  useEffect(() => {
    if (contentRef.current) {
      const savedRange = saveSelection(); // Save cursor position
      contentRef.current.innerHTML = getHighlightedText(rawValue); // Update HTML content
      restoreSelection(savedRange); // Restore cursor position
    }
  }, [rawValue]);

  return (
    <ContentEditable
      innerRef={contentRef}
      html={getHighlightedText(rawValue)} // Display highlighted text
      onChange={handleChange} // Update raw value on change
      className="content-editable"
      tagName="div"
      style={{
        padding: "8px",
        border: "1px solid #ddd",
        borderRadius: "4px",
        minHeight: "40px",
      }}
    />
  );
};

export default WebhookOverlayInput;
