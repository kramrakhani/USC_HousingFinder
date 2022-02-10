myStorage = window.sessionStorage;


document.querySelectorAll(".locality__toggle").forEach((option) => {
	option.onclick = function() {
		fetch("./PostHandler?mode=locality&locality=" + option.dataset.locality)
			.then(response => response.json())
			.then(responseJson => {
				populate(responseJson);
			});
		document.querySelector("#locality__current").innerHTML = option.innerHTML;
	}
});

document.querySelector("#locality__reset").onclick = function () {
	fetch("./PostHandler?mode=all")
		.then(response => response.json())
		.then(responseJson => populate(responseJson));
	document.querySelector("#locality__current").innerHTML = "All Locations";
}

document.querySelector("#search").onsubmit = function (e) {
	e.preventDefault();
	document.querySelector("#locality__current").innerHTML = "All Locations";
	fetch("./PostHandler?mode=search&search=" + document.querySelector("#searchbar").value)
		.then(response => response.json())
		.then(responseJson => populate(responseJson));
	document.querySelector("#searchbar").value = "";
}

fetch("./Updated")
	.then(response => response.json())
	.then(responseJson => {
		myStorage.setItem('count', responseJson.count)
		doPoll();
		});

fetch('./PostHandler?mode=all')
	.then(response => response.json())
	.then(responseJson => populate(responseJson));
	
function doPoll(){
	fetch("./Updated")
		.then(response => response.json())
		.then(responseJson => {
			if ((responseJson.count - myStorage.getItem("count")) > 0) {
			document.querySelector("#newPosts").innerHTML = (responseJson.count - myStorage.getItem("count"));
			document.querySelector("#generate").classList.remove("d-none");
			document.querySelector("#generate").classList.add("d-block");
			}
			setTimeout(doPoll, 5000);
		});
}


document.querySelector("#generate").onclick = function() {
	fetch('./PostHandler?mode=all')
	.then(response => response.json())
	.then(responseJson => populate(responseJson.slice(0, 20)));
	fetch("./Updated")
	.then(response => response.json())
	.then(responseJson => {
		myStorage.setItem('count', responseJson.count)
		});
	this.classList.add("d-none");
	this.classList.remove("d-block");
	document.querySelector("#locality__current").innerHTML = "All Locations";
}

var dummy = [
			{"author_id":1,
			"post_id":1739,
			"description":"a description",
			"address":"123 A street",
			"time":"2021-04-18 00:00:00"},

			{"author_id":2,
			"post_id":1740,
			"description":"A second description",
			"address":"234 Another Street",
			"time":"2021-04-18 00:00:00"}
			];

const populate = (properties) => {
	document.querySelector(".properties").innerHTML = "";
	properties.forEach((property) => {
		var newProperty = document.createElement("div");
		newProperty.classList.add("property");

		// newly added content-----------------------------------------------
		var image = document.createElement("img");
		image.src = "https://cdn3.iconfinder.com/data/icons/placeholder-1/64/house-placeholder-pin-pointer-gps-map-location-512.png";
		image.classList.add("placeholder_image");
		newProperty.appendChild(image);
		// ------------------------------------------------------------------

		var title = document.createElement("h2");
		var rooms = document.createElement("h3");
		
		title.appendChild(document.createTextNode(property.address));
		var paragraph = document.createElement("p");
		if (property.description) {
			paragraph.appendChild(document.createTextNode(property.description));
		}
		newProperty.appendChild(title);
		if (property.rooms) {
			rooms.appendChild(document.createTextNode("Rooms: " + property.rooms));
		}
		newProperty.appendChild(rooms);

		if (property.price) {
			rooms.appendChild(document.createTextNode(" | Price Per Month: $" + property.price));
		}
		newProperty.appendChild(rooms);
		newProperty.appendChild(paragraph);
		var button = document.createElement("a");
		button.href = "./details_email/details.html?post_id=" + property.post_id;
		button.appendChild(document.createTextNode("details"));
		var locality = document.createElement("h6");
		if (property.locality) {
		locality.appendChild(document.createTextNode(property.locality));
		}	
		newProperty.appendChild(locality);
		newProperty.appendChild(button);
		document.querySelector(".properties").appendChild(newProperty);
	});

}