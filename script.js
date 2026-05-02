// ── SESSION GUARD ──
// Call this at top of each dashboard page
function requireLogin(expectedRole) {
  const raw = sessionStorage.getItem('hms_user');
  if (!raw) { window.location.href = 'index.html'; return null; }
  const user = JSON.parse(raw);
  if (expectedRole && user.role !== expectedRole) {
    window.location.href = 'index.html'; return null;
  }
  return user;
}

function logout() {
  sessionStorage.removeItem('hms_user');
  window.location.href = 'index.html';
}

// ── TOAST NOTIFICATIONS ──
function showToast(msg, type = 'default') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'show ' + type;
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.className = ''; }, 3000);
}

// ── MODAL HELPERS ──
function openModal(id) {
  document.getElementById(id).classList.add('open');
}
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}
// Close on overlay click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

// ── NAV HELPER ──
function activatePage(pageId, navId) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const page = document.getElementById(pageId);
  const nav  = document.getElementById(navId);
  if (page) page.classList.add('active');
  if (nav)  nav.classList.add('active');
}

// ── FORMAT HELPERS ──
function fmtDate(str) {
  if (!str) return '—';
  return new Date(str).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}
function fmtDateTime(str) {
  if (!str) return '—';
  return new Date(str).toLocaleString('en-IN', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' });
}
function fmtCurrency(n) {
  return '₹' + Number(n).toLocaleString('en-IN');
}

// ── MOCK DATA STORE (replace with JDBC calls later) ──
const DB = {
  patients: [
    { id:'P001', name:'Riya Sharma',    dob:'1995-03-12', gender:'Female', blood:'B+', phone:'9876543210', address:'Mysuru', doctor_id:'D001' },
    { id:'P002', name:'Arjun Mehta',    dob:'1988-07-22', gender:'Male',   blood:'O+', phone:'9123456789', address:'Bengaluru', doctor_id:'D002' },
    { id:'P003', name:'Sneha Rao',      dob:'2001-11-05', gender:'Female', blood:'A-', phone:'9000001111', address:'Mysuru', doctor_id:'D001' },
  ],
  doctors: [
    { id:'D001', name:'Dr. Kavya Nair',   specialization:'Cardiology',    phone:'9988776655', available:'Mon,Wed,Fri' },
    { id:'D002', name:'Dr. Rohan Patel',  specialization:'Neurology',     phone:'9977665544', available:'Tue,Thu,Sat' },
    { id:'D003', name:'Dr. Anita Desai',  specialization:'Orthopedics',   phone:'9966554433', available:'Mon,Tue,Thu' },
  ],
  appointments: [
    { id:'A001', patient_id:'P001', doctor_id:'D001', datetime:'2025-06-10T10:00', status:'scheduled',  type:'regular' },
    { id:'A002', patient_id:'P002', doctor_id:'D002', datetime:'2025-06-10T11:30', status:'completed',  type:'regular' },
    { id:'A003', patient_id:'P003', doctor_id:'D001', datetime:'2025-06-11T09:00', status:'scheduled',  type:'emergency' },
    { id:'A004', patient_id:'P001', doctor_id:'D003', datetime:'2025-06-12T14:00', status:'cancelled',  type:'regular' },
  ],
  records: [
    { id:'R001', patient_id:'P002', doctor_id:'D002', appt_id:'A002', diagnosis:'Migraine', prescription:'Sumatriptan 50mg', notes:'Follow-up in 2 weeks', date:'2025-06-10' },
  ],
  bills: [
    { id:'B001', patient_id:'P001', appt_id:'A001', amount:800,  status:'pending',  date:'2025-06-10' },
    { id:'B002', patient_id:'P002', appt_id:'A002', amount:1200, status:'paid',     date:'2025-06-10' },
    { id:'B003', patient_id:'P003', appt_id:'A003', amount:2500, status:'pending',  date:'2025-06-11' },
  ],

  // helpers
  getPatient(id) { return this.patients.find(p => p.id === id); },
  getDoctor(id)  { return this.doctors.find(d => d.id === id); },
  nextId(prefix, arr) {
    const nums = arr.map(x => parseInt(x.id.replace(prefix,''))).filter(Boolean);
    return prefix + String((Math.max(0,...nums)+1)).padStart(3,'0');
  },

  // Check double booking
  hasConflict(doctor_id, datetime, excludeId = null) {
    return this.appointments.some(a =>
      a.doctor_id === doctor_id &&
      a.datetime  === datetime  &&
      a.status    !== 'cancelled' &&
      a.id        !== excludeId
    );
  }
};