import { useEffect, useState } from "react";
import { canAccess, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const emptyForm = { name: "", contactPerson: "", email: "", phone: "", address: "" };

const Suppliers = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const role = normalizeRole(getUser()?.role);
  const canManageSuppliers = canAccess(role, "supplierMutate");
  const canDeleteSuppliers = canAccess(role, "supplierDelete");
  const showActions = canManageSuppliers || canDeleteSuppliers;

  const loadSuppliers = async () => {
    setLoading(true);
    try {
      setItems(await request("/suppliers"));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = window.setTimeout(loadSuppliers, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    try {
      if (editingId) {
        await request(`/suppliers/${editingId}`, { method: "PUT", body: form });
        setSuccess("Supplier updated successfully.");
      } else {
        await request("/suppliers", { method: "POST", body: form });
        setSuccess("Supplier created successfully.");
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadSuppliers();
    } catch (err) {
      setError(err.message);
    }
  };

  const editSupplier = (supplier) => {
    setEditingId(supplier.id);
    setForm({
      name: supplier.name || "",
      contactPerson: supplier.contactPerson || "",
      email: supplier.email || "",
      phone: supplier.phone || "",
      address: supplier.address || "",
    });
  };

  const deleteSupplier = async (id) => {
    if (!window.confirm("Delete this supplier?")) {
      return;
    }

    try {
      await request(`/suppliers/${id}`, { method: "DELETE" });
      setSuccess("Supplier deleted successfully.");
      await loadSuppliers();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Suppliers</h2>
          <p className="muted">
            Maintain supplier records through the backend API with admin and manager visibility.
          </p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <div className="grid-2">
        <form className="panel form" onSubmit={submit}>
          <h3>{editingId ? "Edit Supplier" : "New Supplier"}</h3>
          <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <input placeholder="Contact person" value={form.contactPerson} onChange={(e) => setForm({ ...form, contactPerson: e.target.value })} />
          <input placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input placeholder="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <input placeholder="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
          <div className="actions">
            <button className="button">{editingId ? "Update" : "Create"}</button>
            {editingId && (
              <button type="button" className="button button-secondary" onClick={() => { setEditingId(null); setForm(emptyForm); }}>
                Cancel
              </button>
            )}
          </div>
        </form>

        <div className="panel table-wrap">
          {loading ? (
            <div>Loading suppliers...</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Contact</th>
                  <th>Email</th>
                  <th>Phone</th>
                  {showActions && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {items.map((supplier) => (
                  <tr key={supplier.id}>
                    <td>{supplier.name}</td>
                    <td>{supplier.contactPerson || "-"}</td>
                    <td>{supplier.email || "-"}</td>
                    <td>{supplier.phone || "-"}</td>
                    {showActions && (
                      <td>
                        <div className="row-actions">
                          {canManageSuppliers && (
                            <button className="link-button" type="button" onClick={() => editSupplier(supplier)}>Edit</button>
                          )}
                          {canDeleteSuppliers && (
                            <button className="link-button danger" type="button" onClick={() => deleteSupplier(supplier.id)}>Delete</button>
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

export default Suppliers;
