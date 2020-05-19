$(document).ready(function() {
	$('#pwd').bind('keypress', function(event) {
		if (event.keyCode == "13") {
			contextFormValid();
		}
	});
	$("a[id='submit']").click(function() {
		contextFormValid();
	});
});

function contextFormValid() {
    var url = window.location.href;
    var ref_url = "";
    var username = $("#usr").val();
    var password = $("#pwd").val();
    if (url.indexOf("/signin?") > -1) {
        ref_url = url.split("/signin?")[1];
    }
    if (ref_url.length === 0) {
        ref_url = "/";
    }
    $("#ref_url").val(ref_url);
    if (username.length === 0 || password.length === 0) {
        $("#alert_mssage").text("Account or password is not null.").show();
        setTimeout(function() {
            $("#alert_mssage").hide()
        }, 3000);
        return false;
    }
    return true;
}