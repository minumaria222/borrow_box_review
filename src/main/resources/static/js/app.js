const API = "/api";
let students = [];
let currentStudent = null;
let toastTimer;

const escapeHtml = (value = "") => String(value).replace(/[&<>"']/g, character => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
}[character]));

async function api(path, options = {}) {
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
  const response = await fetch(`${API}${path}`, { ...options, headers });
  const text = response.status === 204 ? "" : await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`);
  return data;
}

function showToast(message, isError = false) {
  const toast = document.querySelector("#toast");
  if (!toast) return;
  toast.textContent = message;
  toast.classList.toggle("is-error", isError);
  toast.classList.add("is-visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("is-visible"), 3400);
}

function safeImage(url) {
  if (!url) return "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=960&q=80";
  try {
    const parsed = new URL(url, window.location.origin);
    return ["https:", "http:"].includes(parsed.protocol) ? parsed.href : "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=960&q=80";
  } catch {
    return "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=960&q=80";
  }
}

function imageFallbacks(root = document) {
  root.querySelectorAll("img[data-fallback]").forEach(image => {
    image.addEventListener("error", () => {
      image.src = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=960&q=80";
      image.removeAttribute("data-fallback");
    }, { once: true });
  });
}

function itemCard(item) {
  const available = item.available;
  return `<article class="item-card">
    <a href="details.html?id=${encodeURIComponent(item.id)}" aria-label="View ${escapeHtml(item.name)}">
      <div class="item-image-wrap"><img class="item-image" data-fallback src="${escapeHtml(safeImage(item.imageUrl))}" alt="${escapeHtml(item.name)}"><span class="availability-pill ${available ? "" : "unavailable"}">${available ? "Available" : "On loan"}</span></div>
      <div class="item-card-body"><span class="category-tag">${escapeHtml(item.category)}</span><h3>${escapeHtml(item.name)}</h3><div class="item-meta"><span class="item-owner"><i class="bi bi-person"></i>${escapeHtml(item.ownerName)}</span><span>${escapeHtml(item.condition)}</span></div><div class="item-card-link"><span>View details</span><i class="bi bi-arrow-up-right"></i></div></div>
    </a>
  </article>`;
}

function statusClass(status) {
  return `status-${String(status).toLowerCase()}`;
}

function formatDate(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", year: "numeric" }).format(new Date(`${value.slice(0, 10)}T12:00:00`));
}

function dateInputValue(date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function setUpNavigation() {
  const toggle = document.querySelector(".nav-toggle");
  const links = document.querySelector(".nav-links");
  toggle?.addEventListener("click", () => {
    const open = links.classList.toggle("is-open");
    toggle.setAttribute("aria-expanded", String(open));
    toggle.innerHTML = open ? '<i class="bi bi-x-lg"></i>' : '<i class="bi bi-list"></i>';
  });
  document.querySelectorAll("#currentStudent").forEach(select => {
    select.addEventListener("change", () => {
      localStorage.setItem("borrowbox.studentId", select.value);
      currentStudent = students.find(student => String(student.id) === select.value) || students[0];
      document.querySelectorAll("#currentStudent").forEach(other => { other.value = String(currentStudent.id); });
      renderPage();
    });
  });
}

async function initialize() {
  setUpNavigation();
  try {
    students = await api("/students");
    if (!students.length) throw new Error("No demo students were found. Restart the app to initialize demo data.");
    const savedId = localStorage.getItem("borrowbox.studentId");
    currentStudent = students.find(student => String(student.id) === savedId) || students[0];
    document.querySelectorAll("#currentStudent").forEach(select => {
      select.innerHTML = students.map(student => `<option value="${student.id}">${escapeHtml(student.name)} · ${escapeHtml(student.department)}</option>`).join("");
      select.value = String(currentStudent.id);
    });
    renderPage();
  } catch (error) {
    showToast(error.message || "Could not connect to BorrowBox. Check that the Java server and MySQL are running.", true);
    document.querySelectorAll("main .loading-state").forEach(element => { element.textContent = "BorrowBox could not reach its database. Check the server, then refresh."; });
  }
}

function renderPage() {
  const page = document.body.dataset.page;
  if (page === "home") renderHome();
  if (page === "browse") renderBrowse();
  if (page === "details") renderDetails();
  if (page === "requests") renderMyRequests();
  if (page === "owner") renderOwnerDashboard();
  if (page === "listing") renderListingPage();
}

async function renderHome() {
  const container = document.querySelector("#featuredItems");
  if (!container) return;
  try {
    const items = await api("/items");
    const featured = items.filter(item => item.available).slice(0, 4);
    container.innerHTML = featured.length ? featured.map(itemCard).join("") : '<div class="empty-panel">No items have been listed yet.</div>';
    imageFallbacks(container);
  } catch (error) {
    container.innerHTML = `<p class="empty-panel">${escapeHtml(error.message)}</p>`;
  }
}

async function renderBrowse() {
  const container = document.querySelector("#browseItems");
  if (!container) return;
  let items;
  try {
    items = await api("/items");
  } catch (error) {
    container.innerHTML = `<p class="empty-panel">${escapeHtml(error.message)}</p>`;
    return;
  }
  const categories = [...new Set(items.map(item => item.category))].sort((a, b) => a.localeCompare(b));
  const categorySelect = document.querySelector("#categoryFilter");
  if (categorySelect) categorySelect.innerHTML = '<option value="">All categories</option>' + categories.map(category => `<option value="${escapeHtml(category)}">${escapeHtml(category)}</option>`).join("");
  const applyFilters = () => {
    const query = document.querySelector("#searchItems").value.trim().toLowerCase();
    const category = categorySelect.value;
    const availability = document.querySelector("#availabilityFilter").value;
    const filtered = items.filter(item => {
      const matchesText = `${item.name} ${item.category} ${item.ownerName} ${item.description}`.toLowerCase().includes(query);
      const matchesCategory = !category || item.category === category;
      const matchesAvailability = !availability || (availability === "available" ? item.available : !item.available);
      return matchesText && matchesCategory && matchesAvailability;
    });
    document.querySelector("#resultsCount").textContent = `${filtered.length} ${filtered.length === 1 ? "item" : "items"} in the collection`;
    container.innerHTML = filtered.map(itemCard).join("");
    document.querySelector("#browseEmpty").classList.toggle("d-none", filtered.length > 0);
    container.classList.toggle("d-none", filtered.length === 0);
    imageFallbacks(container);
  };
  document.querySelector("#searchItems").addEventListener("input", applyFilters);
  categorySelect.addEventListener("change", applyFilters);
  document.querySelector("#availabilityFilter").addEventListener("change", applyFilters);
  document.querySelector("#clearFilters").addEventListener("click", () => {
    document.querySelector("#searchItems").value = "";
    categorySelect.value = "";
    document.querySelector("#availabilityFilter").value = "";
    applyFilters();
  });
  applyFilters();
}

async function renderDetails() {
  const container = document.querySelector("#itemDetail");
  const itemId = new URLSearchParams(window.location.search).get("id");
  if (!itemId) {
    container.innerHTML = '<div class="empty-panel"><i class="bi bi-box"></i><h2>Choose an item first</h2><p>Browse the collection to find something to borrow.</p><a class="button button-primary" href="browse.html">Browse items</a></div>';
    return;
  }
  try {
    const item = await api(`/items/${encodeURIComponent(itemId)}`);
    document.title = `${item.name} | BorrowBox`;
    const isOwner = String(item.ownerId) === String(currentStudent.id);
    container.className = "detail-layout";
    container.innerHTML = `<div class="detail-image-wrap"><img class="detail-image" data-fallback src="${escapeHtml(safeImage(item.imageUrl))}" alt="${escapeHtml(item.name)}"><span class="availability-pill ${item.available ? "" : "unavailable"}">${item.available ? "Available to borrow" : "Currently on loan"}</span></div>
      <div class="detail-content"><span class="category-tag">${escapeHtml(item.category)}</span><h1>${escapeHtml(item.name)}</h1><p class="detail-description">${escapeHtml(item.description)}</p><div class="detail-owner"><span class="owner-avatar">${escapeHtml(item.ownerName.slice(0, 1).toUpperCase())}</span><span><strong>Shared by ${escapeHtml(item.ownerName)}</strong><small>${escapeHtml(item.department)} · ${escapeHtml(item.location)}</small></span></div><div class="detail-facts"><div class="detail-fact"><span>Condition</span><strong>${escapeHtml(item.condition)}</strong></div><div class="detail-fact"><span>Availability</span><strong>${item.available ? "Available now" : "Currently borrowed"}</strong></div><div class="detail-fact"><span>Department</span><strong>${escapeHtml(item.department)}</strong></div><div class="detail-fact"><span>Borrowing limit</span><strong>Up to ${item.maxBorrowDays} days</strong></div></div><div class="borrow-action">${isOwner ? '<button class="button button-outline" disabled><i class="bi bi-person-lock"></i> This is your listing</button>' : item.available ? '<button id="showBorrowForm" class="button button-primary"><i class="bi bi-send"></i> Borrow this item</button><div id="borrowFormMount"></div>' : '<button class="button button-outline" disabled><i class="bi bi-clock-history"></i> Not available right now</button>'}</div></div>`;
    imageFallbacks(container);
    document.querySelector("#showBorrowForm")?.addEventListener("click", () => mountBorrowForm(item));
  } catch (error) {
    container.className = "detail-loading";
    container.innerHTML = `<div class="empty-panel"><i class="bi bi-exclamation-circle"></i><h2>We couldn't find that item</h2><p>${escapeHtml(error.message)}</p><a class="button button-outline" href="browse.html">Back to browse</a></div>`;
  }
}

