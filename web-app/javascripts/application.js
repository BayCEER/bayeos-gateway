function rssiLevel(rssi) {
    if (rssi < -85) {
    	return 1;
    } else if (rssi >= -85 && rssi < -75) {
    	return 2;
    } else if (rssi >= -75 && rssi < -65) {
    	return 3;
    } else if (rssi >= -65 && rssi < -45) {
    	return 4;
    } else if (rssi >= -45) {
    	return 5;
    }
}

function statusClass(status) {
    if (status == 0){
        // OK
        return "success";
    } else if (status == 1){
        // WARN
        return "warning";
    } else if (status == 2){
        // CRITICAL
        return "danger";
    } else if (status == 3){
        // UNKNOWN
        return "active";
    } else {
        // DEFAULT
        return 'default';
    }
}

function statusText(status) {
    if (status == 0){
        // OK
        return "Ok";
    } else if (status == 1){
        // WARN
        return "Warning";
    } else if (status == 2){
        // CRITICAL
        return "Critical";
    } else if (status == 3){
        // UNKNOWN
        return "Unknown";
    } else {
        // DEFAULT
        return 'Default';
    }
}