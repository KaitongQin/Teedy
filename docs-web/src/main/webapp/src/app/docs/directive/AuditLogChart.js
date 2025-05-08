'use strict';

angular.module('docs').directive('auditLogChart', ['$timeout', '$window', function($timeout, $window) {
    return {
        restrict: 'E',
        scope: {
            logs: '='
        },
        template: '<div class="chart-wrapper"><canvas id="auditLogChart"></canvas></div>',
        link: function(scope, element, attrs) {
            var chart = null;

            var typeColors = {
                'CREATE': '#28a745', // green
                'UPDATE': '#17a2b8', // blue
                'DELETE': '#dc3545'  // red
            };

            function initChart(data) {
                if (!$window.Chart) {
                    console.error('Chart.js is not loaded');
                    return;
                }

                var ctx = element.find('canvas')[0].getContext('2d');
                
                // Group logs by date and type
                var groupedData = {};
                data.forEach(function(log) {
                    var date = new Date(log.create_date).toLocaleDateString();
                    if (!groupedData[date]) {
                        groupedData[date] = {};
                    }
                    if (!groupedData[date][log.type]) {
                        groupedData[date][log.type] = 0;
                    }
                    groupedData[date][log.type]++;
                });

                // Prepare chart data
                var dates = Object.keys(groupedData).sort();
                var types = ['CREATE', 'UPDATE', 'DELETE'];
                
                var datasets = types.map(function(type) {
                    return {
                        label: type,
                        data: dates.map(function(date) {
                            return groupedData[date][type] || 0;
                        }),
                        borderColor: typeColors[type],
                        backgroundColor: typeColors[type] + '20', // Add transparency
                        fill: true,
                        tension: 0.4,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    };
                });

                if (chart) {
                    chart.destroy();
                }

                chart = new $window.Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: dates,
                        datasets: datasets
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        interaction: {
                            intersect: false,
                            mode: 'index'
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                title: {
                                    display: true,
                                    text: 'Number of Activities',
                                    font: {
                                        weight: 'bold'
                                    }
                                },
                                ticks: {
                                    precision: 0,
                                    font: {
                                        size: 11
                                    }
                                }
                            },
                            x: {
                                title: {
                                    display: true,
                                    text: 'Date',
                                    font: {
                                        weight: 'bold'
                                    }
                                },
                                ticks: {
                                    font: {
                                        size: 11
                                    }
                                }
                            }
                        },
                        plugins: {
                            title: {
                                display: true,
                                text: 'Audit Log Activity Over Time',
                                font: {
                                    size: 16,
                                    weight: 'bold'
                                },
                                padding: {
                                    top: 10,
                                    bottom: 20
                                }
                            },
                            legend: {
                                position: 'top',
                                labels: {
                                    usePointStyle: true,
                                    padding: 20,
                                    font: {
                                        size: 12
                                    }
                                }
                            },
                            tooltip: {
                                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                                padding: 12,
                                titleFont: {
                                    size: 13,
                                    weight: 'bold'
                                },
                                bodyFont: {
                                    size: 12
                                },
                                callbacks: {
                                    title: function(tooltipItems) {
                                        return tooltipItems[0].label;
                                    },
                                    label: function(context) {
                                        return context.dataset.label + ': ' + context.parsed.y;
                                    }
                                }
                            }
                        }
                    }
                });
            }

            // Watch for window resize to redraw chart
            angular.element($window).bind('resize', function() {
                if (chart) {
                    $timeout(function() {
                        initChart(scope.logs);
                    });
                }
            });

            scope.$watch('logs', function(newLogs) {
                if (newLogs && newLogs.length > 0) {
                    $timeout(function() {
                        initChart(newLogs);
                    });
                }
            }, true);

            // Cleanup on destroy
            scope.$on('$destroy', function() {
                if (chart) {
                    chart.destroy();
                }
                angular.element($window).unbind('resize');
            });
        }
    };
}]); 