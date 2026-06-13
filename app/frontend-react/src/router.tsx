import { createBrowserRouter, Navigate } from "react-router-dom";

import { AuthLayout } from "@/app/layouts/AuthLayout";
import { App } from "@/App";
import { SignInPage } from "@/features/auth/SignInPage";
import { JournalWeeklyPage } from "@/views/journal/JournalWeeklyPage";
import { NotFoundPage } from "@/views/system/NotFoundPage";

export const router = createBrowserRouter(
  [
    {
      element: <AuthLayout />,
      children: [
        {
          path: "/sign-in",
          element: <SignInPage />,
        },
      ],
    },
    {
      path: "/",
      element: <App />,
      children: [
        {
          index: true,
          element: <Navigate to="/journal/weekly" replace />,
        },
        {
          path: "journal",
          element: <Navigate to="/journal/weekly" replace />,
        },
        {
          path: "journal/weekly",
          element: <JournalWeeklyPage />,
        },
        {
          path: "*",
          element: <NotFoundPage />,
        },
      ],
    },
  ],
  {
    basename: import.meta.env.BASE_URL,
  },
);
