// ==UserScript==
// @name         SDLT Script
// @namespace    http://localhost
// @lastupdate   17/07/2019
// @version      3.0.1
// @description  This script automated logging in to SDLT Install Tampermonkey on Chrome or Greasemonkey on Firefox and add this to your userscripts to automate logging in to SDLT
// @author       https://github.com/gianick
// @match        http://localhost:9949/gg/sign-in
// @match        http://localhost:9949/auth-login-stub/gg-sign-in
// @match        http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9000%2Fsdlt-filing-frontend
// @match        https://www.development.tax.service.gov.uk/auth-login-stub/gg-sign-in
// @match        https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in
// @match        https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in
// @match        https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in#
// @grant        none
// @require      https://code.jquery.com/jquery-latest.min.js
// @updateURL    https://github.com/hmrc/bta-stubs/blob/main/userscripts/bta-login-script.user.js
// ==/UserScript==

// Note: This should match the version on line 5
var scriptVersion = "3.0.1"

var userProfiles = '[' +
    '{ "divider":"Filing Returns" }, ' +
    '{ "description":"No Return ID", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "" }, ' +
    '{ "description":"Prelim Questions Submitted - Skip to ReturnTaskList - ORG", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "123456" }' +
    '{ "description":"No return agent, no vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-no-vendor" }, ' +
    '{ "description":"No vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-vendor" }, ' +
    '{ "description":"Vendor agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Non vendor agent, main vendor is not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "non-vendor-agent-and-main-vendor-not-represented-by-agent" }, ' +
    '{ "description":"No return agent and main vendor not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-main-vendor-not-represented-by-agent" }' +
    '{ "description":"Error case: Vendor agent, main vendor is not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-main-vendor-not-represented-by-agent" }, ' +
    '{ "description":"Error case: No return agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Error case: Non vendor agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "non-vendor-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Error case: Vendor agent, no main vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-no-main-vendor" }' +
    ']';

var submitFormVal = true;
var clickCount = 0;

function addCssToHead(styles) {
  var head = document.getElementsByTagName('head')[0];
  var s = document.createElement('style');
  s.setAttribute('type', 'text/css');
  if (s.styleSheet) { // IE
    s.styleSheet.cssText = styles;
  } else { // other browsers
    s.appendChild(document.createTextNode(styles));
  }
  head.appendChild(s);
}

function toggleYesNo(element) {
  if (element) {
    if (element.innerHTML === 'YES') {
      element.innerHTML = 'NO';
      element.dataset.setting = 'false';
    } else {
      element.innerHTML = 'YES';
      element.dataset.setting = 'true';
    }
  }
}

function createTopBar() {
  var topBar = document.createElement("div");
  topBar.setAttribute('id', 'page-top-bar');

  document.body.insertBefore(topBar, document.body.firstChild);
  document.body.style.marginTop = "16px";

  var topBarStyles = `
    #page-top-bar                         { background:#333; border-bottom:3px solid #fc0; color:#fff; font-size:12px; font-weight:normal; left:0; padding-left:0%; padding-right:0%; padding-bottom:5px; padding-top:5px; position:fixed; text-align:right; top:0; width:100%; z-index:9999999; }
    #page-top-bar a                       { border:1px solid #fff; color:#fff; display:inline-block; font-weight:bold; padding:3px; text-align:center; text-decoration:none; width:30px; }
    #page-top-bar .page-top-bar-content   { margin-right: 1%; position: relative; }
    #page-top-bar a[data-setting="false"] { background-color:#f00;}
    #page-top-bar a[data-setting="true"]  { background-color:#41c14a; }
    #page-top-bar #top-bar-title          { font-size: 28px; font-weight: bold; text-align: center; }
    #page-top-bar #top-bar-options        { font-size: 15px; position: absolute; right: 0; top: 8px; }
    #page-top-bar #top-bar-options a      { font-size: 12px; }
 `;
  addCssToHead(topBarStyles);

  var topBarContent = '<div class="page-top-bar-content"><div id="top-bar-title">SDLT Filing</div>';
  topBarContent += '<div id="top-bar-options"><span>Auto submit form:</span> <a href="#" id="toggle-submit-form" data-setting="true">YES</a>&nbsp;&nbsp;&nbsp;&nbsp;<span>Hide form:</span> <a href="#" id="toggle-page-overlay" data-setting="true">YES</a></div>';
  topBarContent += '</div>';

  topBar.innerHTML = topBarContent;

  var toggleOverlayLink = document.querySelector('#toggle-page-overlay');
  var toggleSubmitFormLink = document.querySelector('#toggle-submit-form');

  if (toggleOverlayLink) {
    toggleOverlayLink.addEventListener("click", function(e) { e.preventDefault(); e.stopPropagation(); toggleOverlay(); }, false);
  }

  if (toggleSubmitFormLink) {
    toggleSubmitFormLink.addEventListener("click", function(e) { e.preventDefault(); e.stopPropagation(); toggleFormSubmit(); }, false);
  }
}

