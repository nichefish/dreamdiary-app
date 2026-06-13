export function JournalWeeklyPage() {
  return (
    <section className="journal-weekly-page" aria-labelledby="journal-weekly-title">
      <div className="journal-toolbar">
        <div>
          <p className="journal-toolbar__eyebrow">WEEKLY</p>
          <h2 id="journal-weekly-title">주간 VIEW</h2>
        </div>
        <button type="button" className="journal-action-button">
          <span aria-hidden="true">+</span>
          <span>일자 등록</span>
        </button>
      </div>
      <div className="journal-content-grid">
        <aside className="journal-aside">
          <div className="journal-aside__header">
            <strong>FILTER</strong>
            <button type="button" aria-label="정렬 변경">
              ⇅
            </button>
          </div>
          <div className="journal-aside__body">
            <div className="journal-range">
              <span>2026</span>
              <span>06</span>
            </div>
            <button type="button" className="journal-today-button">
              TODAY
            </button>
          </div>
        </aside>
        <article className="journal-card">
          <div className="journal-card__header">
            <strong>일자 태그</strong>
          </div>
          <div className="journal-card__body">
            <p>React 화면 준비 중입니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
}
