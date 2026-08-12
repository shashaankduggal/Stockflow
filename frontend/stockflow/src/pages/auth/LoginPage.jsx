import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getLandingPath } from "../../auth/access";
import { login, setAuth } from "../../services/api";
import "./LoginPage.css";

const LoginPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const validate = () => {
    if (!email.trim()) {
      return "Email is required.";
    }
    if (!/\S+@\S+\.\S+/.test(email)) {
      return "Please enter a valid email address.";
    }
    if (!password) {
      return "Password is required.";
    }
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await login(email, password);
      setAuth(response);
      navigate(getLandingPath(response?.role), { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-badge">StockFlow</div>
        <h1>Enterprise Inventory Management</h1>
        <p className="subtitle">Sign in to see live stock, warehouse, and dashboard data.</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@stockflow.com"
              autoComplete="email"
            />
          </label>

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              autoComplete="current-password"
            />
          </label>

          {error && <div className="error-banner">{error}</div>}

          <button className="button login-button" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="auth-switch">
          New to StockFlow? <Link to="/signup">Create an account</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
