import store from "../store"

export type ActionDispatcher = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type ThunkAppDispatch = ThunkDispatch<RootState, void, Action>;