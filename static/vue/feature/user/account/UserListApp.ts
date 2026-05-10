/**
 * UserListApp.ts
 * 사용자 계정 관리 목록 화면 Vue 앱
 *
 * @author nichefish
 */
import UserListTable from "./components/UserListTable.js";
import UserPagination from "./components/UserPagination.js";
import userDataService from "./services/userDataService.js";
import createUserListActions from "./services/userListActionService.js";
import bindUserListEventBridge from "./services/userListEventBridgeService.js";
import { UserListState } from "./types.js";

const state = Vue.reactive({
    rows: [],
    pagination: {
        currPageNo: 1,
        lastPageNo: 1,
        totalCnt: 0,
        pageSize: 10,
        isFirstPage: true,
        isLastPage: true,
        prevPageNo: 0,
        nextPageNo: 0,
    },
    labels: userDataService.parseLabels(),
}) as UserListState;

const actions = createUserListActions();
bindUserListEventBridge(actions);

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const UserListRootApp = {
    name: "UserListRootApp",
    components: {
        UserListTable,
        UserPagination,
    },
    data(): { state: UserListState } {
        return { state };
    },
    methods: {
        onOpenDetail(id: number): void { actions.detail(id); },
        onGoPage(pageNo: number): void { actions.page(pageNo); },
        onChangePageSize(size: number): void { actions.page(1, size); },
    },
    template: `
    <teleport to="#user_list_div">
        <UserListTable
            :rows="state.rows"
            :labels="state.labels"
            @open-detail="onOpenDetail"
        />
    </teleport>
    <teleport to="#user_pagination_div">
        <UserPagination
            :pagination="state.pagination"
            :labels="state.labels"
            @go-page="onGoPage"
            @change-size="onChangePageSize"
        />
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    state.rows = userDataService.parseRowsFromPageData();
    userDataService.applyPaginationFromPageData(state.pagination);

    if (!document.getElementById("user_app")
        || !document.getElementById("user_list_div")
        || !document.getElementById("user_pagination_div")) {
        console.error("[UserListApp] Vue mount root not found.");
        return;
    }

    Vue.createApp(UserListRootApp).mount("#user_app");
    cF.table.initSort();
});
