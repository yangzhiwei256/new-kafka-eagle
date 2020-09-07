let mime = 'text/x-mariadb';
if (window.location.href.indexOf('mime=') > -1) {
    mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
}

// SQL编辑器对象
let sqlEditor = CodeMirror.fromTextArea(document.getElementById('code'), {
    mode: mime,
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets: true,
    autofocus: true,
    extraKeys: {
        "Alt-/": "autocomplete"
    }
});

// 日志编辑器
let logEditor = CodeMirror.fromTextArea(document.getElementById('log_info'), {
    mode: mime,
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets: true,
    autofocus: true,
    readOnly: true
});

$('#result_tab li:eq(0) a').tab('show');

function displayTopicMessage(sql, kafkaMessages) {
    $("#result_children").dataTable({
        "searching": true,
        // "bSort": false,
        "retrieve": true,
        // "bLengthChange" : false,
        // "bProcessing" : true,
        // "bServerSide" : true,
        // "fnServerData" : retrieveData,
        "aaData": kafkaMessages,
        "aoColumns": [
            {"mDataProp": "messageId"},
            {"mDataProp": "partition"},
            {"mDataProp": "offset"},
            {"mDataProp": "msg"},
        ]
    });
}

/** KSQL查询 **/
$("#query").click(function () {
    let sql = sqlEditor.getValue();
    logEditor.setValue(""); // 置空日志查询面板
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/logical/commit/?sql=' + sql,
        success: function (data) {
            if (data != null) {
                if (data.error) { // 后台处理错误则直接显示错误信息
                    logEditor.setValue(data.message);
                } else {
                    let spent = data.data.topicSqlHistory.spendTime;
                    let message = "query success cost time: " + spent + "ms";
                    logEditor.setValue(message);
                    displayTopicMessage(sql, data.data.queryKafkaMessage.data);
                }
                displayTopicSqlHistory();
            }
        }
    });
});

function displayTopicSqlHistory() {
    $("#ksql_history_result").dataTable({
        "bSort": false,
        "retrieve": true,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": '/topic/sql/history',
        "aoColumns": [
            {"mDataProp": "id"},
            {"mDataProp": "username"},
            {"mDataProp": "host"},
            {"mDataProp": "ksql"},
            {
                "mDataProp": "status",
                "mRender": function (data, type, full) {
                    console.log("=====> %s", data)
                    return 'failure' === data ? '<span class=".label label-danger"' > +data + '</span>' :
                        '<span class=".label label-success"' > +data + '</span>';
                }
            },
            {"mDataProp": "spendTime"},
            {"mDataProp": "created"},
        ]
    });
}