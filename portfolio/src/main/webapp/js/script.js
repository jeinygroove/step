// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// google charts load
google.charts.load('current', {'packages':['corechart']});

/**
 * Return function, which moves to the section with index 'sectionIndex' and,
 * if the navbar ('nav') will hide part of the
 * section's contect, scrolls up by the navbar height.
 * @param  {number} sectionIndex index number of the section
 * @return {function}            callback function for the event
 */
function makeScrollCallBack(sectionIndex) {
    return function () {
        var sectionDOM = document.querySelector(sectionIndex);
        sectionDOM.scrollIntoView();

        var height = document.querySelector('nav').clientHeight;
        var scrolledY = window.scrollY;
        if (scrolledY === sectionDOM.offsetTop) {
            window.scroll(0, scrolledY - height);
        }
    }
}

/**
 * Return function, which hides old description (which has class 'active')
 * and replaces it with the new chosen,
 * changes styles for chosen '.project-name' and old '.project-name.active'.
 * @param  {number} projectIndex index number of the project name and its description, respectively
 * @return {function}            callback function for the event
 */
function makeChangeDescriptionCallBack(projectIndex) {
    return function () {
        document.querySelector('.project-name.active').classList.remove('active');
        document.querySelector('.projects-names').children[projectIndex].classList.add('active');

        document.querySelector('.project-description.active').classList.remove('active');
        document.querySelector('.projects-descriptions').children[projectIndex].classList.add('active');
    }
}

/**
 * Draws charts for words frequencies.
 */
function drawCharts() {
    fetch('/words', {method: 'GET'}).then(response => response.json()).then((wordsArray) => {
        const chartsDOM = document.querySelector('.charts-divs');
        var index = 0;
        wordsArray.forEach(album => {
            var data = new google.visualization.DataTable();
            data.addColumn('string', 'Words');
            data.addColumn('number', 'Number of appearances');
            data.addRows(Object.entries(album.words).map(
                ([_, wordObj]) => [wordObj.word, wordObj.frequency]));

            var options = {'title': album.musician + ' - \"' + album.albumTitle + '\"',
                           'width': 400,
                           'height': 300};

            const divElement = document.createElement('div');
            divElement.id = 'chart-' + index;
            chartsDOM.appendChild(divElement);
            var chart = new google.visualization.PieChart(divElement);
            chart.draw(data, options);

            index++;
        })
    })
}

/**
 * Puts shawarma markers and adds event listeners to show description.
 */
function putShawarmaMarkers() {
    fetch('/shawarma', {method: 'GET'}).then(response => response.json()).then((shawarmaPlaces) => {
        shawarmaPlaces.forEach(shawarmaPlace => {
            const shawarmaMarker = new google.maps.Marker({
                position: {
                    lat: shawarmaPlace.latitude,
                    lng: shawarmaPlace.longitude
                },
                map: map,
                title: shawarmaPlace.name
            });

            const shawarmaContent = `
            <h1>` + shawarmaPlace.name + `</h1>
            <p>` + shawarmaPlace.description + `</p>`;
            const infoWindow = new google.maps.InfoWindow({content: shawarmaContent});
            shawarmaMarker.addListener('click', () => {
                infoWindow.open(map, shawarmaMarker);
            });
        });
    });
}

// Initialize map.
var map;
const stPetersburgCenter = {
    latitude: 59.944186,
    longitude: 30.306510
};

function initMap() {
  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: stPetersburgCenter.latitude, lng: stPetersburgCenter.longitude },
    zoom: 13
  });

  // Initialize map markers.
  putShawarmaMarkers();
}

// comment button
document.querySelector('.home-comment-btn').addEventListener('click', function () {
    document.location.href = '/comments.html';
});

// nav buttons
var sections = ['.home', '.projects', '.cats', '.skills', '.shawarma', '.charts', '.contacts'];

document.querySelector('.nav-logo').addEventListener('click', makeScrollCallBack('.home-section'));

for (var i = 0; i < sections.length; i++) {
    document.querySelector('.nav-item' + sections[i]).addEventListener('click', makeScrollCallBack(sections[i] + '-section'));
}

// projects names buttons
var listOfProjects = document.querySelector('.projects-names').childElementCount;

for (var i = 0; i < listOfProjects; i++) {
    // After clicking on '.project-name' change the visible '.project-description'
    document.querySelector('.projects-names').children[i].addEventListener('click', makeChangeDescriptionCallBack(i));
}

// charts section
google.charts.setOnLoadCallback(drawCharts);