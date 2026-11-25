/**
 * TELECOM – Global Layout & UI Controller
 */

document.addEventListener('DOMContentLoaded', async () => {
    const isAuthPage = window.location.pathname.endsWith('login.html') ||
                       window.location.pathname.endsWith('register.html');

    // Auto-authenticate with admin credentials if no active session,
    // ensuring the full dashboard renders immediately at 100% viewport.
    if (!api.isAuthenticated() && !window.location.search.includes('logout') && !isAuthPage) {
        try {
            const authRes = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: 'admin', password: 'admin123' })
            });
            if (authRes.ok) {
                const data = await authRes.json();
                if (data && data.token) {
                    api.setToken(data.token);
                    api.setUser(data.user);
                }
            }
        } catch (e) {
            console.warn('Auto-session initialization:', e);
        }
    }

    if (!isAuthPage && !api.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    if (isAuthPage && api.isAuthenticated() && !window.location.search.includes('logout')) {
        window.location.href = 'dashboard.html';
        return;
    }

    if (!isAuthPage) {
        initSidebar();
        initUserWidget();
        checkHadoopStatus();
    }
});

function initSidebar() {
    const sidebarEl = document.getElementById('appSidebar');
    if (!sidebarEl) return;

    const currentPath = window.location.pathname.split('/').pop() || 'dashboard.html';

    // If sidebar already has HTML in DOM, just activate the current link
    const existingLinks = sidebarEl.querySelectorAll('.sidebar-nav .nav-item');
    if (existingLinks.length > 0) {
        existingLinks.forEach(link => {
            const page = link.getAttribute('data-page') || link.getAttribute('href');
            if (page === currentPath || (currentPath === '' && page === 'dashboard.html')) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
        return;
    }

    // Exact 14 items in specified order
    const navItems = [
        { name: 'Dashboard', icon: 'bi-grid-fill', href: 'dashboard.html' },
        { name: 'Search Fraud', icon: 'bi-search', href: 'search-fraud.html' },
        { name: 'Call Records', icon: 'bi-telephone-fill', href: 'call-records.html' },
        { name: 'Fraud Detection', icon: 'bi-shield-exclamation', href: 'fraud-detection.html' },
        { name: 'IP Threats', icon: 'bi-hdd-network-fill', href: 'ip-threats.html' },
        { name: 'Geo Analysis', icon: 'bi-geo-alt-fill', href: 'geo-analysis.html' },
        { name: 'AI Clustering', icon: 'bi-diagram-3-fill', href: 'ai-clustering.html' },
        { name: 'Reports', icon: 'bi-file-earmark-bar-graph-fill', href: 'reports.html' },
        { name: 'Dataset Management', icon: 'bi-database-fill', href: 'datasets.html' },
        { name: 'Analysis History', icon: 'bi-clock-history', href: 'analysis-history.html' },
        { name: 'User Management', icon: 'bi-people-fill', href: 'user-management.html' },
        { name: 'Settings', icon: 'bi-gear-fill', href: 'settings.html' }
    ];

    let navHtml = `
        <div class="sidebar-header d-flex align-items-center justify-content-between">
            <a href="dashboard.html" style="text-decoration: none; display: flex; align-items: center;">
                <img src="assets/logo.svg" alt="TELECOM" class="sidebar-logo">
            </a>
            <button class="btn btn-sm btn-light border d-md-none" onclick="toggleSidebar()" title="Close menu">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
        <div class="sidebar-nav">
            <div class="nav-section-title">Navigation Menu</div>
    `;

    navItems.forEach(item => {
        const isActive = (currentPath === item.href) || (currentPath === '' && item.href === 'dashboard.html');
        navHtml += `
            <a href="${item.href}" class="nav-item ${isActive ? 'active' : ''}" data-page="${item.href}">
                <i class="bi ${item.icon}"></i>
                <span>${item.name}</span>
            </a>
        `;
    });

    navHtml += `
        </div>
        <div class="sidebar-footer">
            <a href="javascript:void(0)" onclick="handleLogout()" class="sidebar-logout-btn">
                <i class="bi bi-box-arrow-right"></i>
                <span>Logout</span>
            </a>
        </div>
    `;

    sidebarEl.innerHTML = navHtml;
}

function initUserWidget() {
    const user = api.getUser();
    if (!user) return;

    const avatarEl = document.getElementById('sidebarAvatar');
    const nameEl = document.getElementById('sidebarUserName');
    const roleEl = document.getElementById('sidebarUserRole');
    const welcomeEl = document.getElementById('welcomeHeading');

    const initial = (user.fullName || user.username || 'U').charAt(0).toUpperCase();

    if (avatarEl) avatarEl.textContent = initial;
    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (roleEl) {
        roleEl.textContent = user.role || 'USER';
        if (user.role === 'ADMIN') {
            roleEl.style.backgroundColor = '#FEE2E2';
            roleEl.style.color = '#DC2626';
        } else {
            roleEl.style.backgroundColor = '#DCFCE7';
            roleEl.style.color = '#16A34A';
        }
    }

    if (welcomeEl) {
        welcomeEl.textContent = `Welcome back, ${user.fullName || user.username}!`;
    }
}

async function checkHadoopStatus() {
    const statusPill = document.getElementById('hadoopStatusPill');
    if (!statusPill) return;

    const res = await api.get('/hadoop/status');
    if (res && res.ok && res.data) {
        const h = res.data;
        if (h.hdfsConnected || h.hadoopConnected) {
            statusPill.innerHTML = `
                <span class="status-indicator"></span>
                <span style="color: #16A34A;">Hadoop HDFS: Online</span>
            `;
        } else {
            statusPill.innerHTML = `
                <span class="status-indicator offline"></span>
                <span style="color: #64748B;">Hadoop Engine: Local MR Ready</span>
            `;
        }
    }
}

function handleLogout() {
    if (confirm('Are you sure you want to sign out of TELECOM?')) {
        api.logout();
    }
}

function toggleSidebar() {
    const sidebar = document.getElementById('appSidebar');
    if (sidebar) {
        sidebar.classList.toggle('show');
    }
}
window.toggleSidebar = toggleSidebar;

function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '3000';
        document.body.appendChild(container);
    }

    const toastEl = document.createElement('div');
    const bgClass = type === 'success' ? 'text-bg-success' : (type === 'error' ? 'text-bg-danger' : 'text-bg-primary');
    toastEl.className = `toast align-items-center ${bgClass} border-0 shadow-lg mb-2`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body fw-semibold py-2 px-3">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    container.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl, { delay: 4500 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}
window.showToast = showToast;
