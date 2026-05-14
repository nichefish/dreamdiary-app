import { ref } from "vue";
import { defineStore } from "pinia";
import ApiService from "@metronic/core/services/ApiService";

/**
 * 백엔드 MenuDto 대응 인터페이스.
 * Spring Boot MenuDto 구조와 동일하게 유지한다.
 */
export interface MenuDto {
  id: number;
  menuName: string;
  url: string;
  icon: string;
  submenuExpandType: string;
  dirYn: string;
  useYn: string;
  subMenuList: MenuDto[];
}

const menuItem = (
  id: number,
  menuName: string,
  url: string,
  icon = "bi-circle"
): MenuDto => ({
  id,
  menuName,
  url,
  icon,
  submenuExpandType: "NO_SUB",
  dirYn: "N",
  useYn: "Y",
  subMenuList: [],
});

const menuSection = (
  id: number,
  menuName: string,
  subMenuList: MenuDto[]
): MenuDto => ({
  id,
  menuName,
  url: "",
  icon: "",
  submenuExpandType: "LIST",
  dirYn: "Y",
  useYn: "Y",
  subMenuList,
});

const FALLBACK_MENU_LIST: MenuDto[] = [
  menuSection(-100, "\ub2e4\uc774\uc5b4\ub9ac", [
    menuItem(-101, "\ub300\uc2dc\ubcf4\ub4dc", "/dashboard", "bi-speedometer2"),
    menuItem(-102, "\uc6d4\uac04 \uc77c\uae30", "/journal", "bi-journal-text"),
    menuItem(-103, "\uc8fc\uac04 \uc77c\uae30", "/journal/weekly", "bi-calendar-week"),
    menuItem(-104, "\uc77c\uae30 \uce98\ub9b0\ub354", "/journal/calendar", "bi-calendar3"),
    menuItem(-105, "\uc77c\uae30 \uba54\ud0c0", "/journal/meta", "bi-tags"),
    menuItem(-106, "\uc5f0\uac04 \uacb0\uc0b0", "/annual", "bi-bar-chart"),
    menuItem(-107, "\uc2a4\ub808\ub4dc", "/thread", "bi-chat-square-text"),
    menuItem(-108, "\uc77c\uc815", "/schedule", "bi-calendar-check"),
  ]),
  menuSection(-200, "\uad00\ub9ac", [
    menuItem(-201, "\uad00\ub9ac \ud648", "/admin", "bi-grid"),
    menuItem(-202, "\uba54\ub274 \uad00\ub9ac", "/admin/menu", "bi-list-ul"),
    menuItem(-203, "\uacc4\uc815 \uad00\ub9ac", "/admin/users", "bi-people"),
    menuItem(-204, "\uacc4\uc815 \uc2e0\uccad \uc2b9\uc778", "/user/signup/approval", "bi-person-check"),
    menuItem(-205, "\uc778\uc99d \uc815\ucc45", "/admin/auth-policy", "bi-shield-lock"),
    menuItem(-206, "\uac8c\uc2dc\ud310 \uadf8\ub8f9", "/admin/board-group", "bi-layout-text-window"),
    menuItem(-207, "\ucf54\ub4dc \uad00\ub9ac", "/admin/code", "bi-braces"),
    menuItem(-208, "\ub85c\uadf8", "/admin/log", "bi-card-list"),
    menuItem(-209, "\uc0ac\uc6a9\uc790 \ud1b5\uacc4", "/admin/log/stats-user", "bi-graph-up"),
  ]),
  menuSection(-300, "\uacc4\uc815", [
    menuItem(-301, "\ub0b4 \uc815\ubcf4", "/my", "bi-person-circle"),
  ]),
];

/**
 * useMenuStore
 * Spring Boot API에서 사용자 사이드바 메뉴 목록을 조회하고 캐싱한다.
 * - 메뉴 로드: GET /api/menus → MenuDto[] 반환
 */
export const useMenuStore = defineStore("menu", () => {
  const menuList = ref<MenuDto[]>([]);
  const loaded = ref(false);

  /**
   * 사용자 사이드바 메뉴를 서버에서 조회한다.
   * 이미 로드된 경우 재요청하지 않는다.
   */
  async function fetchUserMenu() {
    if (loaded.value) return;
    try {
      const { data } = await ApiService.get("/api/menus");
      menuList.value = data.rslt && data.list?.length ? data.list : FALLBACK_MENU_LIST;
    } catch (e) {
      menuList.value = FALLBACK_MENU_LIST;
      console.error("메뉴 로딩 실패", e);
    }
    loaded.value = true;
  }

  /** 메뉴 캐시를 초기화하고 재조회한다. (메뉴 변경 후 갱신용) */
  async function refreshMenu() {
    loaded.value = false;
    await fetchUserMenu();
  }

  return { menuList, loaded, fetchUserMenu, refreshMenu };
});
