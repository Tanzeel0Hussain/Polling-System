const STORAGE_KEY = 'polling-system-browser-v2';
const app = document.querySelector('#app');
const modal = document.querySelector('#createPollModal');
const toast = document.querySelector('#toast');

let state = loadState();
let sessionUserId = null;
let selectedElectionId = null;
let selectedCandidateId = null;
let toastTimer = null;

function emptyState() {
  return {
    users: [],
    elections: [],
    candidates: [],
    votes: [],
    ids: { user: 1, election: 1, candidate: 1, vote: 1 }
  };
}

function loadState() {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY));
    if (!parsed || !Array.isArray(parsed.users) || !Array.isArray(parsed.elections)) return emptyState();
    return parsed;
  } catch {
    return emptyState();
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function esc(value = '') {
  return String(value).replace(/[&<>'"]/g, char => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
  }[char]));
}

function notify(message) {
  clearTimeout(toastTimer);
  toast.textContent = message;
  toast.classList.add('show');
  toastTimer = setTimeout(() => toast.classList.remove('show'), 2600);
}

async function hashPassword(password) {
  if (window.crypto?.subtle && window.TextEncoder) {
    const data = new TextEncoder().encode(password);
    const digest = await crypto.subtle.digest('SHA-256', data);
    return [...new Uint8Array(digest)].map(byte => byte.toString(16).padStart(2, '0')).join('');
  }
  let hash = 2166136261;
  for (let index = 0; index < password.length; index += 1) {
    hash ^= password.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return `fallback-${(hash >>> 0).toString(16)}`;
}

function normalizeUsername(username) {
  const normalized = String(username ?? '').trim();
  if (normalized.length < 3 || normalized.length > 40) {
    throw new Error('Username must be between 3 and 40 characters.');
  }
  if (!/^[A-Za-z0-9._-]+$/.test(normalized)) {
    throw new Error('Username may only contain letters, numbers, dot, underscore and hyphen.');
  }
  return normalized;
}

function validatePassword(password) {
  if (password.length < 8) throw new Error('Password must contain at least 8 characters.');
  if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
    throw new Error('Password must include at least one letter and one number.');
  }
}

function hasAdmin() {
  return state.users.some(user => user.role === 'ADMIN');
}

function currentUser() {
  return state.users.find(user => user.id === sessionUserId) ?? null;
}

async function createUser(username, password, role) {
  const cleanUsername = normalizeUsername(username);
  validatePassword(password);
  if (state.users.some(user => user.username.toLowerCase() === cleanUsername.toLowerCase())) {
    throw new Error('That username is already registered.');
  }
  if (role === 'ADMIN' && hasAdmin()) throw new Error('An administrator already exists.');

  const user = {
    id: state.ids.user++,
    username: cleanUsername,
    passwordHash: await hashPassword(password),
    role
  };
  state.users.push(user);
  saveState();
  return user;
}

async function authenticate(username, password, expectedRole) {
  const cleanUsername = normalizeUsername(username);
  const user = state.users.find(item => item.username.toLowerCase() === cleanUsername.toLowerCase());
  if (!user || user.role !== expectedRole) return null;
  const candidateHash = await hashPassword(password);
  return candidateHash === user.passwordHash ? user : null;
}

function listElections(openOnly = false) {
  return [...state.elections]
    .filter(election => !openOnly || election.status === 'OPEN')
    .sort((a, b) => b.id - a.id);
}

function listCandidates(electionId) {
  return state.candidates.filter(candidate => candidate.electionId === electionId).sort((a, b) => a.id - b.id);
}

function hasVoted(electionId, userId) {
  return state.votes.some(vote => vote.electionId === electionId && vote.userId === userId);
}

function createElection(title, description, rawCandidates) {
  const cleanTitle = String(title ?? '').trim();
  const cleanDescription = String(description ?? '').trim();
  if (cleanTitle.length < 3 || cleanTitle.length > 100) {
    throw new Error('Poll title must be between 3 and 100 characters.');
  }

  const uniqueNames = [];
  for (const raw of rawCandidates) {
    const name = String(raw ?? '').trim();
    if (name && !uniqueNames.includes(name)) uniqueNames.push(name);
  }
  if (uniqueNames.length < 2) throw new Error('Add at least two unique candidates/options.');
  if (uniqueNames.some(name => name.length > 80)) {
    throw new Error('Candidate/option names must be 80 characters or fewer.');
  }

  const election = {
    id: state.ids.election++,
    title: cleanTitle,
    description: cleanDescription,
    status: 'OPEN'
  };
  state.elections.push(election);
  uniqueNames.forEach(name => state.candidates.push({
    id: state.ids.candidate++, electionId: election.id, name
  }));
  saveState();
  return election;
}

