import type { RouteLocationNormalizedLoaded, Router } from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { buildSessionExpiredSignInRoute, confirmSessionExpired } from "@/shared/auth/sessionExpired";

/**
 * 팝업을 열기 전에 현재 인증 상태를 확인한다.
 * 세션이 풀린 상태에서는 빈 로그인 팝업을 띄우지 않고 현재 화면에서 로그인 복귀 흐름으로 보낸다.
 */
export async function assertAuthenticatedBeforePopup(
  router: Router,
  route: RouteLocationNormalizedLoaded
): Promise<boolean> {
  const authStore = useAuthStore();
  await authStore.verifyAuth({ force: true });
  if (authStore.isAuthenticated) return true;

  console.warn("[auth] popup open blocked because session is expired", {
    route: route.fullPath,
  });

  const confirmed = await confirmSessionExpired(route.name);
  if (confirmed) await router.push(buildSessionExpiredSignInRoute(route.fullPath));

  return false;
}
