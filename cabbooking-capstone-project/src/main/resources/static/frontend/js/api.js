// Shared helpers loaded on every page before the page-specific script.
// Session state (id/username/role/status) lives in localStorage - this is
// a plain multi-page app served by Spring Boot, not a Claude artifact
// sandbox, so localStorage works normally here.

const API_BASE = '/api';

const api = axios.create({ baseURL: API_BASE });

// Surface backend error messages consistently.
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err.response?.data?.message || err.message || '请求失败';
    return Promise.reject(new Error(msg));
  }
);

const Session = {
  KEY: 'cabbooking_user',

  save(user) {
    localStorage.setItem(this.KEY, JSON.stringify(user));
  },

  get() {
    const raw = localStorage.getItem(this.KEY);
    return raw ? JSON.parse(raw) : null;
  },

  clear() {
    localStorage.removeItem(this.KEY);
  },

  /** Redirects to login if no session, or if role doesn't match expected. Returns the user. */
  requireRole(expectedRole) {
    const user = this.get();
    if (!user || user.role !== expectedRole) {
      window.location.href = 'login.html';
      return null;
    }
    return user;
  }
};

function formatDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('zh-CN', { hour12: false });
}

function formatMoney(value) {
  if (value === null || value === undefined) return '—';
  return '€' + Number(value).toFixed(2);
}

function statusBadge(status) {
  return `<span class="badge-status badge-${status}">${status}</span>`;
}

function renderTopbar(activeUser, title) {
  return `
    <div class="topbar">
      <div class="brand"><span>🚖 Cab Booking</span><span class="badge">${title}</span></div>
      <div class="session">
        <span>${activeUser.username} · ${activeUser.role}</span>
        <button class="logout" onclick="doLogout()">退出登录</button>
      </div>
    </div>
    <div class="checker-strip"></div>
  `;
}

function doLogout() {
  Session.clear();
  window.location.href = 'login.html';
}

function showMessage(el, message, isError = true) {
  el.textContent = message;
  el.className = isError ? 'error-text' : 'success-text';
}
