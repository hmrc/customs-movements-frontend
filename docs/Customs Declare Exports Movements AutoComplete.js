// ==UserScript==
// @name         Customs Declare Exports Movements AutoComplete
// @namespace    http://tampermonkey.net/
// @version      0.20
// @description  Customs Declare Exports Movements
// @author       You
// @match        http*://*/customs-movements*
// @grant        none
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-movements-frontend/master/docs/Customs%20Declare%20Exports%20Movements%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict';
    document.getElementsByTagName("body")[0].appendChild(createQuickButton());
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start');
    } else {
        button.classList.add('govuk-button');
    }
    button.style.position = "absolute"
    button.style.top = "50px"
    button.innerHTML = 'Quick Submit';
    button.onclick = () => completePage();
    return button;
}

function selectFromAutoPredict(element, selected) {
    let index = typeof selected == "number" ? selected : 0;
    let selects = element.getElementsByTagName('select');
    let inputs = element.getElementsByTagName('input');
    for(let j = 0; j < selects.length; j++){
        let options = selects[j].getElementsByTagName('option');
        let option = options[index];
        if(typeof selected == "string"){
            for(let o = 0; o < options.length; o++) {
                if(options[o].value === selected) {
                    option = options[o];
                }
            }
        }
        option.selected = "selected";
        selects[j].value = option.value;
        inputs[j].value = option.value;
    }
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path);
    return matches && matches.length > 0
}

function completePage() {
    if(currentPageIs("/customs-movements/choice")){
        document.getElementById("choice").checked = true
    }
        if(currentPageIs("/customs-movements/consignment-query")){
        document.getElementById("ucr").value = "GB/123456789100-AB123"
    }

    if(currentPageIs("/customs-movements/specific-date-and-time")){
        document.getElementById("choice").checked = true
    }
    if(currentPageIs("/customs-movements/consignment-references")){
        document.getElementById("reference").checked = true
        document.getElementById('ducrValue').value = '8GB12345' + Math.floor(Math.random() * 8999) + 100 + '-101SHIP1';
    }
    if(currentPageIs("/customs-movements/movement-details")){
        let title = document.title.toLowerCase();
        const validDate = new Date();
        validDate.setDate(validDate.getDate() - 1); // One day before
        if (title.indexOf('departure') != -1) {
            document.getElementById('dateOfDeparture_day').value = validDate.getDate();
            document.getElementById('dateOfDeparture_month').value = validDate.getMonth()+1;
            document.getElementById('dateOfDeparture_year').value = validDate.getFullYear();
            document.getElementById('timeOfDeparture_hour').value = '10';
            document.getElementById('timeOfDeparture_minute').value = '00';
            document.getElementById('timeOfDeparture_ampm').options[2].setAttribute("selected", "selected")
        }
        if (title.indexOf('arrival') != -1) {
            document.getElementById('dateOfArrival_day').value = validDate.getDate();
            document.getElementById('dateOfArrival_month').value = validDate.getMonth()+1;
            document.getElementById('dateOfArrival_year').value = validDate.getFullYear();
            document.getElementById('timeOfArrival_hour').value = '10';
            document.getElementById('timeOfArrival_minute').value = '00';
            document.getElementById('timeOfArrival_ampm').options[2].setAttribute("selected", "selected")
        }
    }
    if(currentPageIs("/customs-movements/location")){
        document.getElementById('code').value = 'GBAUEMAEMAEMA';
    }
    if(currentPageIs("/customs-movements/transport")){
        document.getElementById("modeOfTransport").checked = true;
        document.getElementById('transportId').value = 'TransportReference';
        selectFromAutoPredict(document.getElementById('nationality-container'), "GB");
    }
    if(currentPageIs("/customs-movements/mucr-options")){
        document.getElementById("createOrAdd").checked = true;
        document.getElementById("newMucr").value = "GB/1234-123ABC456DEFIIIII"
    }
    if(currentPageIs("/customs-movements/associate-ucr")){
        document.getElementById("kind").checked = true;
        const now = new Date()
        document.getElementById("ducr").value = `5GB123456789000-${now.valueOf()}IIIII`
    }
    if(currentPageIs("/customs-movements/disassociate-ucr")){
        document.getElementById("kind").checked = true;
        const now = new Date()
        document.getElementById("ducr").value = `5GB123456789000-${now.valueOf()}IIIII`
    }
    if(currentPageIs("customs-movements/shut-mucr")){
        const now = new Date()
        document.getElementById("mucr").value = `GB/ABCDE1234-${now.valueOf()}IIIII`
    }
    document.getElementsByClassName('govuk-button')[0].click()
}