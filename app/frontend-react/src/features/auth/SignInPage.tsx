import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { Modal } from "bootstrap";
import axios from "axios";
import { useNavigate, useSearchParams } from "react-router-dom";

import { useAuthStore } from "@/shared/auth/authStore";
import { http } from "@/shared/api/http";
import { swalConfirm } from "@/shared/utils/swal";

const DOMAIN = "dreamdiary.nicheblog.io";

function splitErrorMsg(errorMsg: string): string[] {
  return errorMsg
    .split(/(?:&lt;br\s*\/?&gt;|<br\s*\/?>)/gi)
    .map((line) => line.trim())
    .filter(Boolean);
}

/**
 * 로그인 화면.
 * Vue {@code features/auth/SignIn.vue} DOM/class/동작 1:1 대응.
 */
export function SignInPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const login = useAuthStore((state) => state.login);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<{ username?: string; password?: string }>({});
  const [errorMsgLines, setErrorMsgLines] = useState<string[]>([]);
  const [sessionExpiredNotice, setSessionExpiredNotice] = useState("");

  const passwordChangeModalEl = useRef<HTMLDivElement | null>(null);
  const passwordChangeModalRef = useRef<Modal | null>(null);
  const [passwordChangeUsername, setPasswordChangeUsername] = useState("");
  const [passwordChangeToken, setPasswordChangeToken] = useState("");
  const [isPasswordChanging, setIsPasswordChanging] = useState(false);
  const [passwordChangeForm, setPasswordChangeForm] = useState({
    currPw: "",
    newPw: "",
    newPwCf: "",
  });
  const [passwordChangeErrors, setPasswordChangeErrors] = useState({
    currPw: "",
    newPw: "",
    newPwCf: "",
  });

  const redirectTarget = searchParams.get("redirect") ?? "";

  useEffect(() => {
    if (passwordChangeModalEl.current) {
      passwordChangeModalRef.current = new Modal(passwordChangeModalEl.current, {
        backdrop: "static",
        keyboard: false,
      });
    }
    return () => {
      passwordChangeModalRef.current?.dispose();
      passwordChangeModalRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (searchParams.get("dupLoginAt") === "Y") {
      setErrorMsgLines(["중복 로그인으로 인해 로그아웃되었습니다."]);
    } else if (searchParams.get("sessionExpired") === "Y") {
      setSessionExpiredNotice(
        "세션이 만료되었거나 다른 곳에서 로그인되어 현재 로그인이 해제되었습니다. 다시 로그인해주세요.",
      );
    } else if (searchParams.get("oauthError")) {
      setErrorMsgLines([String(searchParams.get("oauthError"))]);
    }
  }, [searchParams]);

  const setLoginErrorLines = (messages: string[]) => {
    setErrorMsgLines(messages.flatMap((message) => splitErrorMsg(message)));
  };

  const openOAuthPopup = (url: string) => {
    const popup = window.open(url, "Authorization", "width=540,height=720,top=0,left=270");
    if (popup) popup.focus();
  };

  const goUserSignup = () => {
    navigate("/user/signup");
  };

  const resetPasswordChangeForm = () => {
    setPasswordChangeForm({ currPw: "", newPw: "", newPwCf: "" });
    setPasswordChangeErrors({ currPw: "", newPw: "", newPwCf: "" });
  };

  const openPasswordChangeModal = (targetUsername: string) => {
    const action = useAuthStore.getState().loginAction;
    setPasswordChangeUsername(targetUsername);
    setUsername(targetUsername);
    setPasswordChangeToken(action?.passwordToken ?? "");
    console.info("[SignIn] password change required after login failure.", {
      username: targetUsername,
      isCredentialExpired: action?.isCredentialExpired === true,
      needsPasswordReset: action?.needsPasswordReset === true,
    });
    resetPasswordChangeForm();
    passwordChangeModalRef.current?.show();
  };

  const closePasswordChangeModal = () => {
    passwordChangeModalRef.current?.hide();
  };

  const validatePasswordChangeForm = (): boolean => {
    const nextErrors = { currPw: "", newPw: "", newPwCf: "" };
    if (!passwordChangeForm.currPw) nextErrors.currPw = "필수 값을 입력하세요.";
    if (!passwordChangeForm.newPw) nextErrors.newPw = "필수 값을 입력하세요.";
    if (!passwordChangeForm.newPwCf) nextErrors.newPwCf = "필수 값을 입력하세요.";
    setPasswordChangeErrors(nextErrors);
    if (nextErrors.currPw || nextErrors.newPw || nextErrors.newPwCf) return false;
    if (passwordChangeForm.newPw !== passwordChangeForm.newPwCf) {
      setPasswordChangeErrors({ ...nextErrors, newPwCf: "새 비밀번호 확인 값이 일치하지 않습니다." });
      return false;
    }
    if (passwordChangeForm.newPw.length < 9 || passwordChangeForm.newPw.length > 15) {
      setPasswordChangeErrors({ ...nextErrors, newPw: "새 비밀번호는 9자 이상 15자 이하로 입력하세요." });
      return false;
    }
    if (
      !/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[$~@$!%*#?&_!])[a-zA-Z\d$~@$!%*#?&_!]{9,15}$/.test(
        passwordChangeForm.newPw,
      )
    ) {
      setPasswordChangeErrors({ ...nextErrors, newPw: "변경할 비밀번호가 형식에 맞지 않습니다." });
      return false;
    }
    return true;
  };

  const submitPasswordChange = async () => {
    if (!validatePasswordChangeForm()) return;
    setIsPasswordChanging(true);
    try {
      const fd = new FormData();
      fd.append("username", passwordChangeUsername);
      fd.append("currPw", passwordChangeForm.currPw);
      fd.append("newPw", passwordChangeForm.newPw);
      if (passwordChangeToken) fd.append("passwordToken", passwordChangeToken);
      const res = await http.post("/api/auth/login-pw-chg", fd);
      if (!res.data?.rslt) {
        console.warn("[SignIn] password change rejected by server.", { username: passwordChangeUsername });
        setErrorMsgLines([res.data?.message ?? "비밀번호를 변경하지 못했습니다."]);
        return;
      }
      closePasswordChangeModal();
      setErrorMsgLines(["비밀번호가 변경되었습니다. 다시 로그인해주세요."]);
      setPassword("");
      resetPasswordChangeForm();
    } catch (error) {
      if (axios.isAxiosError<{ message?: string }>(error)) {
        console.warn("[SignIn] password change request failed.", {
          username: passwordChangeUsername,
          status: error.response?.status,
        });
        setErrorMsgLines([error.response?.data?.message ?? "비밀번호를 변경하지 못했습니다."]);
      } else {
        console.warn("[SignIn] password change request failed with unknown error.", error);
        setErrorMsgLines(["비밀번호를 변경하지 못했습니다."]);
      }
    } finally {
      setIsPasswordChanging(false);
    }
  };

  const confirmDuplicateLoginAndRetry = async () => {
    const confirmed = await swalConfirm("이미 로그인된 세션이 있습니다. 기존 세션을 끊고 로그인할까요?");
    if (!confirmed) {
      await http.post("/api/auth/expire-session");
      setPassword("");
      console.info("[SignIn] duplicate-login confirmation canceled.", { username });
      return;
    }
    console.info("[SignIn] duplicate-login confirmation accepted.", { username });
    try {
      await login({ username, password });
      navigate(redirectTarget || "/journal/weekly", { replace: true });
    } catch {
      const msgs = useAuthStore.getState().errors.length > 0
        ? useAuthStore.getState().errors
        : ["로그인에 실패했습니다."];
      setLoginErrorLines(msgs);
    }
  };

  const handleLogin = async (event: FormEvent) => {
    event.preventDefault();
    setFieldErrors({});
    setErrorMsgLines([]);
    setIsLoading(true);
    try {
      await login({ username, password });
      navigate(redirectTarget || "/journal/weekly", { replace: true });
    } catch {
      const store = useAuthStore.getState();
      const msgs = store.errors.length > 0 ? store.errors : ["로그인에 실패했습니다."];
      setLoginErrorLines(msgs);
      const action = store.loginAction;
      if (action?.isDupIdLogin) {
        await confirmDuplicateLoginAndRetry();
      } else if (action?.isCredentialExpired || action?.needsPasswordReset) {
        openPasswordChangeModal(action.username);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const displayErrors = useMemo(() => errorMsgLines, [errorMsgLines]);

  return (
    <>
      <div className="w-lg-500px rounded mt-20 p-10 p-lg-15 mx-auto my-auto">
        <div className="d-flex justify-content-center fs-4 mb-15 text-secondary fw-bold ls-1">
          <i className="bi bi-cloud-moon fs-2"></i>
          <span className="px-2">{DOMAIN}</span>
          <i className="bi bi-stars fs-2"></i>
        </div>

        <form name="loginForm" id="loginForm" className="w-100" onSubmit={handleLogin}>
          <div className="opacity-75">
            <label className="form-label fs-6 text-secondary fw-bolder" htmlFor="username">
              아이디
            </label>
            <input
              type="text"
              name="username"
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value.trim())}
              className="form-control form-control-lg"
              placeholder="아이디를 입력하세요."
              autoComplete="off"
              maxLength={20}
            />
          </div>
          <div className="h-15px mt-1 mb-5">
            <span id="username_validate_span" className="text-danger">
              {fieldErrors.username}
            </span>
          </div>

          <div className="opacity-75">
            <label className="form-label text-secondary fw-bolder fs-6 mb-1" htmlFor="password">
              비밀번호
            </label>
            <input
              type="password"
              name="password"
              id="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="form-control form-control-lg"
              placeholder="비밀번호를 입력하세요."
              maxLength={20}
              autoComplete="off"
            />
          </div>
          <div className="h-15px mt-1 mb-3">
            <span id="password_validate_span" className="text-danger">
              {fieldErrors.password}
            </span>
          </div>

          {sessionExpiredNotice ? (
            <div className="alert alert-warning d-flex align-items-start gap-3 py-3 mb-5">
              <i className="bi bi-exclamation-triangle fs-3"></i>
              <div>
                <div className="fw-bold">로그인이 풀렸습니다.</div>
                <div className="fs-7">{sessionExpiredNotice}</div>
              </div>
            </div>
          ) : null}

          <div className="text-left">
            <span id="errorMsgSpan" className="text-danger">
              {displayErrors.map((line, index) => (
                <span key={`${line}-${index}`}>
                  {line}
                  {index < displayErrors.length - 1 ? <br /> : null}
                </span>
              ))}
            </span>
          </div>

          <div className="me-2 float-end">
            <label className="form-check form-check-custom form-check-solid" htmlFor="rememberMe">
              <span className="form-check-label text-secondary me-2">로그인 상태 유지</span>
              <input
                type="checkbox"
                className="form-check-input cursor-pointer"
                id="rememberMe"
                name="rememberMe"
                checked={rememberMe}
                onChange={(event) => setRememberMe(event.target.checked)}
              />
            </label>
          </div>

          <div className="d-flex flex-column text-center mb-4 mt-12 gap-2">
            <button
              type="submit"
              className="btn btn-lg btn-light-primary opacity-75 w-100"
              disabled={isLoading}
              aria-busy={isLoading}
            >
              <span className="d-inline-flex align-items-center justify-content-center gap-2 w-100">
                <span>로그인</span>
                {isLoading ? (
                  <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                ) : null}
              </span>
            </button>

            <div className="d-flex gap-2">
              <button
                type="button"
                className="btn btn-lg btn-light-danger opacity-75 w-100"
                onClick={() => openOAuthPopup("/oauth2/authorization/google")}
              >
                <span className="d-flex-center indicator-label gap-2">
                  <i className="bi bi-google blink"></i>
                  <span>Google 계정</span>
                </span>
              </button>
              <button
                type="button"
                className="btn btn-lg btn-light-success opacity-75 w-100"
                onClick={() => openOAuthPopup("/oauth2/authorization/naver")}
              >
                <span className="d-flex-center indicator-label gap-2">
                  <i className="bi bi-naver blink"></i>
                  <span>Naver 계정</span>
                </span>
              </button>
            </div>
          </div>

          <div className="d-flex justify-content-end mb-5">
            <button
              type="button"
              className="badge btn btn-sm btn-light-primary badge-outlined btn-outlined fw-light opacity-75 blink"
              onClick={goUserSignup}
            >
              <i className="bi bi-person-plus-fill blink"></i>사용자 계정 신청
            </button>
          </div>

          <div className="text-end mb-5 text-secondary fs-8">2024. nichefish. All rights reserved.</div>
        </form>
      </div>

      <div
        ref={passwordChangeModalEl}
        className="modal fade"
        id="login_pw_chg_modal"
        tabIndex={-1}
        role="dialog"
        aria-hidden="true"
        data-bs-keyboard="false"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-dialog-centered modal-md" role="document">
          <div className="modal-content">
            <div className="modal-header bg-dark">
              <h5 className="modal-title text-white">비밀번호 변경</h5>
              <button
                type="button"
                className="btn-close btn-close-white"
                aria-label="닫기"
                onClick={closePasswordChangeModal}
              ></button>
            </div>
            <div className="modal-body">
              <form name="loginPwChgForm" id="loginPwChgForm" className="form" onSubmit={(e) => e.preventDefault()}>
                <input type="hidden" name="username" id="loginUsername" value={passwordChangeUsername} readOnly />
                <div className="row">
                  <div className="col-xl-12 text-danger">
                    {displayErrors.map((line, index) => (
                      <span key={`modal-${line}-${index}`}>
                        {line}
                        {index < displayErrors.length - 1 ? <br /> : null}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="row mb-5">
                  <div className="col-xl-3">
                    <div className="col-form-label text-center fs-6 fw-bold">
                      <label htmlFor="currPw">현재 비밀번호</label>
                    </div>
                  </div>
                  <div className="col-xl-9 text-start">
                    <input
                      type="password"
                      name="currPw"
                      id="currPw"
                      className="form-control required"
                      maxLength={20}
                      value={passwordChangeForm.currPw}
                      onChange={(event) =>
                        setPasswordChangeForm((prev) => ({ ...prev, currPw: event.target.value }))
                      }
                    />
                    <div id="currPw_validate_span" className="text-danger">
                      {passwordChangeErrors.currPw}
                    </div>
                  </div>
                </div>
                <div className="row">
                  <div className="col-xl-3">
                    <div className="col-form-label text-center fs-6 fw-bold">
                      <label htmlFor="newPw">새 비밀번호</label>
                    </div>
                  </div>
                  <div className="col-xl-9 text-start">
                    <input
                      type="password"
                      name="newPw"
                      id="newPw"
                      className="form-control required"
                      maxLength={20}
                      value={passwordChangeForm.newPw}
                      onChange={(event) =>
                        setPasswordChangeForm((prev) => ({ ...prev, newPw: event.target.value }))
                      }
                    />
                    <div className="fs-8 form-text text-noti">영문, 숫자, 특수문자를 조합해 입력하세요.</div>
                    <div id="newPw_validate_span" className="text-danger">
                      {passwordChangeErrors.newPw}
                    </div>
                  </div>
                </div>
                <div className="row mb-5">
                  <div className="col-xl-3">
                    <div className="col-form-label text-center fs-6 fw-bold">
                      <label htmlFor="newPwCf">새 비밀번호 확인</label>
                    </div>
                  </div>
                  <div className="col-xl-9 text-start">
                    <input
                      type="password"
                      name="newPwCf"
                      id="newPwCf"
                      className="form-control required"
                      maxLength={20}
                      value={passwordChangeForm.newPwCf}
                      onChange={(event) =>
                        setPasswordChangeForm((prev) => ({ ...prev, newPwCf: event.target.value }))
                      }
                    />
                    <div id="newPwCf_validate_span" className="text-danger">
                      {passwordChangeErrors.newPwCf}
                    </div>
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-primary"
                disabled={isPasswordChanging}
                onClick={submitPasswordChange}
              >
                {isPasswordChanging ? <span className="spinner-border spinner-border-sm me-1"></span> : null}
                저장
              </button>
              <button type="button" className="btn btn-light" onClick={closePasswordChangeModal}>
                닫기
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
