import { createBrowserHistory } from "history";

export const history = createBrowserHistory();

export const redirectTo = (url: string) => {
  history.push(url); // Change '/login' to your actual login route
};
