import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../api";

function Register() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    try {
      await registerUser(username.trim(), password);
      setSuccess(true);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <h1>Create an Account</h1>

      <div className="card">
        {success ? (
          <div>
            <p>Account created successfully!</p>
            <Link to="/login">Log in now</Link>
          </div>
        ) : (
          <>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="At least 6 characters"
                />
              </div>

              <button type="submit">Register</button>
            </form>

            {error && <p className="error-text">{error}</p>}

            <p style={{ marginTop: "16px" }}>
              Already have an account? <Link to="/login">Log in</Link>
            </p>
          </>
        )}
      </div>
    </div>
  );
}

export default Register;