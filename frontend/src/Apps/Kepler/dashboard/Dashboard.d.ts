export interface ITabConfig {
  chartHeadingTextColor: string | null;
  chartHeadingBg: string | null;
  chartBodyBg: string | null;
  canvasBg: string | null;
  pageBg: string | null;

  topPadding: number;
  rightPadding: number;
  bottomPadding: number;
  leftPadding: number;

  preventCollision: boolean;
  allowOverlap: boolean;
}
