import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { getNavItemsForRole, getRoleProfile, normalizeRole } from "../auth/access";
import { clearAuth, getUser } from "../services/api";

const AppShell = () => {
  const navigate = useNavigate();
  const user = getUser();
  const role = normalizeRole(user?.role);
  const profile = getRoleProfile(role);
  const navItems = getNavItemsForRole(role);

  const handleLogout = () => {
    clearAuth();
    navigate("/login", { replace: true });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="stack">
          <div>
            <div className="brand-mark">StockFlow</div>
            <p className="sidebar-subtitle">Enterprise inventory control</p>
          </div>

          <div className="persona-card">
            <span className="badge">{profile.role}</span>
            <strong>{profile.persona}</strong>
            <p className="muted">{profile.primaryUse}</p>
          </div>
        </div>

        <nav className="nav-list">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <button className="button button-secondary sidebar-logout" onClick={handleLogout}>
          Logout
        </button>
      </aside>

      <main className="content">
        <header className="topbar">
          <h1 className="page-title">StockFlow</h1>

          <div className="user-chip">
            <span className="muted">{user?.email || ""}</span>
          </div>
        </header>

        <Outlet />
      </main>
    </div>
  );
};

export default AppShell;
