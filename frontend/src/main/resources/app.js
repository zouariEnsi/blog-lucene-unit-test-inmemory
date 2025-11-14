// API base URL - adjust based on your deployment
const API_BASE_URL = '/blog-lucene-app/api';

// Poll interval in milliseconds (10 seconds)
const POLL_INTERVAL = 10000;

// Current polling timeout ID
let pollingTimeoutId = null;

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Lucene Indexation Manager initialized');
    loadIndexStatus();
});

/**
 * Load the current index status from the API
 */
async function loadIndexStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/indexation/status`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const status = await response.json();
        updateUI(status);
        
        // If indexation is in progress, start polling
        if (status.status === 'IN_PROGRESS') {
            startPolling();
        }
    } catch (error) {
        console.error('Error loading index status:', error);
        showError('Failed to load index status. Please check if the backend is running.');
    }
}

/**
 * Start a new indexation job
 */
async function startIndexation() {
    const startButton = document.getElementById('startButton');
    startButton.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE_URL}/indexation/start`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Failed to start indexation');
        }
        
        console.log('Indexation started successfully');
        
        // Show progress section
        document.getElementById('progressSection').classList.remove('hidden');
        
        // Start polling for status updates
        startPolling();
        
    } catch (error) {
        console.error('Error starting indexation:', error);
        alert('Failed to start indexation: ' + error.message);
        startButton.disabled = false;
    }
}

/**
 * Start polling for status updates
 */
function startPolling() {
    // Clear any existing polling
    stopPolling();
    
    // Poll immediately, then continue every 10 seconds
    pollStatus();
}

/**
 * Stop polling for status updates
 */
function stopPolling() {
    if (pollingTimeoutId) {
        clearTimeout(pollingTimeoutId);
        pollingTimeoutId = null;
    }
}

/**
 * Poll for status update
 */
