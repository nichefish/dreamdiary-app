package io.nicheblog.dreamdiary.global;

/**
 * TestConstant
 * <pre>
 *  테스트 용으로 공통으로 사용하는 코드성 데이터 정의
 * </pre>
 *
 * @author nichefish
 */
public final class TestConstant {

    // 테스트 JSON RESPONSE
    public static final String EXPECTED_JSON_RESPONSE = "{\"result\":true,\"status\":200}";

    // 테스트 auditor (자동으로 처리되는 등록자)
    public static final String TEST_AUDITOR = "test_auditor";

    public static final String TEST_USER = "test_user";
    public static final String TEST_PASSWORD_ENCODED = "test_password_encoded";
    public static final String TEST_NICK_NM = "test_nickname";

    // 테스트 등록자
    public static final String TEST_REGSTR_ID = "test_created_by";
    public static final String TEST_REGSTR_NM = "test_createdBy_nm";

    // 테스트 수정자
    public static final String TEST_MDFUSR_ID = "test_mdfuser_id";
    public static final String TEST_MDFUSR_NM = "test_mdfuser_id";
    
    // 코드 관련 (code_group.group_code / group_name)
    public static final String TEST_GROUP_CODE = "test_group_code";
    public static final String TEST_GROUP_NAME = "테스트_분류 코드";
    /** code_item.code */
    public static final String TEST_CODE = "test_item_code";
    public static final String TEST_CODE_NAME = "테스트_상세 코드";

    public static final String TEST_GROUP_CODE_1 = "test_group_code_1";
    public static final String TEST_GROUP_NAME_1 = "테스트_분류 코드_1";
    public static final String TEST_CODE_1 = "test_item_code_1";
    public static final String TEST_CODE_NAME_1 = "테스트_상세 코드_1";

    //
    public static final String TEST_DC = "테스트_설명";
    public static final String TEST_DC_1 = "테스트_설명_1";
}
