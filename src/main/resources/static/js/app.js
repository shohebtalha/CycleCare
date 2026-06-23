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
                    scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
                }
            });
        }
    }

    renderCharts();
})();
