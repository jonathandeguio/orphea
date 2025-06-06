import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { User } from "global";

interface UsersState {
  [key: string]: User | Promise<any>;
}

const initialState: UsersState = {};

export const IndexedUsersSlice = createSlice({
  name: "users",
  initialState,
  reducers: {
    addUser: (
      state: UsersState,
      action: PayloadAction<{ id: string; value: User | Promise<any> }>
    ) => {
      state[action.payload.id] = action.payload.value;
    },
    updateUser: (
      state: UsersState,
      action: PayloadAction<{ id: string; value: User | Promise<any> }>
    ) => {
      state[action.payload.id] = action.payload.value;
    },
    removeUser: (state: UsersState, action: PayloadAction<User>) => {
      delete state[action.payload.id];
    },
    removeUserbyId: (state: UsersState, action: PayloadAction<string>) => {
      delete state[action.payload];
    },
  },
});
export const { addUser, updateUser, removeUser, removeUserbyId } =
  IndexedUsersSlice.actions;

export default IndexedUsersSlice.reducer;
