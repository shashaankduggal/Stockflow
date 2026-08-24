import { useEffect, useMemo, useState } from "react";
import { canAccess, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const emptyStockForm = { productId: "", warehouseId: "", quantity: "", remarks: "" };
const emptyTransferForm = { productId: "", fromWarehouseId: "", toWarehouseId: "", quantity: "", remarks: "" };

const Inventory = () => {
  const [transactions, setTransactions] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [stockInForm, setStockInForm] = useState(emptyStockForm);
  const [stockOutForm, setStockOutForm] = useState(emptyStockForm);
  const [transferForm, setTransferForm] = useState(emptyTransferForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const role = normalizeRole(getUser()?.role);
  const canMutateInventory = canAccess(role, "inventoryMutate");

  const loadAll = async () => {
    setLoading(true);
    try {
      const [inventoryData, productData, warehouseData] = await Promise.all([
        request("/inventory"),
        request("/products"),
        request("/warehouses"),
      ]);
      setTransactions(inventoryData);
      setProducts(productData);
      setWarehouses(warehouseData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = window.setTimeout(loadAll, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const productOptions = useMemo(
    () => products.map((product) => ({ value: product.id, label: `${product.name} (${product.sku})` })),
    [products],
  );

  const warehouseOptions = useMemo(
    () => warehouses.map((warehouse) => ({ value: warehouse.id, label: `${warehouse.name} - ${warehouse.location}` })),
    [warehouses],
  );

  const submitStock = async (endpoint, payload, resetForm) => {
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      await request(endpoint, { method: "POST", body: payload });
      setSuccess("Inventory transaction saved successfully.");
      resetForm();
      await loadAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const buildPayload = (form, keys) => ({
    ...keys.reduce((acc, key) => ({ ...acc, [key]: Number(form[key]) }), {}),
    quantity: Number(form.quantity),
    remarks: form.remarks,
  });

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Inventory</h2>
          <p className="muted">Track stock movement and warehouse activity.</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      {canMutateInventory && (
        <div className="panel stack">
          <div className="panel-header">
            <h3>Inventory Operations</h3>
          </div>
          <div className="grid-3">
            <form
              className="form operations-card"
              onSubmit={(event) => {
                event.preventDefault();
                submitStock("/inventory/stock-in", buildPayload(stockInForm, ["productId", "warehouseId"]), () =>
                  setStockInForm(emptyStockForm),
                );
              }}
            >
              <h3>Stock In</h3>
              <select value={stockInForm.productId} onChange={(e) => setStockInForm({ ...stockInForm, productId: e.target.value })}>
                <option value="">Select product</option>
                {productOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <select value={stockInForm.warehouseId} onChange={(e) => setStockInForm({ ...stockInForm, warehouseId: e.target.value })}>
                <option value="">Select warehouse</option>
                {warehouseOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <input type="number" min="1" placeholder="Quantity" value={stockInForm.quantity} onChange={(e) => setStockInForm({ ...stockInForm, quantity: e.target.value })} />
              <input placeholder="Remarks" value={stockInForm.remarks} onChange={(e) => setStockInForm({ ...stockInForm, remarks: e.target.value })} />
              <button className="button" disabled={saving}>Stock In</button>
            </form>

            <form
              className="form operations-card"
              onSubmit={(event) => {
                event.preventDefault();
                submitStock("/inventory/stock-out", buildPayload(stockOutForm, ["productId", "warehouseId"]), () =>
                  setStockOutForm(emptyStockForm),
                );
              }}
            >
              <h3>Stock Out</h3>
              <select value={stockOutForm.productId} onChange={(e) => setStockOutForm({ ...stockOutForm, productId: e.target.value })}>
                <option value="">Select product</option>
                {productOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <select value={stockOutForm.warehouseId} onChange={(e) => setStockOutForm({ ...stockOutForm, warehouseId: e.target.value })}>
                <option value="">Select warehouse</option>
                {warehouseOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <input type="number" min="1" placeholder="Quantity" value={stockOutForm.quantity} onChange={(e) => setStockOutForm({ ...stockOutForm, quantity: e.target.value })} />
              <input placeholder="Remarks" value={stockOutForm.remarks} onChange={(e) => setStockOutForm({ ...stockOutForm, remarks: e.target.value })} />
              <button className="button" disabled={saving}>Stock Out</button>
            </form>

            <form
              className="form operations-card"
              onSubmit={(event) => {
                event.preventDefault();
                submitStock("/inventory/transfer", {
                  productId: Number(transferForm.productId),
                  fromWarehouseId: Number(transferForm.fromWarehouseId),
                  toWarehouseId: Number(transferForm.toWarehouseId),
                  quantity: Number(transferForm.quantity),
                  remarks: transferForm.remarks,
                }, () => setTransferForm(emptyTransferForm));
              }}
            >
              <h3>Transfer</h3>
              <select value={transferForm.productId} onChange={(e) => setTransferForm({ ...transferForm, productId: e.target.value })}>
                <option value="">Select product</option>
                {productOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <select value={transferForm.fromWarehouseId} onChange={(e) => setTransferForm({ ...transferForm, fromWarehouseId: e.target.value })}>
                <option value="">From warehouse</option>
                {warehouseOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <select value={transferForm.toWarehouseId} onChange={(e) => setTransferForm({ ...transferForm, toWarehouseId: e.target.value })}>
                <option value="">To warehouse</option>
                {warehouseOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <input type="number" min="1" placeholder="Quantity" value={transferForm.quantity} onChange={(e) => setTransferForm({ ...transferForm, quantity: e.target.value })} />
              <input placeholder="Remarks" value={transferForm.remarks} onChange={(e) => setTransferForm({ ...transferForm, remarks: e.target.value })} />
              <button className="button" disabled={saving}>Transfer</button>
            </form>
          </div>
        </div>
      )}

      <div className="panel">
        <div className="panel-header">
          <h3>Transactions</h3>
        </div>
        {loading ? (
          <div>Loading inventory...</div>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Product</th>
                  <th>Warehouse</th>
                  <th>Qty</th>
                  <th>Remarks</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((row) => (
                  <tr key={row.id}>
                    <td>{row.id}</td>
                    <td><span className="badge">{row.type}</span></td>
                    <td>{row.product?.name}</td>
                    <td>{row.warehouse?.name}</td>
                    <td>{row.quantity}</td>
                    <td>{row.remarks || "-"}</td>
                    <td>{row.createdAt ? new Date(row.createdAt).toLocaleString() : "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default Inventory;
