/**
 * TELECOM – Dashboard Controller & Chart.js Visualizations
 */

let callVolumeChart = null;
let fraudTrendsChart = null;
let riskDistributionChart = null;
let callTypeChart = null;
let currentVolumePeriod = 'daily';

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
});

async function loadDashboardData() {
    await Promise.all([
        loadStats(),
        checkDatasetRecordCount(),
        loadCallVolumeChart(currentVolumePeriod),
        loadFraudTrendsChart(),
        loadRiskDistributionChart(),
        loadCallTypeChart(),
        loadTopSuspicious(),
        loadRecentAlerts()
    ]);
}

async function loadStats() {
    const res = await api.get('/dashboard/stats');
    if (res && res.ok && res.data) {
        const s = res.data;
        document.getElementById('kpiTotalCalls').textContent = s.totalCalls.toLocaleString();
        document.getElementById('kpiAnalyzedCalls').textContent = s.analyzedCalls.toLocaleString();
        document.getElementById('kpiFraudDetected').textContent = s.fraudDetected.toLocaleString();
        document.getElementById('kpiDetectionRate').textContent = s.fraudDetectionRate + '%';
    }
}

async function loadCallVolumeChart(period = 'daily') {
    const res = await api.get(`/dashboard/call-volume?period=${period}`);
    if (!res || !res.ok || !res.data) return;

    const ctx = document.getElementById('callVolumeChart').getContext('2d');
    const { labels, data } = res.data;

    if (callVolumeChart) {
        callVolumeChart.data.labels = labels;
        callVolumeChart.data.datasets[0].data = data;
        callVolumeChart.update();
        return;
    }

    callVolumeChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Call Volume',
                data: data,
                borderColor: '#E63946',
                backgroundColor: 'rgba(230, 57, 70, 0.08)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.35,
                pointBackgroundColor: '#FFFFFF',
                pointBorderColor: '#E63946',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    padding: 10,
                    backgroundColor: '#1E293B',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    cornerRadius: 6
                }
            },
            scales: {
                x: {
                    grid: { color: '#F1F5F9' },
                    ticks: { color: '#64748B', font: { size: 12 } }
                },
                y: {
                    beginAtZero: true,
                    grid: { color: '#F1F5F9' },
                    ticks: { color: '#64748B', font: { size: 12 } }
                }
            }
        }
    });
}

function setCallVolumePeriod(period) {
    currentVolumePeriod = period;
    document.querySelectorAll('#btnPeriodDaily, #btnPeriodWeekly, #btnPeriodMonthly').forEach(btn => btn.classList.remove('active'));
    if (period === 'daily') document.getElementById('btnPeriodDaily').classList.add('active');
    if (period === 'weekly') document.getElementById('btnPeriodWeekly').classList.add('active');
    if (period === 'monthly') document.getElementById('btnPeriodMonthly').classList.add('active');
    loadCallVolumeChart(period);
}

async function loadFraudTrendsChart() {
    const res = await api.get('/dashboard/fraud-trends');
    if (!res || !res.ok || !res.data) return;

    const ctx = document.getElementById('fraudTrendsChart').getContext('2d');
    const { labels, data } = res.data;

    if (fraudTrendsChart) {
        fraudTrendsChart.data.labels = labels;
        fraudTrendsChart.data.datasets[0].data = data;
        fraudTrendsChart.update();
        return;
    }

    fraudTrendsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Fraud Detections',
                data: data,
                backgroundColor: '#E63946',
                borderRadius: 6,
                maxBarThickness: 28
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: { grid: { display: false }, ticks: { color: '#64748B' } },
                y: { beginAtZero: true, grid: { color: '#F1F5F9' }, ticks: { precision: 0, color: '#64748B' } }
            }
        }
    });
}

