import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import AppShell from "../components/AppShell";
import PublicRoutes from "./PublicRoutes";
import ProtectedRoutes from "./ProtectedRoutes";
import LandingRedirect from "./LandingRedirect";
import { ROLES } from "../auth/access";
import { getToken } from "../services/api";
import LoginPage from "../pages/auth/LoginPage";
import SignupPage from "../pages/auth/SignupPage";
import AccessDeniedPage from "../pages/auth/AccessDeniedPage";
import RoleControl from "../pages/admin/RoleControl";
import Dashboard from "../pages/dashboard/Dashboard";
import Products from "../pages/products/Products";
import Warehouses from "../pages/warehouses/Warehouses";
import Inventory from "../pages/inventory/Inventory";
import Suppliers from "../pages/suppliers/Suppliers";
import Categories from "../pages/categories/Categories";
import AuditLogs from "../pages/audit/AuditLogs";

const AppFallback = () => {
  return getToken() ? <LandingRedirect /> : <Navigate to="/login" replace />;
};

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicRoutes />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Route>

        <Route element={<ProtectedRoutes />}>
          <Route element={<AppShell />}>
            <Route path="/" element={<LandingRedirect />} />
            <Route path="/dashboard" element={<LandingRedirect />} />
            <Route path="/access-denied" element={<AccessDeniedPage />} />

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.ADMIN]} />}>
              <Route path="/admin/dashboard" element={<Dashboard />} />
              <Route path="/admin/roles" element={<RoleControl />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.MANAGER]} />}>
              <Route path="/operations/dashboard" element={<Dashboard />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.STAFF]} />}>
              <Route path="/inventory/dashboard" element={<Dashboard />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.VIEWER]} />}>
              <Route path="/viewer/dashboard" element={<Dashboard />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF, ROLES.VIEWER]} />}>
              <Route path="/products" element={<Products />} />
              <Route path="/inventory" element={<Inventory />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF]} />}>
              <Route path="/warehouses" element={<Warehouses />} />
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={[ROLES.ADMIN, ROLES.MANAGER]} />}>
              <Route path="/suppliers" element={<Suppliers />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/audit-logs" element={<AuditLogs />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<AppFallback />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
