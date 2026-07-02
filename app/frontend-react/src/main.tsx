import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";

import { router } from "@/router";
import "@/styles/metronic.scss";
import "@/styles/main.css";
import "sweetalert2/dist/sweetalert2.css";

const rootElement = document.getElementById("root");

if (!rootElement) {
  console.error("[frontend-react] #root element was not found.");
} else {
  console.info("[frontend-react] bootstrap");
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <RouterProvider router={router} />
    </React.StrictMode>,
  );
}
