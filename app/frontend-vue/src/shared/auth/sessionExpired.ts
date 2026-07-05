import type { RouteLocationRaw, Router } from "vue-router";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const SESSION_EXPIRED_QUERY = { sessionExpired: "Y" };
const POPUP_ROUTE_NAMES = ["journal-entry-search", "journal-daily"];

let authExpiredDialogShowing = false;

export function isAuthPopupRoute(name: unknown): boolean {
  return POPUP_ROUTE_NAMES.includes(String(name));
}

export function buildSessionExpiredSignInRoute(redirect: string): RouteLocationRaw {
  return {
    name: "sign-in",
    query: {
      ...SESSION_EXPIRED_QUERY,
      redirect,
    },
  };
}

/**
 * 세션 만료 안내를 전역에서 단일화한다.
 * 라우터 가드에서 미인증을 감지한 경우와 Axios 401 인터셉터가 감지한 경우 모두 이 함수를 거친다.
 */
export async function confirmSessionExpired(routeName: unknown): Promise<boolean> {
  if (authExpiredDialogShowing) return false;
  authExpiredDialogShowing = true;
  const { t } = useLocaleStore();
  try {
    if (isAuthPopupRoute(routeName)) {
      console.warn("[auth] popup session expired; close dialog shown", { routeName: String(routeName) });
      const result = await Swal.fire({
        icon: "warning",
        title: t("auth.session.expired.popup.title"),
        text: t("auth.session.expired.popup.message"),
        showCancelButton: true,
        confirmButtonText: t("auth.session.expired.popup.confirm"),
        cancelButtonText: t("auth.session.expired.popup.cancel"),
      });
      if (result.isConfirmed) window.close();
      return false;
    }

    console.warn("[auth] session expired; sign-in dialog shown", { routeName: String(routeName) });
    const result = await Swal.fire({
      icon: "warning",
      title: t("auth.session.expired.page.title"),
      text: t("auth.session.expired.page.message"),
      showCancelButton: true,
      confirmButtonText: t("auth.session.expired.page.confirm"),
      cancelButtonText: t("auth.session.expired.page.cancel"),
    });
    return result.isConfirmed;
  } finally {
    authExpiredDialogShowing = false;
  }
}

/**
 * Axios 401 응답처럼 라우터 가드 바깥에서 감지한 세션 만료를 처리한다.
 * 일반 화면은 확인 후 로그인 화면으로 이동하고, 팝업 라우트는 레거시처럼 창 닫기 확인만 수행한다.
 */
export async function handleSessionExpired(router: Router): Promise<void> {
  const route = router.currentRoute.value;
  const authStore = useAuthStore();
  authStore.purgeAuth();

  const confirmed = await confirmSessionExpired(route.name);
  if (!confirmed || isAuthPopupRoute(route.name)) return;

  await router.push(buildSessionExpiredSignInRoute(route.fullPath));
}
