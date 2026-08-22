<template>
  <!--begin::리치 에디터 (TinyMCE 6 자체 호스팅)-->
  <div class="rich-editor-wrapper">
    <Editor
      :model-value="modelValue ?? ''"
      :init="editorInit"
      @update:model-value="emit('update:modelValue', $event)"
    />
    <!--begin::이미지 업로드 hidden file input-->
    <input
      ref="imageFileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageUpload"
    />
    <!--end::이미지 업로드 hidden file input-->
  </div>
  <!--end::리치 에디터-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { computed, ref } from "vue";
import Editor from "@tinymce/tinymce-vue";
import axios from "axios";
/** TinyMCE 6 자체 호스팅 - 번들러(Vite)를 통해 직접 임포트 */
import "tinymce/tinymce";
import "tinymce/themes/silver/theme";
import "tinymce/models/dom/model";
import "tinymce/icons/default/icons";
/** UI 스킨 CSS: Vite 번들에 포함되므로 tinymce 가 URL 로딩하지 않도록 skin:false 설정 */
import "tinymce/skins/ui/oxide/skin.min.css";
/** 플러그인 */
import "tinymce/plugins/help/plugin";
import "tinymce/plugins/help/js/i18n/keynav/en";
import "tinymce/plugins/help/js/i18n/keynav/ko_KR";
import "tinymce/plugins/quickbars/plugin";
import "tinymce/plugins/searchreplace/plugin";
import "tinymce/plugins/link/plugin";
import "tinymce/plugins/autolink/plugin";
import "tinymce/plugins/table/plugin";
import "tinymce/plugins/lists/plugin";
import "tinymce/plugins/advlist/plugin";
import "tinymce/plugins/emoticons/plugin";
import "tinymce/plugins/visualchars/plugin";
import "tinymce/plugins/visualblocks/plugin";
import "tinymce/plugins/pagebreak/plugin";
import "tinymce/plugins/code/plugin";
import "tinymce/plugins/codesample/plugin";