function setElectionStatus(electionId, open) {
  const election = state.elections.find(item => item.id === electionId);
  if (!election) throw new Error('Poll not found.');
  election.status = open ? 'OPEN' : 'CLOSED';
  saveState();
}

function castVote(electionId, userId, candidateId) {
  const election = state.elections.find(item => item.id === electionId);
  if (!election) throw new Error('Poll not found.');
  if (election.status !== 'OPEN') throw new Error('This poll is closed.');
  const candidate = state.candidates.find(item => item.id === candidateId && item.electionId === electionId);
  if (!candidate) throw new Error('Selected option does not belong to this poll.');
  if (hasVoted(electionId, userId)) throw new Error('You have already voted in this poll.');

  state.votes.push({
    id: state.ids.vote++, electionId, userId, candidateId, createdAt: new Date().toISOString()
  });
  saveState();
}

function results(electionId) {
  const candidates = listCandidates(electionId);
  const rows = candidates.map(candidate => ({
    candidate: candidate.name,
    votes: state.votes.filter(vote => vote.candidateId === candidate.id).length
  }));
  const total = rows.reduce((sum, row) => sum + row.votes, 0);
  return rows
    .map(row => ({ ...row, percentage: total === 0 ? 0 : row.votes * 100 / total }))
    .sort((a, b) => b.votes - a.votes || a.candidate.localeCompare(b.candidate));
}

function dashboardStats() {
  return {
    users: state.users.filter(user => user.role === 'USER').length,
    elections: state.elections.length,
    votes: state.votes.length
  };
}

function renderSetup() {
  sessionUserId = null;
  app.innerHTML = `
    <section class="screen-shell">
      <div class="setup-card">
        <div class="card-head"><h1>Polling System — First Run</h1><p>Create the first administrator account</p></div>
        <div class="card-body">
          <div class="intro-block"><h2>Administrator Setup</h2><p>Just like the desktop application, the browser version requires an administrator account before the system can be used.</p></div>
          <form id="setupForm">
            <div class="form-grid">
              <label for="setupUsername">Username</label><input class="field" id="setupUsername" value="admin" autocomplete="username">
              <label for="setupPassword">Password</label><input class="field" id="setupPassword" type="password" autocomplete="new-password">
              <label for="setupConfirm">Confirm password</label><input class="field" id="setupConfirm" type="password" autocomplete="new-password">
            </div>
            <div class="form-message" id="setupMessage"></div>
            <div class="form-actions"><button class="btn secondary" id="clearSetup" type="button">Clear</button><button class="btn primary" type="submit">Create Administrator</button></div>
          </form>
          <div class="helper-note">Browser passwords are stored as one-way hashes in this browser. The downloadable Java desktop app uses BCrypt and SQLite.</div>
        </div>
      </div>
    </section>`;

  document.querySelector('#clearSetup').addEventListener('click', () => {
    document.querySelector('#setupUsername').value = 'admin';
    document.querySelector('#setupPassword').value = '';
    document.querySelector('#setupConfirm').value = '';
    document.querySelector('#setupMessage').textContent = '';
  });

  document.querySelector('#setupForm').addEventListener('submit', async event => {
    event.preventDefault();
    const message = document.querySelector('#setupMessage');
    message.textContent = '';
    const username = document.querySelector('#setupUsername').value;
    const password = document.querySelector('#setupPassword').value;
    const confirm = document.querySelector('#setupConfirm').value;
    try {
      if (password !== confirm) throw new Error('Passwords do not match.');
      await createUser(username, password, 'ADMIN');
      notify('Administrator account created. You can now sign in.');
      renderWelcome();
    } catch (error) {
      message.textContent = error.message;
    }
  });
}

