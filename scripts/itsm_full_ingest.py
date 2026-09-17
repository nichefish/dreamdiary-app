#!/usr/bin/env python3
"""ITSM 세 원장을 읽기 전용으로 로컬 미러에 적재한다."""

from __future__ import annotations

import argparse
import getpass
import hashlib
import html
import http.cookiejar
import json
import logging
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from html.parser import HTMLParser
from pathlib import Path


BASE_URL = "https://itsm.idstrust.com/"
IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif", ".webp"}
EXCLUDED_SYSTEMS = {
    "힐리언스 시스템", "힐리언스 시스템(HMS)", "퇴직자 관리", "출입권한 관리",
    "개별 설비(생산팀)", "개별 설비(QC팀)", "개별 설비(공무팀, 대웅이엔지)",
    "개별 설비(생제생산팀)", "개별 설비(포장팀, 팜팩)",
    "TEST 시스템", "TEST 02", "test",
    "BC(SAP계정초기화) - 자동화 처리", "PC/OA", "PC서비스", "PC/OA for Indonesia(한국 X)",
    "서버 / 네트워크", "서버 / 네트워크(공장)", "NAC(네트워크 접근제어)", "ITSM",
    "Salesforce CRM 계정/권한요청(신규/변경)", "Salesforce CRM 계정 생성/유지",
    "Salesforce CRM 권한요청(권한 신규/변경)", "[TEST]DOCUpro", "[TEST]LIMSpro", "OS/DB 권한 요청",
    "BP마스터", "자재마스터", "GL계정",
    "MYHR", "myhr", "인사평가", "HR", "HR (인사 관리)", "E-HR(EP)", "인사",
    "스마트워크", "우리사주 시스템", "자원예약관리", "법인카드", "홈페이지", "주요과제관리",
    "공수관리시스템", "디자인센터", "IT쉐어드", "본사시스템",
    "BearDoc(한국 X)", "DLP(Gradius) for Indonesia(한국 X)", "Unknown(Etc.)",
    "Unknown(Etc.)(한국 X)", "문둑 리조트 신축 프로젝트", "대웅제약 향남공장 A동",
    "오송IT_기타", "향남IT_기타", "LIMS(SampleManager) 설치요청",
    "D2", "D2(EPM)", "문서중앙화(베어독)", "BearDoc", "베어독",
}
SOURCES = {
    "csr": ("CSR조회", "itsmSearchList.dmn", "itsmSearchList_docView.dmn"),
    "trouble": ("장애처리조회", "troubleSearchList.dmn", "troubleSearchList_docView.dmn"),
    "issue": ("이슈처리조회", "issueSearchList.dmn", "issueSearchList_docView.dmn"),
}
BC_SYSTEMS = {"BC", "BC (시스템 관리/권한 관리)", "BC(시스템 관리/권한 관리)"}
BC_PASSWORD_PATTERN = re.compile(r"비밀번호|패스워드|password|p\s*/?\s*w|\bpw\b|비번|암호|계정\s*잠금\s*해제", re.I)
BC_PASSWORD_KEEP_PATTERN = re.compile(r"정책|parameter|메모리|증설|리스트|추출|현황|분석|감사", re.I)
BC_ACCOUNT_PATTERN = re.compile(r"퇴사자|신규\s*입사|신규입사|계정.{0,12}(생성|신규|발급|삭제|중지|폐기|효력종료|비활성)|아이디.{0,12}(생성|신규|발급|삭제|중지|폐기)", re.I)
BC_TECHNICAL_ACCOUNT_PATTERN = re.compile(r"서비스\s*계정|배치|interface|i/f|rfc|시스템\s*계정|통신\s*계정|연계\s*계정", re.I)
BC_PERMISSION_PATTERN = re.compile(r"(권한|role|롤\s*코드).{0,20}(신청|요청|부여|해제|삭제|회수|변경|취소)|(신청|요청|부여|해제|삭제|회수|변경|취소).{0,20}(권한|role|롤\s*코드)", re.I)
BC_TECHNICAL_PERMISSION_PATTERN = re.compile(r"디버깅|개발|품질\s*서버|프로그램|t-?code|티코드|cds|interface|i/f|프로젝트|cts|이관|릴리즈", re.I)
GLOBAL_PASSWORD_PATTERN = re.compile(r"비밀번호|패스워드|비번|password|계정\s*잠금\s*해제|로그인\s*잠금|잠금\s*해제", re.I)
GLOBAL_ACCOUNT_PATTERN = re.compile(r"(계정|아이디|ID).{0,18}(신규|생성|발급|등록|삭제|폐기|중지|비활성|효력\s*종료)|(신규|퇴사자|입사자).{0,18}(계정|아이디|ID)", re.I)
GLOBAL_INSTALL_PATTERN = re.compile(r"(프로그램|클라이언트|sample\s*manager|analysis\s*office|SAP|LIMS|WMS).{0,18}(설치\s*요청|설치\s*문의)|(설치\s*요청|설치\s*문의).{0,18}(프로그램|클라이언트|sample\s*manager|analysis\s*office|SAP|LIMS|WMS)|설치\s*(요청|문의|부탁)|재설치|설치가?\s*(안|불가|필요)", re.I)
GLOBAL_TECHNICAL_KEEP_PATTERN = re.compile(r"정책|parameter|메모리|증설|감사|서비스\s*계정|배치|interface|i/f|rfc|통신\s*계정|연계\s*계정|디버깅|개발|품질\s*서버|프로그램\s*(수정|개선|개발)|t-?code|cds|프로젝트|cts|이관|릴리즈|로직|오류|에러|장애", re.I)
GLOBAL_PERMISSION_PATTERN = re.compile(r"(권한|role|롤\s*코드).{0,24}(신청|요청|부여|추가|해제|삭제|회수|변경|취소|등록)|(신청|요청|부여|추가|해제|삭제|회수|변경|취소|등록).{0,24}(권한|role|롤\s*코드)", re.I)
GLOBAL_PERMISSION_KEEP_PATTERN = re.compile(r"정책|감사|서비스\s*계정|배치|interface|i/f|rfc|디버깅|개발|프로그램|t-?code|티코드|cds|프로젝트|cts|이관|릴리즈|로직|오류|에러|장애|매핑|맵핑|이력|현황|조회|추출|분리|설계|체계|프로세스|원인|조치|메뉴.{0,12}(추가|개선|변경)|기능.{0,12}(추가|개선|변경)", re.I)
GLOBAL_PERIODIC_AUDIT_PATTERN = re.compile(r"(ITGC|분기|반기|연간|정기).{0,30}(계정|권한).{0,30}(모니터링|점검|추출|자료)|(계정|권한).{0,30}(모니터링|점검).{0,30}(추출|자료)", re.I)
LOG = logging.getLogger("itsm-ingest")


