import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getBill, addItem, splitBill, scanBill, addItemsBulk } from "../api";
import { useAuth } from "../AuthContext";

function BillDetail() {
  const { id } = useParams();
  const { token } = useAuth();
  const [bill, setBill] = useState(null);
  const [error, setError] = useState(null);
  const [splitResult, setSplitResult] = useState(null);

  const [itemName, setItemName] = useState("");
  const [itemPrice, setItemPrice] = useState("");
  const [excludedNames, setExcludedNames] = useState([]);

  const [scanFile, setScanFile] = useState(null);
  const [reviewItems, setReviewItems] = useState([]);
  const [scanning, setScanning] = useState(false);
  const [confirming, setConfirming] = useState(false);

  useEffect(() => {
    loadBill();
  }, [id]);

  async function loadBill() {
    try {
      const data = await getBill(id, token);
      setBill(data);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleSplit() {
    try {
      const result = await splitBill(id, token);
      setSplitResult(result);
    } catch (err) {
      setError(err.message);
    }
  }

  function toggleExcluded(name) {
    setExcludedNames((prev) =>
      prev.includes(name) ? prev.filter((n) => n !== name) : [...prev, name]
    );
  }

  async function handleAddItem(e) {
    e.preventDefault();
    setError(null);

    const price = parseFloat(itemPrice);
    if (!itemName.trim() || isNaN(price)) {
      setError("Enter a valid item name and price.");
      return;
    }

    try {
      await addItem(id, {
        name: itemName.trim(),
        price,
        excludedMemberNames: excludedNames,
      }, token);
      setItemName("");
      setItemPrice("");
      setExcludedNames([]);
      setSplitResult(null);
      await loadBill();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleScan(e) {
    e.preventDefault();
    if (!scanFile) {
      setError("Choose an image first.");
      return;
    }

    setError(null);
    setScanning(true);
    try {
      const drafts = await scanBill(id, scanFile, token);
      const editable = drafts.map((d) => ({
        name: d.name,
        price: d.price !== null ? d.price : 0,
        excludedMemberNames: [],
      }));
      setReviewItems(editable);
    } catch (err) {
      setError(err.message);
    } finally {
      setScanning(false);
    }
  }

  function updateReviewItem(index, field, value) {
    setReviewItems((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [field]: value } : item))
    );
  }

  function toggleReviewExclusion(index, memberName) {
    setReviewItems((prev) =>
      prev.map((item, i) => {
        if (i !== index) return item;
        const excluded = item.excludedMemberNames.includes(memberName)
          ? item.excludedMemberNames.filter((n) => n !== memberName)
          : [...item.excludedMemberNames, memberName];
        return { ...item, excludedMemberNames: excluded };
      })
    );
  }

  function removeReviewItem(index) {
    setReviewItems((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleConfirmScan() {
    setError(null);
    setConfirming(true);
    try {
      await addItemsBulk(id, reviewItems, token);
      setReviewItems([]);
      setScanFile(null);
      setSplitResult(null);
      await loadBill();
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirming(false);
    }
  }

  if (error) return <p className="error-text">{error}</p>;
  if (!bill) return <p>Loading...</p>;

  return (
    <div className="page">
      <h1>{bill.name}</h1>
      <p className="item-excluded" style={{ marginBottom: "24px" }}>
        Members: {bill.members.map((m) => m.name).join(", ")}
      </p>

      {error && <p className="error-text">{error}</p>}

      <div className="bill-detail-grid">
        <div>
          <div className="card">
            <h3>Items</h3>
            {bill.items.length === 0 ? (
              <p className="item-excluded">No items yet.</p>
            ) : (
              bill.items.map((item, index) => (
                <div className="item-row" key={index}>
                  <div>
                    <span className="item-name">{item.name}</span>
                    {item.excludedMembers?.length > 0 && (
                      <span className="item-excluded">
                        excluding: {item.excludedMembers.map((m) => m.name).join(", ")}
                      </span>
                    )}
                  </div>
                  <span className="item-price">₹{item.price.toFixed(2)}</span>
                </div>
              ))
            )}
          </div>

          <div className="card">
            <h3>Add Item</h3>
            <form onSubmit={handleAddItem}>
              <div className="form-row">
                <input
                  type="text"
                  placeholder="Item name"
                  value={itemName}
                  onChange={(e) => setItemName(e.target.value)}
                />
                <input
                  type="number"
                  step="0.01"
                  placeholder="Price"
                  value={itemPrice}
                  onChange={(e) => setItemPrice(e.target.value)}
                />
              </div>

              <div className="checkbox-group">
                {bill.members.map((m) => (
                  <label key={m.name}>
                    <input
                      type="checkbox"
                      checked={excludedNames.includes(m.name)}
                      onChange={() => toggleExcluded(m.name)}
                    />
                    {m.name}
                  </label>
                ))}
              </div>

              <button type="submit">Add Item</button>
            </form>
          </div>
        </div>

        <div>
          <div className="card">
            <h3>Scan Receipt</h3>
            <form onSubmit={handleScan}>
              <div className="form-group">
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => setScanFile(e.target.files[0])}
                />
              </div>
              <button type="submit" disabled={scanning}>
                {scanning ? "Scanning..." : "Scan Receipt"}
              </button>
            </form>
          </div>

          <div className="card">
            <h3>Split</h3>
            <button onClick={handleSplit}>Calculate Split</button>

            {splitResult && (
              <div style={{ marginTop: "16px" }}>
                {Object.entries(splitResult).map(([name, amount]) => (
                  <div className="split-result-row" key={name}>
                    <span>{name}</span>
                    <span className="amount">₹{amount.toFixed(2)}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {reviewItems.length > 0 && (
        <div className="card">
          <h3>Review Scanned Items</h3>
          <table className="review-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Price</th>
                <th>Exclude</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {reviewItems.map((item, index) => (
                <tr key={index}>
                  <td>
                    <input
                      type="text"
                      value={item.name}
                      onChange={(e) => updateReviewItem(index, "name", e.target.value)}
                    />
                  </td>
                  <td>
                    <input
                      type="number"
                      step="0.01"
                      value={item.price}
                      onChange={(e) =>
                        updateReviewItem(index, "price", parseFloat(e.target.value) || 0)
                      }
                    />
                  </td>
                  <td>
                    <div className="checkbox-group">
                      {bill.members.map((m) => (
                        <label key={m.name}>
                          <input
                            type="checkbox"
                            checked={item.excludedMemberNames.includes(m.name)}
                            onChange={() => toggleReviewExclusion(index, m.name)}
                          />
                          {m.name}
                        </label>
                      ))}
                    </div>
                  </td>
                  <td>
                    <button
                      type="button"
                      className="remove-btn"
                      onClick={() => removeReviewItem(index)}
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <button onClick={handleConfirmScan} disabled={confirming} style={{ marginTop: "16px" }}>
            {confirming ? "Adding..." : "Confirm & Add All"}
          </button>
        </div>
      )}
    </div>
  );
}

export default BillDetail;