function createOverlay() {
  var pageOverlay = document.createElement("div");
  pageOverlay.style.display = "block";
  pageOverlay.setAttribute('id', 'page-overlay');

  document.body.insertBefore(pageOverlay, document.body.firstChild);

  var overlayStyles = `
    #page-overlay                         { background:#fff; color:#848d95; display:block; font-size:14px; font-weight:300; height:100%; left:0; padding-top:50px; position:fixed; text-align:center; top:0; width:100%; z-index:99999; opacity:0.99; }
    #page-overlay-wrapper                 { margin: 50px auto; max-height: 800px; max-width: 1100px; overflow-y: scroll; position: relative; }
    .user-type-selector                   { border: 1px solid #c0d8e9; display: block; margin: 7px auto; text-align: left; }
    .user-type-selector:hover             { border: 1px solid #2e8aca; }
    .user-type-selector a                 { color: #444; display: block; font-size: 18px; padding: 7px; text-decoration: none; }
    .user-type-selector a:hover           { color: #2e8aca; }
    .user-type-selector a:focus           { color: #444; }
    .user-type-selector.divider           { border: none; border-bottom: 2px solid green; display: block; margin: 20px auto 7px; text-align: left; }
    .user-type-selector.divider a         { cursor: default; }
    .user-type-selector.divider a:hover   { color: #444; }
    .user-type-selector.divider a:focus   { background-color: transparent; color: #444; outline: none; }
    #shortcuts-wrapper                    { margin: 0 auto; max-width: 1100px; position: relative; }
    #shortcuts-wrapper .shortcuts-list    { border-left: 2px solid #c0d8e9; left:-155px; padding:3px 0 3px 10px; position:absolute; text-align:left; top:55px; width:140px; }
    #shortcuts-wrapper .shortcuts-list li { font-size:16px; }
    #shortcuts-wrapper .shortcuts-heading { font-weight:bold; }
    #shortcuts-wrapper .back-to-top       { font-weight:bold; margin-top:20px; }
    #submit-button-wrapper                { margin: 0 auto; max-width: 1100px; position: relative; }
    #script-submit                        { display: none; position: absolute; right: -100px; bottom: 50px; }
    #script-submit:active                 { top: inherit !important; }
    #script-url                           { position:absolute; right:20px; text-align:right; top:70px; }
  `;
  addCssToHead(overlayStyles);

  var userTypesObject = JSON.parse(userProfiles);

  pageOverlay.innerHTML = "<div id='page-overlay-wrapper'></div>";

  var pageOverlayWrapper = document.getElementById("page-overlay-wrapper");

  for (var i = 0, len = userTypesObject.length; i < len; ++i) {
    (function(i) {
      var newLine = "";
      if ( userTypesObject[i].hasOwnProperty('divider') ) {
        newLine = '<div id="' + userTypesObject[i].divider.toLowerCase().replace(/[^A-Z0-9]+/ig, "-").replace(/\-+$/, "") + '" class="user-type-selector divider"><a href="#">' + userTypesObject[i].divider + '</a></div>';
        pageOverlayWrapper.insertAdjacentHTML('beforeend', newLine);
      } else {
        if ( userTypesObject[i].hasOwnProperty('authorityId') ) {
          newLine = '<div class="user-type-selector" id="select-user-' + i + '"><a href="#">' + userTypesObject[i].description + ' | Return ID: ' + userTypesObject[i].returnId + ' | ' + userTypesObject[i].enrolmentKey + ' | ' + userTypesObject[i].identifierName + ' | ' + userTypesObject[i].identifierValue + ' | CredID: ' +  userTypesObject[i].authorityId + '</a></div>';
          pageOverlayWrapper.insertAdjacentHTML('beforeend', newLine);
          document.getElementById('select-user-' + i).addEventListener("click", function() { clickCount += 1; submitForm(userTypesObject[i].description, userTypesObject[i].affinityGroup, userTypesObject[i].enrolmentKey, userTypesObject[i].identifierName, userTypesObject[i].identifierValue, userTypesObject[i].enrolmentStatus, userTypesObject[i].authorityId, submitFormVal, clickCount, userTypesObject[i].returnId); }, false);
        } else {
          newLine = '<div class="user-type-selector" id="select-user-' + i + '"><a href="#">' + userTypesObject[i].description + ' | Return ID: ' + userTypesObject[i].returnId + ' | ' + userTypesObject[i].enrolmentKey + ' | ' + userTypesObject[i].identifierName + ' | ' + userTypesObject[i].identifierValue + '</a></div>';
          pageOverlayWrapper.insertAdjacentHTML('beforeend', newLine);
          document.getElementById('select-user-' + i).addEventListener("click", function() { clickCount += 1; submitForm(userTypesObject[i].description, userTypesObject[i].affinityGroup, userTypesObject[i].enrolmentKey, userTypesObject[i].identifierName, userTypesObject[i].identifierValue, userTypesObject[i].enrolmentStatus, "noAuthorityId", submitFormVal, clickCount, userTypesObject[i].returnId); }, false);
        }
      }
    }(i));
  }

  document.body.insertBefore(pageOverlay, document.body.firstChild);

  pageOverlay.insertAdjacentHTML('beforeend', "<div id='submit-button-wrapper'><input id='script-submit' class='button' value='Submit' type='submit'></div>");

  document.getElementById("script-submit").addEventListener("click", function () {
    $("#submit").click();
  });
}

