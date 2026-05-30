/** HTML 태그 및 &nbsp; 제거 후 트림 */
export function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, "").replace(/&nbsp;/g, " ").trim();
}
