import { useLocation, useNavigate } from "react-router-dom";
import { getLandingPath, getRoleProfile, normalizeRole } from "../../auth/access";
import { getUser } from "../../services/api";

const AccessDeniedPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const role = normalizeRole(getUser()?.role);
  const profile = getRoleProfile(role);
  const requestedPath = location.state?.from || "this page";

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Access Denied</h2>
          <p className="muted">This route is not available for your current role.</p>
        </div>
      </div>

      <div className="panel access-card">
        <span className="badge">{profile.role}</span>
        <h3>{profile.persona}</h3>
        <p className="muted">Primary use: {profile.primaryUse}</p>
        <p>
          You tried to open <strong>{requestedPath}</strong>, but that page is not exposed to this persona.
        </p>
        <div className="actions">
          <button className="button" onClick={() => navigate(getLandingPath(role), { replace: true })}>
            Go To My Dashboard
          </button>
          <button className="button button-secondary" onClick={() => navigate(-1)}>
            Go Back
          </button>
        </div>
      </div>
    </section>
  );
};

export default AccessDeniedPage;
