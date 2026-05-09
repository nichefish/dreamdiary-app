/**
 * 계정 신청 승인관리 Vue 앱
 *
 * 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../common/messageHelper.js";

type SignupReqRow = {
    id: number;
    username: string;
    nickname: string;
    email: string;
    status: string;
    createdAt: string;
};

type SignupApprovalBootstrap = {
    siteAcs: {
        upperMenuNm: string;
        menuName: string;
        pageName: string;
    };
    messages: {
        emptyList: string;
        pendingTitle: string;
        actionApprove: string;
        actionReject: string;
        statusPending: string;
        statusApproved: string;
        statusRejected: string;
    };
    pendingList: SignupReqRow[];
    recentList: SignupReqRow[];
};

function parseBootstrap(): SignupApprovalBootstrap {
    const empty: SignupApprovalBootstrap = {
        siteAcs: { upperMenuNm: "", menuName: "", pageName: "" },
        messages: {
            emptyList: "No data.",
            pendingTitle: "Pending signup requests",
            actionApprove: "Approve",
            actionReject: "Reject",
            statusPending: "PENDING",
            statusApproved: "APPROVED",
            statusRejected: "REJECTED",
        },
        pendingList: [],
        recentList: [],
    };
    const el = document.getElementById("user_signup_approval_data");
    if (!el)
        return empty;
    try {
        const parsed = JSON.parse(el.textContent || "{}") as Partial<SignupApprovalBootstrap>;
        return {
            siteAcs: parsed.siteAcs || empty.siteAcs,
            messages: parsed.messages || empty.messages,
            pendingList: Array.isArray(parsed.pendingList) ? parsed.pendingList : [],
            recentList: Array.isArray(parsed.recentList) ? parsed.recentList : [],
        };
    }
    catch (e) {
        console.error("[UserSignupApprovalApp] bootstrap parse 실패", e);
        return empty;
    }
}

function approvalAjax(id: number, isApprove: boolean, done: () => void): void {
    const msg = isApprove ? resolveMessage("view.cnfm.cf") : resolveMessage("view.cnfm.uncf");
    Swal.fire({
        text: msg,
        showCancelButton: true,
    }).then(function(result: { value?: boolean }): void {
        if (!result?.value)
            return;
        const path = isApprove ? Url.USER_SIGNUP_REQUEST_APPROVAL : Url.USER_SIGNUP_REQUEST_REJECTION;
        const url = cF.util.bindUrl(path, { id });
        cF.$ajax.post(url, {}, function(res: { rslt?: boolean; message?: string }): void {
            Swal.fire({ text: res?.message || "" }).then(function(): void {
                if (res?.rslt)
                    done();
            });
        }, "block");
    });
}

const UserSignupApprovalRoot = {
    name: "UserSignupApprovalRoot",
    data(): { siteAcs: SignupApprovalBootstrap["siteAcs"]; messages: SignupApprovalBootstrap["messages"]; pendingList: SignupReqRow[]; recentList: SignupReqRow[] } {
        const bt = parseBootstrap();
        return {
            siteAcs: bt.siteAcs,
            messages: bt.messages,
            pendingList: bt.pendingList,
            recentList: bt.recentList,
        };
    },
    methods: {
        statusBadge(status: string): string {
            if (status === this.messages.statusApproved)
                return "badge badge-light-success";
            if (status === this.messages.statusRejected)
                return "badge badge-light-danger";
            return "badge badge-light-warning";
        },
        approve(id: number): void {
            approvalAjax(id, true, () => {
                this.pendingList = this.pendingList.filter((it: SignupReqRow) => it.id !== id);
                const target = this.recentList.find((it: SignupReqRow) => it.id === id);
                if (target)
                    target.status = this.messages.statusApproved;
            });
        },
        reject(id: number): void {
            approvalAjax(id, false, () => {
                this.pendingList = this.pendingList.filter((it: SignupReqRow) => it.id !== id);
                const target = this.recentList.find((it: SignupReqRow) => it.id === id);
                if (target)
                    target.status = this.messages.statusRejected;
            });
        },
    },
    template: `
<div class="card post">
  <div class="card-header">
    <h3 class="card-title">{{ messages.pendingTitle }}</h3>
  </div>
  <div class="card-body">
    <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed mb-0">
      <thead>
      <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
        <th class="text-center w-10">ID</th>
        <th class="text-center w-25">NAME / USERNAME</th>
        <th class="text-center w-25">E-MAIL</th>
        <th class="text-center w-15">REQUESTED</th>
        <th class="text-center w-25">ACTION</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="req in pendingList" :key="'p-' + req.id">
        <td class="text-center">{{ req.id }}</td>
        <td class="text-start">{{ req.nickname || '-' }} ({{ req.username || '-' }})</td>
        <td class="text-start">{{ req.email || '-' }}</td>
        <td class="text-center">{{ req.createdAt || '-' }}</td>
        <td class="text-center">
          <button type="button" class="btn btn-sm btn-primary me-2" @click="approve(req.id)">{{ messages.actionApprove }}</button>
          <button type="button" class="btn btn-sm btn-light-danger" @click="reject(req.id)">{{ messages.actionReject }}</button>
        </td>
      </tr>
      <tr v-if="pendingList.length === 0">
        <td colspan="5" class="text-center">{{ messages.emptyList }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</div>

<div class="card post mt-5">
  <div class="card-header">
    <h3 class="card-title">Recent Requests</h3>
  </div>
  <div class="card-body">
    <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed mb-0">
      <thead>
      <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
        <th class="text-center w-10">ID</th>
        <th class="text-center w-25">NAME / USERNAME</th>
        <th class="text-center w-25">E-MAIL</th>
        <th class="text-center w-15">REQUESTED</th>
        <th class="text-center w-25">STATUS</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="req in recentList" :key="'r-' + req.id">
        <td class="text-center">{{ req.id }}</td>
        <td class="text-start">{{ req.nickname || '-' }} ({{ req.username || '-' }})</td>
        <td class="text-start">{{ req.email || '-' }}</td>
        <td class="text-center">{{ req.createdAt || '-' }}</td>
        <td class="text-center"><span :class="statusBadge(req.status)">{{ req.status || '-' }}</span></td>
      </tr>
      <tr v-if="recentList.length === 0">
        <td colspan="5" class="text-center">{{ messages.emptyList }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</div>
`,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading")
        document.addEventListener("DOMContentLoaded", fn);
    else
        fn();
}

runWhenDomReady(function(): void {
    if (!document.getElementById("user_signup_approval_app")) {
        console.error("[UserSignupApprovalApp] mount root #user_signup_approval_app 미존재");
        return;
    }
    (Vue as any).createApp(UserSignupApprovalRoot).mount("#user_signup_approval_app");
});
