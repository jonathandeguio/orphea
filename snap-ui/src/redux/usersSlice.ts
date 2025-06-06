import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { User } from "../global";

interface UsersState {
  [key: string]: User;
}

const initialState: UsersState = {};

export const IndexedUsersSlice = createSlice({
  name: "users",
  initialState,
  reducers: {
    addUser: (state: UsersState, action: PayloadAction<User>) => {
      state[action.payload.id] = action.payload;
    },
    updateUser: (state: UsersState, action: PayloadAction<User>) => {
      state[action.payload.id] = action.payload;
    },
    removeUser: (state: UsersState, action: PayloadAction<User>) => {
      delete state[action.payload.id];
    },
    removeUserbyId: (state: UsersState, action: PayloadAction<string>) => {
      delete state[action.payload];
    },
  },
});

export default IndexedUsersSlice.reducer;
