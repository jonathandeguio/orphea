import "@fontsource/ibm-plex-mono";
import "@fontsource/ibm-plex-sans";
import "@fontsource/ibm-plex-sans/300.css";
import "@fontsource/ibm-plex-sans/400.css";
import "@fontsource/ibm-plex-sans/500.css";
import "@fontsource/ibm-plex-sans/700.css";
import "@fontsource/roboto";
import "@fontsource/space-mono";

import { Provider } from "react-redux";

import React from "react";
import { createRoot } from "react-dom/client";
import App from "./layouts/app/App";
import "./index.css";
import store from "./redux/store";
import { addInterceptors } from "./utils/axiosInterceptors";

const domNode = document.getElementById("root");

if (domNode != null) {
  const root = createRoot(domNode);
  addInterceptors();
  root.render(
    <Provider store={store}>
      <App />
    </Provider>
  );
}
// ReactDOM.render(
//   <Provider store={store}>
//     {/* <BrowserRouter> */}
//     <app />
//     {/* </BrowserRouter> */}
//   </Provider>,
//   document.getElementById("root")
// );

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
// reportWebVitals(
// Test something

// REACT_APP_BASE_URL_API=http://localhost:8080/api yarn startOld
