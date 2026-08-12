export const ROLES = {
  ADMIN: "ADMIN",
  MANAGER: "MANAGER",
  STAFF: "STAFF",
  VIEWER: "VIEWER",
};

export const ROLE_PROFILES = {
  [ROLES.ADMIN]: {
    role: ROLES.ADMIN,
    persona: "System Administrator",
    primaryUse: "Full control",
    landingPath: "/admin/dashboard",
    dashboardTitle: "Admin Dashboard",
    dashboardSubtitle: "Oversee users, stock, warehouses, and platform activity from one control surface.",
  },
  [ROLES.MANAGER]: {
    role: ROLES.MANAGER,
    persona: "Operations Manager",
    primaryUse: "Monitor and manage stock",
    landingPath: "/operations/dashboard",
    dashboardTitle: "Operations Dashboard",
    dashboardSubtitle: "Track stock movement, warehouse health, and inventory throughput across operations.",
  },
  [ROLES.STAFF]: {
    role: ROLES.STAFF,
    persona: "Warehouse Staff",
    primaryUse: "Perform inventory operations",
    landingPath: "/inventory/dashboard",
    dashboardTitle: "Inventory Dashboard",
    dashboardSubtitle: "Stay focused on stock handling, transfers, and warehouse activity for day-to-day execution.",
  },
  [ROLES.VIEWER]: {
    role: ROLES.VIEWER,
    persona: "External Viewer",
    primaryUse: "Read-only access",
    landingPath: "/viewer/dashboard",
    dashboardTitle: "Read-Only Dashboard",
    dashboardSubtitle: "Review stock and product visibility without access to operational changes.",
  },
};

export const ACCESS_RULES = {
  dashboard: [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF, ROLES.VIEWER],
  products: [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF, ROLES.VIEWER],
  productMutate: [ROLES.ADMIN, ROLES.MANAGER],
  productDelete: [ROLES.ADMIN],
  warehouses: [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF],
  warehouseMutate: [ROLES.ADMIN, ROLES.MANAGER],
  warehouseDelete: [ROLES.ADMIN],
  inventory: [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF, ROLES.VIEWER],
  inventoryMutate: [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF],
  roleControl: [ROLES.ADMIN],
  suppliers: [ROLES.ADMIN, ROLES.MANAGER],
  supplierMutate: [ROLES.ADMIN, ROLES.MANAGER],
  supplierDelete: [ROLES.ADMIN],
  categories: [ROLES.ADMIN, ROLES.MANAGER],
  categoryMutate: [ROLES.ADMIN, ROLES.MANAGER],
  categoryDelete: [ROLES.ADMIN],
};

export const NAV_ITEMS = [
  { to: "/dashboard", label: "Dashboard", accessKey: "dashboard" },
  { to: "/products", label: "Products", accessKey: "products" },
  { to: "/warehouses", label: "Warehouses", accessKey: "warehouses" },
  { to: "/inventory", label: "Inventory", accessKey: "inventory" },
  { to: "/admin/roles", label: "Role Control", accessKey: "roleControl" },
  { to: "/suppliers", label: "Suppliers", accessKey: "suppliers" },
  { to: "/categories", label: "Categories", accessKey: "categories" },
];

export const DASHBOARD_CARDS = {
  [ROLES.ADMIN]: [
    { key: "totalProducts", label: "Products" },
    { key: "totalWarehouses", label: "Warehouses" },
    { key: "totalSuppliers", label: "Suppliers" },
    { key: "totalCategories", label: "Categories" },
    { key: "totalUsers", label: "Users" },
    { key: "totalTransactions", label: "Transactions" },
    { key: "inventoryValue", label: "Inventory Value", currency: true },
  ],
  [ROLES.MANAGER]: [
    { key: "totalProducts", label: "Products" },
    { key: "totalWarehouses", label: "Warehouses" },
    { key: "totalSuppliers", label: "Suppliers" },
    { key: "totalCategories", label: "Categories" },
    { key: "totalTransactions", label: "Transactions" },
    { key: "inventoryValue", label: "Inventory Value", currency: true },
  ],
  [ROLES.STAFF]: [
    { key: "totalProducts", label: "Products" },
    { key: "totalWarehouses", label: "Warehouses" },
    { key: "stockInCount", label: "Stock In" },
    { key: "stockOutCount", label: "Stock Out" },
    { key: "transferCount", label: "Transfers" },
    { key: "totalTransactions", label: "Transactions" },
  ],
  [ROLES.VIEWER]: [
    { key: "totalProducts", label: "Products" },
    { key: "totalTransactions", label: "Transactions" },
    { key: "stockInCount", label: "Stock In" },
    { key: "stockOutCount", label: "Stock Out" },
    { key: "inventoryValue", label: "Inventory Value", currency: true },
  ],
};

export const normalizeRole = (role) => {
  const normalized = String(role || ROLES.VIEWER).trim().toUpperCase();
  const plainRole = normalized.startsWith("ROLE_") ? normalized.slice(5) : normalized;
  return ROLE_PROFILES[plainRole] ? plainRole : ROLES.VIEWER;
};

export const getRoleProfile = (role) => ROLE_PROFILES[normalizeRole(role)];

export const getLandingPath = (role) => getRoleProfile(role).landingPath;

export const canAccess = (role, accessKey) =>
  (ACCESS_RULES[accessKey] || []).includes(normalizeRole(role));

export const getNavItemsForRole = (role) =>
  NAV_ITEMS.filter((item) => canAccess(role, item.accessKey));

export const getDashboardCardsForRole = (role) =>
  DASHBOARD_CARDS[normalizeRole(role)] || DASHBOARD_CARDS[ROLES.VIEWER];