interface Props {
  /** 에디터 콘텐츠 (v-model). undefined 는 빈 문자열로 정규화. */
  modelValue?: string;
  /** 에디터 높이 (px). 기본값 540. */
  height?: number;
  placeholder?: string;
  /** 템플릿 삽입 드롭다운 노출 여부. 기본값 false. 저널 엔트리 작성 에디터에서만 켠다. */
  enableTemplates?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  height: 540,
  placeholder: undefined,
  enableTemplates: false,
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();
const { t } = useLocaleStore();

/** 이미지 업로드용 hidden file input 레퍼런스 */
const imageFileInput = ref<HTMLInputElement | null>(null);

/** 글접기/펼치기 섹션 카운터 (에디터 인스턴스별 독립) */
let sectionCount = 0;

/** TinyMCE 초기화 옵션 (기존 tinymce.ts basicOptions 이식). placeholder 는 locale 카탈로그 기본값 사용. */
const editorInit = computed(() => ({
  editor_encoding: "raw",
  height: props.height,
  placeholder: props.placeholder ?? t("rich-editor.placeholder"),
  menubar: false,
  branding: false,
  statusbar: false,
  promotion: false,
  /** 소스 코드·링크·표 등 TinyMCE 모달을 헤더 드래그로 이동한다. */
  draggable_modal: true,
  /** Vite 번들로 CSS 가 이미 주입되므로 skin URL 로딩 비활성화 */
  skin: false,
  /** iframe 내부 content CSS 는 브라우저 기본값 사용 */
  content_css: false,
  content_style:
    "body { padding: 12px 16px; box-sizing: border-box; } p { margin: 0 0 0.75rem; }",
  default_link_target: "_blank",
  convert_urls: false,
  plugins:
    "help quickbars searchreplace link autolink table lists advlist emoticons visualchars visualblocks pagebreak code codesample",
  toolbar1:
    `undo redo | ${props.enableTemplates ? "tmplat | " : ""}searchreplace | styles fontfamily fontsize | bold italic underline strikethrough | forecolor backcolor | align | code codesample | help`,
  toolbar2:
    "emoticons custom_image link | numlist bullist moreless | visualchars visualblocks pagebreak | table tabledelete | tableprops tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol",
  contextmenu: "link custom_image lists table",
  /** 붙여넣기 후처리: 허용 태그(볼드/이탤릭/목록/링크) 외 서식 제거 */
  paste_postprocess(_plugin: any, args: any): void {
    const allowed = new Set<string>(['strong', 'b', 'em', 'i', 'u', 's', 'del', 'br', 'p', 'a', 'ul', 'ol', 'li', 'code', 'blockquote']);
    const clean = (node: any): void => {
      const children: any[] = Array.from(node.childNodes);
      for (const child of children) {
        if (child.nodeType !== 1) continue;
        const tag: string = child.tagName.toLowerCase();
        if (allowed.has(tag)) {
          const attrs: Attr[] = Array.from(child.attributes);
          for (const attr of attrs) {
            if (tag === 'a' && attr.name === 'href') continue;
            child.removeAttribute(attr.name);
          }
          clean(child);
        } else {
          const parent: Node = child.parentNode;
          while (child.firstChild) {
            parent.insertBefore(child.firstChild, child);
          }
          parent.removeChild(child);
        }
      }
    };
    clean(args.node);
  },
  setup(editor: any): void {
    /** SaveContent 이벤트: HTML 엔티티 자동 이스케이핑 보정 */
    editor.on("SaveContent", (e: any) => {
      e.content = e.content
        .replace(/&#39/g, "&apos")
        .replace(/&amp;/g, "&");
    });

    /** custom_image 버튼: 이미지 파일 선택 후 서버 업로드 */
    editor.ui.registry.addButton("custom_image", {
      icon: "image",
      tooltip: t("rich-editor.image.insert.tooltip"),
      onAction(): void {
        imageFileInput.value?.click();
      },
    });

    /** moreless 버튼: 글접기/펼치기 섹션 삽입 */
    editor.ui.registry.addButton("moreless", {
      icon: "vertical-align",
      tooltip: t("rich-editor.moreless.insert.tooltip"),
      onAction(): void {
        const sid = "tinymce_section_" + sectionCount;
        const cid = "tinymce_section_content_" + sectionCount;
        const tid = "tinymce_toggle_" + sectionCount;
        /** inline onclick: TinyMCE iframe DOM 에서 직접 토글 */
        const html =
          '<div class="tinymce-section" id="' + sid + '">' +
          '<span id="' + tid + '" class="tinymce-collapse-toggle"' +
          ' onclick="(function(c){var el=document.getElementById(c);if(el)el.classList.toggle(\'collapsed\');})(\''+cid+'\')">' +
          t("rich-editor.moreless.toggle-label").replace("{0}", String(sectionCount)) +
          "</span>" +
          '<div id="' + cid + '" class="tinymce-collapsed">' +
          t("rich-editor.moreless.content-placeholder") +
          "</div>" +
          "</div>";
        editor.execCommand("mceInsertContent", false, html);
        sectionCount++;
      },
    });

    /** tmplat 메뉴버튼: 활성(useYn=Y) 템플릿을 드롭다운으로 노출하고 선택 시 커서 위치에 삽입한다. (enableTemplates 시에만 등록) */
    if (props.enableTemplates) {
      editor.ui.registry.addMenuButton("tmplat", {
        text: t("rich-editor.tmplat.button"),
        tooltip: t("rich-editor.tmplat.tooltip"),
        fetch(callback: (items: any[]) => void): void {
          void loadActiveTemplates()
            .then((list: ActiveTmplat[]) => {
              if (!list.length) {
                callback([{ type: "menuitem", text: t("rich-editor.tmplat.empty"), enabled: false, onAction(): void {} }]);
                return;
              }
              callback(
                list.map((tpl) => ({
                  type: "menuitem",
                  text: tpl.title,
                  onAction(): void {
                    editor.execCommand("mceInsertContent", false, tpl.content ?? "");
                  },
                }))
              );
            })
            .catch(() => {
              callback([{ type: "menuitem", text: t("rich-editor.tmplat.empty"), enabled: false, onAction(): void {} }]);
            });
        },
      });
    }
  },
}));

/**
 * 이미지 파일 선택 후 서버에 업로드하고 에디터에 img 태그를 삽입한다.
 * @param event - file input change 이벤트
 */
async function handleImageUpload(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  const MAX_SIZE_BYTES = 10 * 1024 * 1024;
  if (file.size > MAX_SIZE_BYTES) {
    void swalAlert(t("rich-editor.image.size-limit"));
    input.value = "";
    return;
  }

  try {
    const fd = new FormData();
    fd.append("file", file);
    const res = await fetch("/api/file/file-upload", {
      method: "POST",
      headers: { "Accept-Language": window.localStorage.getItem("dreamdiary_locale") || "ko" },
      body: fd,
    });
    const data = await res.json();
    if (!data.rslt) {
      void swalAlert(data.message ?? t("rich-editor.image.upload.failure"));
      return;
    }
    const fileInfo = data.rsltObj;
    const imgTag =
      '<img src="' + fileInfo.url + '"' +
      ' data-mce-src="' + fileInfo.url + '"' +
      ' data-originalFileName="' + fileInfo.orgnFileNm + '" />';
    // @ts-ignore - window.tinymce 는 tinymce 임포트로 전역 주입됨
    (window as any).tinymce?.activeEditor?.execCommand(
      "mceInsertContent",
      true,
      imgTag
    );
  } catch {
    void swalAlert(t("rich-editor.image.upload.error"));
  } finally {
    input.value = "";
  }
}

/** 활성 템플릿 한 건 (백엔드 TmplatDto 부분집합) */
interface ActiveTmplat {
  title: string;
  content?: string;
}

/**
 * 템플릿 관리(전역 공용)에서 활성(useYn=Y) 템플릿 목록을 조회한다.
 * 실패 시 빈 배열을 반환해 드롭다운을 "템플릿 없음"으로 처리한다.
 *
 * @return 활성 템플릿 목록 (정렬순서 오름차순은 서버가 보장)
 */
async function loadActiveTemplates(): Promise<ActiveTmplat[]> {
  const res = await axios.get("/api/tmplats/active");
  if (!res.data?.rslt) return [];
  return Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
}
</script>
