import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useConfigStore } from "@/shared/config/stores/config";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
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
    redirect: "/journal/daily",
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
            redirect: { name: "journal-daily-tab" },
          },
          {
            path: "daily",
            name: "journal-daily-tab",
            component: () => import("@/features/journal/day/JournalDayDaily.vue"),
            meta: { pageTitleKey: "route.title.journal-daily-tab" },
          },
          {
            path: "monthly",
            name: "journal-monthly",
            component: () => import("@/features/journal/day/JournalDayMonthly.vue"),
            meta: { pageTitleKey: "route.title.journal-monthly" },
          },
          {
            path: "weekly",
            name: "journal-weekly",
            component: () => import("@/features/journal/day/JournalDayWeekly.vue"),
            meta: { pageTitleKey: "route.title.journal-weekly" },
          },
          {
            path: "calendar",
            name: "journal-calendar",
            component: () => import("@/features/journal/day/JournalDayCalendar.vue"),
            meta: { pageTitleKey: "route.title.journal-calendar" },
          },
          {
            path: "meta",
            name: "journal-meta",
            component: () => import("@/features/journal/day/JournalDayMeta.vue"),
            meta: { pageTitleKey: "route.title.journal-meta" },
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
            meta: { pageTitleKey: "route.title.annual-list" },
          },
          {
            path: ":yy",
            name: "annual-detail",
            component: () => import("@/features/journal/annual/JournalAnnualDetail.vue"),
            meta: { pageTitleKey: "route.title.annual-detail" },
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
            meta: { pageTitleKey: "route.title.journal-thread" },
          },
          {
            path: "new",
            name: "thread-create",
            component: () => import("@/features/journal/thread/JournalThreadList.vue"),
            meta: { pageTitleKey: "route.title.journal-thread" },
          },
          {
            path: ":id",
            name: "thread-detail",
            component: () => import("@/features/journal/thread/JournalThreadDetailPage.vue"),
            meta: { pageTitleKey: "route.title.journal-thread" },
          },
          {
            path: ":id/edit",
            name: "thread-edit",
            component: () => import("@/features/journal/thread/JournalThreadEditPage.vue"),
            meta: { pageTitleKey: "route.title.journal-thread" },
          },
        ],
      },
      {
        path: "/schedule",
        name: "schedule-calendar",
        component: () => import("@/features/calendar/ScheduleCalendar.vue"),
        meta: { pageTitleKey: "route.title.schedule-calendar" },
      },
      {
        path: "/admin",
        name: "admin-page",
        component: () => import("@/features/admin/AdminPage.vue"),
        meta: { pageTitleKey: "route.title.admin" },
      },
      {
        path: "/admin/auth-policy",
        name: "auth-policy",
        component: () => import("@/features/admin/AuthPolicyPage.vue"),
        meta: { pageTitleKey: "route.title.auth-policy" },
      },
      {
        path: "/admin/board-group",
        name: "board-group-admin",
        component: () => import("@/features/admin/BoardGroupAdminPage.vue"),
        meta: { pageTitleKey: "route.title.board-group-admin" },
      },
      {
        path: "/admin/code",
        name: "code-admin",
        component: () => import("@/features/admin/CodeAdminPage.vue"),
        meta: { pageTitleKey: "route.title.code-admin" },
      },
      {
        path: "/admin/menu",
        name: "menu-admin",
        component: () => import("@/features/admin/MenuAdminPage.vue"),
        meta: { pageTitleKey: "route.title.menu-admin" },
      },
      {
        path: "/admin/user-groups",
        name: "user-group-admin",
        component: () => import("@/features/admin/UserGroupAdminPage.vue"),
        meta: { pageTitleKey: "route.title.user-group-admin" },
      },
      {
        path: "/admin/users",
        name: "user-admin",
        component: () => import("@/features/admin/UserAdminPage.vue"),
        meta: { pageTitleKey: "route.title.user-admin" },
      },
      {
        path: "/admin/log",
        name: "log-list",
        component: () => import("@/features/admin/LogAdminPage.vue"),
        meta: { pageTitleKey: "route.title.log-list" },
      },
      {
        path: "/admin/log/stats-user",
        name: "log-stats-user",
        component: () => import("@/features/admin/LogAdminPage.vue"),
        meta: { pageTitleKey: "route.title.log-stats-user" },
      },
      {
        path: "/my",
        component: () => import("@/features/user/UserMyPage.vue"),
        meta: { pageTitleKey: "route.title.user-my" },
        children: [
          {
            path: "",
            redirect: { name: "user-my-profile" },
          },
          {
            path: "profile",
            name: "user-my-profile",
            component: () => import("@/features/user/components/UserMyProfileTab.vue"),
          },
          {
            path: "security",
            name: "user-my-security",
            component: () => import("@/features/user/components/UserMySecurityTab.vue"),
          },
          {
            path: "prefixes",
            name: "user-my-prefixes",
            component: () => import("@/features/user/components/UserMyPrefixesTab.vue"),
          },
        ],
      },
      {
        /*
         * 계정 신청 승인은 계정 관리의 `계정 신청 승인` 탭으로 흡수됐다.
         * 북마크·레거시 진입을 흘려보내기 위해 리다이렉트만 남긴다.
         */
        path: "/user/signup/approval",
        redirect: { name: "user-admin", query: { tab: "signup" } },
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
            meta: { pageTitleKey: "route.title.board" },
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
        meta: { pageTitleKey: "route.title.sign-in" },
      },
      {
        path: "/user/signup",
        name: "user-signup",
        component: () => import("@/features/auth/UserSignupPage.vue"),
        meta: { pageTitleKey: "route.title.user-signup" },
      },
      {
        path: "/auth/verify-result",
        name: "auth-verify-result",
        component: () => import("@/features/auth/VerifyResultPage.vue"),
        meta: { pageTitleKey: "route.title.auth-verify-result" },
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/app/layouts/SystemLayout.vue"),
    children: [
      {
        path: "/journal/daily-popup",
        component: () => import("@/features/journal/day/JournalDayDailyLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "journal-daily",
            component: () => import("@/features/journal/day/JournalDayDaily.vue"),
            meta: { pageTitleKey: "route.title.journal-daily" },
          },
        ],
      },
      {
        path: "/journal/entry/search",
        name: "journal-entry-search",
        component: () => import("@/features/journal/entry/JournalEntrySearchPage.vue"),
        meta: { pageTitleKey: "route.title.journal-entry-search", middleware: "auth" },
      },
      {
        path: "/error",
        name: "error",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitleKey: "route.title.error", errorType: "general" },
      },
      {
        path: "/400",
        name: "400",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitleKey: "route.title.bad-request", errorType: "bad_request" },
      },
      {
        path: "/403",
        name: "403",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitleKey: "route.title.access-denied", errorType: "access_denied" },
      },
      {
        path: "/404",
        name: "404",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitleKey: "route.title.not-found", errorType: "not_found" },
      },
      {
        path: "/500",
        name: "500",
        component: () => import("@/app/pages/ErrorPage.vue"),
        meta: { pageTitleKey: "route.title.server-error", errorType: "general" },
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
  /* 계정 신청 승인이 /admin/users 탭으로 흡수되어 /admin 밖 예외 경로가 사라졌다. */
  return path === "/admin" || path.startsWith("/admin/");
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
  if (nextMode === "MNGR" && !authStore.canUseMngrMenuMode()) return;

  const menuStore = useMenuStore();
  if (menuStore.mode === nextMode) return;

  console.info("[router] sync menu mode for route", { path, nextMode });
  await menuStore.setMenuMode(nextMode);
}

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();
  const configStore = useConfigStore();
  const localeStore = useLocaleStore();

  markRuntimePending("runtime.pending.navigation");
  try {
    configStore.resetLayoutConfig();

    await localeStore.ensureCatalog();
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
    const titleKey = isAuthVerificationError(error)
      ? "auth.verification.failure"
      : "runtime.error.navigation";
    reportRuntimeError(error, "router-before-each", titleKey);
    next(false);
  }
});

export default router;
