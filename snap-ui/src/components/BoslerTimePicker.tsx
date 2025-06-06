import { TimePicker } from "antd";
import dayjs, { Dayjs } from "dayjs";
import React, { useMemo } from "react";
import { CrossIcon } from "assets/icons/boslerActionIcons";

interface BoslerTimePickerProps {
  placeholder?: string;
  onChange?: (value: number | undefined) => void;
  value?: number;
  loading?: boolean;
  disabledTimes?: {
    disabledHours?: () => number[];
    disabledMinutes?: (selectedHour: number) => number[];
    disabledSeconds?: (selectedHour: number, selectedMinute: number) => number[];
  };
}

const BoslerTimePicker = (props: BoslerTimePickerProps) => {
  const value = useMemo(() => {
    if (props.value === undefined) return undefined;
    const secondsOfDay = props.value / 1000; // Convert milliseconds to seconds
    const time = dayjs().startOf('day').add(secondsOfDay, 'second');
    return time.isValid() ? time : undefined;
  }, [props.value]);

  const handleChange = (time: Dayjs | null) => {
    if (props.onChange) {
      if (time) {
        const secondsOfDay = time.hour() * 3600 + time.minute() * 60 + time.second();
        const timestamp = secondsOfDay * 1000; // Convert seconds back to milliseconds
        console.log('Frontend Timestamp:', timestamp);
        props.onChange(timestamp);
      } else {
        props.onChange(undefined);
      }
    }
  };

  const handleClear = () => {
    if (props.onChange) {
      props.onChange(undefined); // Clear the value
    }
  };

  return (
    <div style={{ position: "relative", width: "100%" }}>
      <TimePicker
        style={{ width: "100%" }}
        format="HH:mm:ss"
        onChange={handleChange as any}
        value={value}
        placeholder={props.placeholder}
        suffixIcon={
          <div
            onClick={handleClear}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 24,
              height: 24,
              cursor: "pointer",
              color: "#888",
            }}
          >
            <CrossIcon />
          </div>
        }
      />
    </div>
  );
};

export default BoslerTimePicker;
