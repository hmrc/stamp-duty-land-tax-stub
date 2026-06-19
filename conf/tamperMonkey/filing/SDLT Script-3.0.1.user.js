// ==UserScript==
// @name         SDLT Script
// @namespace    http://localhost
// @lastupdate   05/06/2026
// @version      3.5.0
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
var scriptVersion = "3.5.0"

var userProfiles = '[' +
    '{ "divider":"Getting Started" }, ' +
    '{ "description":"No Return ID", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STN001", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "" }, ' +
    '{ "description":"Prelim Questions Submitted - Skip to ReturnTaskList - ORG", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "123456" }, ' +

    '{ "divider":"Vendors" }, ' +
    '{ "description":"No return agent, no vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-no-vendor" }, ' +
    '{ "description":"No vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-vendor" }, ' +
    '{ "description":"5 vendors", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-vendors" }, ' +
    '{ "description":"97 vendors, 1 purchaser", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "97-vendors-1-purchaser" }, ' +
    '{ "description":"99 vendors", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "99-vendors" }, ' +
    '{ "description":"Vendor agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Non vendor agent, main vendor is not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "non-vendor-agent-and-main-vendor-not-represented-by-agent" }, ' +
    '{ "description":"No return agent and main vendor not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-main-vendor-not-represented-by-agent" }, ' +

    '{ "divider":"Vendor Error Cases" }, ' +
    '{ "description":"Error case: Vendor agent, main vendor is not represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-main-vendor-not-represented-by-agent" }, ' +
    '{ "description":"Error case: No return agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-return-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Error case: Non vendor agent, main vendor is represented by agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "non-vendor-agent-and-main-vendor-represented-by-agent" }, ' +
    '{ "description":"Error case: Vendor agent, no main vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "vendor-agent-and-no-main-vendor" }, ' +
    '{ "description":"Error case: Bad request deleting vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "error-removing-vendor" }, ' +
    '{ "description":"Error case: Bad request updating return version", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "error-updating-return-version" },' +
    '{ "description":"Error case: Bad request updating purchaser return agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "error-updating-purchaser-return-agent" }, ' +
    '{ "description":"Error case: Bad request creating purchaser return agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "error-creating-purchaser-return-agent" }, ' +

    '{ "divider":"Purchasers" }, ' +
    '{ "description":"Refresh Created Return", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "firstJson" },' +
    '{ "description":"Incomplete Purchaser", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "incomplete-purchaser" },' +
    '{ "description":"Full Purchaser with address line 1", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser" },' +
    '{ "description":"Full Purchaser with return agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent" },' +
    '{ "description":"One Purchaser of type Company", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "one-purchaser-company" },' +
    '{ "description":"One Purchaser of type Individual", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "one-purchaser-individual" },' +
    '{ "description":"2 Purchasers, Main Purchaser is type Company", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "2-purchasers-company" },' +
    '{ "description":"2 Purchasers, Main Purchaser is type Individual", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "2-purchasers-individual" },' +
    '{ "description":"5 Purchasers, Main Purchaser is type Company", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-purchasers-company" },' +
    '{ "description":"5 Purchasers, Main Purchaser is type Individual", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-purchasers-individual" },' +
    '{ "description":"Error case: Bad request updating return info", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "error-updating-return-info" },' +
    '{ "description":"97 purchasers, 1 vendor", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "97-purchasers-1-vendor" }, ' +
    '{ "description":"99 purchasers", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "99-purchasers" }, ' +
    '{ "description":"No Purchaser", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-purchaser" },' +
    '{ "description":"Purchaser Agent", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "purchaser-agent" },' +
    '{ "description":"Purchaser No Agents", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "purchaser-no-agents" }, ' +
    '{ "description":"50 purchasers and 50 vendors", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "50-purchasers-50-vendors" }, ' +

    '{ "divider":"Land Transactions" }, ' +
    '{ "description":"purchaser with land transaction with uk authcodes", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-uk-authcodes" }, ' +
    '{ "description":"purchaser with land transaction with scott authcodes", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-with-scot-authcodes" }, ' +
    '{ "description":"purchaser with land transaction with empty eff and contract dates", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-emptydates" }, ' +
    '{ "description":"purchaser with land transaction with uk authcode with scot postcode", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-ukauthcode-with-scot-postcode" }, ' +
    '{ "description":"purchaser with land transaction with welsh authcodes", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-welsh-authcodes" }, ' +
    '{ "description":"purchaser with land transaction 8998", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-8998" }, ' +
    '{ "description":"purchaser with land transaction 8999", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-8999" }, ' +
    '{ "description":"purchaser with land transaction having no contract date", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-no-contract-date" }, ' +
    '{ "description":"purchaser with land transaction having no effective date", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-purchaser-with-agent-land-transaction-no-effective-date" }, ' +

    '{ "divider":"Full Land Scenarios" }, ' +
    '{ "description":"individual purchaser with agents full land non-residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "individual-purchaser-with-agents-full-land-non-residential" }, ' +
    '{ "description":"individual purchaser with agents full land residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "individual-purchaser-with-agents-full-land-residential" }, ' +
    '{ "description":"individual purchaser with agents full land additional residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "individual-purchaser-with-agents-full-land-additional-residential" }, ' +
    '{ "description":"individual purchaser with agents full land residential - no residency", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "individual-purchaser-with-agents-full-land-residential-no-residency" }, ' +
    '{ "description":"company purchaser with agents full land residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "company-purchaser-with-agents-full-land-residential" }, ' +
    '{ "description":"company purchaser with agents full land additional residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "company-purchaser-with-agents-full-land-additional-residential" }, ' +
    '{ "description":"company purchaser with agents full land residential - no residency", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "company-purchaser-with-agents-full-land-residential-no-residency" }, ' +

    '{ "divider":"Lands" }, ' +
    '{ "description":"No land", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "no-land" }, ' +
    '{ "description":"1 land with mixed property type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "1-land-mixed-property-type" }, ' +
    '{ "description":"1 land with residential property type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "1-land-residential-property-type" }, ' +
    '{ "description":"98 lands", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "98-lands" }, ' +
    '{ "description":"99 lands", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "99-lands" }, ' +
    '{ "description":"5 lands", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands" }, ' +

    '{ "divider":"Prelim Transactions" }, ' +
    '{ "description":"Prelim Only", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelim-only" }, ' +
    '{ "description":"Prelim Transaction Type F", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionF" }, ' +
    '{ "description":"Prelim Transaction Type L - Mixed Property Type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionL-property-type-mixed" }, ' +
    '{ "description":"Prelim Transaction Type L - Residential Property Type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionL-property-type-residential" }, ' +
    '{ "description":"Prelim Transaction Type L - Non Residential Property Type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionL-property-type-nonResidential" }, ' +
    '{ "description":"Prelim Transaction Type L - Additional Property Type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionL-property-type-additional" }, ' +
    '{ "description":"Prelim Transaction Type L - No Land", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionL-no-land" }, ' +
    '{ "description":"Prelim Transaction Type A", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "prelimTransactionA" }, ' +

    '{ "divider":"Freehold" }, ' +
    '{ "description":"freehold-self-assessed", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed" }, ' +
    '{ "description":"freehold-self-assessed-isLinked", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-isLinked" }, ' +
    '{ "description":"freehold-self-assessed-zeroPenalty", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-zeroPenalty" }, ' +
    '{ "description":"freehold-self-assessed-partialRelief", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-partialRelief" }, ' +
    '{ "description":"freehold-self-assessed-OT", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-OT" }, ' +
    '{ "description":"freehold-self-assessed-multipleDwellings", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-multipleDwellings" }, ' +
    '{ "description":"freehold-self-assessed-effectiveDateBeforeMar2012", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-effectiveDateBeforeMar2012" }, ' +
    '{ "description":"freehold-self-assessed-collectiveEnfranchisement", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-self-assessed-collectiveEnfranchisement" }, ' +
    '{ "description":"freehold-multiple-self-assessed-reasons", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-multiple-self-assessed-reasons" }, ' +
    '{ "description":"freehold-tax-calculated-slice", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-tax-calculated-slice" }, ' +
    '{ "description":"freehold-tax-calculated-slab", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-tax-calculated-slab" }, ' +
    '{ "description":"freehold-tax-calculated-zero", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-tax-calculated-zero" }, ' +
    '{ "description":"freehold-tax-calculated-zeroPenalty", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-tax-calculated-zeroPenalty" }, ' +
    '{ "description":"freehold-tax-already-calculated", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "freehold-tax-already-calculated" }, ' +
    '{ "description":"residential, no UK residency ans after 1 Apr 21 - F (sdltc validation fails)", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "residential-no-uk-residency-ans-after-1-Apr-21-F" }, ' +
    '{ "description":"residential, no UK residency ans after 1 Apr 21 - L (sdltc validation fails)", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "residential-no-uk-residency-ans-after-1-Apr-21-L" }, ' +

    '{ "divider":"Leasehold" }, ' +
    '{ "description":"leasehold-self-assessed", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed" }, ' +
    '{ "description":"leasehold-self-assessed-isLinked", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-isLinked" }, ' +
    '{ "description":"leasehold-self-assessed-zeroPenalty", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-zeroPenalty"}, ' +
    '{ "description":"leasehold-self-assessed-partialRelief", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-partialRelief" }, ' +
    '{ "description":"leasehold-self-assessed-OT", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-OT" }, ' +
    '{ "description":"leasehold-self-assessed-multipleDwellings", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-multipleDwellings" }, ' +
    '{ "description":"leasehold-self-assessed-effectiveDateBeforeMar2012", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-effectiveDateBeforeMar2012" }, ' +
    '{ "description":"leasehold-self-assessed-collectiveEnfranchisement", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-collectiveEnfranchisement" }, ' +
    '{ "description":"leasehold-self-assessed-predatesCalc1", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-predatesCalc1" }, ' +
    '{ "description":"leasehold-self-assessed-predatesCalc2", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-self-assessed-predatesCalc2" }, ' +
    '{ "description":"leasehold-multiple-self-assessed-reasons", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-multiple-self-assessed-reasons" }, ' +
    '{ "description":"leasehold-tax-calculated", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-tax-calculated" }, ' +
    '{ "description":"leasehold-tax-calculated-zeroPenalty", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-tax-calculated-zeroPenalty" }, ' +
    '{ "description":"leasehold-tax-calculated-withReliefReason", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "leasehold-tax-calculated-withReliefReason" }, ' +

    '{ "divider":"UK Residency" }, ' +
    '{ "description":"Incomplete main purchaser (multiple purchasers)", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "incomplete-main-purchaser-multiple-purchasers" }, ' +
    '{ "description":"Uk Residency 1 Residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands-One-Residential" }, ' +
    '{ "description":"Uk Residency 1 Additional", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands-One-Additional" }, ' +
    '{ "description":"Uk Residency before the effective date needed", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands-One-Residential-EffectiveDateNotValid" }, ' +
    '{ "description":"Uk Residency No property types", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands-None-Residential" }, ' +
    '{ "description":"Uk Residency 1 Residential and Purchaser Company", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "5-lands-One-Residential-Company" }, ' +
    '{ "description":"land with invalid interestTransferredOption", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "1-land-with-Invalid-InterestTransferredOption" }, ' +

    '{ "divider":"Full Transactions" }, ' +
    '{ "description":"Full transaction, Grant of lease, 08 PartExchange", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-transaction-grantoflease-08partexchange" }, ' +
    '{ "description":"Full transaction, Grant of lease, 20 CharitiesRelief", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-transaction-grantoflease-20charitiesrelief" }, ' +
    '{ "description":"Full transaction, Conveyance/Transfer, mixed property type", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-transaction-conveyance" }, ' +
    '{ "description":"LeaseStartDate GreaterThan EndDates Check", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "LeaseStartDate-GreaterThan-EndDate-Check" }, ' +

    '{ "divider":"Full Lease" }, ' +
    '{ "description":"Full lease, transaction type A", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-lease-transaction-type-a" }, ' +
    '{ "description":"Full lease, transaction type L", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-lease-transaction-type-l" }, ' +

    '{ "divider":"Crossflow Errors" }, ' +
    '{ "description":"Full lease, transaction type F", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-lease-transaction-type-f" }, ' +
    '{ "description":"f17-welsh6996-before-wales-act", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-welsh6996-before-wales-act" }, ' +
    '{ "description":"f17-welsh6996-before-wales-act-multi", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-welsh6996-before-wales-act-multi" }, ' +
    '{ "description":"f23-32-fail-property-not-residential", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-32-fail-property-not-residential" }, ' +
    '{ "description":"f23-33-fail-property-not-allowed", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-33-fail-property-not-allowed" }, ' +
    '{ "description":"f23-34-fail-date-before-2013", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-34-fail-date-before-2013" }, ' +
    '{ "description":"f23-35-fail-date-before-2013", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-35-fail-date-before-2013" }, ' +
    '{ "description":"f23-36-fail-after-window", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-36-fail-after-window" }, ' +
    '{ "description":"f23-36-fail-before-window", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-36-fail-before-window" }, ' +
    '{ "description":"f23-36-pass-inside-window", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-36-pass-inside-window" }, ' +
    '{ "description":"f23-37-fail-after-window", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-37-fail-after-window" }, ' +
    '{ "description":"f23-37-fail-before-window", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-37-fail-before-window" }, ' +
    '{ "description":"f23-38-fail-date-before-2025-03-19", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f23-38-fail-date-before-2025-03-19" }, ' +
    '{ "description":"f24-additional-res", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f24-additional-res" }, ' +
    '{ "description":"f25-fail-contract-date-after-cutoff", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f25-fail-contract-date-after-cutoff" }, ' +
    '{ "description":"f25-fail-contract-date-null", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f25-fail-contract-date-null" }, ' +
    '{ "description":"f25-fail-contract-date-on-cutoff", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f25-fail-contract-date-on-cutoff" }, ' +
    '{ "description":"f25-fail-effective-date-after-cutoff", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f25-fail-effective-date-after-cutoff" }, ' +
    '{ "description":"f25-fail-effective-date-on-cutoff", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f25-fail-effective-date-on-cutoff" }, ' +
    '{ "description":"f28-cap500k-original-window-fail", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f28-cap500k-original-window-fail" }, ' +
    '{ "description":"f28-cap500k-post-2025-fail", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f28-cap500k-post-2025-fail" }, ' +
    '{ "description":"f28-cap625k-middle-window-fail", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f28-cap625k-middle-window-fail" },' +
    '{ "description":"f17-6996-missingeffdate", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6996-missingeffdate" }, ' +
    '{ "description":"f17-6996-preact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6996-preact" }, ' +
    '{ "description":"f17-6997-preact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6997-preact" }, ' +
    '{ "description":"f17-6998-bothdates", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6998-bothdates" }, ' +
    '{ "description":"f17-6998-contractdate-postact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6998-contractdate-postact" }, ' +
    '{ "description":"f17-6998-effdate-preact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6998-effdate-preact" }, ' +
    '{ "description":"f17-6999-bothdates", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6999-bothdates" }, ' +
    '{ "description":"f17-6999-contractdate-afterwalesact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6999-contractdate-afterwalesact" }, ' +
    '{ "description":"f17-6999-effdate-preact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-6999-effdate-preact" }, ' +
    '{ "description":"f17-regularwelsh-postact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f17-regularwelsh-postact" }, ' +
    '{ "description":"f18-dummy8998-contractpostcr223", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-dummy8998-contractpostcr223" }, ' +
    '{ "description":"f18-dummy8998-precr223", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-dummy8998-precr223" }, ' +
    '{ "description":"f18-dummy8999-contractafterscotact", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-dummy8999-contractafterscotact" }, ' +
    '{ "description":"f18-dummy8999-precr223", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-dummy8999-precr223" }, ' +
    '{ "description":"f18-scottishcode-postcr223", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-scottishcode-postcr223" }, ' +
    '{ "description":"f18-scottishpostcode-edinburgh", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-scottishpostcode-edinburgh" }, ' +
    '{ "description":"f18-scottishpostcode-glasgow", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "f18-scottishpostcode-glasgow" },' +
    '{ "description":"F30-Cf-5a", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "F30-Cf-5a" },' +
    '{ "description":"F30-Cf-5b", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "F30-Cf-5b" },' +
    '{ "description":"F30-Cf-5c", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "F30-Cf-5c" },' +
    '{ "description":"F30-CF6", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "F30-CF6" },' +
    '{ "description":"F30-CF6-with-cf5a", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "F30-CF6-with-cf5a" },' +
    '{ "description":"all-f17-and-f18-predate", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "all-f17-and-f18-predate" },' +
    '{ "description":"all-f17-and-f18-postdate", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "all-f17-and-f18-postdate" }, ' +
    '{ "description":"Full lease, transaction type F", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "full-lease-transaction-type-f" }, ' +

    '{ "divider":"Tasklist" }, ' +
    '{ "description":"All sections incomplete", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "all-sections-incomplete" }, ' +
    '{ "description":"All sections complete", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "all-sections-complete" }, ' +
    '{ "description":"Only mandatory sections complete", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "only-mandatory-sections-complete" }, ' +
    '{ "description":"Only mandatory sections incomplete", "enrolmentKey":"IR-SDLT-ORG", "identifierName":"STORN", "identifierValue":"STORN123456", "enrolmentStatus":"Activated", "affinityGroup":"Organisation", "returnId": "only-mandatory-sections-incomplete" } ' +
    ']';


