import type { RouteLocationRaw, Router } from "vue-router";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { useAuthStore } from "@/shared/auth/stores/auth";

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
  try {
    if (isAuthPopupRoute(routeName)) {
      console.warn("[auth] popup session expired; close dialog shown", { routeName: String(routeName) });
      const result = await Swal.fire({
        icon: "warning",
        title: "로그인이 풀렸습니다",
        text: "현재 창에서는 더 이상 저장/조회할 수 없습니다. 창을 닫을까요?",
        showCancelButton: true,
        confirmButtonText: "창 닫기",
        cancelButtonText: "그대로 두기",
      });
      if (result.isConfirmed) window.close();
      return false;
    }

    console.warn("[auth] session expired; sign-in dialog shown", { routeName: String(routeName) });
    const result = await Swal.fire({
      icon: "warning",
      title: "인증이 만료되었습니다",
      text: "인증이 만료되었습니다. 로그인 화면으로 돌아가시겠습니까?",
      showCancelButton: true,
      confirmButtonText: "로그인 화면으로 돌아가기",
      cancelButtonText: "현재 화면에 머무르기",
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
