import { NavLink, Outlet } from "react-router-dom";

const journalTabs = [
  { to: "/journal/weekly", label: "주간 VIEW" },
  { to: "/journal/monthly", label: "월간 VIEW" },
  { to: "/journal/calendar", label: "달력 VIEW" },
  { to: "/journal/meta", label: "메타 VIEW" }
];

export function App() {
  return (
    <main className="react-app-shell">
      <header className="react-app-header">
        <div>
          <p className="react-app-kicker">DreamDiary</p>
          <h1>주간 일기</h1>
        </div>
      </header>
      <nav className="journal-view-tabs" aria-label="일기 보기">
        {journalTabs.map((item) => (
          <NavLink key={item.to} to={item.to} className="journal-view-tab">
            {item.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </main>
  );
}
