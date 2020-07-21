var selectedSortType = localStorage.getItem('sortType', selectedSortType);

/**
 * Adds comments, sorted according to the 'type', to the list '.comments'
 * @param  {string}  type  type for sorting comments ('date' or 'rating'), if it's unknown type, then 'date' by default
 */
function getComments(type) {
    var params = new URLSearchParams();
    params.append('type', type);
    fetch('/comments', {method: 'GET', headers: params}).then(response => response.json()).then((comments) => {
        const listOfCommentsDOM = document.querySelector('.comments-list');
        listOfCommentsDOM.innerHTML = '';
        for (var i = 0; i < comments.length; i++) {
            listOfCommentsDOM.appendChild(
                createListElement(comments[i].value.text, comments[i].value.rating, comments[i].value.date, comments[i].key));
        }
    });
}

/**
 * Upvoted/Downvotes comment with the given ID
 * @param  {boolean}  isUpvote  true if we want to upvote and false if not
 * @param  {String}  commentID  id of comment for voting
 */
function voteComment(isUpvote, commentID) {
    var params = new URLSearchParams();
    params.append('comment-id', commentID);
    params.append('action', 'vote');
    params.append('isUpvote', isUpvote);
    fetch('/comments', {method: 'POST', body: params}).then(response => {
        if (response.status === 200)
            getComments(selectedSortType);
        else
            console.error(response.message);
    });
}

/**
 * Delete comment with the given ID
 * @param  {String}  commentID  id of comment for voting
 */
function deleteComment(commentID) {
    var params = new URLSearchParams();
    params.append('comment-id', commentID);
    params.append('action', 'delete');
    fetch('/comments', {method: 'POST', body: params}).then(response => {
        if (response.status === 200)
            getComments(selectedSortType);
        else
            console.error(response.message);
    });
}

if (selectedSortType === 'date' || selectedSortType === 'rating')
    getComments(selectedSortType);
else {
    getComments('date');
    selectedSortType = 'date';
}

/** Creates an <li class="comment"> element.
 * @param  {String} text       comment text
 * @param  {number} rating     rating of the comment
 * @param  {String} date       date of the comment publication
 * @param  {String} commentID  comment id
 * */
function createListElement(text, rating, date, commentID) {
    const liElement = document.createElement('li');
    liElement.classList.add('comment');
    liElement.id = commentID;
    liElement.innerHTML = `
        <div class="vote-btns">
            <div class="upvote-btn" type="button"></div>
            <p class="rating">` + rating + `</p>
            <div class="downvote-btn" type="button"></div>
        </div>
        <img class="delete-btn" src="images/delete-btn4.png">
        <div class="comment-content">
            <p class="comment-text">` + text + `</p>
            <p class="comment-date">` + date + `</p>
        </div>`;

    // add event listeners for delete and vote buttons
    liElement.querySelector('.delete-btn').addEventListener('click', function() {
        deleteComment(commentID);
    });
    liElement.querySelector('.upvote-btn').addEventListener('click', () => {
        voteComment(true, commentID)
    });
    liElement.querySelector('.downvote-btn').addEventListener('click', () => {
        voteComment(false, commentID)
    });
    return liElement;
}

/* add event listener to the logo */

document.querySelector('.nav-logo').addEventListener('click', function () {
    document.location.href = '/';
});

/* comments sort by selector */

for (const dropdown of document.querySelectorAll(".sort-type-select-wrapper")) {
    dropdown.addEventListener('click', function () {
        this.querySelector('.sort-type-select').classList.toggle('open');
    })
}

for (const option of document.querySelectorAll(".sort-option")) {
    if (option.attributes['data-value'].value === selectedSortType) {
        option.parentNode.querySelector('.sort-option.selected').classList.remove('selected');
        option.classList.add('selected');
        option.closest('.sort-type-select').querySelector('.sort-type-select-trigger span').textContent = option.textContent;
    }

    option.addEventListener('click', function () {
        if (!this.classList.contains('selected')) {
            this.parentNode.querySelector('.sort-option.selected').classList.remove('selected');
            this.classList.add('selected');
            this.closest('.sort-type-select').querySelector('.sort-type-select-trigger span').textContent = this.textContent;
        }
        const type = this.attributes['data-value'].value;
        selectedSortType = type;
        localStorage.setItem('sortType', selectedSortType);
        getComments(type);
    })
}

/* upvote and downvote buttons event listeners */

for (const btn of document.querySelectorAll(".upvote-btn")) {
    btn.addEventListener('click', function () {
        console.log('kekkk');
        const commentID = this.parentNode.parentNode.id;
        voteComment(true, commentID);
    })
}

for (const btn of document.querySelectorAll(".downvote-btn")) {
    btn.addEventListener('click', function () {
        const commentID = this.parentNode.parentNode.id;
        voteComment(false, commentID);
    })
}

/* event listener for the delete comment button */

for (const btn of document.querySelectorAll(".delete-btn")) {
    btn.addEventListener('click', function() {
        const commentID = this.parentNode.id;
        deleteComment(commentID);
    })
}