function renderWelcome() {
  sessionUserId = null;
  selectedElectionId = null;
  selectedCandidateId = null;
  app.innerHTML = `
    <section class="screen-shell">
      <div class="welcome-card">
        <div class="card-head"><h1>POLLING SYSTEM</h1><p>Secure polling with account-based vote protection</p></div>
        <div class="card-body">
          <div class="intro-block"><h2>Choose how you want to continue</h2><p>Create a voter account, sign in to vote, or open the administrator dashboard.</p></div>
          <div class="welcome-actions">
            <article class="action-card"><h3>Voter Login</h3><p>Sign in and vote in currently open polls.</p><button class="btn primary" data-auth="USER_LOGIN" type="button">Sign In</button></article>
            <article class="action-card"><h3>Create Account</h3><p>Register a voter account with a protected password.</p><button class="btn accent" data-auth="REGISTER" type="button">Register</button></article>
            <article class="action-card"><h3>Administrator</h3><p>Create polls, open or close voting and review results.</p><button class="btn primary" data-auth="ADMIN_LOGIN" type="button">Admin Login</button></article>
          </div>
          <p class="privacy-note">Live-app data is stored locally in this browser. The desktop version stores data locally in SQLite and uses BCrypt password hashes.</p>
        </div>
      </div>
    </section>`;
  document.querySelectorAll('[data-auth]').forEach(button => button.addEventListener('click', () => renderAuth(button.dataset.auth)));
}

function authTitle(mode) {
  if (mode === 'USER_LOGIN') return 'Voter Sign In';
  if (mode === 'ADMIN_LOGIN') return 'Administrator Sign In';
  return 'Create Voter Account';
}

function renderAuth(mode) {
  const register = mode === 'REGISTER';
  app.innerHTML = `
    <section class="screen-shell">
      <div class="auth-card">
        <div class="card-head"><h1>${authTitle(mode)}</h1><p>${register ? 'Create a voter account with the same validation rules as the desktop app.' : 'Enter your credentials to continue.'}</p></div>
        <div class="card-body">
          <form id="authForm">
            <div class="form-grid">
              <label for="authUsername">Username</label><input class="field" id="authUsername" autocomplete="username">
              <label for="authPassword">Password</label><input class="field" id="authPassword" type="password" autocomplete="${register ? 'new-password' : 'current-password'}">
              ${register ? '<label for="authConfirm">Confirm Password</label><input class="field" id="authConfirm" type="password" autocomplete="new-password">' : ''}
            </div>
            <div class="form-message" id="authMessage"></div>
            <div class="form-actions"><button class="btn secondary" id="authBack" type="button">Back</button><button class="btn ${register ? 'accent' : 'primary'}" type="submit">${register ? 'Create Account' : 'Sign In'}</button></div>
          </form>
        </div>
      </div>
    </section>`;

  document.querySelector('#authBack').addEventListener('click', renderWelcome);
  document.querySelector('#authForm').addEventListener('submit', async event => {
    event.preventDefault();
    const message = document.querySelector('#authMessage');
    message.textContent = '';
    const username = document.querySelector('#authUsername').value;
    const password = document.querySelector('#authPassword').value;
    try {
      if (register) {
        const confirm = document.querySelector('#authConfirm').value;
        if (password !== confirm) throw new Error('Passwords do not match.');
        await createUser(username, password, 'USER');
        notify('Account created successfully. You can now sign in.');
        renderAuth('USER_LOGIN');
        return;
      }
      const role = mode === 'ADMIN_LOGIN' ? 'ADMIN' : 'USER';
      const user = await authenticate(username, password, role);
      if (!user) throw new Error('Invalid username/password for this account type.');
      sessionUserId = user.id;
      if (role === 'ADMIN') renderAdminDashboard();
      else renderVoterDashboard();
    } catch (error) {
      message.textContent = error.message;
    }
  });
}

function renderVoterDashboard() {
  const user = currentUser();
  if (!user || user.role !== 'USER') return renderWelcome();
  const openPolls = listElections(true);
  if (!openPolls.some(item => item.id === selectedElectionId)) selectedElectionId = openPolls[0]?.id ?? null;
  const election = openPolls.find(item => item.id === selectedElectionId) ?? null;

  app.innerHTML = `
    <section class="dashboard">
      <header class="dashboard-header">
        <div class="dashboard-title"><h1>Voter Dashboard</h1><p>Signed in as ${esc(user.username)}</p></div>
        <div class="dashboard-actions"><button class="btn secondary" id="voterRefresh" type="button">Refresh</button><button class="btn accent" id="voterLogout" type="button">Log Out</button></div>
      </header>
      <div class="dashboard-body">
        <aside class="panel list-panel"><div class="panel-head"><h2>Open Polls</h2><span class="badge open">${openPolls.length} OPEN</span></div><div class="poll-list" id="voterPollList">${openPolls.length ? openPolls.map(item => `<button class="poll-list-item ${item.id === selectedElectionId ? 'active' : ''}" data-election="${item.id}" type="button"><strong>${esc(item.title)}</strong><span>OPEN</span></button>`).join('') : '<div class="poll-list-empty">There are no open polls right now.</div>'}</div></aside>
        <section class="panel detail-panel" id="voterDetail">${renderVoterDetail(user, election)}</section>
      </div>
    </section>`;

  document.querySelector('#voterLogout').addEventListener('click', renderWelcome);
  document.querySelector('#voterRefresh').addEventListener('click', renderVoterDashboard);
  document.querySelectorAll('[data-election]').forEach(button => button.addEventListener('click', () => {
    selectedElectionId = Number(button.dataset.election);
    selectedCandidateId = null;
    renderVoterDashboard();
  }));
  bindVoterDetail(user, election);
}

