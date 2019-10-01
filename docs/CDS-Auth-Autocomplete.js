// ==UserScript==
// @name     CDS Movements Authorisation
// @namespace  http://tampermonkey.net/
// @version   0.2
// @description Authenticates with CDS Declare
// @author    You
// @match     http*://*/auth-login-stub/gg-sign-in?continue=*customs-movements*
// @grant     none
// @updateURL https://raw.githubusercontent.com/hmrc/customs-movements-frontend/master/docs/CDS-Auth-Autocomplete.js
// ==/UserScript==

(function() {
    'use strict';

    document.getElementsByName("redirectionUrl")[0].value = getBaseUrl() + "/customs-movements/start";

    document.getElementById("affinityGroupSelect").selectedIndex = 1;

    document.getElementsByName("enrolment[0].name")[0].value = "HMRC-CUS-ORG";
    document.getElementById("input-0-0-name").value = "EORINumber";
    document.getElementById("input-0-0-value").value = "GB71727550" + (Math.floor(Math.random()*90000) + 10000);

    document.getElementsByName("itmp.dateOfBirth")[0].value = "1993-06-30";


    document.getElementById('global-header').appendChild(createQuickButton())

})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    button.innerHTML = 'Quick Submit';
    button.onclick = () => document.getElementsByClassName('button')[0].click();
    return button;
}

function getBaseUrl() {
    let host = window.location.host;
    if (window.location.hostname === 'localhost') {
        host = 'localhost:6796'
    }
    return window.location.protocol + "//" + host;
}

