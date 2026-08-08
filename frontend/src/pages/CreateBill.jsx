import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createBill } from "../api";

function CreateBill() {
  const navigate = useNavigate();
  const [billName, setBillName] = useState("");
  const [memberNames, setMemberNames] = useState([""]);
  const [error, setError] = useState(null);

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

    const cleanedNames = memberNames.map((n) => n.trim()).filter((n) => n !== "");
    if (cleanedNames.length === 0) {
      setError("Add at least one member.");
      return;
    }

    try {
      const bill = await createBill(billName.trim(), cleanedNames);
      navigate(`/bills/${bill.id}`);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div>
      <h1>Create Bill</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Bill Name
          <input
            type="text"
            value={billName}
            onChange={(e) => setBillName(e.target.value)}
            placeholder="e.g. Goa Trip"
          />
        </label>

        <h3>Members</h3>
        {memberNames.map((name, index) => (
          <div key={index} className="member-row">
            <input
              type="text"
              value={name}
              onChange={(e) => updateMemberName(index, e.target.value)}
              placeholder={`Member ${index + 1}`}
            />
            {memberNames.length > 1 && (
              <button type="button" onClick={() => removeMemberField(index)}>
                Remove
              </button>
            )}
          </div>
        ))}
        <button type="button" onClick={addMemberField}>
          + Add Member
        </button>

        <br />
        <button type="submit">Create Bill</button>
      </form>

      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
}

export default CreateBill;