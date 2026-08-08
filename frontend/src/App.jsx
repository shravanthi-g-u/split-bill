import { Routes, Route, Link } from "react-router-dom";
import BillList from "./pages/BillList.jsx";
import CreateBill from "./pages/CreateBill.jsx";
import BillDetail from "./pages/BillDetail.jsx";
import Summary from "./pages/Summary.jsx";
import "./App.css";

function App() {
  return (
    <div className="app">
      <nav>
        <Link to="/">My Bills</Link> | <Link to="/bills/new">+ New Bill</Link> |{" "}
        <Link to="/summary">Combined Summary</Link>
      </nav>

      <Routes>
        <Route path="/" element={<BillList />} />
        <Route path="/bills/new" element={<CreateBill />} />
        <Route path="/bills/:id" element={<BillDetail />} />
        <Route path="/summary" element={<Summary />} />
      </Routes>
    </div>
  );
}

export default App;