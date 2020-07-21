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

// comment button
document.querySelector('.home-comment-btn').addEventListener('click', function () {
    document.location.href = '/comments.html';
});

// nav buttons
var sections = ['.home', '.projects', '.cats', '.skills', '.contacts'];

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