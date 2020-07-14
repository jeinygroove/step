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

/* nav buttons */

var sections = ['.home', '.projects', '.cats', '.skills']

console.log(document.querySelector('section' + sections[0]))

function makeScrollCallBack(section) {
    return function() {
        var sectionDOM = document.querySelector(section)
        sectionDOM.scrollIntoView(); 
        
        var height = document.querySelector('nav').clientHeight;
        var scrolledY = window.scrollY;
        if (scrolledY === sectionDOM.offsetTop) {
            window.scroll(0, scrolledY - height);
        }
    }
}

document.querySelector('.nav__logo').addEventListener('click', makeScrollCallBack('section.home'));

for (var i = 0; i < sections.length; i++) {
    document.querySelector('.nav__item' + sections[i]).addEventListener('click', makeScrollCallBack('section' + sections[i]));
}

document.querySelector('.nav__item.contacts').addEventListener('click', makeScrollCallBack('footer'));

/* projects buttons */

var listOfProjects = document.querySelector('.projects__names').childElementCount;

function makeChangeDescriptionCallBack(project) {
    return function() {
        document.querySelector('.project.active').classList.remove('active');
        document.querySelector('.projects__names').children[project].classList.add('active');
        
        document.querySelector('.project_description.active').classList.remove('active');
        document.querySelector('.projects__descriptions').children[project].classList.add('active');
    }
}

for (var i = 0; i < listOfProjects; i++) {
    document.querySelector('.projects__names').children[i].addEventListener('click', makeChangeDescriptionCallBack(i));
}