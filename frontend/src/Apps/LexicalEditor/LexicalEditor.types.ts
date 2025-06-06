export enum LexicalToolbarType {
  TOOLTIP = "TOOLTIP",
  HEADER = "HEADER",
}

export interface TToolbarPlugin {
  type: LexicalToolbarType;
}
