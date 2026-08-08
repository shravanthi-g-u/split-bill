import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createBill } from "../api";
import { useAuth } from "../AuthContext";

function CreateBill() {
  const navigate = useNavigate();
  const [billName, setBillName] = useState("");
  const [memberNames, setMemberNames] = useState([""]);
  const [error, setError] = useState(null);
  const { token } = useAuth();

  function updateMemberName(index, value) {
    const updated = [...memberNames];
    updated[index] = value;
    setMemberNames(updated);
  }

  function addMemberField() {
    setMemberNames([...memberNames, ""]);
  }

  function removeMemberField(index) {
    setMemberNames(memberNames.filter((_, i) => i !== index));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    const cleanedNames = memberNames
      .map((n) => n.trim())
      .filter((n) => n !== "");
    if (cleanedNames.length === 0) {
      setError("Add at least one member.");
      return;
    }

    try {
      const bill = await createBill(billName.trim(), cleanedNames, token);
      navigate(`/bills/${bill.id}`);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <h1>Create Bill</h1>

      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Bill Name</label>
            <input
              type="text"
              value={billName}
              onChange={(e) => setBillName(e.target.value)}
              placeholder="e.g. Goa Trip"
            />
          </div>

          <h4>Members</h4>
          {memberNames.map((name, index) => (
            <div className="form-row" key={index}>
              <input
                type="text"
                value={name}
                onChange={(e) => updateMemberName(index, e.target.value)}
                placeholder={`Member ${index + 1}`}
              />
              {memberNames.length > 1 && (
                <button
                  type="button"
                  className="remove-btn"
                  onClick={() => removeMemberField(index)}
                >
                  Remove
                </button>
              )}
            </div>
          ))}
          <button
            type="button"
            onClick={addMemberField}
            style={{ marginBottom: "20px" }}
          >
            + Add Member
          </button>

          <br />
          <button type="submit">Create Bill</button>
        </form>

        {error && <p className="error-text">{error}</p>}
      </div>
    </div>
  );
}

export default CreateBill;
