import { Navigate } from "react-router-dom";
import { getLandingPath } from "../auth/access";
import { getUser } from "../services/api";

const LandingRedirect = () => {
  return <Navigate to={getLandingPath(getUser()?.role)} replace />;
};

export default LandingRedirect;
