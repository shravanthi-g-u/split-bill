const BASE_URL = "http://localhost:8080/api/bills";

export async function createBill(name, memberNames) {
    const response = await fetch(BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, memberNames }),
    });
    if (!response.ok) {
        throw new Error("Failed to create bill");
    }
    return response.json();
}
export async function getBill(id) {
    const response = await fetch(`${BASE_URL}/${id}`);
    if (!response.ok) {
        throw new Error("Failed to fetch bill");
    }
    return response.json();
}

export async function addItem(billId, item) {
    const response = await fetch(`${BASE_URL}/${billId}/items`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(item),
    });
    if (!response.ok) {
        throw new Error("Failed to add item");
    }
    return response.json();
}

export async function splitBill(id) {
    const response = await fetch(`${BASE_URL}/${id}/split`);
    if (!response.ok) {
        throw new Error("Failed to split bill");
    }
    return response.json();
}

export async function scanBill(billId, file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${BASE_URL}/${billId}/scan`, {
        method: "POST",
        body: formData,
    });
    if (!response.ok) {
        throw new Error("Failed to scan receipt");
    }
    return response.json();
}

export async function addItemsBulk(billId, items) {
    const response = await fetch(`${BASE_URL}/${billId}/items/bulk`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ items }),
    });
    if (!response.ok) {
        throw new Error("Failed to add scanned items");
    }
    return response.json();
}

export async function getAllBills() {
    const response = await fetch(BASE_URL);
    if (!response.ok) {
        throw new Error("Failed to fetch bills");
    }
    return response.json();
}

export async function getSummary(billIds) {
    const idsParam = billIds.join(",");
    const response = await fetch(`${BASE_URL}/summary?ids=${idsParam}`);
    if (!response.ok) {
        throw new Error("Failed to fetch summary");
    }
    return response.json();
}