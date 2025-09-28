// 全局变量
let charts = {};
let refreshInterval;
const API_BASE_URL = '/api/v1/evaluation';

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    loadDashboardData();
    
    // 设置自动刷新（每5分钟）
    refreshInterval = setInterval(loadDashboardData, 5 * 60 * 1000);
});

// 初始化页面
function initializePage() {
    updateLastUpdateTime();
    showLoading();
}

// 设置事件监听器
function setupEventListeners() {
    // 刷新按钮
    document.getElementById('refreshBtn').addEventListener('click', function() {
        loadDashboardData();
    });
    
    // 趋势图时间段选择
    document.getElementById('trendPeriod').addEventListener('change', function() {
        loadEvaluationTrend(this.value);
    });
    
    // 查看全部按钮
    document.getElementById('viewAllTasksBtn').addEventListener('click', function() {
        // 这里可以跳转到任务管理页面
        window.open('/api/v1/evaluation/tasks', '_blank');
    });
    
    document.getElementById('viewAllModelsBtn').addEventListener('click', function() {
        // 这里可以跳转到模型管理页面
        window.open('/api/v1/evaluation/models', '_blank');
    });
}

// 加载看板数据
async function loadDashboardData() {
    try {
        showLoading();
        
        // 并行加载所有数据
        const [overview, gradeDistribution, taskStatus, trend, recentTasks, highQualityModels] = await Promise.all([
            fetchSystemOverview(),
            fetchGradeDistribution(),
            fetchTaskStatusDistribution(),
            fetchEvaluationTrend(30),
            fetchRecentTasks(),
            fetchHighQualityModels()
        ]);
        
        // 更新概览卡片
        updateOverviewCards(overview);
        
        // 更新图表
        updateGradeDistributionChart(gradeDistribution);
        updateTaskStatusChart(taskStatus);
        updateEvaluationTrendChart(trend);
        
        // 更新表格
        updateRecentTasksTable(recentTasks);
        updateHighQualityModelsTable(highQualityModels);
        
        updateLastUpdateTime();
        hideLoading();
        
    } catch (error) {
        console.error('加载数据失败:', error);
        showError('加载数据失败，请稍后重试');
        hideLoading();
    }
}

// 获取系统概览数据
async function fetchSystemOverview() {
    const response = await axios.get(`${API_BASE_URL}/system/overview`);
    return response.data.data;
}

// 获取等级分布数据
async function fetchGradeDistribution() {
    const response = await axios.get(`${API_BASE_URL}/statistics/grade-distribution`);
    return response.data.data;
}

// 获取任务状态分布
async function fetchTaskStatusDistribution() {
    const response = await axios.get(`${API_BASE_URL}/tasks/statistics/status-distribution`);
    return response.data.data;
}

// 获取评估趋势数据
async function fetchEvaluationTrend(days) {
    const response = await axios.get(`${API_BASE_URL}/statistics/trend?days=${days}`);
    return response.data.data;
}

// 获取最近任务
async function fetchRecentTasks() {
    const response = await axios.get(`${API_BASE_URL}/tasks/recent?limit=10`);
    return response.data.data;
}

// 获取高质量模型
async function fetchHighQualityModels() {
    const response = await axios.get(`${API_BASE_URL}/models/high-quality?limit=10`);
    return response.data.data;
}

// 更新概览卡片
function updateOverviewCards(overview) {
    // 任务统计
    const taskStats = overview.task_stats || {};
    document.getElementById('totalTasks').textContent = formatNumber(taskStats.total || 0);
    document.getElementById('completedTasks').textContent = formatNumber(taskStats.completed || 0);
    document.getElementById('runningTasks').textContent = formatNumber(taskStats.running || 0);
    
    // 评估统计
    const evalStats = overview.evaluation_stats || {};
    document.getElementById('totalEvaluations').textContent = formatNumber(evalStats.total || 0);
    
    const avgScores = overview.average_scores || {};
    document.getElementById('avgScore').textContent = (avgScores.overall_score || 0).toFixed(1);
    
    const successRate = overview.success_rate || {};
    document.getElementById('successRate').textContent = (successRate.rate || 0).toFixed(1) + '%';
    
    // 评分统计
    const ratingStats = overview.rating_stats || {};
    document.getElementById('totalRatings').textContent = formatNumber(ratingStats.total || 0);
    document.getElementById('avgUserRating').textContent = (ratingStats.average_rating || 0).toFixed(1);
    document.getElementById('feedbackCount').textContent = formatNumber(ratingStats.with_feedback || 0);
}

