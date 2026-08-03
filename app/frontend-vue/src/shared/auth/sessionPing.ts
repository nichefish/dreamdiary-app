import axios from "axios";
import { isAuthExpiredError } from "@/shared/utils/authError";

/**
 * Authenticated modal/action preflight.
 * Returns false only when the global 401 interceptor already handled session expiry.
 */
export async function assertAuthenticatedBeforeModal(): Promise<boolean> {
  try {
    await axios.get("/api/session/ping", { params: { _: Date.now() }, skipLoadingBar: true });
    return true;
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return false;
    throw e;
  }
}
