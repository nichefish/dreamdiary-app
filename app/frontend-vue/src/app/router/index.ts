import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useConfigStore } from "@/shared/config/stores/config";
import { useMenuStore, type MenuMode } from "@/shared/menu/stores/menu";
import {
  buildSessionExpiredSignInRoute,
  confirmSessionExpired,
  isAuthPopupRoute,
} from "@/shared/auth/sessionExpired";
import {
  clearRuntimePending,
  markRuntimePending,
  reportRuntimeError,
} from "@/shared/utils/appRuntimeStatus";
import { isAuthVerificationError } from "@/shared/utils/authError";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/journal/weekly",
    component: () => import("@/app/layouts/default/DefaultLayout.vue"),
    meta: { middleware: "auth" },
    children: [
      {
        path: "/journal",
        component: () => import("@/features/journal/day/JournalDayLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            redirect: { name: "journal-weekly" },
          },
          {
            path: "monthly",
            name: "journal-monthly",
            component: () => import("@/features/journal/day/JournalDayMonthly.vue"),
            meta: { pageTitle: "월간 일기", breadcrumbs: ["일기"] },
          },
          {
            path: "weekly",
            name: "journal-weekly",
            component: () => import("@/features/journal/day/JournalDayWeekly.vue"),
            meta: { pageTitle: "주간 일기", breadcrumbs: ["일기"] },
          },
          {
            path: "calendar",
            name: "journal-calendar",
            component: () => import("@/features/journal/day/JournalDayCalendar.vue"),
            meta: { pageTitle: "일기 캘린더", breadcrumbs: ["일기"] },
          },
          {
            path: "meta",
            name: "journal-meta",
            component: () => import("@/features/journal/day/JournalDayMeta.vue"),
            meta: { pageTitle: "일기 메타", breadcrumbs: ["일기"] },
          },
        ],
      },
      {
        path: "/annual",
        component: () => import("@/features/journal/annual/JournalAnnualLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "annual-list",
            component: () => import("@/features/journal/annual/JournalAnnualList.vue"),
            meta: { pageTitle: "연간 결산", breadcrumbs: ["일기", "결산"] },
          },
          {
            path: ":yy",
            name: "annual-detail",
            component: () => import("@/features/journal/annual/JournalAnnualDetail.vue"),
            meta: { pageTitle: "연간 결산 상세", breadcrumbs: ["일기", "결산"] },
          },
        ],
      },
      {
        path: "/thread",
        component: () => import("@/features/journal/thread/JournalThreadLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "thread-list",
            component: () => import("@/features/journal/thread/JournalThreadList.vue"),
            meta: { pageTitle: "일기 스레드", breadcrumbs: ["일기", "스레드"] },
          },
          {
            path: "new",
            name: "thread-create",
            component: () => import("@/features/journal/thread/JournalThreadList.vue"),
            meta: { pageTitle: "일기 스레드", breadcrumbs: ["일기", "스레드"] },
          },
          {
            path: ":id",
            name: "thread-detail",
            component: () => import("@/features/journal/thread/JournalThreadList.vue"),
            meta: { pageTitle: "일기 스레드", breadcrumbs: ["일기", "스레드"] },
          },
          {
            path: ":id/edit",
            name: "thread-edit",
            component: () => import("@/features/journal/thread/JournalThreadList.vue"),
            meta: { pageTitle: "일기 스레드", breadcrumbs: ["일기", "스레드"] },
          },
        ],
      },
      {
        path: "/schedule",
        name: "schedule-calendar",
        component: () => import("@/features/calendar/ScheduleCalendar.vue"),
        meta: { pageTitle: "일정 캘린더", breadcrumbs: ["일정"] },
      },
      {
        path: "/admin",
        name: "admin-page",
        component: () => import("@/features/admin/AdminPage.vue"),
        meta: { pageTitle: "사이트 관리", breadcrumbs: ["관리"] },
      },
      {
        path: "/admin/auth-policy",
        name: "auth-policy",
        component: () => import("@/features/admin/AuthPolicyPage.vue"),
        meta: { pageTitle: "인증 정책 관리", breadcrumbs: ["관리", "인증 정책"] },
      },
      {
        path: "/admin/board-group",
        name: "board-group-admin",
        component: () => import("@/features/admin/BoardGroupAdminPage.vue"),
        meta: { pageTitle: "게시판 그룹 관리", breadcrumbs: ["관리", "게시판 그룹"] },
      },
      {
        path: "/admin/code",
        name: "code-admin",
        component: () => import("@/features/admin/CodeAdminPage.vue"),
        meta: { pageTitle: "코드 관리", breadcrumbs: ["관리", "코드"] },
      },
      {
        path: "/admin/menu",
        name: "menu-admin",
        component: () => import("@/features/admin/MenuAdminPage.vue"),
        meta: { pageTitle: "메뉴 관리", breadcrumbs: ["관리", "메뉴"] },
      },
      {
        path: "/admin/users",
        name: "user-admin",
        component: () => import("@/features/admin/UserAdminPage.vue"),
        meta: { pageTitle: "계정 관리", breadcrumbs: ["관리", "계정"] },
      },
      {
        path: "/admin/log",
        name: "log-list",
        component: () => import("@/features/admin/LogAdminPage.vue"),
        meta: { pageTitle: "로그 목록", breadcrumbs: ["관리", "로그"] },
      },
      {
        path: "/admin/log/stats-user",
        name: "log-stats-user",
        component: () => import("@/features/admin/LogAdminPage.vue"),
        meta: { pageTitle: "사용자별 로그 통계", breadcrumbs: ["관리", "로그"] },
      },
      {
        path: "/my",
        name: "user-my",
        component: () => import("@/features/user/UserMyPage.vue"),
        meta: { pageTitle: "내 정보", breadcrumbs: ["내 정보"] },
      },
      {
        path: "/user/signup/approval",
        name: "user-signup-approval",
        component: () => import("@/features/user/signup/UserSignupApprovalList.vue"),
        meta: { pageTitle: "계정 신청 승인 관리", breadcrumbs: ["관리", "계정 신청"] },
      },
      {
        path: "/board",
        component: () => import("@/features/board/BoardPostLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: ":boardKey",
            name: "board-post-list",
            component: () => import("@/features/board/BoardPostList.vue"),
            meta: { pageTitle: "게시판", breadcrumbs: ["게시판"] },
          },
        ],
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/app/layouts/AuthLayout.vue"),
    children: [
      {
        path: "/sign-in",
        name: "sign-in",
        component: () => import("@/features/auth/SignIn.vue"),
        meta: { pageTitle: "로그인" },
      },
      {
        path: "/user/signup",
        name: "user-signup",
        component: () => import("@/features/auth/UserSignupPage.vue"),
        meta: { pageTitle: "계정 신청" },
      },
      {
        path: "/auth/verify-result",
        name: "auth-verify-result",
        component: () => import("@/features/auth/VerifyResultPage.vue"),
        meta: { pageTitle: "계정 인증 결과" },
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/app/layouts/SystemLayout.vue"),
    children: [
      {
        path: "/journal/daily",
        component: () => import("@/features/journal/day/JournalDayDailyLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "journal-daily",
            component: () => import("@/features/journal/day/JournalDayDaily.vue"),
            meta: { pageTitle: "일간 일기" },
          },
        ],
      },
      {
        path: "/journal/entry/search",
        name: "journal-entry-search",
        component: () => import("@/features/journal/entry/JournalEntrySearchPage.vue"),
        meta: { pageTitle: "저널 엔트리 검색", middleware: "auth" },
      },
      {
        path: "/error",
        name: "error",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitle: "오류", errorType: "general" },
      },
      {
        path: "/400",
        name: "400",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitle: "잘못된 요청", errorType: "bad_request" },
      },
      {
        path: "/403",
        name: "403",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitle: "접근 권한 없음", errorType: "access_denied" },
      },
      {
        path: "/404",
        name: "404",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitle: "페이지를 찾을 수 없음", errorType: "not_found" },
      },
      {
        path: "/500",
        name: "500",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitle: "서버 오류", errorType: "general" },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, top: 80, behavior: "smooth" };
    }
    return { top: 0, left: 0, behavior: "smooth" };
  },
});

