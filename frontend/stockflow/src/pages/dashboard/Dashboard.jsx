import { useEffect, useState } from "react";
import { getDashboardCardsForRole, getRoleProfile, normalizeRole } from "../../auth/access";
import { getUser, request } from "../../services/api";

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const role = normalizeRole(getUser()?.role);
  const profile = getRoleProfile(role);
  const statCards = getDashboardCardsForRole(role);

  useEffect(() => {
    let active = true;

    const load = async () => {
      try {
        setLoading(true);
        const data = await request("/dashboard");
        if (active) {
          setStats(data);
          setError("");
        }
      } catch (err) {
        if (active) {
          setError(err.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    load();
    return () => {
      active = false;
    };
  }, []);

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>{profile.dashboardTitle}</h2>
          <p className="muted">{profile.dashboardSubtitle}</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {loading && <div className="panel">Loading dashboard data...</div>}

      {stats && (
        <>
          <div className="stat-grid">
            {statCards.map((card) => (
              <article className="stat-card" key={card.key}>
                <span className="stat-label">{card.label}</span>
                <strong className="stat-value">
                  {card.currency
                    ? `$${Number(stats[card.key] || 0).toLocaleString()}`
                    : stats[card.key] ?? 0}
                </strong>
              </article>
            ))}
          </div>

          <div className="panel">
            <div className="panel-header">
              <h3>Recent Transactions</h3>
            </div>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Product</th>
                    <th>Warehouse</th>
                    <th>Quantity</th>
                    <th>Time</th>
                  </tr>
                </thead>
                <tbody>
                  {(stats.recentTransactions || []).map((row) => (
                    <tr key={row.id}>
                      <td>{row.id}</td>
                      <td><span className="badge">{row.type}</span></td>
                      <td>{row.product?.name}</td>
                      <td>{row.warehouse?.name}</td>
                      <td>{row.quantity}</td>
                      <td>{row.createdAt ? new Date(row.createdAt).toLocaleString() : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </section>
  );
};

export default Dashboard;