async function loadRiskDistributionChart() {
    const res = await api.get('/dashboard/risk-distribution');
    if (!res || !res.ok || !res.data) return;

    const ctx = document.getElementById('riskDistributionChart').getContext('2d');
    const { labels, data } = res.data;

    if (riskDistributionChart) {
        riskDistributionChart.data.labels = labels;
        riskDistributionChart.data.datasets[0].data = data;
        riskDistributionChart.update();
        return;
    }

    riskDistributionChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#DC2626', // High Risk
                    '#F59E0B', // Medium Risk
                    '#FACC15', // Low Risk
                    '#16A34A'  // Normal
                ],
                borderWidth: 2,
                borderColor: '#FFFFFF'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '68%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 14,
                        font: { size: 11.5, weight: '500' },
                        color: '#1E293B'
                    }
                }
            }
        }
    });
}

async function loadCallTypeChart() {
    const res = await api.get('/dashboard/call-types');
    if (!res || !res.ok || !res.data) return;

    const ctx = document.getElementById('callTypeChart').getContext('2d');
    const { labels, data } = res.data;

    if (callTypeChart) {
        callTypeChart.data.labels = labels;
        callTypeChart.data.datasets[0].data = data;
        callTypeChart.update();
        return;
    }

    callTypeChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Records',
                data: data,
                backgroundColor: '#3B82F6',
                borderRadius: 6,
                maxBarThickness: 32
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false }, ticks: { color: '#64748B' } },
                y: { beginAtZero: true, grid: { color: '#F1F5F9' }, ticks: { color: '#64748B' } }
            }
        }
    });
}

async function loadTopSuspicious() {
    const res = await api.get('/dashboard/top-suspicious');
    const tbody = document.getElementById('topSuspiciousTableBody');
    if (!tbody) return;

    if (!res || !res.ok || !res.data || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">No suspicious mobile entities identified yet.</td></tr>';
        return;
    }

    tbody.innerHTML = res.data.map(item => `
        <tr>
            <td>
                <span class="phone-pill"><i class="bi bi-phone me-1 text-muted"></i>+91 ${item.mobileNumber}</span>
            </td>
            <td><span class="fw-semibold text-secondary">${item.totalCalls}</span></td>
            <td>
                <div class="d-flex align-items-center gap-2">
                    <span class="fw-bold ${item.riskScore >= 70 ? 'text-danger' : 'text-warning'}">${item.riskScore}</span>
                    <div class="risk-meter flex-fill" style="width: 50px;">
                        <div class="risk-meter-fill ${item.riskScore >= 70 ? 'risk-fill-high' : 'risk-fill-medium'}" style="width: ${item.riskScore}%;"></div>
                    </div>
                </div>
            </td>
            <td>
                <a href="search-fraud.html?number=${item.mobileNumber}" class="btn btn-sm btn-outline-danger py-1 px-2" style="font-size: 11px;">
                    Investigate
                </a>
            </td>
        </tr>
    `).join('');
}

