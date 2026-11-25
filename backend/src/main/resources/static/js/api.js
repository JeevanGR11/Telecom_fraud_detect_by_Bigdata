/**
 * TELECOM – Frontend API Client & Session Manager
 */

const API_BASE = '/api';

const api = {
    getToken() {
        return localStorage.getItem('telecom_jwt_token');
    },

    setToken(token) {
        if (token) {
            localStorage.setItem('telecom_jwt_token', token);
        } else {
            localStorage.removeItem('telecom_jwt_token');
        }
    },

    getUser() {
        const u = localStorage.getItem('telecom_user');
        try {
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    setUser(user) {
        if (user) {
            localStorage.setItem('telecom_user', JSON.stringify(user));
        } else {
            localStorage.removeItem('telecom_user');
        }
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        this.setToken(null);
        this.setUser(null);
        window.location.href = 'login.html';
    },

    async request(endpoint, options = {}) {
        const url = endpoint.startsWith('http') ? endpoint : (API_BASE + endpoint);
        const headers = options.headers || {};

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        if (!options.isFormData && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                // Ignore 401 for login endpoint
                if (!endpoint.includes('/auth/login')) {
                    this.setToken(null);
                    this.setUser(null);
                    window.location.href = 'login.html?expired=true';
                    return null;
                }
            }

            // If response is downloadable blob/attachment
            const disposition = response.headers.get('Content-Disposition');
            if (disposition && disposition.includes('attachment')) {
                return await response.blob();
            }

            const data = await response.json();
            return { ok: response.ok, status: response.status, data };
        } catch (error) {
            console.error('API Error:', error);
            return { ok: false, status: 0, data: { error: error.message || 'Network request failed' } };
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    upload(endpoint, formData) {
        return this.request(endpoint, {
            method: 'POST',
            body: formData,
            isFormData: true
        });
    }
};

/**
 * Toast Notification Helper
 */
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast align-items-center show border-${type === 'error' ? 'danger' : type}`;
    toast.setAttribute('role', 'alert');
    toast.style.minWidth = '280px';
    toast.style.padding = '12px 16px';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.justifyContent = 'space-between';
    toast.style.gap = '12px';

    const bgMap = {
        success: '#16A34A',
        error: '#DC2626',
        warning: '#F59E0B',
        info: '#0284C7'
    };

    toast.innerHTML = `
        <div style="display: flex; align-items: center; gap: 10px;">
            <span style="width: 10px; height: 10px; border-radius: 50%; background-color: ${bgMap[type] || '#0284C7'};"></span>
            <span style="font-size: 13.5px; font-weight: 500; color: #1E293B;">${message}</span>
        </div>
        <button type="button" class="btn-close btn-close-sm" style="font-size: 10px;" onclick="this.parentElement.remove()"></button>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        if (toast && toast.parentElement) {
            toast.remove();
        }
    }, 4500);
}
