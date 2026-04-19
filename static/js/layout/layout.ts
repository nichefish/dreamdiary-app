/**
 * Layout.ts
 * 레이아웃 스크립트 모듈
 *
 * @author nichefish
 */
const Layout: Page = (function(): Page {
    return {
        SIDEBAR_STATE_KEY: "layout_sidebar_desktop_state",
        ASIDE_STATE_KEY_PREFIX: "layout_aside_state:",

        /**
         * Layout 객체 초기화
         */
        init: function(): void {
            // 공백 자동 제거
            cF.validate.noSpaces(".no-space");
            // 개별 input 유효성검사
            cF.validate.onlyNum(".number");

            Layout.aliveCheck(60);

            Layout.setBtnDelay();
            Layout.modalBtnCloseSafe();
            Layout.initResponsiveDesktopDefaults();
            Layout.initResponsiveAsideDefaults();
        },

        /**
         * 터치 기반 기기 여부
         */
        isTouchDevice: function(): boolean {
            return navigator.maxTouchPoints > 0
                || window.matchMedia("(pointer: coarse)").matches
                || window.matchMedia("(hover: none)").matches;
        },

        /**
         * 모바일/태블릿 기기가 데스크톱 뷰로 보이는지 여부
         */
        isTouchDesktopView: function(): boolean {
            return Layout.isTouchDevice() && window.matchMedia("(min-width: 1200px)").matches;
        },

        /**
         * 태블릿 폭 여부
         */
        isTabletViewport: function(): boolean {
            return window.matchMedia("(min-width: 768px) and (max-width: 1199.98px)").matches;
        },

        /**
         * aside가 있는 레이아웃 여부
         */
        hasAppAside: function(): boolean {
            return document.querySelector("#kt_app_aside") != null
                && document.body?.getAttribute("data-kt-app-aside-enabled") === "true";
        },

        /**
         * 현재 경로 기준 aside 상태 저장 키
         */
        getAsideStateKey: function(): string {
            return `${Layout.ASIDE_STATE_KEY_PREFIX}${location.pathname}`;
        },

        /**
         * 터치 기기의 데스크톱 뷰 기본값 적용
         */
        initResponsiveDesktopDefaults: function(): void {
            const body = document.body;
            const sidebarToggle = document.querySelector("#kt_app_sidebar_toggle");
            if (body == null) return;

            const storedSidebarState = localStorage.getItem(Layout.SIDEBAR_STATE_KEY);
            if (storedSidebarState === "minimized") {
                body.setAttribute("data-kt-app-sidebar-minimize", "on");
            } else if (storedSidebarState === "expanded") {
                body.removeAttribute("data-kt-app-sidebar-minimize");
            } else if (Layout.isTabletViewport()) {
                body.setAttribute("data-kt-app-sidebar-minimize", "on");
            } else if (Layout.isTouchDesktopView()) {
                body.setAttribute("data-kt-app-sidebar-minimize", "on");
            }

            sidebarToggle?.addEventListener("click", function(): void {
                window.setTimeout(function(): void {
                    const isMinimized = body.getAttribute("data-kt-app-sidebar-minimize") === "on";
                    localStorage.setItem(Layout.SIDEBAR_STATE_KEY, isMinimized ? "minimized" : "expanded");
                }, 0);
            });
        },

        /**
         * aside 레이아웃 기본값 적용
         */
        initResponsiveAsideDefaults: function(): void {
            const body = document.body;
            const aside = document.querySelector("#kt_app_aside");
            const asideToggle = document.querySelector("#kt_app_engage_primary_btn") as HTMLAnchorElement | null;
            const asideDrawerToggle = document.querySelector("#kt_app_aside_toggle") as HTMLButtonElement | null;
            if (body == null || aside == null || asideToggle == null || !Layout.hasAppAside()) return;

            const tabletMediaQuery = window.matchMedia("(max-width: 1199.98px)");
            const mobileMediaQuery = window.matchMedia("(max-width: 767.98px)");
            const desktopCollapseAttr = "data-kt-app-aside-collapse";
            const tabletHideAttr = "data-app-hide-aside";
            const asideStateKey = Layout.getAsideStateKey();

            const syncButtonText = function(isOpen: boolean): void {
                asideToggle.innerHTML = isOpen
                    ? '<i class="bi bi-x-lg me-1"></i>Filter'
                    : '<i class="bi bi-layout-sidebar-inset-reverse me-1"></i>Filter';
                asideToggle.setAttribute("aria-expanded", isOpen ? "true" : "false");
            };

            const syncAsideState = function(): void {
                asideToggle.setAttribute("href", "javascript:void(0);");
                asideToggle.removeAttribute("target");
                asideToggle.setAttribute("role", "button");
                asideToggle.setAttribute("aria-controls", "kt_app_aside");

                const storedAsideState = localStorage.getItem(asideStateKey);
                if (mobileMediaQuery.matches) {
                    body.removeAttribute(desktopCollapseAttr);
                    if (storedAsideState === "expanded") {
                        body.removeAttribute(tabletHideAttr);
                        syncButtonText(true);
                    } else {
                        body.setAttribute(tabletHideAttr, "true");
                        syncButtonText(false);
                    }
                    return;
                }
                if (tabletMediaQuery.matches) {
                    body.removeAttribute(desktopCollapseAttr);
                    if (storedAsideState === "expanded") {
                        body.removeAttribute(tabletHideAttr);
                        syncButtonText(true);
                    } else {
                        body.setAttribute(tabletHideAttr, "true");
                        syncButtonText(false);
                    }
                    return;
                }

                body.removeAttribute(tabletHideAttr);
                if (storedAsideState === "collapsed") {
                    body.setAttribute(desktopCollapseAttr, "on");
                } else if (storedAsideState === "expanded") {
                    body.removeAttribute(desktopCollapseAttr);
                } else if (Layout.isTouchDesktopView()) {
                    body.setAttribute(desktopCollapseAttr, "on");
                } else {
                    body.removeAttribute(desktopCollapseAttr);
                }
                syncButtonText(body.getAttribute(desktopCollapseAttr) !== "on");
            };

            asideToggle.addEventListener("click", function(event: Event): void {
                event.preventDefault();

                if (mobileMediaQuery.matches) {
                    const willOpen = body.getAttribute(tabletHideAttr) === "true";
                    if (willOpen) {
                        body.removeAttribute(tabletHideAttr);
                        localStorage.setItem(asideStateKey, "expanded");
                    } else {
                        body.setAttribute(tabletHideAttr, "true");
                        localStorage.setItem(asideStateKey, "collapsed");
                    }
                    asideDrawerToggle?.click();
                    syncButtonText(willOpen);
                    return;
                }

                if (tabletMediaQuery.matches) {
                    const willOpen = body.getAttribute(tabletHideAttr) === "true";
                    if (willOpen) {
                        body.removeAttribute(tabletHideAttr);
                        aside.scrollIntoView({
                            behavior: "smooth",
                            block: "start",
                        });
                        localStorage.setItem(asideStateKey, "expanded");
                    } else {
                        body.setAttribute(tabletHideAttr, "true");
                        localStorage.setItem(asideStateKey, "collapsed");
                    }
                    syncButtonText(willOpen);
                    return;
                }

                const willOpen = body.getAttribute(desktopCollapseAttr) === "on";
                if (willOpen) {
                    body.removeAttribute(desktopCollapseAttr);
                    localStorage.setItem(asideStateKey, "expanded");
                } else {
                    body.setAttribute(desktopCollapseAttr, "on");
                    localStorage.setItem(asideStateKey, "collapsed");
                }
                syncButtonText(willOpen);
            });

            if (typeof tabletMediaQuery.addEventListener === "function") {
                tabletMediaQuery.addEventListener("change", syncAsideState);
            } else {
                tabletMediaQuery.addListener(syncAsideState);
            }

            syncAsideState();
        },

        /**
         * alive-check
         * 주기적으로 alive check
         * @param seconds 주기(초), 기본값 = 60
         */
        aliveCheck: function(seconds: number = 60): void {
            const aliveCheckUrl: string = Url.API_ALIVE_CHECK;
            setInterval(() => {
                // fetch(aliveCheckUrl);
            }, seconds * 1000);
        },

        /**
         * (안전클릭 제외한) 모든 버튼에 딜레이 기능 추가
         */
        setBtnDelay: function(): void {
            const buttons: NodeListOf<HTMLElement> = document.querySelectorAll("button:not(.modal-btn-close-safe), .btn:not(.modal-btn-close-safe), .badge:not(.modal-btn-close-safe)");
            buttons.forEach(function(button: HTMLElement): void {
                button.addEventListener("click", function(): void {
                    cF.ui.delayBtn(this);
                });
            });
        },

        /**
         * 모달 닫기 버튼에 안전장치 적용
         */
        modalBtnCloseSafe: function(): void {
            // 모든 닫기 버튼을 선택
            const closeButtons: NodeListOf<HTMLElement> = document.querySelectorAll('.modal-btn-close-safe');
            // 각 버튼마다 클릭 이벤트 추가
            closeButtons.forEach(function(button: HTMLElement): void {
                let isAllowed: boolean = false;
                button.removeAttribute('data-bs-dismiss');

                button.addEventListener('click', function(event: Event): void {
                    event.preventDefault();
                    if (isAllowed) return;

                    isAllowed = true;
                    button.setAttribute('data-bs-dismiss', 'modal');
                    // 닫기 함수 존재시 실행
                    const func: string = this.getAttribute('data-func');
                    if (func) eval(func);  // 안전하게 실행
                    // 2초 후 안전장치 다시 on
                    setTimeout(function(): void {
                        isAllowed = false;
                        button.removeAttribute('data-bs-dismiss');
                    }, 2000);
                });
            });
        },

        /**
         * 창 닫기
         */
        close: function(): void {
            window.close();
        },

        /**
         * 페이지 상단으로 이동
         */
        toPageTop: function(): void {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        },

        /**
         * 내 정보 상세 페이지 이동
         */
        myInfoDtl: function(): void {
            cF.ui.blockUIReplace(Url.USER_MY_DTL);
        },

        /**
         * 로그아웃 처리
         */
        logout: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.logout"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                location.replace(Url.API_AUTH_LGOUT);
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Layout.init();
});