function renderVoterDetail(user, election) {
  if (!election) return '<div class="empty-detail"><div><strong>No poll selected</strong><span>There are no open polls right now.</span></div></div>';
  const candidates = listCandidates(election.id);
  const alreadyVoted = hasVoted(election.id, user.id);
  if (!candidates.some(candidate => candidate.id === selectedCandidateId)) selectedCandidateId = null;
  return `
    <div class="detail-top"><div><h2>${esc(election.title)}</h2><p>${esc(election.description || 'No description provided.')}</p><span class="status-text ${alreadyVoted ? 'open' : ''}">${alreadyVoted ? 'Vote recorded — this account has already voted in this poll.' : 'Select one option. A submitted vote cannot be changed.'}</span></div></div>
    <div class="options-area">${candidates.map(candidate => `<label class="candidate-option ${alreadyVoted ? 'disabled' : ''}"><input type="radio" name="candidate" value="${candidate.id}" ${selectedCandidateId === candidate.id ? 'checked' : ''} ${alreadyVoted ? 'disabled' : ''}><span>${esc(candidate.name)}</span></label>`).join('')}</div>
    <div class="vote-submit-row"><button class="btn accent" id="submitVote" type="button" ${alreadyVoted || candidates.length === 0 ? 'disabled' : ''}>Submit Vote</button></div>`;
}

function bindVoterDetail(user, election) {
  if (!election) return;
  document.querySelectorAll('input[name="candidate"]').forEach(input => input.addEventListener('change', () => {
    selectedCandidateId = Number(input.value);
  }));
  const submit = document.querySelector('#submitVote');
  if (!submit) return;
  submit.addEventListener('click', () => {
    const selected = listCandidates(election.id).find(candidate => candidate.id === selectedCandidateId);
    if (!selected) return notify('Select an option before submitting your vote.');
    if (!confirm(`Submit your vote for "${selected.name}"?\nThis action cannot be changed.`)) return;
    try {
      castVote(election.id, user.id, selected.id);
      selectedCandidateId = null;
      notify('Your vote has been recorded successfully.');
      renderVoterDashboard();
    } catch (error) {
      notify(error.message);
      renderVoterDashboard();
    }
  });
}

function renderAdminDashboard() {
  const user = currentUser();
  if (!user || user.role !== 'ADMIN') return renderWelcome();
  const elections = listElections(false);
  if (!elections.some(item => item.id === selectedElectionId)) selectedElectionId = elections[0]?.id ?? null;
  const election = elections.find(item => item.id === selectedElectionId) ?? null;
  const stats = dashboardStats();

  app.innerHTML = `
    <section class="dashboard">
      <header class="dashboard-header">
        <div class="dashboard-title"><h1>Administrator Dashboard</h1><p>Signed in as ${esc(user.username)}</p></div>
        <div class="dashboard-actions"><button class="btn accent" id="adminCreate" type="button">Create Poll</button><button class="btn secondary" id="adminRefresh" type="button">Refresh</button><button class="btn secondary" id="adminLogout" type="button">Log Out</button></div>
      </header>
      <div class="stats-grid"><div class="stat-card"><strong>${stats.users}</strong><span>Registered Voters</span></div><div class="stat-card"><strong>${stats.elections}</strong><span>Total Polls</span></div><div class="stat-card"><strong>${stats.votes}</strong><span>Votes Cast</span></div></div>
      <div class="dashboard-body">
        <aside class="panel list-panel"><div class="panel-head"><h2>All Polls</h2></div><div class="poll-list">${elections.length ? elections.map(item => `<button class="poll-list-item ${item.id === selectedElectionId ? 'active' : ''}" data-admin-election="${item.id}" type="button"><strong>${esc(item.title)}</strong><span>${item.status}</span></button>`).join('') : '<div class="poll-list-empty">No polls yet. Create your first poll to begin.</div>'}</div></aside>
        <section class="panel detail-panel">${renderAdminDetail(election)}</section>
      </div>
    </section>`;

  document.querySelector('#adminLogout').addEventListener('click', renderWelcome);
  document.querySelector('#adminRefresh').addEventListener('click', renderAdminDashboard);
  document.querySelector('#adminCreate').addEventListener('click', openPollModal);
  document.querySelectorAll('[data-admin-election]').forEach(button => button.addEventListener('click', () => {
    selectedElectionId = Number(button.dataset.adminElection);
    renderAdminDashboard();
  }));
  const toggle = document.querySelector('#togglePoll');
  if (toggle && election) toggle.addEventListener('click', () => {
    const currentlyOpen = election.status === 'OPEN';
    const action = currentlyOpen ? 'close' : 'reopen';
    if (!confirm(`Are you sure you want to ${action} "${election.title}"?`)) return;
    try {
      setElectionStatus(election.id, !currentlyOpen);
      notify(`Poll ${currentlyOpen ? 'closed' : 'reopened'} successfully.`);
      renderAdminDashboard();
    } catch (error) {
      notify(error.message);
    }
  });
}

