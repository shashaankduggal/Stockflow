import { Navigate, Outlet } from "react-router-dom";
import { getLandingPath } from "../auth/access";
import { getToken, getUser } from "../services/api";

const PublicRoutes = () => {
  return getToken() ? <Navigate to={getLandingPath(getUser()?.role)} replace /> : <Outlet />;
};

export default PublicRoutes;
