const user = Session.requireRole('ADMIN');

document.getElementById('topbarSlot').innerHTML = renderTopbar(user, 'ADMIN');

async function loadUsers() {
  const wrap = document.getElementById('usersWrap');
  try {
    const res = await api.get('/admin/users', { params: { adminId: user.id } });
    const users = res.data;
    wrap.innerHTML = `
      <table>
        <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>手机号</th><th>操作</th></tr></thead>
        <tbody>
          ${users.map(u => `
            <tr>
              <td>${u.id}</td>
              <td>${u.username}</td>
              <td>${u.role}</td>
              <td>${statusBadge(u.status)}</td>
              <td>${u.phone || '—'}</td>
              <td>
                ${u.role === 'ADMIN' ? '—' :
                  u.status === 'ACTIVE'
                    ? `<button class="btn-danger" style="padding:5px 10px;font-size:12px" onclick="banUser(${u.id})">封禁</button>`
                    : `<button class="btn-success" style="padding:5px 10px;font-size:12px" onclick="unbanUser(${u.id})">解封</button>`
                }
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
    populateDriverSelect(users.filter(u => u.role === 'DRIVER'));
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

function populateDriverSelect(drivers) {
  const sel = document.getElementById('assignDriverSelect');
  sel.innerHTML = drivers.length
    ? drivers.map(d => `<option value="${d.id}">${d.username} (#${d.id})</option>`).join('')
    : '<option value="">暂无司机</option>';
}

async function banUser(id) {
  if (!confirm('确定封禁该用户吗？')) return;
  try {
    await api.put(`/admin/users/${id}/ban`, null, { params: { adminId: user.id } });
    await loadUsers();
  } catch (err) {
    alert(err.message);
  }
}

async function unbanUser(id) {
  try {
    await api.put(`/admin/users/${id}/unban`, null, { params: { adminId: user.id } });
    await loadUsers();
  } catch (err) {
    alert(err.message);
  }
}

document.getElementById('cabForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msg = document.getElementById('cabMsg');
  msg.textContent = '';
  try {
    await api.post('/admin/cabs', {
      licensePlate: document.getElementById('licensePlate').value.trim(),
      model: document.getElementById('model').value.trim()
    }, { params: { adminId: user.id } });
    document.getElementById('cabForm').reset();
    await loadCabs();
  } catch (err) {
    showMessage(msg, err.message);
  }
});

async function loadCabs() {
  const wrap = document.getElementById('cabsWrap');
  try {
    const res = await api.get('/admin/cabs', { params: { adminId: user.id } });
    const cabs = res.data;
    if (cabs.length === 0) {
      wrap.innerHTML = '<div class="empty-state">暂无车辆</div>';
    } else {
      wrap.innerHTML = `
        <table>
          <thead><tr><th>车牌</th><th>车型</th><th>状态</th></tr></thead>
          <tbody>
            ${cabs.map(c => `<tr><td>${c.licensePlate}</td><td>${c.model}</td><td>${statusBadge(c.status)}</td></tr>`).join('')}
          </tbody>
        </table>
      `;
    }
    const sel = document.getElementById('assignCabSelect');
    sel.innerHTML = cabs.length
      ? cabs.map(c => `<option value="${c.id}">${c.licensePlate} — ${c.model}</option>`).join('')
      : '<option value="">暂无车辆</option>';
  } catch (err) {
    wrap.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

async function assignCab() {
  const msg = document.getElementById('assignMsg');
  msg.textContent = '';
  const cabId = document.getElementById('assignCabSelect').value;
  const driverId = document.getElementById('assignDriverSelect').value;
  if (!cabId || !driverId) {
    msg.textContent = '请先选择车辆和司机';
    return;
  }
  try {
    await api.post(`/admin/cabs/${cabId}/assign/${driverId}`, null, { params: { adminId: user.id } });
    msg.className = 'success-text';
    msg.textContent = '分配成功';
  } catch (err) {
    showMessage(msg, err.message);
  }
}

loadUsers();
loadCabs();
