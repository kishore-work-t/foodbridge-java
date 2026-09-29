const API_BASE_URL = 'http://localhost:8080/api';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('authToken');
        this.user = JSON.parse(localStorage.getItem('currentUser') || 'null');
    }

    setAuth(token, user) {
        this.token = token;
        this.user = user;
        localStorage.setItem('authToken', token);
        localStorage.setItem('currentUser', JSON.stringify(user));
    }

    clearAuth() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
    }

    getHeaders(includeAuth = true) {
        const headers = {
            'Content-Type': 'application/json',
        };
        if (includeAuth && this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    }

    async handleResponse(response) {
        if (response.status === 401) {
            this.clearAuth();
            window.location.href = '/login.html';
            throw new Error('Session expired. Please login again.');
        }
        if (response.status === 403) {
            throw new Error('Access denied. You do not have permission to perform this action.');
        }
        const contentType = response.headers.get('content-type');
        const isJson = contentType && contentType.includes('application/json');
        const data = isJson ? await response.json() : await response.text();
        if (!response.ok) {
            const message = (data && data.message) || `Request failed with status ${response.status}`;
            throw new Error(message);
        }
        return data;
    }

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers: this.getHeaders(options.auth !== false),
            ...options,
        };
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            config.body = JSON.stringify(options.body);
        }
        try {
            const response = await fetch(url, config);
            return await this.handleResponse(response);
        } catch (error) {
            if (error instanceof TypeError && error.message.includes('fetch')) {
                throw new Error('Network error. Please check if the server is running.');
            }
            throw error;
        }
    }

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    post(endpoint, body) {
        return this.request(endpoint, { method: 'POST', body });
    }

    put(endpoint, body) {
        return this.request(endpoint, { method: 'PUT', body });
    }

    patch(endpoint, body) {
        return this.request(endpoint, { method: 'PATCH', body });
    }

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    isAuthenticated() {
        return !!this.token;
    }

    getCurrentUser() {
        return this.user;
    }

    hasRole(role) {
        return this.user && this.user.role === role;
    }
}

const api = new ApiClient();

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container') || createToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span>${message}</span>
        <button class="btn-close" onclick="this.parentElement.remove()">&times;</button>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
}

function showLoading(element) {
    if (!element) return;
    element.classList.add('loading');
    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.innerHTML = '<div class="spinner"></div>';
    element.appendChild(overlay);
    element.style.position = 'relative';
}

function hideLoading(element) {
    if (!element) return;
    element.classList.remove('loading');
    const overlay = element.querySelector('.loading-overlay');
    if (overlay) overlay.remove();
    element.style.position = '';
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDateOnly(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function getStatusBadgeClass(status) {
    const statusMap = {
        'AVAILABLE': 'success',
        'REQUESTED': 'info',
        'PARTIALLY_RESERVED': 'warning',
        'FULLY_RESERVED': 'warning',
        'EXPIRED': 'danger',
        'CANCELLED': 'secondary',
        'COMPLETED': 'success',
        'PENDING': 'warning',
        'APPROVED': 'info',
        'REJECTED': 'danger',
        'COLLECTED': 'success',
        'VERIFIED': 'success',
        'REJECTED': 'danger',
        'ACTIVE': 'success',
        'INACTIVE': 'secondary',
        'SUSPENDED': 'warning'
    };
    return statusMap[status] || 'secondary';
}

function getStatusLabel(status) {
    if (!status) return '-';
    return status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
}

function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

function closeModalOnOutsideClick(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    modal.addEventListener('click', (e) => {
        if (e.target === modal) hideModal(modalId);
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePhone(phone) {
    return /^[\d\s\-\+\(\)]{10,}$/.test(phone);
}

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('authToken');
    const user = JSON.parse(localStorage.getItem('currentUser') || 'null');
    if (token && user) {
        api.setAuth(token, user);
    }

    document.querySelectorAll('[data-modal-close]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-overlay');
            if (modal) modal.classList.remove('active');
        });
    });

    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('active');
        });
    });
});

function requireAuth(allowedRoles = []) {
    if (!api.isAuthenticated()) {
        window.location.href = '/login.html';
        return false;
    }
    if (allowedRoles.length > 0 && !allowedRoles.includes(api.getCurrentUser().role)) {
        window.location.href = '/index.html';
        return false;
    }
    return true;
}

function updateAuthUI() {
    const user = api.getCurrentUser();
    const authButtons = document.getElementById('auth-buttons');
    const userMenu = document.getElementById('user-menu');
    const userName = document.getElementById('user-name');
    const userRole = document.getElementById('user-role');

    if (user && authButtons && userMenu) {
        authButtons.classList.add('d-none');
        userMenu.classList.remove('d-none');
        if (userName) userName.textContent = user.name;
        if (userRole) userRole.textContent = user.role;
    } else if (authButtons && userMenu) {
        authButtons.classList.remove('d-none');
        userMenu.classList.add('d-none');
    }
}

function logout() {
    api.clearAuth();
    window.location.href = '/index.html';
}