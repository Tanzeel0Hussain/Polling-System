(() => {
  const appRoot = document.querySelector('#app');
  const homeButton = document.querySelector('#navHome');
  const voterButton = document.querySelector('#navVoter');
  const registerButton = document.querySelector('#navRegister');

  function cleanUrl() {
    history.replaceState(null, '', window.location.pathname);
  }

  function showHome() {
    cleanUrl();
    if (typeof hasAdmin === 'function' && hasAdmin()) renderWelcome();
    else renderSetup();
  }

  function showVoterLogin() {
    if (typeof hasAdmin !== 'function' || !hasAdmin()) {
      renderSetup();
      return;
    }
    history.replaceState(null, '', `${window.location.pathname}?view=voter`);
    renderAuth('USER_LOGIN');
  }

  function showRegister() {
    if (typeof hasAdmin !== 'function' || !hasAdmin()) {
      renderSetup();
      return;
    }
    history.replaceState(null, '', `${window.location.pathname}?view=register`);
    renderAuth('REGISTER');
  }

  function openVoterTab() {
    const target = `${window.location.origin}${window.location.pathname}?view=voter`;
    window.open(target, '_blank', 'noopener');
  }

  function enhanceDashboard() {
    const title = document.querySelector('.dashboard-title h1');
    const actions = document.querySelector('.dashboard-actions');
    if (!title || !actions) return;

    if (!actions.querySelector('[data-dashboard-home]')) {
      const back = document.createElement('button');
      back.type = 'button';
      back.className = 'btn secondary';
      back.dataset.dashboardHome = 'true';
      back.textContent = 'Home';
      back.addEventListener('click', showHome);
      actions.prepend(back);
    }

    if (title.textContent.includes('Administrator') && !actions.querySelector('[data-open-voter]')) {
      const voter = document.createElement('button');
      voter.type = 'button';
      voter.className = 'btn accent';
      voter.dataset.openVoter = 'true';
      voter.textContent = 'Open Voter Login';
      voter.addEventListener('click', openVoterTab);
      actions.prepend(voter);
    }
  }

  homeButton?.addEventListener('click', showHome);
  voterButton?.addEventListener('click', showVoterLogin);
  registerButton?.addEventListener('click', showRegister);

  const requestedView = new URLSearchParams(window.location.search).get('view');
  if (typeof hasAdmin === 'function' && hasAdmin()) {
    if (requestedView === 'voter') renderAuth('USER_LOGIN');
    else if (requestedView === 'register') renderAuth('REGISTER');
    else if (requestedView === 'admin') renderAuth('ADMIN_LOGIN');
    else if (requestedView === 'home') renderWelcome();
  }

  const observer = new MutationObserver(enhanceDashboard);
  if (appRoot) observer.observe(appRoot, { childList: true, subtree: true });
  enhanceDashboard();
})();
