import { useEffect } from "react";
import { Outlet } from "react-router-dom";

const THEME_MODE_LS_KEY = "kt_theme_mode_value";

/**
 * 인증 전용 레이아웃.
 * Vue {@code app/layouts/AuthLayout.vue} 와 동일: dreamdiary 배경 + 항상 라이트 모드.
 */
export function AuthLayout() {
  const base = import.meta.env.BASE_URL;

  useEffect(() => {
    document.documentElement.setAttribute("data-bs-theme", "light");
    return () => {
      const saved = localStorage.getItem(THEME_MODE_LS_KEY) ?? "light";
      document.documentElement.setAttribute("data-bs-theme", saved);
    };
  }, []);

  return (
    <div className="d-flex flex-column flex-root app-root" id="kt_app_root">
      <div
        className="app-page flex-column flex-column-fluid"
        style={{
          backgroundImage: `url('${base}dreamdiary/img/dreamdiary.jpg')`,
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          backgroundSize: "cover",
        }}
      >
        <Outlet />
      </div>
    </div>
  );
}
