const user = Session.requireRole('RIDER');

let locations = [];
let map, markers = [];
let ratingTripId = null;
let selectedStars = 0;

document.getElementById('topbarSlot').innerHTML = renderTopbar(user, 'RIDER');

async function init() {
  await loadLocations();
  initMap();
  await loadTrips();
}

async function loadLocations() {
  const res = await api.get('/locations');
  locations = res.data;
  const startSel = document.getElementById('startLocation');
  const endSel = document.getElementById('endLocation');
  const options = locations.map(l => `<option value="${l.name}">${l.name}</option>`).join('');
  startSel.innerHTML = options;
  endSel.innerHTML = options;
  endSel.selectedIndex = 1;
  startSel.addEventListener('change', updateMap);
  endSel.addEventListener('change', updateMap);
}

function initMap() {
  map = L.map('map').setView([63.8355, 23.1295], 12);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(map);
  updateMap();
}

function updateMap() {
  markers.forEach(m => map.removeLayer(m));
  markers = [];

  const startName = document.getElementById('startLocation').value;
  const endName = document.getElementById('endLocation').value;
  const start = locations.find(l => l.name === startName);
  const end = locations.find(l => l.name === endName);

  if (start) {
    markers.push(L.marker([start.latitude, start.longitude]).addTo(map).bindPopup('起点: ' + start.name));
  }
  if (end) {
    markers.push(L.marker([end.latitude, end.longitude]).addTo(map).bindPopup('终点: ' + end.name));
  }
  if (start && end) {
    const bounds = L.latLngBounds([[start.latitude, start.longitude], [end.latitude, end.longitude]]);
    map.fitBounds(bounds, { padding: [30, 30] });
  }
}

document.getElementById('estimateBtn').addEventListener('click', async () => {
  const startLocation = document.getElementById('startLocation').value;
  const endLocation = document.getElementById('endLocation').value;
  const fareBox = document.getElementById('fareBox');
  if (startLocation === endLocation) {
    fareBox.textContent = '起点和终点不能相同';
    return;
  }
  try {
    const res = await api.get('/trips/estimate', { params: { startLocation, endLocation } });
    fareBox.innerHTML = `预估车费: <strong>${formatMoney(res.data.estimatedFare)}</strong>` +
      ` (基础价 ${formatMoney(res.data.baseFare)} + 里程附加费 ${formatMoney(res.data.surcharge)})`;
  } catch (err) {
    fareBox.textContent = err.message;
  }
});

document.getElementById('bookForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msg = document.getElementById('bookMsg');
  msg.textContent = '';
  const startLocation = document.getElementById('startLocation').value;
  const endLocation = document.getElementById('endLocation').value;

  try {
    await api.post('/trips', { riderId: user.id, startLocation, endLocation });
    showMessage(msg, '行程已发起，等待司机接单', false);
    await loadTrips();
  } catch (err) {
    showMessage(msg, err.message);
  }
});

async function loadTrips() {
  const wrap = document.getElementById('tripsTableWrap');
  try {
    const res = await api.get(`/trips/rider/${user.id}`);
    const trips = res.data;
    if (trips.length === 0) {
      wrap.innerHTML = '<div class="empty-state">还没有行程记录</div>';
      return;
    }
    wrap.innerHTML = `
      <table>
        <thead><tr><th>路线</th><th>状态</th><th>司机</th><th>车费</th><th>操作</th></tr></thead>
        <tbody>
          ${trips.map(renderTripRow).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

function renderTripRow(t) {
  const canCancel = t.status === 'PENDING' || t.status === 'ACCEPTED';
  const canRate = t.status === 'COMPLETED';
  return `
    <tr>
      <td>${t.startLocation}<br><span class="helper-text">→ ${t.endLocation}</span></td>
      <td>${statusBadge(t.status)}</td>
      <td>${t.driverUsername || '—'}</td>
      <td>${formatMoney(t.fare)}</td>
      <td>
        ${canCancel ? `<button class="btn-danger" style="padding:5px 10px;font-size:12px" onclick="cancelTrip(${t.id})">取消</button>` : ''}
        ${canRate ? `<button class="btn-outline" style="padding:5px 10px;font-size:12px" onclick="openRatingModal(${t.id})">评分</button>` : ''}
      </td>
    </tr>
  `;
}

async function cancelTrip(tripId) {
  if (!confirm('确定要取消这个行程吗？')) return;
  try {
    await api.put(`/trips/${tripId}/cancel`, null, { params: { riderId: user.id } });
    await loadTrips();
  } catch (err) {
    alert(err.message);
  }
}

function openRatingModal(tripId) {
  ratingTripId = tripId;
  selectedStars = 0;
  document.getElementById('ratingComment').value = '';
  document.getElementById('ratingMsg').textContent = '';
  renderStars();
  document.getElementById('ratingOverlay').style.display = 'flex';
}

function closeRatingModal() {
  document.getElementById('ratingOverlay').style.display = 'none';
}

function renderStars() {
  document.querySelectorAll('#starPicker span').forEach(span => {
    const v = parseInt(span.dataset.v, 10);
    span.classList.toggle('active', v <= selectedStars);
    span.onclick = () => { selectedStars = v; renderStars(); };
  });
}

async function submitRating() {
  const msg = document.getElementById('ratingMsg');
  if (selectedStars === 0) {
    msg.textContent = '请先选择星级';
    return;
  }
  try {
    await api.post('/ratings', {
      tripId: ratingTripId,
      riderId: user.id,
      score: selectedStars,
      comment: document.getElementById('ratingComment').value.trim() || null
    });
    closeRatingModal();
    await loadTrips();
  } catch (err) {
    msg.textContent = err.message;
  }
}

init();
setInterval(loadTrips, 8000); // lightweight polling to reflect driver-side updates
