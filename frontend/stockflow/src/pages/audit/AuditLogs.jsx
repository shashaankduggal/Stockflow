import { useEffect, useState } from "react";
import { request } from "../../services/api";

const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadLogs = async () => {
      try {
        setLogs(await request("/audit-logs"));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    loadLogs();
  }, []);

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <h2>Audit Logs</h2>
          <p className="muted">Review inventory actions by user and time.</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      <div className="panel">
        {loading ? (
          <div>Loading audit logs...</div>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Action</th>
                  <th>Details</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td>{log.userName}</td>
                    <td>{log.userEmail}</td>
                    <td><span className="badge">{log.action}</span></td>
                    <td>{log.details}</td>
                    <td>{log.createdAt ? new Date(log.createdAt).toLocaleString() : "-"}</td>
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

export default AuditLogs;
