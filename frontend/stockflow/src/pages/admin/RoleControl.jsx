import { useEffect, useState } from "react";
import { ROLES, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const roleOptions = [ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF, ROLES.VIEWER];

const RoleControl = () => {
  const currentUser = getUser();
  const [users, setUsers] = useState([]);
  const [draftRoles, setDraftRoles] = useState({});
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadUsers = async () => {
    setLoading(true);
    try {
      const data = await request("/users");
      setUsers(data);
      setDraftRoles(
        Object.fromEntries(
          data.map((user) => [user.id, normalizeRole(user.role?.name || user.role)])
        )
      );
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const updateRole = async (userId) => {
    setSavingId(userId);
    setError("");
    setSuccess("");

    try {
      await request(`/users/${userId}/role`, {
        method: "PUT",
        body: { role: draftRoles[userId] },
      });
      setSuccess("User role updated successfully.");
      await loadUsers();
    } catch (err) {
      setError(err.message);
    } finally {
      setSavingId(null);
    }
  };

  const deleteUser = async (user) => {
    if (!window.confirm(`Delete ${user.fullName}?`)) {
      return;
    }

    setDeletingId(user.id);
    setError("");
    setSuccess("");

    try {
      await request(`/users/${user.id}`, { method: "DELETE" });
      setSuccess("User deleted successfully.");
      await loadUsers();
    } catch (err) {
      setError(err.message);
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Role Control</h2>
          <p className="muted">Review users, update assigned roles, and remove accounts when needed.</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <div className="panel">
        <div className="panel-header">
          <h3>User Access</h3>
        </div>

        {loading ? (
          <div>Loading users...</div>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const isCurrentAdmin = currentUser?.email === user.email;

                  return (
                    <tr key={user.id}>
                      <td>{user.fullName}</td>
                      <td>{user.email}</td>
                      <td>
                        <select
                          value={draftRoles[user.id] || ROLES.VIEWER}
                          disabled={isCurrentAdmin || savingId === user.id}
                          onChange={(event) =>
                            setDraftRoles({
                              ...draftRoles,
                              [user.id]: event.target.value,
                            })
                          }
                        >
                          {roleOptions.map((role) => (
                            <option key={role} value={role}>
                              {role}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button
                            className="button button-secondary"
                            type="button"
                            disabled={
                              isCurrentAdmin ||
                              savingId === user.id ||
                              draftRoles[user.id] === normalizeRole(user.role?.name || user.role)
                            }
                            onClick={() => updateRole(user.id)}
                          >
                            {savingId === user.id ? "Saving..." : "Save Role"}
                          </button>
                          <button
                            className="link-button danger"
                            type="button"
                            disabled={isCurrentAdmin || deletingId === user.id}
                            onClick={() => deleteUser(user)}
                          >
                            {deletingId === user.id ? "Deleting..." : "Delete"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default RoleControl;
