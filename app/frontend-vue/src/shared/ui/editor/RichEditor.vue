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
import { ref } from "vue";
import Editor from "@tinymce/tinymce-vue";
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
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  height: 540,
  placeholder: "내용을 입력하세요.",
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

/** 이미지 업로드용 hidden file input 레퍼런스 */
const imageFileInput = ref<HTMLInputElement | null>(null);

/** 글접기/펼치기 섹션 카운터 (에디터 인스턴스별 독립) */
let sectionCount = 0;

/** TinyMCE 초기화 옵션 (기존 tinymce.ts basicOptions 이식) */
const editorInit = {
  editor_encoding: "raw",
  height: props.height,
  menubar: false,
  branding: false,
  statusbar: false,
  promotion: false,
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
    "undo redo | searchreplace | styles fontfamily fontsize | bold italic underline strikethrough | forecolor backcolor | align | code codesample | help",
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
      tooltip: "Insert Image",
      onAction(): void {
        imageFileInput.value?.click();
      },
    });

    /** moreless 버튼: 글접기/펼치기 섹션 삽입 */
    editor.ui.registry.addButton("moreless", {
      icon: "vertical-align",
      tooltip: "Insert moreless section",
      onAction(): void {
        const sid = "tinymce_section_" + sectionCount;
        const cid = "tinymce_section_content_" + sectionCount;
        const tid = "tinymce_toggle_" + sectionCount;
        /** inline onclick: TinyMCE iframe DOM 에서 직접 토글 */
        const html =
          '<div class="tinymce-section" id="' + sid + '">' +
          '<span id="' + tid + '" class="tinymce-collapse-toggle"' +
          ' onclick="(function(c){var el=document.getElementById(c);if(el)el.classList.toggle(\'collapsed\');})(\''+cid+'\')">' +
          "Toggle Section " + sectionCount +
          "</span>" +
          '<div id="' + cid + '" class="tinymce-collapsed">Section content goes here.</div>' +
          "</div>";
        editor.execCommand("mceInsertContent", false, html);
        sectionCount++;
      },
    });
  },
};

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
    void swalAlert("이미지 파일 크기는 10MB 이하여야 합니다.");
    input.value = "";
    return;
  }

  try {
    const fd = new FormData();
    fd.append("file", file);
    const res = await fetch("/api/file/file-upload", {
      method: "POST",
      body: fd,
    });
    const data = await res.json();
    if (!data.rslt) {
      void swalAlert(data.message ?? "이미지 업로드에 실패했습니다.");
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
    void swalAlert("이미지 업로드 중 오류가 발생했습니다.");
  } finally {
    input.value = "";
  }
}
</script>
