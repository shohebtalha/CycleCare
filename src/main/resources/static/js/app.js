(function () {
    const body = document.body;
    const themeToggle = document.getElementById("themeToggle");
    const savedTheme = localStorage.getItem("cyclecare-theme");

    if (savedTheme === "dark") {
        body.dataset.theme = "dark";
    }

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            const nextTheme = body.dataset.theme === "dark" ? "light" : "dark";
            if (nextTheme === "dark") {
                body.dataset.theme = "dark";
                localStorage.setItem("cyclecare-theme", "dark");
            } else {
                delete body.dataset.theme;
                localStorage.setItem("cyclecare-theme", "light");
            }
        });
    }

    if (window.lucide) {
        window.lucide.createIcons();
    }

    const nutritionLog = document.getElementById("nutritionLog");
    if (nutritionLog) {
        const mealFields = [
            ["Breakfast", document.getElementById("breakfast")],
            ["Lunch", document.getElementById("lunch")],
            ["Snacks", document.getElementById("snacks")],
            ["Dinner", document.getElementById("dinner")]
        ];
        const journalForm = nutritionLog.closest("form");

        function buildNutritionLog() {
            return mealFields
                .map(function (meal) {
                    const label = meal[0];
                    const field = meal[1];
                    const value = field ? field.value.trim() : "";
                    return value ? label + ": " + value : "";
                })
                .filter(Boolean)
                .join("\n");
        }

        function syncNutritionLog() {
            nutritionLog.value = buildNutritionLog();
        }

        mealFields.forEach(function (meal) {
            if (meal[1]) {
                meal[1].addEventListener("input", syncNutritionLog);
            }
        });

        if (journalForm) {
            journalForm.addEventListener("submit", syncNutritionLog);
        }
    }

    function entries(data) {
        return Object.entries(data || {});
    }

    function ensureData(items, fallbackLabel) {
        return items.length > 0 ? items : [[fallbackLabel, 0]];
    }

    function chartColors() {
        const styles = getComputedStyle(document.body);
        return {
            brand: styles.getPropertyValue("--brand").trim(),
            teal: styles.getPropertyValue("--teal").trim(),
            amber: styles.getPropertyValue("--amber").trim(),
            rose: styles.getPropertyValue("--rose").trim(),
            blue: styles.getPropertyValue("--blue").trim(),
            text: styles.getPropertyValue("--text").trim(),
            border: styles.getPropertyValue("--border").trim()
        };
    }

    function renderCharts() {
        if (!window.Chart || !window.cycleCareCharts) {
            return;
        }
        const colors = chartColors();
        Chart.defaults.color = colors.text;
        Chart.defaults.borderColor = colors.border;

        const cycleCanvas = document.getElementById("cycleChart");
        if (cycleCanvas) {
            const cycleItems = ensureData(entries(window.cycleCareCharts.cycleTrend), "No data");
            new Chart(cycleCanvas, {
                type: "line",
                data: {
                    labels: cycleItems.map(item => item[0]),
                    datasets: [{
                        label: "Cycle length",
                        data: cycleItems.map(item => item[1]),
                        borderColor: colors.brand,
                        backgroundColor: colors.brand,
                        tension: 0.35
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }

        const moodCanvas = document.getElementById("moodChart");
        if (moodCanvas) {
            const moodItems = ensureData(entries(window.cycleCareCharts.moodTrend), "No data");
            new Chart(moodCanvas, {
                type: "doughnut",
                data: {
                    labels: moodItems.map(item => item[0]),
                    datasets: [{
                        data: moodItems.map(item => item[1]),
                        backgroundColor: [colors.teal, colors.brand, colors.amber, colors.rose, colors.blue, "#6c757d", "#198754"]
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }

        const symptomCanvas = document.getElementById("symptomChart");
        if (symptomCanvas) {
            const symptomItems = ensureData(entries(window.cycleCareCharts.symptomFrequency), "No data");
            new Chart(symptomCanvas, {
                type: "bar",
                data: {
                    labels: symptomItems.map(item => item[0]),
                    datasets: [{
                        label: "Entries",
                        data: symptomItems.map(item => item[1]),
                        backgroundColor: colors.teal
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    layout: {
                        padding: {
                            bottom: 15
                        }
                    },
                    scales: {
                        x: {
                            ticks: {
                                autoSkip: false,
                                maxRotation: 30,
                                minRotation: 30
                            }
                        },
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }

                }
            });
        }

        const flowCanvas = document.getElementById("flowChart");
        if (flowCanvas) {
            const flowItems = ensureData(entries(window.cycleCareCharts.flowDistribution), "No data");
            new Chart(flowCanvas, {
                type: "bar",
                data: {
                    labels: flowItems.map(item => item[0]),
                    datasets: [{
                        label: "Entries",
                        data: flowItems.map(item => item[1]),
                        backgroundColor: colors.rose
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        }
    }

    renderCharts();

    // Initialize Toasts
    const toastElList = document.querySelectorAll('.toast');
    if (toastElList.length > 0) {
        const toastList = [...toastElList].map(toastEl => {
            const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
            toast.show();
            return toast;
        });
    }

    // Initialize Confirmation Modal
    const confirmForms = document.querySelectorAll('form[data-confirm]');
    if (confirmForms.length > 0) {
        const modalEl = document.getElementById('confirmationModal');
        if (modalEl) {
            const confirmationModal = new bootstrap.Modal(modalEl);
            const modalBody = document.getElementById('confirmationModalBody');
            const confirmBtn = document.getElementById('confirmationModalConfirmBtn');
            let currentFormToSubmit = null;

            confirmForms.forEach(form => {
                form.addEventListener('submit', function (e) {
                    e.preventDefault();
                    currentFormToSubmit = this;
                    const message = this.getAttribute('data-confirm');
                    if (message) {
                        modalBody.textContent = message;
                    }
                    confirmationModal.show();
                });
            });

            confirmBtn.addEventListener('click', function () {
                if (currentFormToSubmit) {
                    currentFormToSubmit.submit();
                }
            });
        }
    }
})();
