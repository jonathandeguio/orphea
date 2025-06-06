import {
  CLEAR_EDITOR_PANES,
  EDITOR_PANE_CHANGE,
  EDITOR_PANE_CLOSE,
  EDITOR_PANE_OPEN,
  FILE_EDIT,
  FILE_SAVE,
  SET_BRANCH,
  SET_FONT,
  SET_REPOSITORY,
} from "../constants/editorConstants";

export const editorPaneReducer = (
  state : any = {
    activeKey: "0",
    newTabIndex: 0,
    editorPanes: [],
    activePane: undefined,
  },
  action: any
) => {
  const activeKey = `${state.newTabIndex}`;
  let lastIndex = 0;
  let newPanes: { [id: number]: any; length: number };
  switch (action.type) {
    case EDITOR_PANE_OPEN:
      state.newTabIndex++;
      return {
        newTabIndex: state.newTabIndex,
        activeKey: activeKey,
        editorPanes: [
          ...state.editorPanes,
          {
            name: action.title,
            content: atob(action.newPane["fileContents.b64"]),
            key: activeKey,
            path: action.path,
          },
        ],
        activePane: {
          name: action.title,
          content: atob(action.newPane["fileContents.b64"]),
          key: activeKey,
          path: action.path,
          clean: true,
        },
      };

    case FILE_EDIT:
      return {
        newTabIndex: state.newTabIndex,
        activeKey: state.activeKey,
        editorPanes: state.editorPanes,
        activePane: {
          ...(state.activePane as unknown as object),
          content: action.content,
          clean: false,
        },
      };

    case FILE_SAVE:
      return {
        newTabIndex: state.newTabIndex,
        activeKey: state.activeKey,
        editorPanes: state.editorPanes,
        activePane: {
          ...(state.activePane as unknown as object),
          clean: true,
        },
      };

    case EDITOR_PANE_CLOSE:
      state.editorPanes.forEach((pane : any, i : any) => {
        if ((pane as any).key === action.pid) {
          lastIndex = i - 1;
        }
      });

      newPanes = state.editorPanes.filter(
        (pane : any) => (pane as any).key !== action.pid
      );

      if (newPanes.length && state.activeKey === action.pid) {
        if (lastIndex !== undefined && lastIndex >= 0) {
          state.activeKey = newPanes[lastIndex].key;
        } else {
          state.activeKey = (newPanes[0] as any).key;
        }
      }

      return {
        newTabIndex: state.newTabIndex,
        activeKey: state.activeKey,
        editorPanes: newPanes,
        activePane: state.editorPanes.filter(
          (pane : any) => (pane as any).key === state.activeKey
        )[0],
      };

    case EDITOR_PANE_CHANGE:
      return {
        newTabIndex: state.newTabIndex,
        activeKey: `${action.pid}`,
        editorPanes: state.editorPanes,
        activePane: state.editorPanes.filter(
          (pane : any) => (pane as any).key === action.pid
        )[0],
      };

    case CLEAR_EDITOR_PANES:
      return {
        activeKey: "0",
        newTabIndex: 0,
        editorPanes: [],
        activePane: undefined,
      };

    default:
      return state;
  }
};

export const editorVariables = (
  state = {
    repository: undefined,
    branch: undefined,
    fontSize: 18,
  },
  action: any
) => {
  switch (action.type) {
    case SET_BRANCH:
      return {
        branch: action.branch,
        repository: state.repository,
        fontSize: state.fontSize,
      };
    case SET_REPOSITORY:
      return {
        branch: state.branch,
        repository: action.repository,
        fontSize: state.fontSize,
      };
    case SET_FONT:
      return {
        branch: state.branch,
        repository: state.repository,
        fontSize: action.fontSize,
      };
    default:
      return state;
  }
};
