STOCKFLOW API PAYLOADS
Base URL: http://localhost:8080

========================================
LOGIN
========================================

POST /api/auth/login

{
  "email": "admin@stockflow.com",
  "password": "Admin@123"
}

========================================
PRODUCTS
========================================

# CREATE
POST /api/products

{
  "name": "Gaming Mouse",
  "sku": "GM-101",
  "price": 1499.0
}

# GET
GET /api/products/{{productId}}

No Body

# GET ALL
GET /api/products

No Body

# SEARCH
GET /api/products/search?query=Mouse

No Body

# UPDATE
PUT /api/products/{{productId}}

{
  "name": "Gaming Mouse Pro",
  "sku": "GM-101",
  "price": 1799.0
}

# DELETE
DELETE /api/products/{{productId}}

No Body

========================================
WAREHOUSES
========================================

# CREATE
POST /api/warehouses

{
  "name": "Delhi Warehouse",
  "location": "New Delhi"
}

# GET
GET /api/warehouses/{{warehouseId}}

No Body

# GET ALL
GET /api/warehouses

No Body

# UPDATE
PUT /api/warehouses/{{warehouseId}}

{
  "name": "Delhi Central Warehouse",
  "location": "New Delhi"
}

# DELETE
DELETE /api/warehouses/{{warehouseId}}

No Body

========================================
CATEGORIES
========================================

# CREATE
POST /api/categories

{
  "name": "Electronics",
  "description": "Electronic accessories"
}

# GET
GET /api/categories/{{categoryId}}

No Body

# GET ALL
GET /api/categories

No Body

# UPDATE
PUT /api/categories/{{categoryId}}

{
  "name": "Premium Electronics",
  "description": "Updated category"
}

# DELETE
DELETE /api/categories/{{categoryId}}

No Body

========================================
SUPPLIERS
========================================

# CREATE
POST /api/suppliers

{
  "name": "Tech Supplies Ltd",
  "contactPerson": "Rahul Sharma",
  "email": "rahul@techsupplies.com",
  "phone": "9876543210",
  "address": "Nehru Place, Delhi"
}

# GET
GET /api/suppliers/{{supplierId}}

No Body

# GET ALL
GET /api/suppliers

No Body

# UPDATE
PUT /api/suppliers/{{supplierId}}

{
  "name": "Tech Supplies India",
  "contactPerson": "Rahul Sharma",
  "email": "rahul@techsupplies.com",
  "phone": "9876543210",
  "address": "Connaught Place, Delhi"
}

# DELETE
DELETE /api/suppliers/{{supplierId}}

No Body

========================================
INVENTORY
========================================

# STOCK IN
POST /api/inventory/stock-in

{
  "productId": 1,
  "warehouseId": 1,
  "quantity": 50,
  "remarks": "Initial inventory"
}

# STOCK OUT
POST /api/inventory/stock-out

{
  "productId": 1,
  "warehouseId": 1,
  "quantity": 10,
  "remarks": "Customer sale"
}

# TRANSFER
POST /api/inventory/transfer

{
  "productId": 1,
  "fromWarehouseId": 1,
  "toWarehouseId": 2,
  "quantity": 15,
  "remarks": "Inter warehouse transfer"
}

# GET ALL TRANSACTIONS
GET /api/inventory

No Body

========================================
USERS
========================================

# GET ALL USERS
GET /api/users

No Body

# FIND USER
GET /api/users/email/admin@stockflow.com

No Body

========================================
DASHBOARD
========================================

GET /api/dashboard

No Body

========================================
AUDIT LOGS
========================================

GET /api/audit-logs

No Body