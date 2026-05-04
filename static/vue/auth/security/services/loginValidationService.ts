/**
 * loginValidationService.ts
 * 로그인 화면의 입력 검증과 에러 메시지 생성을 담당합니다.
 *
 * @author nichefish
 */
export type LoginForm = {
    username: string;
    password: string;
    passwordDisabled: boolean;
};

export type PasswordChangeForm = {
    currPw: string;
    newPw: string;
    newPwCf: string;
};

export type LoginErrors = {
    username: string;
    password: string;
};

export type PasswordChangeErrors = {
    currPw: string;
    newPw: string;
    newPwCf: string;
};

type Translate = (key: string) => string;

function emptyLoginErrors(): LoginErrors {
    return {
        username: "",
        password: "",
    };
}

function emptyPasswordChangeErrors(): PasswordChangeErrors {
    return {
        currPw: "",
        newPw: "",
        newPwCf: "",
    };
}

function hasLoginErrors(errors: LoginErrors): boolean {
    return !!errors.username || !!errors.password;
}

function hasPasswordChangeErrors(errors: PasswordChangeErrors): boolean {
    return !!errors.currPw || !!errors.newPw || !!errors.newPwCf;
}

export default {
    emptyLoginErrors,
    emptyPasswordChangeErrors,
    validateLogin(form: LoginForm, t: Translate): LoginErrors {
        const errors = emptyLoginErrors();

        if (!form.username) errors.username = t("msg.auth.login.required");
        if (!form.password && !form.passwordDisabled) errors.password = t("msg.auth.login.required");

        return errors;
    },
    validatePasswordChange(form: PasswordChangeForm, passwordPattern: RegExp, t: Translate): PasswordChangeErrors {
        const errors = emptyPasswordChangeErrors();

        if (!form.currPw) errors.currPw = t("msg.auth.login.required");
        if (!form.newPw) {
            errors.newPw = t("msg.auth.login.required");
        } else if (!passwordPattern.test(form.newPw)) {
            errors.newPw = t("msg.auth.login.password-pattern");
        }

        if (!form.newPwCf) {
            errors.newPwCf = t("msg.auth.login.required");
        } else if (form.newPw !== form.newPwCf) {
            errors.newPwCf = t("msg.auth.login.password-mismatch");
        }

        return errors;
    },
    hasLoginErrors,
    hasPasswordChangeErrors,
};