// 更新等级分布饼图
function updateGradeDistributionChart(data) {
    const ctx = document.getElementById('gradeDistributionChart').getContext('2d');
    
    if (charts.gradeDistribution) {
        charts.gradeDistribution.destroy();
    }
    
    const labels = data.map(item => getGradeLabel(item.grade));
    const values = data.map(item => item.count);
    const colors = data.map(item => getGradeColor(item.grade));
    
    charts.gradeDistribution = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        usePointStyle: true
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return `${context.label}: ${context.parsed} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// 更新任务状态图表
function updateTaskStatusChart(data) {
    const ctx = document.getElementById('taskStatusChart').getContext('2d');
    
    if (charts.taskStatus) {
        charts.taskStatus.destroy();
    }
    
    const labels = data.map(item => getStatusLabel(item.status));
    const values = data.map(item => item.count);
    const colors = data.map(item => getStatusColor(item.status));
    
    charts.taskStatus = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderRadius: 8,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

// 更新评估趋势图表
function updateEvaluationTrendChart(data) {
    const ctx = document.getElementById('evaluationTrendChart').getContext('2d');
    
    if (charts.evaluationTrend) {
        charts.evaluationTrend.destroy();
    }
    
    const labels = data.map(item => formatDate(item.date));
    const evaluationCounts = data.map(item => item.evaluation_count || 0);
    const avgScores = data.map(item => item.avg_score || 0);
    
    charts.evaluationTrend = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '评估数量',
                data: evaluationCounts,
                borderColor: '#4f46e5',
                backgroundColor: 'rgba(79, 70, 229, 0.1)',
                tension: 0.4,
                yAxisID: 'y'
            }, {
                label: '平均评分',
                data: avgScores,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    position: 'top'
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: '日期'
                    }
                },
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: '评估数量'
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: '平均评分'
                    },
                    grid: {
                        drawOnChartArea: false
                    },
                    min: 0,
                    max: 10
                }
            }
        }
    });
}

// 更新最近任务表格
function updateRecentTasksTable(tasks) {
    const tbody = document.querySelector('#recentTasksTable tbody');
    tbody.innerHTML = '';
    
    tasks.forEach(task => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${task.taskId}</td>
            <td>${task.modelId}</td>
            <td>${getEvaluationTypeLabel(task.evaluationType)}</td>
            <td><span class="status-badge status-${task.status.toLowerCase()}">${getStatusLabel(task.status)}</span></td>
            <td>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${task.progress || 0}%"></div>
                </div>
                <small>${task.progress || 0}%</small>
            </td>
            <td>${formatDateTime(task.createdAt)}</td>
            <td>
                <button class="btn btn-secondary" onclick="viewTaskDetail('${task.taskId}')">查看</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 更新高质量模型表格
function updateHighQualityModelsTable(models) {
    const tbody = document.querySelector('#highQualityModelsTable tbody');
    tbody.innerHTML = '';
    
    models.forEach(model => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${model.modelId}</td>
            <td>${model.overallScore.toFixed(2)}</td>
            <td><span class="grade-badge grade-${model.grade.toLowerCase()}">${getGradeLabel(model.grade)}</span></td>
            <td>${model.geometricScore.toFixed(2)}</td>
            <td>${model.visualScore.toFixed(2)}</td>
            <td>${model.technicalScore.toFixed(2)}</td>
            <td>${formatDateTime(model.evaluatedAt)}</td>
        `;
        tbody.appendChild(row);
    });
}

// 工具函数
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
}

function getGradeLabel(grade) {
    const labels = {
        'EXCELLENT': '优秀',
        'GOOD': '良好',
        'FAIR': '一般',
        'POOR': '较差'
    };
    return labels[grade] || grade;
}

function getGradeColor(grade) {
    const colors = {
        'EXCELLENT': '#10b981',
        'GOOD': '#3b82f6',
        'FAIR': '#f59e0b',
        'POOR': '#ef4444'
    };
    return colors[grade] || '#6b7280';
}

function getStatusLabel(status) {
    const labels = {
        'PENDING': '待处理',
        'RUNNING': '运行中',
        'COMPLETED': '已完成',
        'FAILED': '失败'
    };
    return labels[status] || status;
}

function getStatusColor(status) {
    const colors = {
        'PENDING': '#f59e0b',
        'RUNNING': '#3b82f6',
        'COMPLETED': '#10b981',
        'FAILED': '#ef4444'
    };
    return colors[status] || '#6b7280';
}

function getEvaluationTypeLabel(type) {
    const labels = {
        'FULL': '完整评估',
        'GEOMETRIC': '几何评估',
        'VISUAL': '视觉评估',
        'TECHNICAL': '技术评估'
    };
    return labels[type] || type;
}

function updateLastUpdateTime() {
    const now = new Date();
    document.getElementById('lastUpdateTime').textContent = now.toLocaleString('zh-CN');
}

function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

function showError(message) {
    const errorElement = document.getElementById('errorMessage');
    const errorText = errorElement.querySelector('.error-text');
    errorText.textContent = message;
    errorElement.style.display = 'block';
    
    // 5秒后自动隐藏
    setTimeout(hideError, 5000);
}

function hideError() {
    document.getElementById('errorMessage').style.display = 'none';
}

// 加载特定时间段的趋势数据
async function loadEvaluationTrend(days) {
    try {
        const trend = await fetchEvaluationTrend(days);
        updateEvaluationTrendChart(trend);
    } catch (error) {
        console.error('加载趋势数据失败:', error);
        showError('加载趋势数据失败');
    }
}

// 查看任务详情
function viewTaskDetail(taskId) {
    // 这里可以打开任务详情模态框或跳转到详情页面
    window.open(`/api/v1/evaluation/tasks/${taskId}`, '_blank');
}

// 页面卸载时清理资源
window.addEventListener('beforeunload', function() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
    
    // 销毁所有图表
    Object.values(charts).forEach(chart => {
        if (chart) {
            chart.destroy();
        }
    });
});