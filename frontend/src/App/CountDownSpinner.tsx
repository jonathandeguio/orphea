import React, { useEffect, useState } from "react";
import "./CountdownSpinner.scss";

interface CountdownSpinnerProps {
  seconds: number;
}

const CountdownSpinner: React.FC<CountdownSpinnerProps> = ({ seconds }) => {
  const [rotation, setRotation] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setRotation((rotation) => rotation - 360 / seconds);
    }, 1000);

    return () => clearInterval(interval);
  }, [seconds]);

  return (
    <div className="countdown-spinner">
      <div className="spinner" style={{ transform: `rotate(${rotation}deg)` }}>
        <div className="bar"></div>
        <div className="bar"></div>
      </div>
    </div>
  );
};

export default CountdownSpinner;