function mountBorrowForm(item) {
  const mount = document.querySelector("#borrowFormMount");
  const trigger = document.querySelector("#showBorrowForm");
  const start = new Date();
  start.setDate(start.getDate() + 1);
  const end = new Date(start);
  end.setDate(end.getDate() + Math.min(item.maxBorrowDays, 3));
  trigger.classList.add("d-none");
  mount.innerHTML = `<div class="request-form-wrap"><h2>Request to borrow</h2><form id="borrowRequestForm" class="app-form"><label class="form-field"><span>Borrowing dates</span><div class="form-row"><input id="borrowStart" type="date" min="${dateInputValue(new Date())}" value="${dateInputValue(start)}" required aria-label="Borrowing start date"><input id="borrowEnd" type="date" value="${dateInputValue(end)}" required aria-label="Expected return date"></div></label><label class="form-field"><span>Message <small>Optional</small></span><textarea id="borrowMessage" maxlength="1000" rows="2" placeholder="A note for ${escapeHtml(item.ownerName)}"></textarea></label><p class="form-error d-none" id="borrowError"></p><button class="button button-primary" type="submit"><i class="bi bi-send"></i> Send request</button></form><p class="form-footnote">The owner will review your request before confirming.</p></div>`;
  document.querySelector("#borrowStart").addEventListener("change", event => {
    const minEnd = new Date(`${event.target.value}T12:00:00`);
    minEnd.setDate(minEnd.getDate() + 1);
    const maxEnd = new Date(`${event.target.value}T12:00:00`);
    maxEnd.setDate(maxEnd.getDate() + item.maxBorrowDays);
    const endInput = document.querySelector("#borrowEnd");
    endInput.min = dateInputValue(minEnd);
    endInput.max = dateInputValue(maxEnd);
    if (endInput.value < endInput.min || endInput.value > endInput.max) endInput.value = dateInputValue(minEnd);
  });
  document.querySelector("#borrowRequestForm").addEventListener("submit", async event => {
    event.preventDefault();
    const error = document.querySelector("#borrowError");
    error.classList.add("d-none");
    const button = event.currentTarget.querySelector("button[type=submit]");
    button.disabled = true;
    try {
      await api("/requests", { method: "POST", body: JSON.stringify({
        studentId: currentStudent.id,
        itemId: item.id,
        startDate: document.querySelector("#borrowStart").value,
        expectedReturnDate: document.querySelector("#borrowEnd").value,
        message: document.querySelector("#borrowMessage").value.trim() || null
      }) });
      showToast("Your borrowing request was sent.");
      mount.innerHTML = '<div class="request-form-wrap"><span class="status-badge status-pending"><i class="bi bi-check2"></i>&nbsp; Request sent</span><h2 class="mt-3">You’re all set.</h2><p class="detail-description">The owner can now review your dates and message.</p><a class="button button-primary" href="requests.html">View my requests <i class="bi bi-arrow-right"></i></a></div>';
    } catch (requestError) {
      error.textContent = requestError.message;
      error.classList.remove("d-none");
      button.disabled = false;
    }
  });
}

