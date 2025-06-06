import React, { useEffect, useRef } from "react";
import { isIpPlatform } from "utils/utilities";

interface DancingLinesProps {
  lineStroke: string;
}

export const DancingLines = ({ lineStroke }: DancingLinesProps) => {
  let DancingLines = true;
  if (isIpPlatform()) DancingLines = false;

  const canvasRef = useRef<any>(null);
  // Background curves
  let curve_array: any = [];
  let ctx =
    canvasRef && canvasRef.current ? canvasRef.current.getContext("2d") : null;
  function canvasResize() {
    canvasRef.current.width = window.innerWidth;
    canvasRef.current.height = window.innerHeight;
  }

  function canvasInit() {
    for (let i = 0; i < 15; i++) {
      const x1 = canvasRef.current.width * Math.random();
      const y1 = canvasRef.current.height * Math.random();
      const x2 = canvasRef.current.width * Math.random();
      const y2 = canvasRef.current.height * Math.random();

      const x1dx = (Math.random() * 2 - 1) / 4;
      const y1dy = (Math.random() * 2 - 1) / 4;
      const x2dx = (Math.random() * 2 - 1) / 4;
      const y2dy = (Math.random() * 2 - 1) / 4;
      curve_array.push({
        x1: x1,
        y1: y1,
        x2: x2,
        y2: y2,
        x1dx: x1dx,
        y1dy: y1dy,
        x2dx: x2dx,
        y2dy: y2dy,
      });
    }
  }
  function canvasDraw() {
    if (!canvasRef || !canvasRef.current) {
      // Hack for dispatch login call
      return;
    }
    ctx.clearRect(0, 0, canvasRef.current.width, canvasRef.current.height);
    ctx.lineWidth = 1;
    ctx.strokeStyle = lineStroke;

    for (let i = 0; i < curve_array.length; i++) {
      const curr_curve = curve_array[i];
      ctx.beginPath();
      ctx.moveTo(-100, canvasRef.current.height + 100);
      ctx.bezierCurveTo(
        curr_curve.x1,
        curr_curve.y1,
        curr_curve.x2,
        curr_curve.y2,
        canvasRef.current.width + 100,
        -100
      );

      ctx.stroke();

      if (curr_curve.x1 < 0 || curr_curve.x1 > canvasRef.current.width) {
        curr_curve.x1 -= curr_curve.x1dx;
        curr_curve.x1dx *= -1;
      }
      if (curr_curve.y1 < 0 || curr_curve.y1 > canvasRef.current.height) {
        curr_curve.y1 -= curr_curve.y1dy;
        curr_curve.y1dy *= -1;
      }
      if (curr_curve.x2 < 0 || curr_curve.x2 > canvasRef.current.width) {
        curr_curve.x2 -= curr_curve.x2dx;
        curr_curve.x2dx *= -1;
      }
      if (curr_curve.y2 < 0 || curr_curve.y2 > canvasRef.current.height) {
        curr_curve.y2 -= curr_curve.y2dy;
        curr_curve.y2dy *= -1;
      }

      curr_curve.x1 += curr_curve.x1dx;
      curr_curve.y1 += curr_curve.y1dy;
      curr_curve.x2 += curr_curve.x2dx;
      curr_curve.y2 += curr_curve.y2dy;

      curve_array[i] = curr_curve;
    }
    window.requestAnimationFrame(canvasDraw);
  }

  function myCanvas() {
    canvasResize();
    canvasInit();
    canvasDraw();
  }
  function setScrollBehaviour() {}
  const handleRenitializeOnResize = () => {
    canvasResize();
    curve_array = [];
    canvasInit();
  };

  useEffect(() => {
    if (canvasRef && canvasRef.current) {
      ctx = canvasRef.current.getContext("2d");
      setScrollBehaviour();
      myCanvas();
      window.addEventListener("resize", handleRenitializeOnResize);
      return () => {
        window.removeEventListener("resize", handleRenitializeOnResize);
      };
    }
  }, [canvasRef]);

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        background: "var(--background-color)",
      }}
    >
      {DancingLines && (
        <canvas
          ref={canvasRef}
          style={{
            opacity: 0.2,
          }}
        />
      )}
    </div>
  );
};
