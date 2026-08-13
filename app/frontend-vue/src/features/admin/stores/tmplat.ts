import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";

/** 템플릿 목록 행 (백엔드 TmplatDto 대응) */
export interface TmplatRow {
  rnum?: number;
  id: number;
  title: string;
  content?: string;
  sortOrder?: number;
  useYn: string;
}

/** 등록/수정 모달 폼 */
export interface TmplatForm {
  id: number | null;
  title: string;
  content: string;
  sortOrder: number;
  useYn: string;
}

/** 빈 폼을 새로 만든다. */
function emptyForm(): TmplatForm {
  return { id: null, title: "", content: "", sortOrder: 0, useYn: "Y" };
}

function yn(value: string | undefined): string {
  return String(value ?? "Y").toUpperCase() === "Y" ? "Y" : "N";
}

/** 서버 응답 행을 폼으로 정규화한다. */
function normalizeForm(row?: Partial<TmplatRow>): TmplatForm {
  return {
    id: row?.id ?? null,
    title: row?.title ?? "",
    content: row?.content ?? "",
    sortOrder: Number(row?.sortOrder ?? 0),
    useYn: yn(row?.useYn),
  };
}

/** 폼을 multipart FormData 로 직렬화한다 (Spring @ModelAttribute 바인딩). */
function toFormData(form: TmplatForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("title", form.title.trim());
  fd.append("content", form.content ?? "");
  fd.append("sortOrder", String(Number(form.sortOrder) || 0));
  fd.append("useYn", yn(form.useYn));
  return fd;
}

/**
 * 템플릿 관리(전역 공용) 스토어.
 * 평면 단일 목록 CRUD 로, 목록은 GET /api/tmplats(rsltList) 로 조회한다.
 */
export const useTmplatAdminStore = defineStore("tmplatAdmin", () => {
  const { t } = useLocaleStore();
  const rows = ref<TmplatRow[]>([]);
  const loading = ref(false);
  const error = ref("");

  const modalOpen = ref(false);
  const saving = ref(false);
  const form = ref<TmplatForm>(emptyForm());

  const isEdit = computed(() => form.value.id != null);

  /** 템플릿 목록 조회 (정렬순서 오름차순은 서버가 보장). */
  async function fetchList() {
    loading.value = true;
    error.value = "";
    try {
      const res = await axios.get("/api/tmplats");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("tmplat.list.load.failure"));
      rows.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e) {
      error.value = e instanceof Error ? e.message : t("tmplat.list.load.failure");
    } finally {
      loading.value = false;
    }
  }

  /** 등록 모달 열기. */
  async function openCreate() {
    if (!await assertAuthenticatedBeforeModal()) return;
    form.value = emptyForm();
    modalOpen.value = true;
  }

  /** 수정 모달 열기 (상세 조회 후 폼 채움). */
  async function openEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    const res = await axios.get(`/api/tmplat/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("tmplat.load.failure"));
    form.value = normalizeForm(res.data?.rsltObj ?? {});
    modalOpen.value = true;
  }

  function closeModal() {
    modalOpen.value = false;
    form.value = emptyForm();
  }

  /**
   * 템플릿 등록/수정 처리.
   * 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function submit() {
    saving.value = true;
    try {
      const id = form.value.id;
      const url = id != null ? `/api/tmplat/${id}` : "/api/tmplats";
      const res = await axios.post(url, toFormData(form.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("tmplat.save.failure"));
      closeModal();
      const message = res.data?.message ?? t("common.result.saved");
      await swalAlert(message);
      await fetchList();
      return message;
    } finally {
      saving.value = false;
    }
  }

  /**
   * 템플릿 삭제 처리 (soft-delete).
   * 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function remove(id: number) {
    const res = await axios.delete(`/api/tmplat/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("tmplat.delete.failure"));
    const message = res.data?.message ?? t("common.result.deleted");
    await swalAlert(message);
    await fetchList();
    return message;
  }

  return {
    rows,
    loading,
    error,
    modalOpen,
    saving,
    form,
    isEdit,
    fetchList,
    openCreate,
    openEdit,
    closeModal,
    submit,
    remove,
  };
});