function requestCard(request, mode) {
  const otherPerson = mode === "mine" ? `Owner: ${request.ownerName}` : `Requested by ${request.studentName} · ${request.studentDepartment}`;
  const details = `<span><i class="bi bi-calendar3"></i>${formatDate(request.startDate)} – ${formatDate(request.expectedReturnDate)}</span><span><i class="bi bi-clock"></i>Requested ${formatDate(request.requestedAt)}</span>`;
  let actions = "";
  if (mode === "mine" && request.status === "PENDING") actions = `<button class="button button-small button-danger" data-action="cancel" data-id="${request.id}"><i class="bi bi-x-lg"></i> Cancel</button>`;
  if (mode === "owner" && request.status === "PENDING") actions = `<button class="button button-small button-success" data-action="approve" data-id="${request.id}"><i class="bi bi-check-lg"></i> Approve</button><button class="button button-small button-danger" data-action="reject" data-id="${request.id}"><i class="bi bi-x-lg"></i> Decline</button>`;
  if (mode === "owner" && request.status === "APPROVED") actions = `<button class="button button-small button-outline" data-action="return" data-id="${request.id}"><i class="bi bi-arrow-return-left"></i> Mark returned</button>`;
  return `<article class="request-card"><img class="request-thumb" data-fallback src="${escapeHtml(safeImage(request.itemImageUrl))}" alt="${escapeHtml(request.itemName)}"><div class="request-info"><div class="request-title-row"><h3>${escapeHtml(request.itemName)}</h3><span class="status-badge ${statusClass(request.status)}">${escapeHtml(request.status)}</span></div><p>${escapeHtml(otherPerson)}${request.message ? ` · “${escapeHtml(request.message)}”` : ""}</p><div class="request-detail-row">${details}</div></div><div class="request-actions">${actions}</div></article>`;
}

