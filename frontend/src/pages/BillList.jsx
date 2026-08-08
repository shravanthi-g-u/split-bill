import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { getAllBills } from "../api";

function BillList() {
  const [bills, setBills] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadBills();
  }, []);

  async function loadBills() {
    try {
      const data = await getAllBills();
      setBills(data);
    } catch (err) {
      setError(err.message);
    }
  }

  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <div className="page">
      <h1>My Bills</h1>

      {error && <p className="error-text">{error}</p>}

      {bills.length === 0 ? (
        <div className="card">
          <p>
            No bills yet. <Link to="/bills/new">Create one</Link>.
          </p>
        </div>
      ) : (
        <div className="card">
          {bills.map((bill) => (
            <Link
              to={`/bills/${bill.id}`}
              className="bill-list-item"
              key={bill.id}
            >
              <span className="bill-name">{bill.name}</span>
              <span className="bill-meta">
                {bill.members.map((m) => m.name).join(", ")} —{" "}
                {bill.items.length} item
                {bill.items.length !== 1 ? "s" : ""}
              </span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default BillList;
