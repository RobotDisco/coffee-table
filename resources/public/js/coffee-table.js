log = function(msg, e) {
    console.log(msg + ":= " + e);
    console.dir(e);
};

coffee_table = {
    delete: function() {
	x = new XMLHttpRequest();
	x.onload = function(e) {
	    if (e.target.status == 200) {
		window.location.reload();
	    }
	};
	x.onerror = function(e) { log("onerror is ", e); };
	x.open("DELETE", window.location.pathname);
	x.send("");
    },
    update: function() {
	var f = new FormData(document.getElementById("visit"));
	var j = {};
	f.forEach(function(v, k) {
	    j[k] = v;
	});
	["service_rating", "ambience_rating", "beverage_rating"].forEach(function(v) {
	    if (typeof(j[v]) !== 'undefined') {
		j[v] = parseInt(j[v], 10);
	    }
	});

	x = new XMLHttpRequest();
	x.onload = function(e) {
	    if (e.target.status == 204) {
		window.location.reload();
	    }
	};
	x.onerror = function(e) { log("onerror is ", e); };
	x.open("PUT", window.location.pathname);
	x.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	x.send(JSON.stringify(j));
    }
};