def now() -> str:
    return datetime.now(timezone(timedelta(hours=9))).isoformat(timespec="seconds")


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def atomic_write(path: Path, data: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".part")
    temporary.write_bytes(data)
    temporary.replace(path)


def write_json(path: Path, value: object) -> None:
    atomic_write(path, (json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode("utf-8"))


def safe_name(value: str, limit: int = 110) -> str:
    value = re.sub(r"[<>:\"/\\|?*\x00-\x1f]", "_", value).strip(" .")
    return (value[:limit].rstrip(" .") or "제목없음")


def safe_system_name(value: str) -> str:
    """원격 시스템명을 보존하면서 Windows 폴더 금지문자만 전각 문자로 치환한다."""
    translations = str.maketrans({'<': '＜', '>': '＞', ':': '：', '"': '＂', '/': '／',
                                  '\\': '＼', '|': '｜', '?': '？', '*': '＊'})
    safe = value.translate(translations).strip(" .")
    return safe or "_시스템미지정"


def exclusion_reason(item: dict[str, object]) -> str | None:
    """시스템 전체 제외와 BC 반복 운영 요청 제외 사유를 반환한다."""
    system = str(item.get("system", ""))
    if system in EXCLUDED_SYSTEMS:
        return "excluded_system"
    title = str(item.get("title", ""))
    if GLOBAL_PERIODIC_AUDIT_PATTERN.search(title):
        return "global_periodic_audit_extract"
    if not GLOBAL_TECHNICAL_KEEP_PATTERN.search(title):
        if GLOBAL_PASSWORD_PATTERN.search(title):
            return "global_password"
        if GLOBAL_ACCOUNT_PATTERN.search(title):
            return "global_human_account"
        if GLOBAL_INSTALL_PATTERN.search(title):
            return "global_client_install"
    if GLOBAL_PERMISSION_PATTERN.search(title):
        if GLOBAL_PERMISSION_KEEP_PATTERN.search(title):
            return None
        return "global_simple_permission"
    if system not in BC_SYSTEMS:
        return None
    if BC_PASSWORD_PATTERN.search(title) and not BC_PASSWORD_KEEP_PATTERN.search(title):
        return "bc_password"
    if BC_ACCOUNT_PATTERN.search(title) and not BC_TECHNICAL_ACCOUNT_PATTERN.search(title):
        return "bc_human_account"
    if BC_PERMISSION_PATTERN.search(title) and not BC_TECHNICAL_PERMISSION_PATTERN.search(title):
        return "bc_simple_permission"
    return None


class TreeParser(HTMLParser):
    """목록 행, 링크와 본문 텍스트를 손실 없이 추출한다."""

    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.rows: list[list[dict[str, object]]] = []
        self.links: list[dict[str, str]] = []
        self._row: list[dict[str, object]] | None = None
        self._cell: dict[str, object] | None = None
        self._link: dict[str, str] | None = None
        self.text_parts: list[str] = []
        self.section_parts: dict[str, list[str]] = {}
        self.section_titles: dict[str, list[str]] = {}
        self.section_rows: dict[str, list[list[dict[str, object]]]] = {}
        self._section_id: str | None = None
        self._title_section_id: str | None = None
        self._row_section_id: str | None = None
        self._ignored_depth = 0
        self._content_div_depth = 0

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        values = {key: value or "" for key, value in attrs}
        if tag in {"script", "style", "noscript", "template"}:
            self._ignored_depth += 1
            return
        if self._ignored_depth:
            return
        if tag == "div":
            if values.get("id") == "container":
                self._content_div_depth = 1
            elif self._content_div_depth:
                self._content_div_depth += 1
        if tag == "dd" and values.get("id", "").startswith("ddContent_"):
            self._section_id = values["id"]
            self.section_parts.setdefault(self._section_id, [])
        elif tag == "dt" and values.get("id", "").startswith("dtContent_"):
            self._title_section_id = values["id"].replace("dtContent_", "ddContent_", 1)
            self.section_titles.setdefault(self._title_section_id, [])
        if tag == "tr":
            self._row = []
            self._row_section_id = self._section_id
        elif tag in {"td", "th"} and self._row is not None:
            self._cell = {"text": [], "links": []}
        elif tag == "a":
            self._link = {"href": values.get("href", ""), "text": ""}
        elif tag in {"br", "p", "div", "li", "tr"}:
            self.text_parts.append("\n")

    def handle_data(self, data: str) -> None:
        if self._ignored_depth:
            return
        if self._content_div_depth:
            self.text_parts.append(data)
        if self._section_id is not None:
            self.section_parts[self._section_id].append(data)
        if self._title_section_id is not None:
            self.section_titles[self._title_section_id].append(data)
        if self._cell is not None:
            self._cell["text"].append(data)
        if self._link is not None:
            self._link["text"] += data

    def handle_endtag(self, tag: str) -> None:
        if tag in {"script", "style", "noscript", "template"}:
            if self._ignored_depth:
                self._ignored_depth -= 1
            return
        if self._ignored_depth:
            return
        if tag == "a" and self._link is not None:
            self._link["text"] = clean_text(self._link["text"])
            self.links.append(self._link)
            if self._cell is not None:
                self._cell["links"].append(self._link)
            self._link = None
        elif tag in {"td", "th"} and self._cell is not None and self._row is not None:
            self._cell["text"] = clean_text(" ".join(self._cell["text"]))
            self._row.append(self._cell)
            self._cell = None
        elif tag == "tr" and self._row is not None:
            if self._row:
                self.rows.append(self._row)
                if self._row_section_id is not None:
                    self.section_rows.setdefault(self._row_section_id, []).append(self._row)
            self._row = None
            self._row_section_id = None
            self.text_parts.append("\n")
        elif tag == "dd" and self._section_id is not None:
            self._section_id = None
        elif tag == "dt" and self._title_section_id is not None:
            self._title_section_id = None
        if tag == "div" and self._content_div_depth:
            self._content_div_depth -= 1


def clean_text(value: str) -> str:
    lines = [re.sub(r"[\t\r ]+", " ", line).strip() for line in html.unescape(value).splitlines()]
    return "\n".join(line for line in lines if line)


def parse_html(data: bytes) -> TreeParser:
    parser = TreeParser()
    parser.feed(data.decode("utf-8", errors="replace"))
    return parser


@dataclass
class Client:
    opener: urllib.request.OpenerDirector

    @classmethod
    def authenticate(cls, sso_url: str) -> "Client":
        parsed = urllib.parse.urlparse(sso_url)
        if parsed.scheme != "https" or parsed.hostname != "itsm.idstrust.com" or parsed.path != "/newMemberLoginSSOchk.dmn":
            raise ValueError("허용된 ITSM SSO URL이 아닙니다.")
        jar = http.cookiejar.CookieJar()
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
        request = urllib.request.Request(sso_url, headers={"User-Agent": "Mozilla/5.0"})
        with opener.open(request, timeout=60) as response:
            response.read()
            final_host = urllib.parse.urlparse(response.geturl()).hostname
        if final_host != "itsm.idstrust.com":
            raise RuntimeError("ITSM 인증이 허용된 호스트 밖으로 이동했습니다.")
        LOG.info("인증 확인 완료")
        return cls(opener)

    def request(self, path: str, form: dict[str, str] | None = None, attempts: int = 4) -> bytes:
        url = urllib.parse.urljoin(BASE_URL, path)
        body = urllib.parse.urlencode(form).encode() if form is not None else None
        for attempt in range(1, attempts + 1):
            try:
                request = urllib.request.Request(url, data=body, headers={"User-Agent": "Mozilla/5.0"})
                with self.opener.open(request, timeout=90) as response:
                    data = response.read()
                    if urllib.parse.urlparse(response.geturl()).hostname != "itsm.idstrust.com":
                        raise RuntimeError("응답이 허용된 호스트 밖으로 이동했습니다.")
                    return data
            except (urllib.error.URLError, TimeoutError) as error:
                LOG.warning("조회 재시도 path=%s attempt=%d error=%s", path, attempt, type(error).__name__)
                if attempt == attempts:
                    raise
                time.sleep(min(8, 2 ** attempt))
        raise AssertionError("unreachable")


def total_count(parser: TreeParser) -> int:
    match = re.search(r"([\d,]+)건", clean_text(" ".join(parser.text_parts)))
    return int(match.group(1).replace(",", "")) if match else 0


def list_items(parser: TreeParser, detail_name: str, page: int) -> list[dict[str, object]]:
    items: list[dict[str, object]] = []
    for row in parser.rows:
        detail = next((link for cell in row for link in cell["links"] if detail_name in link["href"]), None)
        if detail is None:
            continue
        cells = [str(cell["text"]) for cell in row]
        match = re.search(r"docno=(\d+)", detail["href"])
        if not match:
            continue
        number = next((value for value in cells if re.fullmatch(r"\d{8}", value)), "")
        items.append({
            "sourceType": "", "displayNumber": number, "docno": match.group(1),
            "company": cells[0] if len(cells) > 0 else "", "system": cells[1] if len(cells) > 1 else "",
            "processType": cells[3] if len(cells) > 3 else "", "processStage": cells[4] if len(cells) > 4 else "",
            "title": detail["text"], "requester": cells[6] if len(cells) > 6 else "",
            "requestedAt": cells[7] if len(cells) > 7 else "", "completionAt": cells[8] if len(cells) > 8 else "",
            "assignee": cells[9] if len(cells) > 9 else "", "status": cells[10] if len(cells) > 10 else "",
            "detailUrl": urllib.parse.urljoin(BASE_URL, detail["href"]), "page": page,
        })
    return items


def collect_baseline(client: Client, source: str, start: str, end: str, output: Path) -> dict[str, object]:
    label, list_path, detail_name = SOURCES[source]
    form = {"frdate": start, "todate": end, "searchType": "sstitle", "searchKeyword": "", "page": "1", "displayRowCount": "50"}
    first = parse_html(client.request(list_path, form))
    total = total_count(first)
    pages = (total + 49) // 50
    checkpoint_path = output / "checkpoint.json"
    if checkpoint_path.exists():
        saved = json.loads(checkpoint_path.read_text(encoding="utf-8"))
    else:
        saved = {}
    saved_baseline = saved.get("baseline", {})
    completed_page_scan = (saved.get("status") == "baseline_collecting"
                           and int(saved.get("nextPage", 1)) > int(saved_baseline.get("pageCount", 0)))
    resumable = (saved.get("status") == "baseline_collecting"
                 and (saved_baseline.get("displayedTotal") == total or completed_page_scan)
                 and saved.get("baseline", {}).get("startDate") == start
                 and saved.get("baseline", {}).get("endDate") == end)
    rows: list[dict[str, object]] = list(saved["baseline"]["items"]) if resumable else []
    page_counts: list[int] = list(saved["baseline"]["pageCounts"]) if resumable else []
    next_page = int(saved.get("nextPage", 1)) if resumable else 1
    if resumable:
        total = int(saved_baseline["displayedTotal"])
        pages = int(saved_baseline["pageCount"])
        LOG.info("기준선 재개 source=%s nextPage=%d rows=%d", source, next_page, len(rows))
    for page in range(next_page, pages + 1):
        parser = first if page == 1 else parse_html(client.request(list_path, {**form, "page": str(page)}))
        current = list_items(parser, detail_name, page)
        for item in current:
            item["sourceType"] = source
        rows.extend(current)
        page_counts.append(len(current))
        if page == 1 or page % 25 == 0 or page == pages:
            partial = {"source": source, "listName": label, "sourceUrl": urllib.parse.urljoin(BASE_URL, list_path),
                       "startDate": start, "endDate": end, "displayedTotal": total, "pageCount": pages,
                       "pageCounts": page_counts, "collectedAt": now(), "items": rows}
            write_json(checkpoint_path, {"status": "baseline_collecting", "baseline": partial,
                                        "nextPage": page + 1, "updatedAt": now()})
            LOG.info("기준선 source=%s page=%d/%d rows=%d", source, page, pages, len(rows))
    unique_rows: list[dict[str, object]] = []
    by_docno: dict[str, dict[str, object]] = {}
    pagination_duplicates: list[dict[str, object]] = []
    for item in rows:
        docno = str(item["docno"])
        previous = by_docno.get(docno)
        if previous is None:
            by_docno[docno] = item
            unique_rows.append(item)
            continue
        comparable_previous = {key: value for key, value in previous.items() if key != "page"}
        comparable_current = {key: value for key, value in item.items() if key != "page"}
        if comparable_previous != comparable_current:
            raise RuntimeError(f"{label} 식별자 충돌 docno={docno} pages={previous['page']},{item['page']}")
        pagination_duplicates.append({"docno": docno, "pages": [previous["page"], item["page"]]})
    rows = unique_rows
    if len(rows) != total:
        raise RuntimeError(f"{label} 기준선 불일치 total={total} unique={len(rows)}")
    excluded_items = [item for item in rows if exclusion_reason(item)]
    excluded_reason_counts: dict[str, int] = {}
    for item in excluded_items:
        reason = exclusion_reason(item)
        if reason:
            excluded_reason_counts[reason] = excluded_reason_counts.get(reason, 0) + 1
    rows = [item for item in rows if not exclusion_reason(item)]
    if excluded_items:
        LOG.info("제외 시스템 필터 source=%s excluded=%d", source, len(excluded_items))
    baseline = {"source": source, "listName": label, "sourceUrl": urllib.parse.urljoin(BASE_URL, list_path),
                "startDate": start, "endDate": end, "displayedTotal": total, "pageCount": pages,
                "pageCounts": page_counts, "collectedAt": now(), "paginationDuplicates": pagination_duplicates,
                "excludedSystems": sorted(EXCLUDED_SYSTEMS), "excludedItemCount": len(excluded_items),
                "excludedReasonCounts": excluded_reason_counts,
                "items": rows}
    baseline_hash = sha256(json.dumps(rows, ensure_ascii=False, sort_keys=True).encode("utf-8"))
    checkpoint = {"status": "collecting", "baselineHash": baseline_hash, "baseline": baseline,
                  "paginationDuplicates": pagination_duplicates,
                  "nextIndex": 0, "success": [], "failures": [], "attachmentBlocked": [], "updatedAt": now()}
    write_json(checkpoint_path, checkpoint)
    return checkpoint


def extract_attachments(parser: TreeParser) -> list[dict[str, str]]:
    attachments: list[dict[str, str]] = []
    for link in parser.links:
        if "downloadFile.dmn" not in link["href"]:
            continue
        query = urllib.parse.parse_qs(urllib.parse.urlparse(link["href"]).query)
        name = query.get("downname", [link["text"] or "attachment"])[0]
        attachments.append({"fileName": name, "remoteUrl": urllib.parse.urljoin(BASE_URL, link["href"])})
    return attachments


def row_value(parser: TreeParser, labels: tuple[str, ...]) -> str:
    """상세 표에서 첫 번째 일치 라벨의 원문 값을 반환한다."""
    for row in parser.rows:
        if len(row) < 2:
            continue
        label = clean_text(str(row[0]["text"])).replace(" ", "")
        if label in labels:
            return clean_text(str(row[1]["text"]))
    return ""


def detail_sections(source_type: str, parser: TreeParser) -> tuple[str, str, str]:
    """원장별 요청 원문, 단계별 처리 내역과 결과 필드를 분리한다."""
    request_labels = {
        "csr": ("요청내용",),
        "trouble": ("장애내용",),
        "issue": ("이슈내용", "요청내용"),
    }
    request = row_value(parser, request_labels.get(source_type, ("요청내용",)))
    section_ids = sorted(parser.section_parts, key=lambda value: int(value.rsplit("_", 1)[-1]))
    if source_type == "csr":
        section_ids = [value for value in section_ids if value != "ddContent_1"]
    history_blocks: list[str] = []
    for section_id in section_ids:
        title = clean_text(" ".join(parser.section_titles.get(section_id, []))).replace("접기 메세지", "").strip()
        body = clean_text("\n".join(parser.section_parts[section_id]))
        if body:
            history_blocks.append(f"### {title or section_id}\n\n{body}")
    result_labels = {
        "처리내용", "완료내용", "조치사항", "장애원인", "재발방지대책", "처리결과",
        "결과", "릴리즈내용", "릴리즈결과", "해결내용", "원인", "대책",
    }
    result_lines: list[str] = []
    for row in parser.rows:
        if len(row) < 2:
            continue
        label = clean_text(str(row[0]["text"])).replace(" ", "")
        value = clean_text(str(row[1]["text"]))
        if label in result_labels and value:
            result_lines.extend([f"### {label}", "", value, ""])
    return request, "\n\n".join(history_blocks), "\n".join(result_lines).rstrip()


def markdown_for(item: dict[str, object], detail: TreeParser, attachments: list[dict[str, object]], fetched_at: str) -> str:
    identifier = str(item["displayNumber"] or item["docno"])
    request, history, result = detail_sections(str(item["sourceType"]), detail)
    lines = [f"# {identifier} {item['title']}", "", f"- sourceType: {item['sourceType']}",
             f"- sourceUrl: {item['detailUrl']}", f"- displayNumber: {item['displayNumber']}",
             f"- docno: {item['docno']}", f"- status: {item['status']}", f"- system: {item['system']}",
             f"- requester: {item['requester']}", f"- requestedAt: {item['requestedAt']}",
             f"- completedAt: {item['completionAt']}", f"- assignee: {item['assignee']}", f"- fetchedAt: {fetched_at}",
             "", "## 요청 내용", "", request or "없음", "", "## 처리 내역", "", history or "없음",
             "", "## 처리 결과", "", result or "없음", "", "## 첨부파일", ""]
    if not attachments:
        lines.append("없음")
    for attachment in attachments:
        lines.extend([f"- 파일명: {attachment['fileName']}", f"  - 확장자: {attachment['extension']}",
                      f"  - 원격 URL: {attachment['remoteUrl']}", f"  - 저장 상태: {attachment['storageStatus']}"])
        if attachment.get("localPath"):
            lines.extend([f"  - 로컬 경로: {attachment['localPath']}", f"  - SHA-256: {attachment['sha256']}",
                          f"  - 크기: {attachment['size']} bytes"])
    return "\n".join(lines) + "\n"


def collect_one_detail(client: Client, item: dict[str, object], index: int, output: Path) -> dict[str, object]:
    """상세 한 건과 이미지 첨부를 수집하고 체크포인트 반영용 결과를 반환한다."""
    identifier = str(item["displayNumber"] or item["docno"])
    system_directory = safe_system_name(str(item.get("system", "")))
    item_output = output / system_directory
    try:
            detail_bytes = client.request(urllib.parse.urlparse(str(item["detailUrl"])).path.lstrip("/") + "?" + urllib.parse.urlparse(str(item["detailUrl"])).query)
            parser = parse_html(detail_bytes)
            visible = clean_text(" ".join(parser.text_parts))
            if identifier not in visible:
                raise RuntimeError("상세 표시번호 불일치")
            attachments = extract_attachments(parser)
            blocked: list[str] = []
            for attachment in attachments:
                extension = Path(attachment["fileName"]).suffix.lower()
                attachment["extension"] = extension
                attachment["storageStatus"] = "metadata_only"
                if extension not in IMAGE_EXTENSIONS:
                    continue
                try:
                    data = client.request(urllib.parse.urlparse(attachment["remoteUrl"]).path.lstrip("/") + "?" + urllib.parse.urlparse(attachment["remoteUrl"]).query)
                    local = Path("attachments") / identifier / safe_name(attachment["fileName"], 180)
                    atomic_write(item_output / local, data)
                    attachment.update({"storageStatus": "stored", "localPath": local.as_posix(), "sha256": sha256(data), "size": len(data)})
                except Exception as error:  # 상세 원문은 이미지 실패와 독립적으로 보존한다.
                    blocked.append(attachment["fileName"])
                    attachment["storageStatus"] = "ATTACHMENT_BLOCKED"
                    LOG.warning("이미지 차단 source=%s docno=%s file=%s error=%s", item["sourceType"], item["docno"], attachment["fileName"], type(error).__name__)
            fetched_at = now()
            markdown = markdown_for(item, parser, attachments, fetched_at).encode("utf-8")
            filename = f"{identifier}__{safe_name(str(item['title']))}.md"
            markdown_path = Path(system_directory) / filename
            atomic_write(output / markdown_path, markdown)
            item.update({"systemDirectory": system_directory, "markdownPath": markdown_path.as_posix(),
                         "attachmentCount": len(attachments),
                         "imageCount": sum(a["storageStatus"] == "stored" for a in attachments),
                         "metadataOnlyCount": sum(a["storageStatus"] == "metadata_only" for a in attachments),
                         "contentSha256": sha256(markdown), "ingestionStatus": "ATTACHMENT_BLOCKED" if blocked else "complete",
                         "fetchedAt": fetched_at, "attachmentBlocked": blocked})
            return {"ok": True, "index": index, "docno": str(item["docno"]), "blocked": blocked}
    except Exception as error:
        LOG.error("상세 실패 source=%s index=%d docno=%s error=%s", item["sourceType"], index, item["docno"], type(error).__name__)
        return {"ok": False, "index": index, "docno": str(item["docno"]), "error": type(error).__name__}


def collect_details(client: Client, checkpoint: dict[str, object], output: Path, limit: int | None) -> None:
    items = checkpoint["baseline"]["items"]
    start_index = int(checkpoint["nextIndex"])
    stop = len(items) if limit is None else min(len(items), start_index + limit)
    batch_size = 250
    with ThreadPoolExecutor(max_workers=4, thread_name_prefix="itsm-detail") as executor:
        for batch_start in range(start_index, stop, batch_size):
            batch_stop = min(stop, batch_start + batch_size)
            work = [(items[index], index) for index in range(batch_start, batch_stop)]
            results = executor.map(lambda pair: collect_one_detail(client, pair[0], pair[1], output), work)
            for result in results:
                if result["ok"]:
                    checkpoint["success"].append(result["docno"])
                    if result["blocked"]:
                        checkpoint["attachmentBlocked"].append({"docno": result["docno"], "files": result["blocked"]})
                else:
                    checkpoint["failures"].append({"docno": result["docno"], "index": result["index"],
                                                   "stage": "detail", "error": result["error"]})
            checkpoint["nextIndex"] = batch_stop
            checkpoint["updatedAt"] = now()
            write_json(output / "checkpoint.json", checkpoint)
            LOG.info("상세 진행 source=%s index=%d/%d success=%d failures=%d", items[batch_start]["sourceType"], batch_stop, len(items), len(checkpoint["success"]), len(checkpoint["failures"]))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=Path("daily/text/raw/itsm"))
    parser.add_argument("--start", default="2000-01-01")
    parser.add_argument("--end", default=datetime.now().date().isoformat())
    parser.add_argument("--source", choices=[*SOURCES, "all"], default="all")
    parser.add_argument("--limit", type=int, help="이번 실행에서 원장별로 수집할 상세 수")
    parser.add_argument("--refresh-details", action="store_true", help="기준선을 유지하고 모든 상세 Markdown을 다시 조회해 재생성")
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    sso_url = getpass.getpass("ITSM SSO URL: ") if sys.stdin.isatty() else sys.stdin.readline().strip()
    if not sso_url:
        raise RuntimeError("ITSM SSO URL 입력이 없습니다.")
    client = Client.authenticate(sso_url)
    sso_url = ""
    sources = list(SOURCES) if args.source == "all" else [args.source]
    for source in sources:
        output = args.output / source
        checkpoint_path = output / "checkpoint.json"
        if checkpoint_path.exists():
            checkpoint = json.loads(checkpoint_path.read_text(encoding="utf-8"))
            if checkpoint.get("status") == "baseline_collecting":
                checkpoint = collect_baseline(client, source, args.start, args.end, output)
            else:
                LOG.info("체크포인트 재개 source=%s nextIndex=%d", source, checkpoint["nextIndex"])
        else:
            checkpoint = collect_baseline(client, source, args.start, args.end, output)
        if args.refresh_details:
            checkpoint["nextIndex"] = 0
            checkpoint["success"] = []
            checkpoint["failures"] = []
            checkpoint["attachmentBlocked"] = []
            checkpoint["status"] = "collecting"
            checkpoint["updatedAt"] = now()
            write_json(checkpoint_path, checkpoint)
            LOG.info("상세 전체 재생성 source=%s items=%d", source, len(checkpoint["baseline"]["items"]))
        collect_details(client, checkpoint, output, args.limit)
    return 0


if __name__ == "__main__":
    sys.exit(main())