function setRequestActionHandlers(container, ownerId, mode) {
  container.querySelectorAll("[data-action]").forEach(button => {
    button.addEventListener("click", async () => {
      const action = button.dataset.action;
      const personParam = mode === "mine" ? `studentId=${ownerId}` : `ownerId=${ownerId}`;
      button.disabled = true;
      try {
        await api(`/requests/${button.dataset.id}/${action}?${personParam}`, { method: "PUT" });
        showToast(action === "return" ? "Item marked as returned and available again." : `Request ${action === "approve" ? "approved" : action === "reject" ? "declined" : "cancelled"}.`);
        renderPage();
      } catch (error) {
        showToast(error.message, true);
        button.disabled = false;
      }
    });
  });
}

async function renderMyRequests() {
  const container = document.querySelector("#myRequests");
  if (!container || !currentStudent) return;
  try {
    const requests = await api(`/requests/mine?studentId=${currentStudent.id}`);
    const pending = requests.filter(request => request.status === "PENDING").length;
    const active = requests.filter(request => request.status === "APPROVED").length;
    document.querySelector("#requestSummary").innerHTML = `<div class="summary-tile"><span class="summary-icon"><i class="bi bi-inbox"></i></span><span><strong>${requests.length}</strong><small>Total requests</small></span></div><div class="summary-tile"><span class="summary-icon"><i class="bi bi-hourglass-split"></i></span><span><strong>${pending}</strong><small>Waiting for owner</small></span></div><div class="summary-tile"><span class="summary-icon"><i class="bi bi-bookmark-check"></i></span><span><strong>${active}</strong><small>Currently borrowing</small></span></div>`;
    container.innerHTML = requests.length ? requests.map(request => requestCard(request, "mine")).join("") : '<div class="empty-panel"><i class="bi bi-inbox"></i><h2>No requests yet</h2><p>Find something useful in the campus collection.</p><a class="button button-primary" href="browse.html">Browse items</a></div>';
    imageFallbacks(container);
    setRequestActionHandlers(container, currentStudent.id, "mine");
  } catch (error) { container.innerHTML = `<div class="empty-panel">${escapeHtml(error.message)}</div>`; }
}

async function renderOwnerDashboard() {
  const container = document.querySelector("#incomingRequests");
  if (!container || !currentStudent) return;
  try {
    const [requests, ownedItems] = await Promise.all([
      api(`/requests/received?ownerId=${currentStudent.id}`),
      api(`/items?ownerId=${currentStudent.id}`)
    ]);
    const pending = requests.filter(request => request.status === "PENDING").length;
    const active = requests.filter(request => request.status === "APPROVED").length;
    document.querySelector("#ownerSummary").innerHTML = `<div class="summary-tile"><span class="summary-icon"><i class="bi bi-box-seam"></i></span><span><strong>${ownedItems.length}</strong><small>Your listings</small></span></div><div class="summary-tile"><span class="summary-icon"><i class="bi bi-bell"></i></span><span><strong>${pending}</strong><small>Requests to review</small></span></div><div class="summary-tile"><span class="summary-icon"><i class="bi bi-arrow-left-right"></i></span><span><strong>${active}</strong><small>Items currently lent</small></span></div>`;
    container.innerHTML = requests.length ? requests.map(request => requestCard(request, "owner")).join("") : '<div class="empty-panel"><i class="bi bi-inboxes"></i><h2>Nothing to review</h2><p>Requests for your listed items will appear here.</p><a class="button button-outline" href="list-item.html">Manage your listings</a></div>';
    imageFallbacks(container);
    setRequestActionHandlers(container, currentStudent.id, "owner");
  } catch (error) { container.innerHTML = `<div class="empty-panel">${escapeHtml(error.message)}</div>`; }
}