async function pollStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/indexation/status`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const status = await response.json();
        updateUI(status);
        
        // Continue polling if still in progress
        if (status.status === 'IN_PROGRESS') {
            pollingTimeoutId = setTimeout(pollStatus, POLL_INTERVAL);
        } else {
            stopPolling();
            // Re-enable start button when not in progress
            document.getElementById('startButton').disabled = false;
        }
    } catch (error) {
        console.error('Error polling status:', error);
        // Continue polling even on error
        pollingTimeoutId = setTimeout(pollStatus, POLL_INTERVAL);
    }
}

/**
 * Update the UI based on current status
 */
function updateUI(status) {
    updateExistingIndexInfo(status);
    updateProgressSection(status);
    updateHistorySection(status);
}

/**
 * Update existing index information section
 */
function updateExistingIndexInfo(status) {
    const existingIndexInfo = document.getElementById('existingIndexInfo');
    
    if (status.status === 'NOT_STARTED') {
        existingIndexInfo.innerHTML = '<p class="no-data">No index has been created yet. Click "Start New Indexation" to begin.</p>';
    } else {
        const statusIcon = getStatusIcon(status.status);
        const statusClass = getStatusClass(status.status);
        
        let html = `<p><strong>Status:</strong> <span class="${statusClass}">${statusIcon} ${status.status}</span></p>`;
        
        if (status.totalUsers > 0) {
            html += `<p><strong>Total Documents:</strong> ${status.totalUsers.toLocaleString()}</p>`;
        }
        
        if (status.startTime) {
            const startDate = new Date(status.startTime);
            html += `<p><strong>Started:</strong> ${formatDateTime(startDate)}</p>`;
        }
        
        if (status.endTime) {
            const endDate = new Date(status.endTime);
            html += `<p><strong>Completed:</strong> ${formatDateTime(endDate)}</p>`;
        }
        
        existingIndexInfo.innerHTML = html;
    }
}

/**
 * Update progress section
 */
function updateProgressSection(status) {
    const progressSection = document.getElementById('progressSection');
    const startButton = document.getElementById('startButton');
    
    if (status.status === 'IN_PROGRESS') {
        progressSection.classList.remove('hidden');
        startButton.disabled = true;
        
        // Calculate progress percentage
        const percentage = status.totalPages > 0 
            ? Math.round((status.processedPages / status.totalPages) * 100)
            : 0;
        
        // Update progress elements
        document.getElementById('statusMessage').textContent = status.message || 'Processing...';
        document.getElementById('progressPercentage').textContent = percentage + '%';
        document.getElementById('progressFill').style.width = percentage + '%';
        
        const details = `Processed: ${status.processedPages} of ${status.totalPages} pages | Users indexed: ${status.totalUsers.toLocaleString()}`;
        document.getElementById('progressDetails').textContent = details;
        
    } else {
        progressSection.classList.add('hidden');
        startButton.disabled = false;
    }
}

/**
 * Update history section
 */
function updateHistorySection(status) {
    const historyInfo = document.getElementById('historyInfo');
    
    if (status.status === 'NOT_STARTED') {
        historyInfo.innerHTML = '<p class="no-data">No indexation history available</p>';
    } else if (status.status === 'COMPLETED' || status.status === 'FAILED') {
        const statusIcon = getStatusIcon(status.status);
        const statusClass = getStatusClass(status.status);
        
        let html = `<p><strong>Last Status:</strong> <span class="${statusClass}">${statusIcon} ${status.status}</span></p>`;
        html += `<p><strong>Message:</strong> ${status.message}</p>`;
        
        if (status.totalUsers > 0) {
            html += `<p><strong>Users Indexed:</strong> ${status.totalUsers.toLocaleString()}</p>`;
        }
        
        if (status.startTime) {
            const startDate = new Date(status.startTime);
            html += `<p><strong>Started:</strong> ${formatDateTime(startDate)}</p>`;
        }
        
        if (status.endTime) {
            const endDate = new Date(status.endTime);
            html += `<p><strong>Completed:</strong> ${formatDateTime(endDate)}</p>`;
        }
        
        if (status.durationFormatted) {
            html += `<p><strong>Duration:</strong> ${status.durationFormatted}</p>`;
        }
        
        historyInfo.innerHTML = html;
    }
}

/**
 * Get status icon HTML
 */
function getStatusIcon(status) {
    const iconClass = {
        'NOT_STARTED': 'idle',
        'IN_PROGRESS': 'active',
        'COMPLETED': 'completed',
        'FAILED': 'failed'
    }[status] || 'idle';
    
    return `<span class="status-icon ${iconClass}"></span>`;
}

/**
 * Get CSS class for status
 */
function getStatusClass(status) {
    return {
        'NOT_STARTED': 'no-data',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'success',
        'FAILED': 'error'
    }[status] || '';
}

/**
 * Format date and time for display
 */
function formatDateTime(date) {
    const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    return date.toLocaleString('en-US', options);
}

/**
 * Show error message
 */
function showError(message) {
    const existingIndexInfo = document.getElementById('existingIndexInfo');
    existingIndexInfo.innerHTML = `<p class="error">${message}</p>`;
}

// Clean up polling when page is unloaded
window.addEventListener('beforeunload', function() {
    stopPolling();
});

/**
 * Switch between tabs
 */
function switchTab(tabName) {
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all tab buttons
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => {
        button.classList.remove('active');
    });
    
    // Show selected tab
    if (tabName === 'indexation') {
        document.getElementById('indexationTab').classList.add('active');
        document.querySelectorAll('.tab-button')[0].classList.add('active');
    } else if (tabName === 'search') {
        document.getElementById('searchTab').classList.add('active');
        document.querySelectorAll('.tab-button')[1].classList.add('active');
    }
}

/**
 * Search for users by name
 */
async function searchUsers() {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const searchResults = document.getElementById('searchResults');
    const query = searchInput.value.trim();
    
    if (!query) {
        searchResults.innerHTML = '<p class="no-data">Please enter a name to search</p>';
        return;
    }
    
    // Disable search button and show loading
    searchButton.disabled = true;
    searchResults.innerHTML = '<p class="search-loading">Searching...</p>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/search/users?name=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Failed to search users');
        }
        
        // Display results
        displaySearchResults(data);
        
    } catch (error) {
        console.error('Error searching users:', error);
        searchResults.innerHTML = `<div class="search-error">Error: ${error.message}</div>`;
    } finally {
        searchButton.disabled = false;
    }
}

/**
 * Display search results
 */
function displaySearchResults(users) {
    const searchResults = document.getElementById('searchResults');
    
    if (!users || users.length === 0) {
        searchResults.innerHTML = '<p class="no-data">No users found matching your search</p>';
        return;
    }
    
    let html = `<p class="search-count">Found ${users.length} user${users.length > 1 ? 's' : ''}</p>`;
    
    users.forEach(user => {
        const title = user.name?.title || '';
        const firstName = user.name?.first || '';
        const lastName = user.name?.last || '';
        const email = user.email || 'No email';
        
        const fullName = `${title} ${firstName} ${lastName}`.trim();
        
        html += `
            <div class="user-card">
                <div class="user-name">${fullName}</div>
                <div class="user-email"><a href="mailto:${email}">${email}</a></div>
            </div>
        `;
    });
    
    searchResults.innerHTML = html;
}

/**
 * Handle Enter key in search input
 */
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                searchUsers();
            }
        });
    }
});