function showOverlay() {
  document.getElementById("page-overlay").style.display = "block";
}

function hideOverlay() {
  document.getElementById("page-overlay").style.display = "none";
}

function toggleOverlay() {
  if ( document.getElementById("page-overlay") ) {
    var overlay = document.getElementById("page-overlay");
    if (overlay.style.display !== "block") {
      toggleYesNo(document.getElementById("toggle-page-overlay"));
      showOverlay();
    } else {
      toggleYesNo(document.getElementById("toggle-page-overlay"));
      hideOverlay();
    }
  } else {
    toggleYesNo(document.getElementById("toggle-page-overlay"));
    createOverlay();
  }
}

function toggleFormSubmit() {
  if ( submitFormVal == true ) {
    toggleYesNo(document.getElementById("toggle-submit-form"));
    document.getElementById("script-submit").style.display = "block";
    submitFormVal = false;
  } else {
    toggleYesNo(document.getElementById("toggle-submit-form"));
    document.getElementById("script-submit").style.display = "none";
    submitFormVal = true;
  }
}

var enrolmentsArray = [];

function submitForm(description, affinityGroup, enrlKey, idName, idVal, status, authId, submitFormVal, numOfClicks, returnId) {
  var loginForm = document.getElementById('inputForm');
  var redirectUrl = "";
  var rowId = numOfClicks - 1;

  if (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1") {
      if (description == "No Return ID") {
          redirectUrl = "http://localhost:10910/stamp-duty-land-tax-filing";
      } else {
    redirectUrl = "http://localhost:10910/stamp-duty-land-tax-filing/returnTaskList";
      }
  } else {
    redirectUrl = "/stamp-duty-land-tax-filing/returnTaskList";
  }

  // Add returnId to redirect URL if it exists
  if (returnId && returnId !== "") {
    redirectUrl += "?returnId=" + encodeURIComponent(returnId);
  }

  if (enrolmentsArray.indexOf(enrlKey) === -1) {
    enrolmentsArray.push(enrlKey);

    $("input[type='text'][name='redirectionUrl']").val(redirectUrl);

    var enrolments = enrlKey.split('/');

    if (enrolments.length > 1) {

      var identifiers = idName.split('/');
      var idVals = idVal.split('/');
      var statuses = status.split('/');
      var affinities = affinityGroup.split('/');
      var EPAYEcount = 0;

      $("select[name='affinityGroup']").val(affinities[0]);


      if (authId !== "noAuthorityId") {
        $("input[type='text'][name='authorityId']").val(authId);
      } else {
        $("input[type='text'][name='authorityId']").val(""); // empty CredId
      }

      enrolments.forEach(function(enrolment, index) {
        $("input[type='text'][name='enrolment[" + rowId + "].name']").val(enrolments[rowId]);

        if (enrolments[rowId] == "IR-PAYE") {

          EPAYEcount++;

          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].name']").val(identifiers[rowId]);
          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].value']").val(idVals[rowId]);
          $("#add-ident-btn-" + rowId).click();

          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[1].name']").val(identifiers[rowId+EPAYEcount]);
          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[1].value']").val(idVals[rowId+EPAYEcount]);
        }
        else{
          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].name']").val(identifiers[rowId+EPAYEcount]);
          $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].value']").val(idVals[rowId+EPAYEcount]);
        }

        $("select[name='enrolment[" + rowId + "].state']").val(statuses[rowId]);
        rowId++;
        if (rowId >= 3) {
          $("#js-add-enrolment").click();
        }
      });

    } else {
      $("select[name='affinityGroup']").val(affinityGroup);

      $("input[type='text'][name='enrolment[" + rowId + "].name']").val(enrlKey);

      if (authId !== "noAuthorityId") {
        $("input[type='text'][name='authorityId']").val(authId);
      } else {
        $("input[type='text'][name='authorityId']").val(""); // empty CredId
      }

      if (enrlKey == "IR-PAYE") {

        var identifierNameArray = idName.split('/'), idName0 = identifierNameArray[0], idName1 = identifierNameArray[1];
        var identifierValueArray = idVal.split('/'), idVal0 = identifierValueArray[0], idVal1 = identifierValueArray[1];

        $("#add-ident-btn-" + rowId).click();

        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].name']").val(idName0);
        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].value']").val(idVal0);
        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[1].name']").val(idName1);
        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[1].value']").val(idVal1);


      } else {
        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].name']").val(idName);
        $("input[type='text'][name='enrolment[" + rowId + "].taxIdentifier[0].value']").val(idVal);
      }
      $("select[name='enrolment[" + rowId + "].state']").val(status);

      if (rowId >= 3) {
        $("#js-add-enrolment").click();
      }
    }
  } else {
    alert("You cannot add another '" + enrlKey + "' enrolment.");
    console.log("Existing enrolments:", enrolmentsArray);
  }
  if (submitFormVal == true) {
    $("#submit").click();
  }
}

// Wait for jQuery to be loaded
function waitForJQuery(callback) {
  if (typeof jQuery !== 'undefined') {
    callback();
  } else {
    setTimeout(function() {
      waitForJQuery(callback);
    }, 100);
  }
}

waitForJQuery(function() {
  $(document).ready(function() {
    'use strict';

    createOverlay();
    createTopBar();

    var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
    var wrapperHeight = viewPortHeight - 155;
    document.getElementById('page-overlay-wrapper').style.height = wrapperHeight + "px";
  });
});