async function loadRecentAlerts() {
    const res = await api.get('/dashboard/recent-alerts');
    const tbody = document.getElementById('recentAlertsTableBody');
    if (!tbody) return;

    if (!res || !res.ok || !res.data || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No fraud alerts detected. System activity is normal.</td></tr>';
        return;
    }

    tbody.innerHTML = res.data.map(alert => {
        let badgeClass = 'badge-normal';
        if (alert.riskLevel.includes('HIGH')) badgeClass = 'badge-high';
        else if (alert.riskLevel.includes('MEDIUM')) badgeClass = 'badge-medium';
        else if (alert.riskLevel.includes('LOW')) badgeClass = 'badge-low';

        return `
            <tr>
                <td>
                    <span class="phone-pill">+91 ${alert.mobileNumber}</span>
                </td>
                <td><span class="fw-semibold text-secondary">${alert.totalCalls}</span></td>
                <td><span class="fw-bold ${alert.riskScore >= 70 ? 'text-danger' : 'text-dark'}">${alert.riskScore} / 100</span></td>
                <td><span class="${badgeClass}">${alert.riskLevel}</span></td>
                <td class="text-muted" style="font-size: 12.5px;">${alert.detectionDate}</td>
                <td>
                    <button class="btn btn-sm btn-light border py-1 px-2" style="font-size: 11px;"
                            onclick='openAlertModal(${JSON.stringify(alert).replace(/'/g, "&#39;")})'>
                        <i class="bi bi-eye"></i> Details
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function openAlertModal(alert) {
    document.getElementById('modalMobileTitle').innerHTML = `<i class="bi bi-shield-alert text-danger me-2"></i>+91 ${alert.mobileNumber}`;
    document.getElementById('modalInvestigateBtn').href = `search-fraud.html?number=${alert.mobileNumber}`;

    let reasonsHtml = '';
    if (alert.reasons) {
        const parts = alert.reasons.split(' | ');
        reasonsHtml = parts.map(r => `<li class="mb-1 text-danger-emphasis">${r}</li>`).join('');
    } else {
        reasonsHtml = '<li>Standard activity</li>';
    }

    document.getElementById('modalAlertContent').innerHTML = `
        <div class="mb-3 p-3 rounded" style="background-color: #F8FAFC; border: 1px solid #E2E8F0;">
            <div class="row g-2 text-center">
                <div class="col-4">
                    <small class="text-muted">Total Calls</small>
                    <div class="fw-bold fs-5">${alert.totalCalls}</div>
                </div>
                <div class="col-4">
                    <small class="text-muted">Distinct Targets</small>
                    <div class="fw-bold fs-5">${alert.distinctReceivers || 'N/A'}</div>
                </div>
                <div class="col-4">
                    <small class="text-muted">Risk Score</small>
                    <div class="fw-bold fs-5 text-danger">${alert.riskScore} / 100</div>
                </div>
            </div>
        </div>
        <h6 class="fw-bold mb-2 text-primary" style="font-size: 13.5px;">Triggered Fraud Rules:</h6>
        <ul style="font-size: 13px; padding-left: 20px;">
            ${reasonsHtml}
        </ul>
        <p class="text-muted mb-0" style="font-size: 11.5px;">
            <i class="bi bi-info-circle me-1"></i> Classified using MapReduce heuristic risk scoring. High-risk classification indicates abnormal volume or Ping-call patterns, not definitive criminal proof.
        </p>
    `;

    const modal = new bootstrap.Modal(document.getElementById('alertDetailsModal'));
    modal.show();
}

async function checkDatasetRecordCount() {
    try {
        const res = await api.get('/dashboard/stats');
        if (res && res.ok && res.data) {
            const count = res.data.totalCalls;
            const badge = document.getElementById('dashRecordCountText');
            if (badge) {
                badge.textContent = count > 0 ? `${count.toLocaleString()} Records in DB` : '0 Records Loaded';
            }
        }
    } catch (e) {
        console.warn('Dataset count check failed', e);
    }
}

async function handleDashLoadSampleCDR(force = false) {
    const btn = document.getElementById('dashLoadSampleBtn');
    const alertBox = document.getElementById('dashActionAlert');
    const badge = document.getElementById('dashJobStatusBadge');

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span> Loading Sample CDR...';
    }
    if (alertBox) alertBox.className = 'alert d-none';

    try {
        const res = await api.post(`/hadoop/load-sample${force ? '?force=true' : ''}`, { force });
        if (res && res.data && res.data.alreadyLoaded && !force) {
            const confirmReload = confirm(
                `Sample CDR dataset is already loaded (${res.data.existingRecords.toLocaleString()} records in database).\n\nDo you want to reload and replace existing records?`
            );
            if (confirmReload) {
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-cloud-arrow-down-fill me-1"></i> Load Sample CDR';
                }
                return handleDashLoadSampleCDR(true);
            } else {
                if (alertBox) {
                    alertBox.className = 'alert alert-info mt-3 mb-0 py-2 px-3';
                    alertBox.textContent = `Existing dataset retained (${res.data.existingRecords.toLocaleString()} records ready for fraud detection).`;
                }
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-cloud-arrow-down-fill me-1"></i> Load Sample CDR';
                }
                return;
            }
        }

        if (res && res.ok && res.data && res.data.success) {
            if (alertBox) {
                alertBox.className = 'alert alert-success mt-3 mb-0 py-2 px-3';
                alertBox.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i> <strong>Sample CDR Loaded:</strong> ${res.data.loadedRecords.toLocaleString()} Indian mobile records verified and saved to database (${res.data.rejectedRecords} rejected). Ready for MapReduce execution.`;
            }
            if (badge) {
                badge.className = 'badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 fs-6';
                badge.textContent = 'DATASET READY';
            }
            highlightDashStep(2);
            await loadDashboardData();
            await checkDatasetRecordCount();
        } else {
            if (alertBox) {
                alertBox.className = 'alert alert-danger mt-3 mb-0 py-2 px-3';
                alertBox.textContent = res?.data?.error || res?.data?.message || 'Failed to import sample CDR dataset.';
            }
        }
    } catch (e) {
        if (alertBox) {
            alertBox.className = 'alert alert-danger mt-3 mb-0 py-2 px-3';
            alertBox.textContent = 'Network error importing sample CDR: ' + e.message;
        }
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-cloud-arrow-down-fill me-1"></i> Load Sample CDR';
        }
    }
}

