const BASE_URL = "https://split-bill-woah.onrender.com/api/bills";
const AUTH_URL = "https://split-bill-woah.onrender.com/api/auth";

async function authFetch(url, options = {}, token) {
    const headers = {
        ...(options.headers || {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
    const response = await fetch(url, { ...options, headers });
    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        throw new Error(errorBody.message || "Request failed");
    }
    return response.json();
}

export async function registerUser(username, password) {
    const response = await fetch(`${AUTH_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
    });
    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        throw new Error(errorBody.message || "Registration failed");
    }
    return response.json();
}

export async function loginUser(username, password) {
    const response = await fetch(`${AUTH_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
    });
    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        throw new Error(errorBody.message || "Login failed");
    }
    return response.json();
}

export async function createBill(name, memberNames, token) {
    return authFetch(BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, memberNames }),
    }, token);
}

export async function getBill(id, token) {
    return authFetch(`${BASE_URL}/${id}`, {}, token);
}

export async function addItem(billId, item, token) {
    return authFetch(`${BASE_URL}/${billId}/items`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(item),
    }, token);
}

export async function splitBill(id, token) {
    return authFetch(`${BASE_URL}/${id}/split`, {}, token);
}

export async function scanBill(billId, file, token) {
    const formData = new FormData();
    formData.append("file", file);
    return authFetch(`${BASE_URL}/${billId}/scan`, {
        method: "POST",
        body: formData,
    }, token);
}

export async function addItemsBulk(billId, items, token) {
    return authFetch(`${BASE_URL}/${billId}/items/bulk`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ items }),
    }, token);
}

export async function getAllBills(token) {
    return authFetch(BASE_URL, {}, token);
}

export async function getSummary(billIds, token) {
    const idsParam = billIds.join(",");
    return authFetch(`${BASE_URL}/summary?ids=${idsParam}`, {}, token);
}