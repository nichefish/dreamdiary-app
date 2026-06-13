import axios from "axios";

/**
 * DreamDiary React SPA HTTP client.
 * JWT HttpOnly cookie 인증을 위해 withCredentials 를 켠다.
 */
export const http = axios.create({
  withCredentials: true,
  headers: {
    Accept: "application/json",
  },
});
