import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useConfigStore } from "@/stores/config";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/dashboard",
    component: () => import("@/layouts/default/DefaultLayout.vue"),
    meta: { middleware: "auth" },
    children: [
      {
        path: "/dashboard",
        name: "dashboard",
        component: () => import("@/views/Dashboard.vue"),
        meta: { pageTitle: "대시보드", breadcrumbs: ["홈"] },
      },
      {
        path: "/journal",
        component: () => import("@/views/journal/JournalLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            redirect: { name: "journal-weekly" },
          },
          {
            path: "monthly",
            name: "journal-monthly",
            component: () => import("@/views/journal/JournalMonthly.vue"),
            meta: { pageTitle: "월간 일기", breadcrumbs: ["일기"] },
          },
          {
            path: "weekly",
            name: "journal-weekly",
            component: () => import("@/views/journal/JournalWeekly.vue"),
            meta: { pageTitle: "주간 일기", breadcrumbs: ["일기"] },
          },
          {
            path: "calendar",
            name: "journal-calendar",
            component: () => import("@/views/journal/JournalCalendar.vue"),
            meta: { pageTitle: "일기 캘린더", breadcrumbs: ["일기"] },
          },
          {
            path: "meta",
            name: "journal-meta",
            component: () => import("@/views/journal/JournalMeta.vue"),
            meta: { pageTitle: "일기 메타", breadcrumbs: ["일기"] },
          },
        ],
      },
      {
        path: "/annual",
        component: () => import("@/views/journal/annual/JournalAnnualLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "annual-list",
            component: () => import("@/views/journal/annual/JournalAnnualList.vue"),
            meta: { pageTitle: "연간 결산", breadcrumbs: ["일기", "결산"] },
          },
          {
            path: ":yy",
            name: "annual-detail",
            component: () => import("@/views/journal/annual/JournalAnnualDetail.vue"),
            meta: { pageTitle: "연간 결산 상세", breadcrumbs: ["일기", "결산"] },
          },
        ],
      },
      {
        path: "/thread",
        component: () => import("@/views/journal/thread/JournalThreadLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "thread-list",
            component: () => import("@/views/journal/thread/JournalThreadList.vue"),
            meta: { pageTitle: "일기 스레드", breadcrumbs: ["일기", "스레드"] },
          },
        ],
      },
      {
        path: "/schedule",
        name: "schedule-calendar",
        component: () => import("@/views/schedule/ScheduleCalendar.vue"),
        meta: { pageTitle: "일정 캘린더", breadcrumbs: ["일정"] },
      },
      {
        path: "/admin",
        name: "admin-page",
        component: () => import("@/views/admin/AdminPage.vue"),
        meta: { pageTitle: "사이트 관리", breadcrumbs: ["관리"] },
      },
      {
        path: "/admin/auth-policy",
        name: "auth-policy",
        component: () => import("@/views/admin/AuthPolicyPage.vue"),
        meta: { pageTitle: "인증 정책 관리", breadcrumbs: ["관리", "인증 정책"] },
      },
      {
        path: "/admin/board-group",
        name: "board-group-admin",
        component: () => import("@/views/admin/BoardGroupAdminPage.vue"),
        meta: { pageTitle: "게시판 그룹 관리", breadcrumbs: ["관리", "게시판 그룹"] },
      },
      {
        path: "/admin/code",
        name: "code-admin",
        component: () => import("@/views/admin/CodeAdminPage.vue"),
        meta: { pageTitle: "코드 관리", breadcrumbs: ["관리", "코드"] },
      },
      {
        path: "/admin/menu",
        name: "menu-admin",
        component: () => import("@/views/admin/MenuAdminPage.vue"),
        meta: { pageTitle: "메뉴 관리", breadcrumbs: ["관리", "메뉴"] },
      },
      {
        path: "/admin/users",
        name: "user-admin",
        component: () => import("@/views/admin/UserAdminPage.vue"),
        meta: { pageTitle: "계정 관리", breadcrumbs: ["관리", "계정"] },
      },
      {
        path: "/admin/log",
        name: "log-list",
        component: () => import("@/views/admin/LogAdminPage.vue"),
        meta: { pageTitle: "로그 목록", breadcrumbs: ["관리", "로그"] },
      },
      {
        path: "/admin/log/stats-user",
        name: "log-stats-user",
        component: () => import("@/views/admin/LogAdminPage.vue"),
        meta: { pageTitle: "사용자별 로그 통계", breadcrumbs: ["관리", "로그"] },
      },
      {
        path: "/my",
        name: "user-my",
        component: () => import("@/views/user/UserMyPage.vue"),
        meta: { pageTitle: "내 정보", breadcrumbs: ["내 정보"] },
      },
      {
        path: "/user/signup/approval",
        name: "user-signup-approval",
        component: () => import("@/views/user/signup/UserSignupApprovalList.vue"),
        meta: { pageTitle: "계정 신청 승인 관리", breadcrumbs: ["관리", "계정 신청"] },
      },
      {
        path: "/board",
        component: () => import("@/views/board/BoardPostLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: ":boardKey",
            name: "board-post-list",
            component: () => import("@/views/board/BoardPostList.vue"),
            meta: { pageTitle: "게시판", breadcrumbs: ["게시판"] },
          },
        ],
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/layouts/AuthLayout.vue"),
    children: [
      {
        path: "/sign-in",
        name: "sign-in",
        component: () => import("@/views/auth/SignIn.vue"),
        meta: { pageTitle: "로그인" },
      },
      {
        path: "/user/signup",
        name: "user-signup",
        component: () => import("@/views/auth/UserSignupPage.vue"),
        meta: { pageTitle: "계정 신청" },
      },
      {
        path: "/auth/verify-result",
        name: "auth-verify-result",
        component: () => import("@/views/auth/VerifyResultPage.vue"),
        meta: { pageTitle: "계정 인증 결과" },
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/layouts/SystemLayout.vue"),
    children: [
      {
        path: "/journal/daily",
        component: () => import("@/views/journal/JournalDailyLayout.vue"),
        meta: { middleware: "auth" },
        children: [
          {
            path: "",
            name: "journal-daily",
            component: () => import("@/views/journal/JournalDaily.vue"),
            meta: { pageTitle: "일간 일기" },
          },
        ],
      },
      {
        path: "/journal/entry/search",
        name: "journal-entry-search",
        component: () => import("@/views/journal/entry/JournalEntrySearchPage.vue"),
        meta: { pageTitle: "저널 엔트리 검색", middleware: "auth" },
      },
      {
        path: "/error",
        name: "error",
        component: () => import("@/views/ErrorPage.vue"),
        meta: { pageTitle: "오류", errorType: "general" },
      },
      {
        path: "/400",
        name: "400",
        component: () => import("@/views/ErrorPage.vue"),
        meta: { pageTitle: "잘못된 요청", errorType: "bad_request" },
      },
      {
        path: "/403",
        name: "403",
        component: () => import("@/views/ErrorPage.vue"),
        meta: { pageTitle: "접근 권한 없음", errorType: "access_denied" },
      },
      {
        path: "/404",
        name: "404",
        component: () => import("@/views/ErrorPage.vue"),
        meta: { pageTitle: "페이지를 찾을 수 없음", errorType: "not_found" },
      },
      {
        path: "/500",
        name: "500",
        component: () => import("@/views/ErrorPage.vue"),
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

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const configStore = useConfigStore();

  document.title = `${to.meta.pageTitle ?? ""} - ${import.meta.env.VITE_APP_NAME}`;
  configStore.resetLayoutConfig();

  await authStore.verifyAuth();

  const requiresAuth = to.matched.some((r) => r.meta.middleware === "auth");
  if (requiresAuth) {
    if (authStore.isAuthenticated) {
      next();
    } else {
      next({ name: "sign-in" });
    }
  } else {
    next();
  }
});

export default router;