async function renderListingPage() {
  if (!currentStudent) return;
  const name = document.querySelector("#listingStudentName");
  if (name) name.textContent = currentStudent.name;
  const form = document.querySelector("#itemForm");
  const cancelEdit = document.querySelector("#cancelEditButton");
  form.onsubmit = async event => {
    event.preventDefault();
    const id = document.querySelector("#editingItemId").value;
    const payload = {
      name: document.querySelector("#itemName").value.trim(),
      category: document.querySelector("#itemCategory").value,
      description: document.querySelector("#itemDescription").value.trim(),
      conditionLabel: document.querySelector("#itemCondition").value,
      location: document.querySelector("#itemLocation").value.trim(),
      maxBorrowDays: Number(document.querySelector("#itemMaxDays").value),
      imageUrl: document.querySelector("#itemImageUrl").value.trim() || null,
      ownerId: currentStudent.id
    };
    const button = document.querySelector("#saveItemButton");
    button.disabled = true;
    try {
      await api(id ? `/items/${id}` : "/items", { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
      showToast(id ? "Your listing was updated." : "Your item is now listed.");
      resetListingForm();
      await loadOwnedItems();
    } catch (error) { showToast(error.message, true); }
    finally { button.disabled = false; }
  };
  cancelEdit.onclick = resetListingForm;
  await loadOwnedItems();
}

function resetListingForm() {
  document.querySelector("#itemForm").reset();
  document.querySelector("#itemMaxDays").value = 7;
  document.querySelector("#editingItemId").value = "";
  document.querySelector("#listingFormTitle").textContent = "New listing";
  document.querySelector("#saveItemButton").innerHTML = '<i class="bi bi-plus-lg"></i> Publish listing';
  document.querySelector("#cancelEditButton").classList.add("d-none");
}

async function loadOwnedItems() {
  const container = document.querySelector("#ownedItems");
  try {
    const items = await api(`/items?ownerId=${currentStudent.id}`);
    document.querySelector("#ownedCount").textContent = `${items.length} ${items.length === 1 ? "listing" : "listings"}`;
    container.innerHTML = items.length ? items.map(item => `<article class="owned-row"><img data-fallback src="${escapeHtml(safeImage(item.imageUrl))}" alt="${escapeHtml(item.name)}"><div><h3>${escapeHtml(item.name)}</h3><p>${escapeHtml(item.category)} · ${item.available ? "Available" : "Currently borrowed"}</p></div><div class="owned-actions"><button class="button button-small button-outline" data-edit="${item.id}" ${item.available ? "" : "disabled"}><i class="bi bi-pencil"></i> Edit</button><button class="button button-small button-danger" data-delete="${item.id}" ${item.available ? "" : "disabled"}><i class="bi bi-trash3"></i> Delete</button></div></article>`).join("") : '<div class="empty-panel"><i class="bi bi-box2"></i><h2>No listings yet</h2><p>Your items will show up here once published.</p></div>';
    imageFallbacks(container);
    container.querySelectorAll("[data-edit]").forEach(button => button.addEventListener("click", () => editListing(items.find(item => String(item.id) === button.dataset.edit))));
    container.querySelectorAll("[data-delete]").forEach(button => button.addEventListener("click", async () => {
      if (!window.confirm("Delete this listing? This cannot be undone.")) return;
      button.disabled = true;
      try {
        await api(`/items/${button.dataset.delete}?ownerId=${currentStudent.id}`, { method: "DELETE" });
        showToast("Listing deleted.");
        await loadOwnedItems();
      } catch (error) { showToast(error.message, true); button.disabled = false; }
    }));
  } catch (error) { container.innerHTML = `<div class="empty-panel">${escapeHtml(error.message)}</div>`; }
}

function editListing(item) {
  if (!item) return;
  document.querySelector("#editingItemId").value = item.id;
  document.querySelector("#itemName").value = item.name;
  document.querySelector("#itemCategory").value = item.category;
  document.querySelector("#itemDescription").value = item.description;
  document.querySelector("#itemCondition").value = item.condition;
  document.querySelector("#itemLocation").value = item.location;
  document.querySelector("#itemMaxDays").value = item.maxBorrowDays;
  document.querySelector("#itemImageUrl").value = item.imageUrl || "";
  document.querySelector("#listingFormTitle").textContent = "Edit listing";
  document.querySelector("#saveItemButton").innerHTML = '<i class="bi bi-check-lg"></i> Save changes';
  document.querySelector("#cancelEditButton").classList.remove("d-none");
  document.querySelector("#itemForm").scrollIntoView({ behavior: "smooth", block: "center" });
}

document.addEventListener("DOMContentLoaded", initialize);
