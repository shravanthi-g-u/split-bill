import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { getAllBills } from "../api";
import { useAuth } from "../AuthContext";

function BillList() {
  const { token } = useAuth();
  const [bills, setBills] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadBills();
  }, []);

  async function loadBills() {
    try {
      const data = await getAllBills(token);
      setBills(data);
    } catch (err) {
      setError(err.message);
    }
  }

  if (error) return <p className="error-text">{error}</p>;

  return (
    <div className="page">
      <h1>My Bills</h1>

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
                {bill.createdAt &&
                  ` — ${new Date(bill.createdAt).toLocaleDateString()}`}
              </span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default BillList;