function isPopupProtectedRoute(name: unknown): boolean {
  return ["journal-entry-search", "journal-daily"].includes(String(name));
}

function isManagerMenuRoute(path: string): boolean {
  return path === "/admin" || path.startsWith("/admin/") || path === "/user/signup/approval";
}

function isUserMenuRoute(path: string): boolean {
  return (
    path.startsWith("/journal/") ||
    path === "/annual" ||
    path.startsWith("/annual/") ||
    path === "/thread" ||
    path.startsWith("/thread/") ||
    path === "/schedule" ||
    path === "/board" ||
    path.startsWith("/board/")
  );
}

function resolveMenuModeForRoute(path: string): MenuMode | null {
  if (isManagerMenuRoute(path)) return "MNGR";
  if (isUserMenuRoute(path)) return "USER";
  return null;
}

async function syncMenuModeForRoute(path: string) {
  const authStore = useAuthStore();
  const nextMode = resolveMenuModeForRoute(path);
  if (!nextMode) return;
  if (nextMode === "MNGR" && !authStore.user?.isMngr) return;

  const menuStore = useMenuStore();
  if (menuStore.mode === nextMode) return;

  console.info("[router] sync menu mode for route", { path, nextMode });
  await menuStore.setMenuMode(nextMode);
}

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();
  const configStore = useConfigStore();

  markRuntimePending("화면 이동 중입니다.");
  try {
    document.title = `${to.meta.pageTitle ?? ""} - ${import.meta.env.VITE_APP_NAME}`;
    configStore.resetLayoutConfig();

    await authStore.verifyAuth();

    const requiresAuth = to.matched.some((r) => r.meta.middleware === "auth");
    if (requiresAuth) {
      if (authStore.isAuthenticated) {
        await syncMenuModeForRoute(to.path);
        next();
      } else {
        if (isPopupProtectedRoute(to.name) || isAuthPopupRoute(to.name)) {
          console.warn("[router] popup route blocked by expired auth", { path: to.fullPath });
          await confirmSessionExpired(to.name);
          next(false);
          return;
        }
        console.warn("[router] protected route blocked by expired auth", { path: to.fullPath });
        const confirmed = await confirmSessionExpired(to.name);
        if (confirmed) {
          next(buildSessionExpiredSignInRoute(to.fullPath));
          return;
        }
        next(false);
      }
    } else {
      next();
    }
  } catch (error) {
    clearRuntimePending();
    const title = isAuthVerificationError(error)
      ? "인증 상태를 확인하는 중 오류가 발생했습니다."
      : "화면 이동 중 오류가 발생했습니다";
    reportRuntimeError(error, "router-before-each", title);
    next(false);
  }
});

export default router;
