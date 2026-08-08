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
    <div>
      <h1>My Bills</h1>

      {bills.length === 0 ? (
        <p>No bills yet. <Link to="/bills/new">Create one</Link>.</p>
      ) : (
        <ul>
          {bills.map((bill) => (
            <li key={bill.id}>
              <Link to={`/bills/${bill.id}`}>{bill.name}</Link>
              {" — "}
              {bill.members.map((m) => m.name).join(", ")}
              {" — "}
              {bill.items.length} item{bill.items.length !== 1 ? "s" : ""}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default BillList;