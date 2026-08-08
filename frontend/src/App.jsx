import { Routes, Route, Link, useNavigate } from "react-router-dom";
import BillList from "./pages/BillList.jsx";
import CreateBill from "./pages/CreateBill.jsx";
import BillDetail from "./pages/BillDetail.jsx";
import Summary from "./pages/Summary.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import { useAuth } from "./AuthContext.jsx";
import "./App.css";

function App() {
  const { token, username, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="app">
      <nav className="navbar">
        <span className="brand">SplitEase</span>
        {token ? (
          <>
            <Link to="/">My Bills</Link>
            <Link to="/bills/new">+ New Bill</Link>
            <Link to="/summary">Combined Summary</Link>
            <span style={{ marginLeft: "auto" }}>
              {username} · <button onClick={handleLogout}>Log Out</button>
            </span>
          </>
        ) : (
          <span style={{ marginLeft: "auto" }}>
            <Link to="/login">Log In</Link> · <Link to="/register">Register</Link>
          </span>
        )}
      </nav>

      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={token ? <BillList /> : <Login />} />
        <Route path="/bills/new" element={token ? <CreateBill /> : <Login />} />
        <Route path="/bills/:id" element={token ? <BillDetail /> : <Login />} />
        <Route path="/summary" element={token ? <Summary /> : <Login />} />
      </Routes>
    </div>
  );
}

export default App;