// Category colors for visual distinction
var sectionColors = {
  'getting-started':    '#3b82f6',
  'vendors':            '#8b5cf6',
  'vendor-error-cases': '#ef4444',
  'purchasers':         '#10b981',
  'land-transactions':  '#f59e0b',
  'full-land-scenarios':'#14b8a6',
  'lands':              '#06b6d4',
  'prelim-transactions':'#6366f1',
  'freehold':           '#ec4899',
  'leasehold':          '#f97316',
  'uk-residency':       '#84cc16',
  'full-transactions':  '#a855f7',
  'crossflow-errors':  '#6d0202',
    'tasklist':         '#ffeb00',
};

var submitFormVal = true;
var clickCount = 0;

function slugify(text) {
  return text.toLowerCase().replace(/[^A-Z0-9]+/ig, "-").replace(/\-+$/, "");
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, function(c) {
    return { '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c];
  });
}

function addCssToHead(styles) {
  var head = document.getElementsByTagName('head')[0];
  var s = document.createElement('style');
  s.setAttribute('type', 'text/css');
  if (s.styleSheet) {
    s.styleSheet.cssText = styles;
  } else {
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
    #page-top-bar { background: #0f172a; border-bottom: 1px solid #1e293b; color: #f1f5f9; font-size: 13px; font-weight: 400; left: 0; padding: 10px 24px; position: fixed; top: 0; width: 100%; z-index: 9999999; font-family: -apple-system, BlinkMacSystemFont, "Inter", "Segoe UI", Roboto, sans-serif; box-sizing: border-box; }
    #page-top-bar .page-top-bar-content { display: flex; align-items: center; justify-content: space-between; max-width: 100%; }
    #page-top-bar #top-bar-title { font-size: 15px; font-weight: 600; letter-spacing: -0.01em; display: flex; align-items: center; gap: 10px; }
    #page-top-bar #top-bar-title::before { content: ''; width: 8px; height: 8px; background: #10b981; border-radius: 50%; display: inline-block; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); }
    #page-top-bar #top-bar-options { display: flex; align-items: center; gap: 16px; font-size: 12px; color: #94a3b8; }
    #page-top-bar #top-bar-options span { font-weight: 500; }
    #page-top-bar a { border-radius: 5px; color: #fff; display: inline-block; font-weight: 600; padding: 4px 10px; text-align: center; text-decoration: none; font-size: 11px; letter-spacing: 0.5px; transition: all 0.15s; min-width: 36px; }
    #page-top-bar a[data-setting="false"] { background-color: #dc2626; }
    #page-top-bar a[data-setting="false"]:hover { background-color: #b91c1c; }
    #page-top-bar a[data-setting="true"] { background-color: #16a34a; }
    #page-top-bar a[data-setting="true"]:hover { background-color: #15803d; }
  `;
  addCssToHead(topBarStyles);

  var topBarContent = '<div class="page-top-bar-content">';
  topBarContent += '<div id="top-bar-title">SDLT Filing — Auth Stub</div>';
  topBarContent += '<div id="top-bar-options">';
  topBarContent += '<div><span>Auto submit</span> <a href="#" id="toggle-submit-form" data-setting="true">YES</a></div>';
  topBarContent += '<div><span>Hide form</span> <a href="#" id="toggle-page-overlay" data-setting="true">YES</a></div>';
  topBarContent += '</div></div>';
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
  pageOverlay.style.display = "flex";
  pageOverlay.setAttribute('id', 'page-overlay');
  document.body.insertBefore(pageOverlay, document.body.firstChild);

  var overlayStyles = `
    #page-overlay { background: #f8fafc; color: #0f172a; font-family: -apple-system, BlinkMacSystemFont, "Inter", "Segoe UI", Roboto, sans-serif; font-size: 14px; font-weight: 400; position: fixed; top: 42px; left: 0; right: 0; bottom: 0; z-index: 99999; display: flex; flex-direction: column; -webkit-font-smoothing: antialiased; text-align: left; }

    /* Header */
    #app-header { background: #fff; border-bottom: 1px solid #e2e8f0; padding: 24px 40px 20px; flex-shrink: 0; }
    #app-header-inner { max-width: 1400px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; gap: 24px; }
    #app-title-block h1 { font-size: 22px; font-weight: 700; letter-spacing: -0.02em; color: #0f172a; margin: 0 0 2px; line-height: 1.2; }
    #app-title-block .subtitle { font-size: 13px; color: #64748b; margin: 0; }
    #app-header-stats { display: flex; gap: 20px; align-items: center; }
    .stat-item { text-align: right; }
    .stat-item .stat-value { font-size: 20px; font-weight: 700; color: #0f172a; line-height: 1; letter-spacing: -0.02em; }
    .stat-item .stat-label { font-size: 11px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 600; margin-top: 4px; }
    #version-badge { font-size: 11px; color: #64748b; background: #f1f5f9; padding: 4px 10px; border-radius: 999px; font-weight: 500; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }

    /* Help button */
    #help-button { display: inline-flex; align-items: center; gap: 7px; padding: 8px 14px; background: #fff; border: 1px solid #cbd5e1; border-radius: 8px; color: #475569; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.12s; font-family: inherit; text-decoration: none; }
    #help-button:hover { border-color: #3b82f6; color: #1e40af; background: #eff6ff; box-shadow: 0 2px 4px rgba(59, 130, 246, 0.08); }
    #help-button svg { flex-shrink: 0; }

    /* Main layout */
    #app-body { flex: 1; min-height: 0; display: flex; max-width: 1400px; width: 100%; margin: 0 auto; }

    /* Sidebar */
    #shortcuts-wrapper { width: 220px; flex-shrink: 0; padding: 24px 16px 24px 40px; overflow-y: auto; border-right: 1px solid #e2e8f0; background: #fff; box-sizing: border-box; }
    #shortcuts-wrapper .shortcuts-heading { font-weight: 600; font-size: 11px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 12px; padding: 0 10px; }
    #shortcuts-wrapper ul { list-style: none; padding: 0; margin: 0; }
    #shortcuts-wrapper ul li { margin: 1px 0; }
    #shortcuts-wrapper ul li a { display: flex; align-items: center; gap: 10px; padding: 7px 10px; color: #475569; text-decoration: none; font-size: 13px; border-radius: 6px; transition: all 0.12s; font-weight: 500; position: relative; cursor: pointer; }
    #shortcuts-wrapper ul li a:hover { background: #f8fafc; color: #0f172a; }
    #shortcuts-wrapper ul li a.active { background: #eff6ff; color: #1e40af; font-weight: 600; }
    #shortcuts-wrapper ul li a.active::before { content: ''; position: absolute; left: -8px; top: 50%; transform: translateY(-50%); width: 3px; height: 18px; background: #3b82f6; border-radius: 0 3px 3px 0; }
    #shortcuts-wrapper ul li a .sb-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    #shortcuts-wrapper ul li a .sb-label { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    #shortcuts-wrapper ul li a .sb-count { color: #94a3b8; font-size: 11px; font-weight: 500; font-variant-numeric: tabular-nums; }
    #shortcuts-wrapper ul li a.active .sb-count { color: #3b82f6; }
    #shortcuts-wrapper .back-to-top { font-weight: 500; margin-top: 16px; padding-top: 14px; border-top: 1px solid #f1f5f9; font-size: 12px; padding-left: 10px; padding-right: 10px; }
    #shortcuts-wrapper .back-to-top a { color: #3b82f6; text-decoration: none; display: flex; align-items: center; gap: 6px; cursor: pointer; }
    #shortcuts-wrapper .back-to-top a:hover { color: #1e40af; }

    /* Scroll area */
    #page-overlay-wrapper { flex: 1; min-width: 0; min-height: 0; overflow-y: auto; padding: 24px 40px 80px; box-sizing: border-box; }
    #page-overlay-wrapper::-webkit-scrollbar { width: 10px; }
    #page-overlay-wrapper::-webkit-scrollbar-track { background: transparent; }
    #page-overlay-wrapper::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 5px; border: 2px solid #f8fafc; }
    #page-overlay-wrapper::-webkit-scrollbar-thumb:hover { background: #94a3b8; }

    /* Cards */
    .user-type-selector { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; display: block; margin: 6px 0; transition: all 0.12s ease; position: relative; }
    .user-type-selector:hover { border-color: #cbd5e1; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06), 0 1px 3px rgba(15, 23, 42, 0.04); transform: translateY(-1px); }
    .user-type-selector a { color: #0f172a; display: block; padding: 12px 16px; text-decoration: none; }
    .user-type-selector .row-title { font-size: 14px; font-weight: 600; color: #0f172a; display: block; margin-bottom: 6px; letter-spacing: -0.005em; }
    .user-type-selector:hover .row-title { color: #1e40af; }
    .user-type-selector .row-meta-line { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    .user-type-selector .row-returnid { display: inline-flex; align-items: center; background: #f1f5f9; color: #334155; padding: 3px 9px; border-radius: 5px; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 11.5px; font-weight: 500; }
    .user-type-selector .row-returnid.empty { background: #fef3c7; color: #92400e; font-style: italic; font-family: inherit; }
    .user-type-selector .row-tech { color: #94a3b8; font-size: 11px; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.01em; }

    .user-type-selector[data-section] { border-left: 3px solid transparent; }

    .user-type-selector.error-case { border-left: 3px solid #dc2626 !important; background: #fef2f2; }
    .user-type-selector.error-case .row-title { color: #991b1b; }
    .user-type-selector.error-case .row-title::before { content: '⚠'; margin-right: 6px; color: #dc2626; font-weight: 700; }

    /* Section header */
    .section-header { display: flex; align-items: center; gap: 10px; margin: 28px 0 8px; padding: 0 4px; scroll-margin-top: 16px; }
    .section-header:first-child { margin-top: 0; }
    .section-header .section-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
    .section-header .section-title { font-size: 12px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.08em; }
    .section-header .section-count { font-size: 11px; color: #94a3b8; font-weight: 500; font-variant-numeric: tabular-nums; background: #f1f5f9; padding: 2px 8px; border-radius: 999px; }
    .section-header .section-line { flex: 1; height: 1px; background: linear-gradient(to right, #e2e8f0, transparent); }

    /* Submit button */
    #submit-button-wrapper { position: fixed; right: 24px; bottom: 24px; z-index: 200; }
    #script-submit { display: none; padding: 11px 22px; background: #3b82f6; color: #fff; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; font-family: inherit; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); transition: all 0.12s; }
    #script-submit:hover { background: #2563eb; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4); }

    /* ====== HELP MODAL ====== */
    #help-modal-backdrop { display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.55); backdrop-filter: blur(4px); -webkit-backdrop-filter: blur(4px); z-index: 9999998; align-items: center; justify-content: center; padding: 40px 20px; animation: helpFadeIn 0.15s ease-out; }
    #help-modal-backdrop.open { display: flex; }
    @keyframes helpFadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes helpSlideUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
    #help-modal { background: #fff; border-radius: 12px; max-width: 760px; width: 100%; max-height: calc(100vh - 80px); overflow: hidden; display: flex; flex-direction: column; box-shadow: 0 20px 50px rgba(15, 23, 42, 0.3), 0 0 0 1px rgba(15, 23, 42, 0.05); animation: helpSlideUp 0.18s ease-out; font-family: -apple-system, BlinkMacSystemFont, "Inter", "Segoe UI", Roboto, sans-serif; }
    #help-modal-header { padding: 22px 28px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; flex-shrink: 0; }
    #help-modal-header h2 { margin: 0 0 2px; font-size: 18px; font-weight: 700; color: #0f172a; letter-spacing: -0.015em; }
    #help-modal-header .help-subtitle { margin: 0; font-size: 13px; color: #64748b; }
    #help-modal-close { background: transparent; border: none; cursor: pointer; padding: 6px; border-radius: 6px; color: #64748b; transition: all 0.12s; display: flex; align-items: center; justify-content: center; }
    #help-modal-close:hover { background: #f1f5f9; color: #0f172a; }
    #help-modal-body { padding: 24px 28px 28px; overflow-y: auto; flex: 1; }
    #help-modal-body::-webkit-scrollbar { width: 8px; }
    #help-modal-body::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
    .help-step { margin-bottom: 24px; }
    .help-step:last-child { margin-bottom: 0; }
    .help-step-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
    .help-step-num { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; background: #3b82f6; color: #fff; border-radius: 50%; font-size: 12px; font-weight: 700; flex-shrink: 0; }
    .help-step-title { font-size: 15px; font-weight: 600; color: #0f172a; letter-spacing: -0.01em; }
    .help-step-desc { color: #475569; font-size: 13px; line-height: 1.5; margin: 0 0 10px 34px; }
    .help-step-desc code { background: #f1f5f9; color: #1e40af; padding: 1px 6px; border-radius: 4px; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 12px; }
    .help-code-block { position: relative; margin-left: 34px; background: #0f172a; border-radius: 8px; overflow: hidden; }
    .help-code-block pre { margin: 0; padding: 14px 16px; padding-right: 80px; color: #e2e8f0; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 12.5px; line-height: 1.55; overflow-x: auto; white-space: pre; }
    .help-code-block pre::-webkit-scrollbar { height: 6px; }
    .help-code-block pre::-webkit-scrollbar-thumb { background: #334155; border-radius: 3px; }
    .help-copy-btn { position: absolute; top: 8px; right: 8px; background: #1e293b; border: 1px solid #334155; color: #cbd5e1; padding: 4px 10px; border-radius: 5px; font-size: 11px; font-weight: 600; cursor: pointer; transition: all 0.12s; font-family: inherit; }
    .help-copy-btn:hover { background: #334155; color: #fff; border-color: #475569; }
    .help-copy-btn.copied { background: #16a34a; border-color: #16a34a; color: #fff; }
    .help-tip { background: #eff6ff; border-left: 3px solid #3b82f6; padding: 12px 14px; border-radius: 6px; margin-top: 16px; font-size: 13px; color: #1e40af; line-height: 1.5; }
    .help-tip strong { color: #1e3a8a; }

    /* Syntax-ish highlighting via CSS classes */
    .hc-key { color: #93c5fd; }
    .hc-str { color: #86efac; }
    .hc-comment { color: #64748b; font-style: italic; }
    .hc-punct { color: #cbd5e1; }
    .hc-var { color: #fcd34d; }

    body.overlay-open { overflow: hidden; }

    @media (max-width: 900px) {
      #shortcuts-wrapper { display: none; }
      #app-header { padding: 20px; }
      #page-overlay-wrapper { padding: 20px; }
      #app-header-stats { gap: 12px; }
      .stat-item { display: none; }
    }
  `;
  addCssToHead(overlayStyles);

  var userTypesObject = JSON.parse(userProfiles);

  var sectionCounts = {};
  var currentSection = null;
  var totalProfiles = 0;
  for (var k = 0; k < userTypesObject.length; k++) {
    if (userTypesObject[k].hasOwnProperty('divider')) {
      currentSection = slugify(userTypesObject[k].divider);
      sectionCounts[currentSection] = 0;
    } else if (currentSection) {
      sectionCounts[currentSection]++;
      totalProfiles++;
    }
  }

  pageOverlay.innerHTML = `
    <div id='app-header'>
      <div id='app-header-inner'>
        <div id='app-title-block'>
          <h1>SDLT Filing Test Profiles</h1>
          <p class='subtitle'>Select a profile to authenticate and proceed to the filing journey</p>
        </div>
        <div id='app-header-stats'>
          <div class='stat-item'>
            <div class='stat-value'>${totalProfiles}</div>
            <div class='stat-label'>Profiles</div>
          </div>
          <div class='stat-item'>
            <div class='stat-value'>${Object.keys(sectionCounts).length}</div>
            <div class='stat-label'>Categories</div>
          </div>
          <button id='help-button' type='button'>
            <svg width='15' height='15' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2.2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='10'></circle><path d='M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3'></path><line x1='12' y1='17' x2='12.01' y2='17'></line></svg>
            How to add a profile
          </button>
          <div id='version-badge'>v${scriptVersion}</div>
        </div>
      </div>
    </div>
    <div id='app-body'>
      <nav id='shortcuts-wrapper'>
        <div class='shortcuts-heading'>Categories</div>
        <ul id='sidebar-list'></ul>
        <div class='back-to-top'><a id='back-to-top-link'>↑ Back to top</a></div>
      </nav>
      <main id='page-overlay-wrapper'>
        <div id='profile-list'></div>
      </main>
    </div>
    <div id='submit-button-wrapper'><button id='script-submit'>Submit</button></div>
  `;

  // Help modal markup (appended to body so it sits above everything)
  var helpModalHtml = `
    <div id='help-modal-backdrop'>
      <div id='help-modal' role='dialog' aria-labelledby='help-modal-title'>
        <div id='help-modal-header'>
          <div>
            <h2 id='help-modal-title'>How to add a new profile or category</h2>
            <p class='help-subtitle'>Edit this userscript and add entries to <code style="background:#f1f5f9;padding:1px 6px;border-radius:4px;font-family:ui-monospace,monospace;font-size:12px;color:#1e40af">userProfiles</code></p>
          </div>
          <button id='help-modal-close' type='button' aria-label='Close'>
            <svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><line x1='18' y1='6' x2='6' y2='18'></line><line x1='6' y1='6' x2='18' y2='18'></line></svg>
          </button>
        </div>
        <div id='help-modal-body'>

          <div class='help-step'>
            <div class='help-step-header'>
              <span class='help-step-num'>1</span>
              <span class='help-step-title'>Add a new category (divider)</span>
            </div>
            <p class='help-step-desc'>Dividers create section headers in the main list and entries in the sidebar. Drop one in <code>userProfiles</code> before the profiles you want to group.</p>
            <div class='help-code-block'>
              <pre data-copy='{ &quot;divider&quot;:&quot;My New Section&quot; }, +'><span class="hc-punct">'</span><span class="hc-punct">{ </span><span class="hc-key">"divider"</span><span class="hc-punct">:</span><span class="hc-str">"My New Section"</span><span class="hc-punct"> }, '</span> <span class="hc-punct">+</span></pre>
              <button class='help-copy-btn' data-copy-target='prev'>Copy</button>
            </div>
          </div>

          <div class='help-step'>
            <div class='help-step-header'>
              <span class='help-step-num'>2</span>
              <span class='help-step-title'>Add a profile entry</span>
            </div>
            <p class='help-step-desc'>Each profile is a JSON object. Place it under the divider it belongs to. The <code>returnId</code> drives the redirect to the filing journey.</p>
            <div class='help-code-block'>
              <pre data-copy='{ &quot;description&quot;:&quot;My new profile&quot;, &quot;enrolmentKey&quot;:&quot;IR-SDLT-ORG&quot;, &quot;identifierName&quot;:&quot;STORN&quot;, &quot;identifierValue&quot;:&quot;STORN123456&quot;, &quot;enrolmentStatus&quot;:&quot;Activated&quot;, &quot;affinityGroup&quot;:&quot;Organisation&quot;, &quot;returnId&quot;: &quot;my-return-id&quot; }, +'><span class="hc-punct">'</span><span class="hc-punct">{ </span><span class="hc-key">"description"</span><span class="hc-punct">:</span><span class="hc-str">"My new profile"</span><span class="hc-punct">, </span><span class="hc-key">"enrolmentKey"</span><span class="hc-punct">:</span><span class="hc-str">"IR-SDLT-ORG"</span><span class="hc-punct">, </span>
<span class="hc-punct">  </span><span class="hc-key">"identifierName"</span><span class="hc-punct">:</span><span class="hc-str">"STORN"</span><span class="hc-punct">, </span><span class="hc-key">"identifierValue"</span><span class="hc-punct">:</span><span class="hc-str">"STORN123456"</span><span class="hc-punct">, </span>
<span class="hc-punct">  </span><span class="hc-key">"enrolmentStatus"</span><span class="hc-punct">:</span><span class="hc-str">"Activated"</span><span class="hc-punct">, </span><span class="hc-key">"affinityGroup"</span><span class="hc-punct">:</span><span class="hc-str">"Organisation"</span><span class="hc-punct">, </span>
<span class="hc-punct">  </span><span class="hc-key">"returnId"</span><span class="hc-punct">: </span><span class="hc-str">"my-return-id"</span><span class="hc-punct"> }, '</span> <span class="hc-punct">+</span></pre>
              <button class='help-copy-btn' data-copy-target='prev'>Copy</button>
            </div>
          </div>

          <div class='help-step'>
            <div class='help-step-header'>
              <span class='help-step-num'>3</span>
              <span class='help-step-title'>Pick a colour for the category</span>
            </div>
            <p class='help-step-desc'>The sidebar dot and card accent are driven by <code>sectionColors</code>. Use the slugified divider name as the key — lowercase, hyphens for spaces.</p>
            <div class='help-code-block'>
              <pre data-copy="var sectionColors = {
  // ... existing entries
  'my-new-section': '#3b82f6',  // any hex colour
};"><span class="hc-comment">// in sectionColors:</span>
<span class="hc-var">var</span> sectionColors <span class="hc-punct">=</span> <span class="hc-punct">{</span>
<span class="hc-punct">  </span><span class="hc-comment">// ... existing entries</span>
<span class="hc-punct">  </span><span class="hc-str">'my-new-section'</span><span class="hc-punct">: </span><span class="hc-str">'#3b82f6'</span><span class="hc-punct">,  </span><span class="hc-comment">// any hex colour</span>
<span class="hc-punct">};</span></pre>
              <button class='help-copy-btn' data-copy-target='prev'>Copy</button>
            </div>
          </div>

          <div class='help-tip'>
            <strong>Tip:</strong> The section slug is the divider name lowercased with spaces replaced by hyphens. <code style="background:#dbeafe;padding:1px 5px;border-radius:3px;font-family:ui-monospace,monospace;font-size:12px">"My New Section"</code> becomes <code style="background:#dbeafe;padding:1px 5px;border-radius:3px;font-family:ui-monospace,monospace;font-size:12px">my-new-section</code>. If the slug doesn't match the key in <code style="background:#dbeafe;padding:1px 5px;border-radius:3px;font-family:ui-monospace,monospace;font-size:12px">sectionColors</code>, it falls back to a neutral grey.
          </div>

        </div>
      </div>
    </div>
  `;
  document.body.insertAdjacentHTML('beforeend', helpModalHtml);

  document.body.classList.add('overlay-open');

  var profileList = document.getElementById("profile-list");
  var pageOverlayWrapper = document.getElementById("page-overlay-wrapper");

  var sectionForRow = null;
  for (var i = 0, len = userTypesObject.length; i < len; ++i) {
    (function(i) {
      var entry = userTypesObject[i];
      if (entry.hasOwnProperty('divider')) {
        sectionForRow = slugify(entry.divider);
        var color = sectionColors[sectionForRow] || '#64748b';
        var headerHtml = '<div class="section-header" id="' + sectionForRow + '" data-section="' + sectionForRow + '">' +
            '<div class="section-dot" style="background:' + color + '"></div>' +
            '<div class="section-title">' + escapeHtml(entry.divider) + '</div>' +
            '<div class="section-count">' + sectionCounts[sectionForRow] + '</div>' +
            '<div class="section-line"></div>' +
            '</div>';
        profileList.insertAdjacentHTML('beforeend', headerHtml);
      } else {
        var isError = entry.description.indexOf('Error case') === 0;
        var rowClass = 'user-type-selector' + (isError ? ' error-case' : '');
        var color = sectionColors[sectionForRow] || '#64748b';
        var borderStyle = isError ? '' : ' style="border-left-color:' + color + '"';

        var returnIdHtml = entry.returnId
            ? '<span class="row-returnid">' + escapeHtml(entry.returnId) + '</span>'
            : '<span class="row-returnid empty">no return id</span>';
        var credIdHtml = entry.hasOwnProperty('authorityId')
            ? ' · CredID ' + escapeHtml(entry.authorityId)
            : '';
        var techHtml = escapeHtml(entry.enrolmentKey) + ' · ' + escapeHtml(entry.identifierName) + ' · ' + escapeHtml(entry.identifierValue) + credIdHtml;

        var rowHtml = '<div class="' + rowClass + '" id="select-user-' + i + '" data-section="' + sectionForRow + '"' + borderStyle + '>' +
            '<a href="#">' +
            '<span class="row-title">' + escapeHtml(entry.description) + '</span>' +
            '<span class="row-meta-line">' +
            returnIdHtml +
            '<span class="row-tech">' + techHtml + '</span>' +
            '</span>' +
            '</a>' +
            '</div>';
        profileList.insertAdjacentHTML('beforeend', rowHtml);

        var authId = entry.hasOwnProperty('authorityId') ? entry.authorityId : "noAuthorityId";
        document.getElementById('select-user-' + i).addEventListener("click", function(e) {
          e.preventDefault();
          clickCount += 1;
          submitForm(entry.description, entry.affinityGroup, entry.enrolmentKey, entry.identifierName, entry.identifierValue, entry.enrolmentStatus, authId, submitFormVal, clickCount, entry.returnId);
        }, false);
      }
    }(i));
  }

  // Build sidebar
  var sidebarList = document.getElementById('sidebar-list');
  var sidebarHtml = '';
  for (var s = 0; s < userTypesObject.length; s++) {
    if (userTypesObject[s].hasOwnProperty('divider')) {
      var slug = slugify(userTypesObject[s].divider);
      var sbColor = sectionColors[slug] || '#64748b';
      sidebarHtml += '<li><a data-section-link="' + slug + '">' +
          '<span class="sb-dot" style="background:' + sbColor + '"></span>' +
          '<span class="sb-label">' + escapeHtml(userTypesObject[s].divider) + '</span>' +
          '<span class="sb-count">' + sectionCounts[slug] + '</span>' +
          '</a></li>';
    }
  }
  sidebarList.innerHTML = sidebarHtml;

  document.getElementById("script-submit").addEventListener("click", function() {
    $("#submit").click();
  });

  // --- Sidebar click navigation ---
  function scrollToSection(slug) {
    var target = document.getElementById(slug);
    if (!target) return;
    var wrapperTop = pageOverlayWrapper.getBoundingClientRect().top;
    var targetTop = target.getBoundingClientRect().top;
    var delta = targetTop - wrapperTop - 16;
    pageOverlayWrapper.scrollTop = pageOverlayWrapper.scrollTop + delta;
  }

  var sidebarLinkEls = document.querySelectorAll('#shortcuts-wrapper a[data-section-link]');
  sidebarLinkEls.forEach(function(link) {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      scrollToSection(link.getAttribute('data-section-link'));
    });
  });

  document.getElementById('back-to-top-link').addEventListener('click', function(e) {
    e.preventDefault();
    pageOverlayWrapper.scrollTop = 0;
  });

  // --- Active section highlighting ---
  function updateActiveSection() {
    var headers = profileList.querySelectorAll('.section-header');
    var wrapperRect = pageOverlayWrapper.getBoundingClientRect();
    var current = null;
    for (var h = 0; h < headers.length; h++) {
      var header = headers[h];
      var top = header.getBoundingClientRect().top - wrapperRect.top;
      if (top < 80) {
        current = header.getAttribute('data-section');
      } else {
        break;
      }
    }
    if (!current && headers.length > 0) {
      current = headers[0].getAttribute('data-section');
    }
    sidebarLinkEls.forEach(function(link) {
      if (link.getAttribute('data-section-link') === current) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }
  pageOverlayWrapper.addEventListener('scroll', updateActiveSection);
  setTimeout(updateActiveSection, 50);

  // --- Help modal ---
  var helpBackdrop = document.getElementById('help-modal-backdrop');
  var helpButton = document.getElementById('help-button');
  var helpClose = document.getElementById('help-modal-close');

  function openHelp() {
    helpBackdrop.classList.add('open');
  }
  function closeHelp() {
    helpBackdrop.classList.remove('open');
  }

  helpButton.addEventListener('click', openHelp);
  helpClose.addEventListener('click', closeHelp);
  helpBackdrop.addEventListener('click', function(e) {
    if (e.target === helpBackdrop) closeHelp();
  });
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && helpBackdrop.classList.contains('open')) {
      closeHelp();
    }
  });

  // Copy buttons in help modal
  document.querySelectorAll('.help-copy-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var pre = btn.previousElementSibling;
      var text = pre && pre.getAttribute('data-copy') ? pre.getAttribute('data-copy') : (pre ? pre.textContent : '');
      if (!text) return;
      var done = function() {
        btn.classList.add('copied');
        btn.textContent = '✓ Copied';
        setTimeout(function() {
          btn.classList.remove('copied');
          btn.textContent = 'Copy';
        }, 1600);
      };
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(done).catch(function() {
          // Fallback for non-secure contexts
          var ta = document.createElement('textarea');
          ta.value = text;
          document.body.appendChild(ta);
          ta.select();
          try { document.execCommand('copy'); done(); } catch(err) {}
          document.body.removeChild(ta);
        });
      } else {
        var ta = document.createElement('textarea');
        ta.value = text;
        document.body.appendChild(ta);
        ta.select();
        try { document.execCommand('copy'); done(); } catch(err) {}
        document.body.removeChild(ta);
      }
    });
  });
}

function showOverlay() {
  document.getElementById("page-overlay").style.display = "flex";
  document.body.classList.add('overlay-open');
}

function hideOverlay() {
  document.getElementById("page-overlay").style.display = "none";
  document.body.classList.remove('overlay-open');
}

function toggleOverlay() {
  if (document.getElementById("page-overlay")) {
    var overlay = document.getElementById("page-overlay");
    if (overlay.style.display === "none") {
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
  if (submitFormVal == true) {
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
  });
});