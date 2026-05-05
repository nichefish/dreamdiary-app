/**
 * 페이지 부트스트랩 JSON(user_signup_page_data) 파서
 *
 * @author nichefish
 */
/** 코드 상세 행(Freemarker에서 직렬화) */
export type CodeRow = {
    code: string;
    codeName: string;
};

export type UserSignupBootstrap = {
    staffRankCd: string;
    authUserRoleKey: string;
    isReg: boolean;
    siteAcs: {
        upperMenuNm?: string | null;
        menuName?: string | null;
        pageName?: string | null;
    };
    /** CodeLookupService 에서 추가된 코드 그룹 키(CMPY_CD, TEAM_CD, EMPLYM_CD, JOB_TITLE_CD) */
    codeLists: Record<string, CodeRow[]>;
    userDefaults: {
        id: number | null;
        fileGroupId: string;
        nickname: string;
        username: string;
        emailId: string;
        emailDomain: string;
        phoneNumber: string;
        content: string;
    };
};

/** JSON 파서 실패·엘리먼트 누락 시에도 앱 로딩 가능한 최소 구조 반환한다. 변경 전후: null 반환으로 Vue 전체가 시작되지 못했다. 변경 후 브레이스 상태로 헤더 렌더·로그 처리만 보장하고 실제 기능은 페이지 데이터 복구가 필요함을 알린다 */
function skeletonBootstrap(reason: string): UserSignupBootstrap {
    console.error("[UserSignupApp] user_signup_page_data 이용 불가. reason=", reason);

    return {
        staffRankCd: "",
        authUserRoleKey: "",
        isReg: false,
        siteAcs: {},
        codeLists: {},
        userDefaults: {
            id: null,
            fileGroupId: "",
            nickname: "",
            username: "",
            emailId: "",
            emailDomain: "",
            phoneNumber: "",
            content: "",
        },
    };
}

function safeParseBootstrap(): UserSignupBootstrap {
    const el = document.getElementById("user_signup_page_data");
    if (!el)
        return skeletonBootstrap("script#user_signup_page_data 미존재");

    try {
        const parsed = JSON.parse(el.textContent || "{}");
        const codeLists = parsed.codeLists && typeof parsed.codeLists === "object" ? parsed.codeLists : {};

        const out = {
            staffRankCd: String(parsed.staffRankCd || ""),
            authUserRoleKey: String(parsed.authUserRoleKey || ""),
            isReg: !!parsed.isReg,
            siteAcs: parsed.siteAcs || {},
            codeLists,

            userDefaults: {
                id: typeof parsed.userDefaults?.id === "number" ? parsed.userDefaults.id : null,
                fileGroupId: String(parsed.userDefaults?.fileGroupId ?? ""),
                nickname: String(parsed.userDefaults?.nickname ?? ""),
                username: String(parsed.userDefaults?.username ?? ""),
                emailId: String(parsed.userDefaults?.emailId ?? ""),
                emailDomain: String(parsed.userDefaults?.emailDomain ?? ""),
                phoneNumber: String(parsed.userDefaults?.phoneNumber ?? ""),
                content: String(parsed.userDefaults?.content ?? ""),
            },
        };

        if (!out.authUserRoleKey)
            console.warn("[UserSignupApp] authUserRoleKey empty — 서버 JSON 확인 필요.");

        return out;
    }

    catch (e) {

        console.error("[UserSignupApp] user_signup_page_data parse failed", e);

        return skeletonBootstrap("parse error");

    }
}

export default {
    parse(): UserSignupBootstrap {
        return safeParseBootstrap();
    },
    codesOrEmpty(list: CodeRow[] | undefined | null): CodeRow[] {
        return Array.isArray(list) ? list : [];
    },
};
