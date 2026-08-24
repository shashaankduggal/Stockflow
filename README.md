# Stockflow

Stockflow is an inventory management application with a Spring Boot REST API and a React/Vite dashboard.

## What it does

- Authenticate users with JWT and enforce role-based access.
- Manage products, categories, suppliers, warehouses, and users.
- Record stock-in, stock-out, and warehouse transfer operations.
- View dashboard inventory summaries and search products.
- Review audit logs for tracked inventory and administration activity.
- Manage roles and permissions from the admin area.

## Stack

- Backend: Java 21, Spring Boot, Spring Data JPA, Spring Security
- Frontend: React 19, Vite, React Router, Axios
- Database: PostgreSQL

## Run locally

Create a PostgreSQL database named `inventory`, then start the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Start the frontend in a second terminal:

```powershell
cd frontend/stockflow
npm install
npm run dev
```

The frontend runs at `http://localhost:5173` and the API is available under `/api`.

## Configuration

The backend defaults to a local PostgreSQL instance. Override these environment variables when needed:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `CORS_ALLOWED_ORIGINS`

Change the development JWT and database credentials before deploying outside a local environment.

## Main API areas

`/api/auth` · `/api/products` · `/api/categories` · `/api/suppliers` · `/api/warehouses` · `/api/inventory` · `/api/audit-logs` · `/api/users`
