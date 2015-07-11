<?php

require_once 'android_login_api/include/Config.php';

$dbcnx = mysql_connect (DB_HOST, DB_USER, DB_PASSWORD);
mysql_select_db(DB_DATABASE) or die(mysql_error());
?>

<html>
  <head>
    <meta charset="utf-8">
    <title>Make the Road by Walking</title>
    <style>
      html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
      #panel {
        position: absolute;
        top: 5px;
        left: 50%;
        margin-left: -180px;
        z-index: 5;
        background-color: #fff;
        padding: 5px;
        border: 1px solid #999;
      }
    </style>
    <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization"></script>
    <script>

var map, pointarray, heatmap;

var locations = [

<?
$userquery = mysql_query("SELECT * FROM users");
 while ($userrow = mysql_fetch_array($userquery)){
 $uid = $userrow['uid'];

 $query = mysql_query("SELECT * FROM loc".$uid);
 while ($row = mysql_fetch_array($query)){
 $lat=$row['lat'];
 $lon=$row['lon'];
 $speed=$row['speed'];
 //each point is weighted with its speed multiplied by a factor of 3, to compensate for the fact, that slow speeds produce points in shorter spatial intervals.
 echo ("{location: new google.maps.LatLng($lat, $lon), weight: ($speed * 3)}, ");
 }}
?>

];

function initialize() {
  var mapOptions = {
    center: new google.maps.LatLng(0, 0),
    zoom: 2,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  map = new google.maps.Map(document.getElementById('map-canvas'),
      mapOptions);

  var pointArray = new google.maps.MVCArray(locations);

  heatmap = new google.maps.visualization.HeatmapLayer({
    data: pointArray
  });

  heatmap.setMap(map);

  var bounds = new google.maps.LatLngBounds();
  for (i = 0; i < locations.length; i++) {
	bounds.extend(locations[i].location);
  }

  map.fitBounds(bounds);
}



function toggleHeatmap() {
  heatmap.setMap(heatmap.getMap() ? null : map);
}

function changeGradient() {
  var gradient = [
    'rgba(0, 255, 255, 0)',
    'rgba(0, 255, 255, 1)',
    'rgba(0, 191, 255, 1)',
    'rgba(0, 127, 255, 1)',
    'rgba(0, 63, 255, 1)',
    'rgba(0, 0, 255, 1)',
    'rgba(0, 0, 223, 1)',
    'rgba(0, 0, 191, 1)',
    'rgba(0, 0, 159, 1)',
    'rgba(0, 0, 127, 1)',
    'rgba(63, 0, 91, 1)',
    'rgba(127, 0, 63, 1)',
    'rgba(191, 0, 31, 1)',
    'rgba(255, 0, 0, 1)'
  ]
  heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}

function changeRadius() {
  heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
  heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

google.maps.event.addDomListener(window, 'load', initialize);

    </script>
  </head>

  <body>
    <div id="panel">
      <button onclick="toggleHeatmap()">Toggle Heatmap</button>
      <button onclick="changeGradient()">Change gradient</button>
      <button onclick="changeRadius()">Change radius</button>
      <button onclick="changeOpacity()">Change opacity</button>
    </div>
    <div id="map-canvas"></div>
  </body>
</html>