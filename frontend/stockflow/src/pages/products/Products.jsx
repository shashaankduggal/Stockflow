import { useEffect, useState } from "react";
import { canAccess, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const emptyForm = { name: "", sku: "", quantity: "", price: "" };

const Products = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const role = normalizeRole(getUser()?.role);
  const canManageProducts = canAccess(role, "productMutate");
  const canDeleteProducts = canAccess(role, "productDelete");
  const showActions = canManageProducts || canDeleteProducts;

  const loadProducts = async (search = "") => {
    setLoading(true);
    try {
      const data = search.trim()
        ? await request(`/products/search?query=${encodeURIComponent(search)}`)
        : await request("/products");
      setItems(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    if (!form.name || !form.sku || !form.quantity || !form.price) {
      setError("Please fill in every product field.");
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      const payload = {
        name: form.name.trim(),
        sku: form.sku.trim(),
        quantity: Number(form.quantity),
        price: Number(form.price),
      };

      if (editingId) {
        await request(`/products/${editingId}`, { method: "PUT", body: payload });
        setSuccess("Product updated successfully.");
      } else {
        await request("/products", { method: "POST", body: payload });
        setSuccess("Product created successfully.");
      }

      setForm(emptyForm);
      setEditingId(null);
      await loadProducts(query);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const editProduct = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name || "",
      sku: product.sku || "",
      quantity: String(product.quantity ?? ""),
      price: String(product.price ?? ""),
    });
  };

  const deleteProduct = async (id) => {
    if (!window.confirm("Delete this product?")) {
      return;
    }

    try {
      await request(`/products/${id}`, { method: "DELETE" });
      setSuccess("Product deleted successfully.");
      await loadProducts(query);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSearch = async (event) => {
    event.preventDefault();
    await loadProducts(query);
  };

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Products</h2>
          <p className="muted">Track products and SKU details in real time.</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <div className={canManageProducts ? "grid-2" : ""}>
        {canManageProducts ? (
          <form className="panel form" onSubmit={submit}>
            <h3>{editingId ? "Edit Product" : "New Product"}</h3>
            <label className="field">
              <span>Name</span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </label>
            <label className="field">
              <span>SKU</span>
              <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} />
            </label>
            <div className="grid-2 compact">
              <label className="field">
                <span>Quantity</span>
                <input
                  type="number"
                  min="0"
                  value={form.quantity}
                  onChange={(e) => setForm({ ...form, quantity: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Price</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.price}
                  onChange={(e) => setForm({ ...form, price: e.target.value })}
                />
              </label>
            </div>
            <div className="actions">
              <button className="button" disabled={saving}>
                {saving ? "Saving..." : editingId ? "Update Product" : "Create Product"}
              </button>
              {editingId && (
                <button
                  type="button"
                  className="button button-secondary"
                  onClick={() => {
                    setEditingId(null);
                    setForm(emptyForm);
                  }}
                >
                  Cancel
                </button>
              )}
            </div>
          </form>
        ) : null}

        <div className="panel stack">
          <form className="toolbar" onSubmit={handleSearch}>
            <input
              placeholder="Search products or SKU"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            <button className="button button-secondary" type="submit">
              Search
            </button>
          </form>

          {loading ? (
            <div>Loading products...</div>
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>SKU</th>
                    <th>Qty</th>
                    <th>Price</th>
                    {showActions && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {items.map((product) => (
                    <tr key={product.id}>
                      <td>{product.name}</td>
                      <td>{product.sku}</td>
                      <td>{product.quantity}</td>
                      <td>${Number(product.price).toFixed(2)}</td>
                      {showActions && (
                        <td>
                          <div className="row-actions">
                            {canManageProducts && (
                              <button className="link-button" type="button" onClick={() => editProduct(product)}>
                                Edit
                              </button>
                            )}
                            {canDeleteProducts && (
                              <button className="link-button danger" type="button" onClick={() => deleteProduct(product.id)}>
                                Delete
                              </button>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};

export default Products;
