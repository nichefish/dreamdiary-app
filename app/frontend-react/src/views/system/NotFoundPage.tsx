import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <section className="not-found-page" aria-labelledby="not-found-title">
      <h2 id="not-found-title">화면을 찾을 수 없습니다.</h2>
      <Link to="/journal/weekly">주간 VIEW</Link>
    </section>
  );
}
