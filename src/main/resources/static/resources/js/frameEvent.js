var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#eventTable").show();
    }
    else {
        $("#eventTable").hide();
    }
    $("#eventRows").html("");
}

function connect() {
    var socket = new SockJS('/gateway/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/frameEvents', function (event) {
            addFrameEvent(JSON.parse(event.body));
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}


function addFrameEvent(e) {
    $("#eventRows").append("<tr><td>" + e.time + "</td><td>" + e.origin + "</td><td>" + e.type + "</td></tr>");
}

$(function () {
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
});