/**
 * Checks if current user is logged in.
 */
function init() {
    fetch('/auth', { method: 'GET' }).then(response => response.json()).then(authResult => {
        if (authResult.isLoggedIn) {
            addLogoutBtn(authResult.logoutUrl);
            localStorage.setItem('currentUserID', authResult.id);
        } else {
            addSignInBtn(authResult.loginUrl);
        }
    });
}

/**
 * Adds Sign In button to the nav bar.
 * @param {String}  loginUrl    URL for login
 */
function addSignInBtn(loginUrl) {
    document.querySelector('nav').insertAdjacentHTML('beforeEnd',
    `<a href="` + loginUrl + `">
        <div class="log-btn">
            <p class="nav-log">Sign in</p>
        </div>
    </a>`);
}

/**
 * Adds logout button to the nav bar.
 * @param {String}  logoutUrl    URL for logout
 */
function addLogoutBtn(logoutUrl) {
    document.querySelector('nav').insertAdjacentHTML('beforeEnd',
    `<a href="` + logoutUrl + `">
        <div class="log-btn">
            <p class="nav-log">Logout</p>
        </div>
    </a>`);
}

init();