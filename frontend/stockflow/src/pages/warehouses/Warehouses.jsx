import { useEffect, useState } from "react";
import { canAccess, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const emptyForm = { name: "", location: "" };

const Warehouses = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const role = normalizeRole(getUser()?.role);
  const canManageWarehouses = canAccess(role, "warehouseMutate");
  const canDeleteWarehouses = canAccess(role, "warehouseDelete");
  const showActions = canManageWarehouses || canDeleteWarehouses;

  const loadWarehouses = async () => {
    setLoading(true);
    try {
      setItems(await request("/warehouses"));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWarehouses();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    if (!form.name || !form.location) {
      setError("Please fill in both warehouse fields.");
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      const payload = { name: form.name.trim(), location: form.location.trim() };
      if (editingId) {
        await request(`/warehouses/${editingId}`, { method: "PUT", body: payload });
        setSuccess("Warehouse updated successfully.");
      } else {
        await request("/warehouses", { method: "POST", body: payload });
        setSuccess("Warehouse created successfully.");
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadWarehouses();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const editWarehouse = (warehouse) => {
    setEditingId(warehouse.id);
    setForm({ name: warehouse.name || "", location: warehouse.location || "" });
  };

  const deleteWarehouse = async (id) => {
    if (!window.confirm("Delete this warehouse?")) {
      return;
    }

    try {
      await request(`/warehouses/${id}`, { method: "DELETE" });
      setSuccess("Warehouse deleted successfully.");
      await loadWarehouses();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Warehouses</h2>
          <p className="muted">Monitor warehouse locations and stock context.</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <div className={canManageWarehouses ? "grid-2" : ""}>
        {canManageWarehouses ? (
          <form className="panel form" onSubmit={submit}>
            <h3>{editingId ? "Edit Warehouse" : "New Warehouse"}</h3>
            <label className="field">
              <span>Name</span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </label>
            <label className="field">
              <span>Location</span>
              <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            </label>
            <div className="actions">
              <button className="button" disabled={saving}>
                {saving ? "Saving..." : editingId ? "Update Warehouse" : "Create Warehouse"}
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

        <div className="panel table-wrap">
          {loading ? (
            <div>Loading warehouses...</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Location</th>
                  {showActions && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {items.map((warehouse) => (
                  <tr key={warehouse.id}>
                    <td>{warehouse.name}</td>
                    <td>{warehouse.location}</td>
                    {showActions && (
                      <td>
                        <div className="row-actions">
                          {canManageWarehouses && (
                            <button className="link-button" type="button" onClick={() => editWarehouse(warehouse)}>
                              Edit
                            </button>
                          )}
                          {canDeleteWarehouses && (
                            <button className="link-button danger" type="button" onClick={() => deleteWarehouse(warehouse.id)}>
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
          )}
        </div>
      </div>
    </section>
  );
};

export default Warehouses;
