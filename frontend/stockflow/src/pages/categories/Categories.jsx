import { useEffect, useState } from "react";
import { canAccess, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const emptyForm = { name: "", description: "" };

const Categories = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const role = normalizeRole(getUser()?.role);
  const canManageCategories = canAccess(role, "categoryMutate");
  const canDeleteCategories = canAccess(role, "categoryDelete");
  const showActions = canManageCategories || canDeleteCategories;

  const loadCategories = async () => {
    setLoading(true);
    try {
      setItems(await request("/categories"));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = window.setTimeout(loadCategories, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    try {
      if (editingId) {
        await request(`/categories/${editingId}`, { method: "PUT", body: form });
        setSuccess("Category updated successfully.");
      } else {
        await request("/categories", { method: "POST", body: form });
        setSuccess("Category created successfully.");
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadCategories();
    } catch (err) {
      setError(err.message);
    }
  };

  const editCategory = (category) => {
    setEditingId(category.id);
    setForm({
      name: category.name || "",
      description: category.description || "",
    });
  };

  const deleteCategory = async (id) => {
    if (!window.confirm("Delete this category?")) {
      return;
    }

    try {
      await request(`/categories/${id}`, { method: "DELETE" });
      setSuccess("Category deleted successfully.");
      await loadCategories();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Categories</h2>
          <p className="muted">
            Organize your product catalog with backend-driven categories.
          </p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <div className="grid-2">
        <form className="panel form" onSubmit={submit}>
          <h3>{editingId ? "Edit Category" : "New Category"}</h3>
          <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <textarea placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
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
            <div>Loading categories...</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Description</th>
                  {showActions && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {items.map((category) => (
                  <tr key={category.id}>
                    <td>{category.name}</td>
                    <td>{category.description || "-"}</td>
                    {showActions && (
                      <td>
                        <div className="row-actions">
                          {canManageCategories && (
                            <button className="link-button" type="button" onClick={() => editCategory(category)}>Edit</button>
                          )}
                          {canDeleteCategories && (
                            <button className="link-button danger" type="button" onClick={() => deleteCategory(category.id)}>Delete</button>
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

export default Categories;
