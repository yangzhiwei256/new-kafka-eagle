$(document).ready(function() {
	var uri = window.location.pathname;
	if (uri.indexOf("/cluster") > -1) {
		$("#demo2").addClass('collapse in');
		$("#demo2").attr("aria-expanded", true);
	} else if (uri == "/consumers") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_consumers").addClass('active');
	} else if (uri == "/") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_dash").addClass('active');
	} else if (uri.indexOf("/topic") > -1) {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
	} else if (uri.indexOf("/alarm") > -1) {
		$("#demo1").addClass('collapse in');
		$("#demo1").attr("aria-expanded", true);
		if (uri.indexOf("/alarm/add") > -1 || uri.indexOf("/alarm/modify") > -1) {
			$("#demo1_1").addClass('collapse in');
			$("#demo1_1").attr("aria-expanded", true);
		} else if (uri.indexOf("/alarm/create") > -1 || uri.indexOf("/alarm/history") > -1) {
			$("#demo1_2").addClass('collapse in');
			$("#demo1_2").attr("aria-expanded", true);
		} else if (uri.indexOf("/alarm/config") > -1 || uri.indexOf("/alarm/list") > -1) {
			$("#demo1_3").addClass('collapse in');
			$("#demo1_3").attr("aria-expanded", true);
		}
	} else if (uri.indexOf("/system") > -1) {
		$("#demo3").addClass('collapse in');
		$("#demo3").attr("aria-expanded", true);
	} else if (uri.indexOf("/metrics") > -1) {
		$("#demo4").addClass('collapse in');
		$("#demo4").attr("aria-expanded", true);
	}

	$(document).on('click', 'a[name=ke_account_reset]', function() {
		$('#ke_account_reset_dialog').modal('show');
		$(".modal-backdrop").css({
			"z-index" : "999"
		});
	});
});