import { Navigate, Outlet, useLocation } from "react-router-dom";
import { getLandingPath, normalizeRole } from "../auth/access";
import { getToken, getUser } from "../services/api";

const ProtectedRoutes = ({ allowedRoles }) => {
  const location = useLocation();

  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }

  const role = normalizeRole(getUser()?.role);
  const normalizedAllowedRoles = allowedRoles?.map(normalizeRole);

  if (normalizedAllowedRoles?.length && !normalizedAllowedRoles.includes(role)) {
    return (
      <Navigate
        to="/access-denied"
        replace
        state={{ from: location.pathname, home: getLandingPath(role) }}
      />
    );
  }

  return <Outlet />;
};

export default ProtectedRoutes;
