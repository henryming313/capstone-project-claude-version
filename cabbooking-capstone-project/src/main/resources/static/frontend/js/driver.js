const user = Session.requireRole('DRIVER');

document.getElementById('topbarSlot').innerHTML = renderTopbar(user, 'DRIVER');

async function loadEarnings() {
  const wrap = document.getElementById('earningsTiles');
  try {
    const res = await api.get(`/trips/driver/${user.id}/earnings`);
    const e = res.data;
    wrap.innerHTML = `
      <div class="stat-tile"><div class="num">${e.totalCompletedTrips}</div><div class="label">已完成行程</div></div>
      <div class="stat-tile"><div class="num">${formatMoney(e.totalEarnings)}</div><div class="label">总收入</div></div>
      <div class="stat-tile"><div class="num">${formatMoney(e.averageFare)}</div><div class="label">平均车费</div></div>
      <div class="stat-tile"><div class="num">${e.averageRating ? e.averageRating.toFixed(1) + ' ★' : '—'}</div><div class="label">平均评分</div></div>
    `;
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

async function loadAvailable() {
  const wrap = document.getElementById('availableWrap');
  try {
    const res = await api.get('/trips/available', { params: { driverId: user.id } });
    const trips = res.data;
    if (trips.length === 0) {
      wrap.innerHTML = '<div class="empty-state">目前没有待接订单</div>';
      return;
    }
    wrap.innerHTML = `
      <table>
        <thead><tr><th>路线</th><th>乘客</th><th>操作</th></tr></thead>
        <tbody>
          ${trips.map(t => `
            <tr>
              <td>${t.startLocation}<br><span class="helper-text">→ ${t.endLocation}</span></td>
              <td>${t.riderUsername}</td>
              <td>
                <button class="btn-success" style="padding:5px 10px;font-size:12px" onclick="acceptTrip(${t.id})">接单</button>
                <button class="btn-outline" style="padding:5px 10px;font-size:12px" onclick="rejectTrip(${t.id})">拒绝</button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

async function loadMyTrips() {
  const wrap = document.getElementById('myTripsWrap');
  try {
    const res = await api.get(`/trips/driver/${user.id}`);
    const trips = res.data;
    if (trips.length === 0) {
      wrap.innerHTML = '<div class="empty-state">还没有接过行程</div>';
      return;
    }
    wrap.innerHTML = `
      <table>
        <thead><tr><th>路线</th><th>状态</th><th>车费</th><th>操作</th></tr></thead>
        <tbody>
          ${trips.map(renderMyTripRow).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

function renderMyTripRow(t) {
  let action = '';
  if (t.status === 'ACCEPTED') {
    action = `<button class="btn-primary" style="padding:5px 10px;font-size:12px" onclick="startTrip(${t.id})">开始行程</button>`;
  } else if (t.status === 'IN_PROGRESS') {
    action = `<button class="btn-success" style="padding:5px 10px;font-size:12px" onclick="completeTrip(${t.id})">完成行程</button>`;
  }
  return `
    <tr>
      <td>${t.startLocation}<br><span class="helper-text">→ ${t.endLocation}</span></td>
      <td>${statusBadge(t.status)}</td>
      <td>${formatMoney(t.fare)}</td>
      <td>${action || '—'}</td>
    </tr>
  `;
}

async function acceptTrip(tripId) {
  try {
    await api.put(`/trips/${tripId}/accept`, null, { params: { driverId: user.id } });
    await refreshAll();
  } catch (err) {
    alert(err.message);
  }
}

async function rejectTrip(tripId) {
  const reason = prompt('拒绝原因（可留空）：') || '';
  try {
    await api.put(`/trips/${tripId}/reject`, { driverId: user.id, reason });
    await refreshAll();
  } catch (err) {
    alert(err.message);
  }
}

async function startTrip(tripId) {
  try {
    await api.put(`/trips/${tripId}/start`, null, { params: { driverId: user.id } });
    await refreshAll();
  } catch (err) {
    alert(err.message);
  }
}

async function completeTrip(tripId) {
  try {
    await api.put(`/trips/${tripId}/complete`, null, { params: { driverId: user.id } });
    await refreshAll();
  } catch (err) {
    alert(err.message);
  }
}

async function refreshAll() {
  await Promise.all([loadEarnings(), loadAvailable(), loadMyTrips()]);
}

refreshAll();
setInterval(refreshAll, 8000);