async function handleDashStartFraudDetection() {
    const btn = document.getElementById('dashStartJobBtn');
    const alertBox = document.getElementById('dashActionAlert');
    const spinner = document.getElementById('dashJobSpinner');
    const badge = document.getElementById('dashJobStatusBadge');

    if (btn) btn.disabled = true;
    if (spinner) {
        spinner.classList.remove('d-none');
        spinner.classList.add('d-flex');
    }
    if (alertBox) alertBox.className = 'alert d-none';
    if (badge) {
        badge.className = 'badge bg-warning-subtle text-warning border border-warning-subtle px-3 py-2 fs-6';
        badge.textContent = 'RUNNING';
    }

    highlightDashStep(3);

    try {
        const res = await api.post('/hadoop/start', {
            datasetName: 'Indian-Telecom-CDR-Primary',
            frequencyThreshold: 8,
            distinctReceiversThreshold: 6,
            wangiriDurationThreshold: 5,
            excessiveDurationThreshold: 3600,
            nightRatioThreshold: 0.35
        });

        highlightDashStep(4);
        await new Promise(r => setTimeout(r, 500));
        highlightDashStep(5);

        if (res && res.ok && res.data && res.data.status === 'COMPLETED') {
            if (badge) {
                badge.className = 'badge bg-success-subtle text-success border border-success-subtle px-3 py-2 fs-6';
                badge.textContent = 'COMPLETED';
            }
            if (alertBox) {
                alertBox.className = 'alert alert-success mt-3 mb-0 py-2 px-3';
                alertBox.innerHTML = `<i class="bi bi-shield-check me-1"></i> <strong>MapReduce Finished (${res.data.durationSeconds}s):</strong> ${res.data.processedRecords.toLocaleString()} CDR records processed. Identified <strong>${res.data.fraudRecords} suspicious cases</strong> (persisted to MySQL). Dashboard updated.`;
            }
            await loadDashboardData();
            await checkDatasetRecordCount();
        } else {
            if (badge) {
                badge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2 fs-6';
                badge.textContent = 'FAILED';
            }
            if (alertBox) {
                alertBox.className = 'alert alert-danger mt-3 mb-0 py-2 px-3';
                alertBox.textContent = res?.data?.errorDetails || 'Hadoop MapReduce processing failed.';
            }
        }
    } catch (e) {
        if (badge) {
            badge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2 fs-6';
            badge.textContent = 'FAILED';
        }
        if (alertBox) {
            alertBox.className = 'alert alert-danger mt-3 mb-0 py-2 px-3';
            alertBox.textContent = 'Failed to execute MapReduce job: ' + e.message;
        }
    } finally {
        if (btn) btn.disabled = false;
        if (spinner) {
            spinner.classList.add('d-none');
            spinner.classList.remove('d-flex');
        }
    }
}

function highlightDashStep(stepNum) {
    for (let i = 1; i <= 5; i++) {
        const el = document.getElementById('dashStep' + i);
        if (!el) continue;
        if (i < stepNum) {
            el.className = 'p-2 flex-fill rounded bg-success-subtle text-success border border-success-subtle fw-semibold';
        } else if (i === stepNum) {
            el.className = 'p-2 flex-fill rounded bg-danger-subtle text-danger border border-danger fw-bold';
        } else {
            el.className = 'p-2 flex-fill rounded bg-white border';
        }
    }
}
