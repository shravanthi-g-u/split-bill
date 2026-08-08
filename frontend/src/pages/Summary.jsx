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
  <div className="page">
    <h1>Combined Summary</h1>

    {bills.length === 0 ? (
      <div className="card">
        <p>No bills yet.</p>
      </div>
    ) : (
      <div className="card">
        <h3>Select Bills</h3>
        <div className="checkbox-group" style={{ flexDirection: "column", alignItems: "flex-start" }}>
          {bills.map((bill) => (
            <label key={bill.id}>
              <input
                type="checkbox"
                checked={selectedIds.includes(bill.id)}
                onChange={() => toggleSelected(bill.id)}
              />
              {bill.name} ({bill.members.map((m) => m.name).join(", ")})
            </label>
          ))}
        </div>

        <button onClick={handleGetSummary} style={{ marginTop: "16px" }}>
          Get Combined Total
        </button>
      </div>
    )}

    {error && <p className="error-text">{error}</p>}

    {summary && (
      <>
        <div className="card">
          <h3>Combined Totals</h3>
          {Object.entries(summary.combinedTotals).map(([name, amount]) => (
            <div className="split-result-row" key={name}>
              <span>{name}</span>
              <span className="amount">₹{amount.toFixed(2)}</span>
            </div>
          ))}
        </div>

        <div className="card">
          <h3>Per-Bill Breakdown</h3>
          {Object.entries(summary.perBill).map(([billId, totals]) => {
            const billName = bills.find((b) => b.id === billId)?.name || billId;
            return (
              <div key={billId} style={{ marginBottom: "20px" }}>
                <h4>{billName}</h4>
                {Object.entries(totals).map(([name, amount]) => (
                  <div className="split-result-row" key={name}>
                    <span>{name}</span>
                    <span className="amount">₹{amount.toFixed(2)}</span>
                  </div>
                ))}
              </div>
            );
          })}
        </div>
      </>
    )}
  </div>
);
}

export default Summary;