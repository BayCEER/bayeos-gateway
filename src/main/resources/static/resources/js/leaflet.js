const osm = L.tileLayer(
	"https://tile.openstreetmap.org/{z}/{x}/{y}.png",
	{
		maxZoom: 19,
		attribution:
			'&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
	}
);


const esri = L.tileLayer(
	'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
	{
		attribution:
			'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
	}
);

const baseLayers = {
     			'OpenStreetMap': osm,
     			'Esri.WorldImagery': esri
     	};

     	
function getSVGMarker(color){
	 return `<svg viewBox="0 0 500 820" version="1.1" xmlns="http://www.w3.org/2000/svg" xml:space="preserve"
     style="fill-rule: evenodd; clip-rule: evenodd; stroke-linecap: round;">    
	    <g transform="matrix(19.5417,0,0,19.5417,-7889.1,-9807.44)">
	        <path fill="#FFFFFF" d="M421.2,515.5c0,2.6-2.1,4.7-4.7,4.7c-2.6,0-4.7-2.1-4.7-4.7c0-2.6,2.1-4.7,4.7-4.7 C419.1,510.8,421.2,512.9,421.2,515.5z"/>
	        <path d="M416.544,503.612C409.971,503.612 404.5,509.303 404.5,515.478C404.5,518.256 406.064,521.786 407.194,524.224L416.5,542.096L425.762,524.224C426.892,521.786 428.5,518.433 428.5,515.478C428.5,509.303 423.117,503.612 416.544,503.612ZM416.544,510.767C419.128,510.784 421.223,512.889 421.223,515.477C421.223,518.065 419.128,520.14 416.544,520.156C413.96,520.139 411.865,518.066 411.865,515.477C411.865,512.889 413.96,510.784 416.544,510.767Z" stroke-width="1.1px" fill="${color}" stroke="white"/>
	    </g></svg>`;	 
}     	


const markerColors = [
  		"#5cb85c",
  		"#f0ad4e",
  		"#d9534f"
]; 
  	