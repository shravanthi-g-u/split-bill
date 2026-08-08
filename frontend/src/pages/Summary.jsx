import { useState, useEffect } from "react";
import { getAllBills, getSummary } from "../api";

function Summary() {
  const [bills, setBills] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [summary, setSummary] = useState(null);
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

  function toggleSelected(id) {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  }

  async function handleGetSummary() {
    if (selectedIds.length === 0) {
      setError("Select at least one bill.");
      return;
    }
    setError(null);
    try {
      const result = await getSummary(selectedIds);
      setSummary(result);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div>
      <h1>Combined Summary</h1>

      {bills.length === 0 ? (
        <p>No bills yet.</p>
      ) : (
        <div>
          <h3>Select Bills</h3>
          {bills.map((bill) => (
            <label key={bill.id} style={{ display: "block" }}>
              <input
                type="checkbox"
                checked={selectedIds.includes(bill.id)}
                onChange={() => toggleSelected(bill.id)}
              />
              {bill.name} ({bill.members.map((m) => m.name).join(", ")})
            </label>
          ))}

          <button onClick={handleGetSummary}>Get Combined Total</button>
        </div>
      )}

      {error && <p style={{ color: "red" }}>{error}</p>}

      {summary && (
        <div>
          <h3>Combined Totals</h3>
          <ul>
            {Object.entries(summary.combinedTotals).map(([name, amount]) => (
              <li key={name}>
                {name}: ₹{amount.toFixed(2)}
              </li>
            ))}
          </ul>

          <h4>Per-Bill Breakdown</h4>
          {Object.entries(summary.perBill).map(([billId, totals]) => {
            const billName = bills.find((b) => b.id === billId)?.name || billId;
            return (
              <div key={billId}>
                <strong>{billName}</strong>
                <ul>
                  {Object.entries(totals).map(([name, amount]) => (
                    <li key={name}>
                      {name}: ₹{amount.toFixed(2)}
                    </li>
                  ))}
                </ul>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default Summary;