function renderAdminDetail(election) {
  if (!election) return '<div class="empty-detail"><div><strong>No polls yet</strong><span>Create your first poll to begin collecting votes.</span></div></div>';
  const rows = results(election.id);
  const open = election.status === 'OPEN';
  return `
    <div class="detail-top"><div><h2>${esc(election.title)}</h2><p>${esc(election.description || 'No description provided.')}</p><span class="status-text ${open ? 'open' : 'closed'}">${open ? 'Voting is currently OPEN.' : 'Voting is CLOSED. Results remain available below.'}</span></div><button class="btn primary" id="togglePoll" type="button">${open ? 'Close Poll' : 'Reopen Poll'}</button></div>
    <table class="result-table"><thead><tr><th>Option</th><th>Votes</th><th>Share</th></tr></thead><tbody>${rows.map(row => `<tr><td>${esc(row.candidate)}</td><td>${row.votes}</td><td>${row.percentage.toFixed(1)}%</td></tr>`).join('')}</tbody></table>`;
}

function openPollModal() {
  document.querySelector('#pollTitleInput').value = '';
  document.querySelector('#pollDescriptionInput').value = '';
  document.querySelector('#pollCandidatesInput').value = 'Option A\nOption B';
  document.querySelector('#pollFormMessage').textContent = '';
  modal.classList.add('open');
  modal.setAttribute('aria-hidden', 'false');
  setTimeout(() => document.querySelector('#pollTitleInput').focus(), 20);
}

function closePollModal() {
  modal.classList.remove('open');
  modal.setAttribute('aria-hidden', 'true');
  document.querySelector('#pollFormMessage').textContent = '';
}

async function createPollFromModal() {
  const message = document.querySelector('#pollFormMessage');
  message.textContent = '';
  try {
    const candidates = document.querySelector('#pollCandidatesInput').value.split(/\r?\n/);
    const election = createElection(
      document.querySelector('#pollTitleInput').value,
      document.querySelector('#pollDescriptionInput').value,
      candidates
    );
    selectedElectionId = election.id;
    closePollModal();
    notify('Poll created and opened for voting.');
    renderAdminDashboard();
  } catch (error) {
    message.textContent = error.message;
  }
}

document.querySelector('#closePollModal').addEventListener('click', closePollModal);
document.querySelector('#cancelPollModal').addEventListener('click', closePollModal);
document.querySelector('#createPollButton').addEventListener('click', createPollFromModal);
modal.addEventListener('click', event => { if (event.target === modal) closePollModal(); });

document.querySelector('#brandHome').addEventListener('click', () => hasAdmin() ? renderWelcome() : renderSetup());
document.querySelector('#resetApp').addEventListener('click', () => {
  if (!confirm('Reset all browser app data? This will remove accounts, polls and votes stored in this browser.')) return;
  localStorage.removeItem(STORAGE_KEY);
  state = emptyState();
  sessionUserId = null;
  selectedElectionId = null;
  selectedCandidateId = null;
  closePollModal();
  notify('Browser app data reset.');
  renderSetup();
});

document.addEventListener('keydown', event => {
  if (event.key === 'Escape' && modal.classList.contains('open')) closePollModal();
});

if (hasAdmin()) renderWelcome();
else renderSetup();
