$("select").dropdown();

var Color = net.brehaut.Color;

function afterJavaLoading () {
    var a = 15;
    const KEY_PROVIDERS_COUNT = RGStats.getProvidersCount();
    const TABLE_PRECISION=16;
    var colors = new Array(KEY_PROVIDERS_COUNT);
    var colorGroups = [
        'red', 'green', 'pink', 'orange', 'blue', 'monochrome', 'yellow', 'purple'
    ];
    var testNames = [
        "Критерий Пирсона", "Тест Чезаро", "Критерий серий"
    ];
    var providerNames = RGStats.getProviderNames(new Array(KEY_PROVIDERS_COUNT));

    var huePicker = Math.floor(Math.random()*colorGroups.length);
    for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
        colors[i] = Color(
            randomColor({
                luminosity: 'light',
                hue: colorGroups[(huePicker++)%colorGroups.length]
            })
        ).saturateByAmount(0.8).toString();
    }

    //Заполняем легенду (встроенная от Chart.js над тремя графиками плохо смотрится)
    (function () {
        var htmlStr = "";
        for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
            htmlStr += "<span><span class=\"badge\" style=\"background-color: "+colors[i]+"\">&nbsp;&nbsp;</span>&nbsp; "+providerNames[i]+"</span>";
        }
        $(".providers-legends").html(htmlStr)
    }).call();

    //Графики фиксированной высоты лучше смотрятся
    var chartsHeight=300;

    const expectedQuantille = [
        6.63490, 11.34487, 30.57791
    ];
    //Пирсон
    var pearsonStats = RGStats.getPearsonStats(new Array(KEY_PROVIDERS_COUNT*3));
    function setPearsonTable() {
        var rowsHtml = "";
        for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
            rowsHtml+="<tr>";
            rowsHtml+="<td>"+providerNames[i]+"</td>";
            for (var j=0; j<3; j++) {
                var ps = pearsonStats[i*3+j];
                rowsHtml+="<td><span class=\""+(ps>expectedQuantille[j] ? "red" : "green")+"\">"+ps.toFixed(5)+"</span></td>";
            }
            rowsHtml+="</tr>";
        }
        document.getElementById("pearson-columns").innerHTML = rowsHtml;
    }
    setPearsonTable();

    //Тест Чезаро
    var chesaroStats = RGStats.getChesaroStats(new Array(KEY_PROVIDERS_COUNT));
    var chesaroDatasets = new Array(KEY_PROVIDERS_COUNT);

    for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
        chesaroDatasets[i] = {
            label: providerNames[i],
            backgroundColor: colors[i],
            borderColor: Color(colors[i]).darkenByRatio(0.25).shiftHue(10).toString(),
            borderWidth: 1,
            hoverBackgroundColor: colors[i],
            hoverBorderColor: Color(colors[i]).darkenByRatio(0.25).shiftHue(10).toString(),
            data: [chesaroStats[i]]
        }
    }

    var chesaroChart = new Chart(document.getElementById("chesaro-chart"), {
        type: "bar",
        data: {
            labels: [testNames[1]],
            datasets: chesaroDatasets
        },
        options: {
            scales: {
                xAxes: [{
                    stacked: false,
                    barPercentage: 0.95,
                    ticks: {
                        beginAtZero: true
                    }
                }],
                yAxes: [{
                    stacked: false,
                    ticks: {
                        max: 1.5,
                        min: 0
                    }
                }]
            },
            legend: {
                display: false
            },
            tooltips: {
                enabled: false
            },
            responsive: true,
            maintainAspectRatio: false,
            height: chartsHeight
        }
    });

    //Критерий серий
    var seriesStats = RGStats.getSeriesStats(new Array(KEY_PROVIDERS_COUNT));
    var seriesDatasets = new Array(KEY_PROVIDERS_COUNT);

    for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
        seriesDatasets[i] = {
            label: providerNames[i],
            backgroundColor: colors[i],
            borderColor: Color(colors[i]).darkenByRatio(0.25).shiftHue(10).toString(),
            borderWidth: 1,
            hoverBackgroundColor: colors[i],
            hoverBorderColor: Color(colors[i]).darkenByRatio(0.25).shiftHue(10).toString(),
            data: [seriesStats[i]]
        }
    }

    var seriesChart = new Chart(document.getElementById("series-chart"), {
        type: "bar",
        data: {
            labels: [testNames[2]],
            datasets: seriesDatasets
        },
        options: {
            scales: {
                xAxes: [{
                    stacked: false,
                    barPercentage: 0.95,
                    ticks: {
                        beginAtZero: true
                    }
                }],
                yAxes: [{
                    stacked: false,
                }]
            },
            legend: {
                display: false
            },
            tooltips: {
                enabled: false
            },
            responsive: true,
            maintainAspectRatio: false,
            height: chartsHeight
        }
    });

    function writeCoeffTable() {
        var innerHtml = "";
        for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
            innerHtml += "<tr>" +
                "<td>"+providerNames[i]+"</td>" +
                "<td>"+"<span class=\""+(chesaroStats[i]<0.5 ? "green" : "red")+"\">"+chesaroStats[i].toFixed(TABLE_PRECISION)+"</span></td>" +
                "<td>"+"<span class=\""+(seriesStats[i]<0.125 ? "green" : "red")+"\">"+seriesStats[i].toFixed(TABLE_PRECISION)+"</span></td>" +
                "</tr>";
        }
        $("#coeff-table > tbody").html(innerHtml);
    }
    writeCoeffTable();

    //Триггерит вычисление новых характеристик
    showResult = function() {
        var keysCount = $("#keys-count").val();
        RGStats.resetKeys(keysCount);

        pearsonStats = RGStats.getPearsonStats(pearsonStats);

        chesaroStats = RGStats.getChesaroStats(chesaroStats);
        for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
            chesaroChart.data.datasets[i].data[0] = chesaroStats[i];
        }
        chesaroChart.update();

        seriesStats = RGStats.getSeriesStats(seriesStats);
        for (var i=0; i<KEY_PROVIDERS_COUNT; i++) {
            seriesChart.data.datasets[i].data[0] = seriesStats[i];
        }
        seriesChart.update();
        writeCoeffTable();
        pearsonStats = RGStats.getPearsonStats(new Array(KEY_PROVIDERS_COUNT*3));
        setPearsonTable();
    };
};
