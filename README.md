
# stamp-duty-land-tax-stub

This is a repository used to stub https://github.com/hmrc/formp-proxy

## Running the service
Before starting, you will need to have  [service-manager](https://github.com/hmrc/service-manager) installed/configured

### Running locally:
Service Manager:
- Start dependent services `sm2 --start SDLT_ALL`
- Stop this service `sm2 --stop STAMP-DUTY-LAND-TAX-STUB`
- Start the server locally on `port 10914` with `sbt run`

## SDLT Stub data

| **STORN** | **Agents** | **In progress Returns** | **Submitted Returns** | **Deleted Returns** |
|-----------|------------|-------------------------|-----------------------|---------------------|
| STN001    | 35         | 51                      | 62                    | 57                  |
| STN002    | 0          | 31                      | 0                     | 11                  |
| STN003    | 25         | 0                       | 62                    | 0                   |
| STN004    | 24         | 0                       | 0                     | 0                   |


## Return IDs and scenarios

A filing return is looked up by its `returnResourceRef` (the "return ID"), which maps straight onto a
directory: `conf/resources/data/filing.full/<returnId>/fullReturnDetails.json`. Any ID without a matching
directory returns a `404` from `POST /stamp-duty-land-tax-stub/returns/getFullReturn`.

The tampermonkey script in `conf/tamperMonkey/filing/SDLT Script-3.0.1.user.js` exposes these IDs as a
dropdown on the auth-login-stub page; the groupings below mirror its dividers. Every fixture has a
matching dropdown entry, so adding a new fixture directory means adding it to the script too.

### Getting Started

| Return ID | Scenario |
|-----------|----------|
| _(blank)_ | No return ID - starts a new return |
| `123456` | Prelim questions submitted - skip to return task list (Organisation) |
| `firstJson` | Refresh created return |
| `prelim-only` | Prelim only |

### Vendors

| Return ID | Scenario |
|-----------|----------|
| `no-return-agent-and-no-vendor` | No return agent, no vendor |
| `no-vendor` | No vendor |
| `one-vendor` | 1 vendor |
| `5-vendors` | 5 vendors |
| `97-vendors-1-purchaser` | 97 vendors, 1 purchaser |
| `99-vendors` | 99 vendors |
| `vendor-agent-and-main-vendor-represented-by-agent` | Vendor agent, main vendor is represented by agent |
| `non-vendor-agent-and-main-vendor-not-represented-by-agent` | Non vendor agent, main vendor is not represented by agent |
| `no-return-agent-and-main-vendor-not-represented-by-agent` | No return agent, main vendor not represented by agent |

### Vendor error cases

| Return ID | Scenario |
|-----------|----------|
| `vendor-agent-and-main-vendor-not-represented-by-agent` | Vendor agent, main vendor is not represented by agent |
| `no-return-agent-and-main-vendor-represented-by-agent` | No return agent, main vendor is represented by agent |
| `non-vendor-agent-and-main-vendor-represented-by-agent` | Non vendor agent, main vendor is represented by agent |
| `vendor-agent-and-no-main-vendor` | Vendor agent, no main vendor |
| `error-removing-vendor` | Bad request deleting vendor |
| `error-updating-return-version` | Bad request updating return version |
| `error-updating-purchaser-return-agent` | Bad request updating purchaser return agent |
| `error-creating-purchaser-return-agent` | Bad request creating purchaser return agent |

### Purchasers

| Return ID | Scenario |
|-----------|----------|
| `no-purchaser` | No purchaser |
| `incomplete-purchaser` | Incomplete purchaser of type Company |
| `incomplete-purchaser-individual` | Incomplete purchaser of type Individual |
| `full-purchaser` | Full purchaser with address line 1 |
| `full-purchaser-with-agent` | Full purchaser with return agent |
| `one-purchaser-company` | One purchaser of type Company |
| `one-purchaser-individual` | One purchaser of type Individual |
| `2-purchasers-company` | 2 purchasers, main purchaser is type Company |
| `2-purchasers-individual` | 2 purchasers, main purchaser is type Individual |
| `5-purchasers-company` | 5 purchasers, main purchaser is type Company |
| `5-purchasers-individual` | 5 purchasers, main purchaser is type Individual |
| `97-purchasers-1-vendor` | 97 purchasers, 1 vendor |
| `98-purchasers-company` | 98 purchasers, main purchaser is type Company |
| `99-purchasers` | 99 purchasers |
| `50-purchasers-50-vendors` | 50 purchasers and 50 vendors |
| `purchaser-agent` | Purchaser agent |
| `purchaser-no-agents` | Purchaser, no agents |
| `no-company-details` | Company purchaser with no company details |
| `error-updating-return-info` | Bad request updating return info |

### Land transactions

| Return ID | Scenario |
|-----------|----------|
| `full-purchaser-with-agent-land-transaction-uk-authcodes` | Purchaser with land transaction, UK auth codes |
| `full-purchaser-with-agent-land-transaction-with-scot-authcodes` | Purchaser with land transaction, Scottish auth codes |
| `full-purchaser-with-agent-land-transaction-welsh-authcodes` | Purchaser with land transaction, Welsh auth codes |
| `full-purchaser-with-agent-land-transaction-ukauthcode-with-scot-postcode` | Purchaser with land transaction, UK auth code with Scottish postcode |
| `full-purchaser-with-agent-land-transaction-8998` | Purchaser with land transaction, auth code 8998 |
| `full-purchaser-with-agent-land-transaction-8999` | Purchaser with land transaction, auth code 8999 |
| `full-purchaser-with-agent-land-transaction-emptydates` | Purchaser with land transaction, empty effective and contract dates |
| `full-purchaser-with-agent-land-transaction-no-contract-date` | Purchaser with land transaction, no contract date |
| `full-purchaser-with-agent-land-transaction-no-effective-date` | Purchaser with land transaction, no effective date |

### Full land scenarios

| Return ID | Scenario |
|-----------|----------|
| `individual-purchaser-with-agents-full-land-non-residential` | Individual purchaser with agents, full land, non-residential |
| `individual-purchaser-with-agents-full-land-residential` | Individual purchaser with agents, full land, residential |
| `individual-purchaser-with-agents-full-land-additional-residential` | Individual purchaser with agents, full land, additional residential |
| `individual-purchaser-with-agents-full-land-residential-no-residency` | Individual purchaser with agents, full land, residential - no residency |
| `company-purchaser-with-agents-full-land-residential` | Company purchaser with agents, full land, residential |
| `company-purchaser-with-agents-full-land-additional-residential` | Company purchaser with agents, full land, additional residential |
| `company-purchaser-with-agents-full-land-residential-no-residency` | Company purchaser with agents, full land, residential - no residency |

### Lands

| Return ID | Scenario |
|-----------|----------|
| `no-land` | No land |
| `1-land-mixed-property-type` | 1 land with mixed property type |
| `1-land-residential-property-type` | 1 land with residential property type |
| `5-lands` | 5 lands |
| `98-lands` | 98 lands |
| `99-lands` | 99 lands |
| `land-no-postcode` | Prelim Land with no postcode |

### Prelim transactions

| Return ID | Scenario |
|-----------|----------|
| `prelimTransactionA` | Prelim transaction type A |
| `prelimTransactionF` | Prelim transaction type F |
| `prelimTransactionO` | Prelim transaction type O |
| `prelimTransactionL-property-type-mixed` | Prelim transaction type L - mixed property type |
| `prelimTransactionL-property-type-residential` | Prelim transaction type L - residential property type |
| `prelimTransactionL-property-type-nonResidential` | Prelim transaction type L - non-residential property type |
| `prelimTransactionL-property-type-additional` | Prelim transaction type L - additional property type |
| `prelimTransactionL-no-land` | Prelim transaction type L - no land |

### Freehold

| Return ID | Scenario |
|-----------|----------|
| `freehold-self-assessed` | Self assessed |
| `freehold-self-assessed-isLinked` | Self assessed, linked transaction |
| `freehold-self-assessed-zeroPenalty` | Self assessed, zero penalty |
| `freehold-self-assessed-partialRelief` | Self assessed, partial relief |
| `freehold-self-assessed-OT` | Self assessed, other transaction type |
| `freehold-self-assessed-multipleDwellings` | Self assessed, multiple dwellings |
| `freehold-self-assessed-effectiveDateBeforeMar2012` | Self assessed, effective date before March 2012 |
| `freehold-self-assessed-collectiveEnfranchisement` | Self assessed, collective enfranchisement |
| `freehold-multiple-self-assessed-reasons` | Multiple self assessed reasons |
| `freehold-tax-calculated` | Tax calculated |
| `freehold-tax-calculated-slice` | Tax calculated, slice basis |
| `freehold-tax-calculated-slab` | Tax calculated, slab basis |
| `freehold-tax-calculated-zero` | Tax calculated, zero tax |
| `freehold-tax-calculated-zeroPenalty` | Tax calculated, zero penalty |
| `freehold-tax-already-calculated` | Tax already calculated |
| `res-no-uk-res-ans-after-1-Apr-21-F` | Residential, no UK residency answer after 1 Apr 21 - type F (SDLTC validation fails) |
| `res-no-uk-res-ans-after-1-Apr-21-tax-calc-answered-F` | Residential, no UK residency answer after 1 Apr 21 - type F (tax calc answered) |

### Leasehold

| Return ID | Scenario |
|-----------|----------|
| `leasehold-self-assessed` | Self assessed |
| `leasehold-self-assessed-completed` | Self assessed, completed |
| `leasehold-self-assessed-isLinked` | Self assessed, linked transaction |
| `leasehold-self-assessed-zeroPenalty` | Self assessed, zero penalty |
| `leasehold-self-assessed-partialRelief` | Self assessed, partial relief |
| `leasehold-self-assessed-OT` | Self assessed, other transaction type |
| `leasehold-self-assessed-multipleDwellings` | Self assessed, multiple dwellings |
| `leasehold-self-assessed-effectiveDateBeforeMar2012` | Self assessed, effective date before March 2012 |
| `leasehold-self-assessed-collectiveEnfranchisement` | Self assessed, collective enfranchisement |
| `leasehold-self-assessed-predatesCalc1` | Self assessed, predates calculator (case 1) |
| `leasehold-self-assessed-predatesCalc2` | Self assessed, predates calculator (case 2) |
| `leasehold-multiple-self-assessed-reasons` | Multiple self assessed reasons |
| `leasehold-tax-calculated` | Tax calculated |
| `leasehold-tax-calculated-zeroPenalty` | Tax calculated, zero penalty |
| `leasehold-tax-calculated-withReliefReason` | Tax calculated, with relief reason |
| `leasehold-tax-calculated-firstTimeBuyerRelief` | Tax calculated, first time buyer relief |
| `res-no-uk-res-ans-after-1-Apr-21-L` | Residential, no UK residency answer after 1 Apr 21 - type L (SDLTC validation fails) |
| `res-no-uk-res-ans-after-1-Apr-21-tax-calc-answered-L` | Residential, no UK residency answer after 1 Apr 21 - type L (tax calc answered) |

### UK residency

| Return ID | Scenario |
|-----------|----------|
| `incomplete-main-purchaser-multiple-purchasers` | Incomplete main purchaser (multiple purchasers) |
| `5-lands-One-Residential` | 1 residential land |
| `5-lands-One-Additional` | 1 additional residential land |
| `5-lands-One-Residential-EffectiveDateNotValid` | Residency before the effective date needed |
| `5-lands-None-Residential` | No residential property types |
| `5-lands-One-Residential-Company` | 1 residential land, purchaser is a Company |
| `1-land-with-Invalid-InterestTransferredOption` | Land with an invalid `interestTransferredOption` |

### Full transactions

| Return ID | Scenario |
|-----------|----------|
| `full-transaction-conveyance` | Conveyance/transfer, mixed property type |
| `full-transaction-grantoflease-08partexchange` | Grant of lease, relief 08 part exchange |
| `full-transaction-grantoflease-20charitiesrelief` | Grant of lease, relief 20 charities relief |
| `grantoflease-effectiveDate-after-cutoff` | Grant of lease, effective date after cut off - type L |
| `grantoflease-effectiveDate-before-cutoff` | Grant of lease, effective date before cut off - type L |
| `LeaseStartDate-GreaterThan-EndDate-Check` | Lease start date greater than end date |

### Full lease

| Return ID | Scenario |
|-----------|----------|
| `full-lease-transaction-type-a` | Full lease, transaction type A |
| `full-lease-transaction-type-l` | Full lease, transaction type L |

### Crossflow errors

| Return ID                                             | Scenario                                                               |
|-------------------------------------------------------|------------------------------------------------------------------------|
| `f17-welsh6996-before-wales-act`                      | F17: Welsh auth code 6996 before the Wales Act                         |
| `f17-welsh6996-before-wales-act-multi`                | F17: Welsh auth code 6996 before the Wales Act, multiple lands         |
| `f17-6996-missingeffdate`                             | F17: 6996, missing effective date                                      |
| `f17-6996-preact`                                     | F17: 6996, pre Wales Act                                               |
| `f17-6997-preact`                                     | F17: 6997, pre Wales Act                                               |
| `f17-6998-bothdates`                                  | F17: 6998, both dates present                                          |
| `f17-6998-contractdate-postact`                       | F17: 6998, contract date after the Wales Act                           |
| `f17-6998-effdate-preact`                             | F17: 6998, effective date pre Wales Act                                |
| `f17-6998-nocontractdate`                             | F17: 6998, no contract date                                            |
| `f17-6999-bothdates`                                  | F17: 6999, both dates present                                          |
| `f17-6999-contractdate-afterwalesact`                 | F17: 6999, contract date after the Wales Act                           |
| `f17-6999-effdate-preact`                             | F17: 6999, effective date pre Wales Act                                |
| `f17-6999-nocontractdate`                             | F17: 6999, no contract date                                            |
| `f17-regularwelsh-postact`                            | F17: regular Welsh auth code after the Wales Act                       |
| `f18-dummy8998-precr223`                              | F18: dummy code 8998 before CR223                                      |
| `f18-dummy8998-contractpostcr223`                     | F18: dummy code 8998, contract date after CR223                        |
| `f18-dummy8998-nocontractdate`                        | F18: dummy code 8998, no contract date                                 |
| `f18-dummy8999-precr223`                              | F18: dummy code 8999 before CR223                                      |
| `f18-dummy8999-contractafterscotact`                  | F18: dummy code 8999, contract date after the Scotland Act             |
| `f18-dummy8999-nocontractdate`                        | F18: dummy code 8999, no contract date                                 |
| `f18-scottishcode-postcr223`                          | F18: Scottish auth code after CR223                                    |
| `f18-scottishpostcode-edinburgh`                      | F18: Scottish postcode - Edinburgh                                     |
| `f18-scottishpostcode-glasgow`                        | F18: Scottish postcode - Glasgow                                       |
| `all-f17-and-f18-predate`                             | All F17 and F18 codes, dates before the acts                           |
| `all-f17-and-f18-postdate`                            | All F17 and F18 codes, dates after the acts                            |
| `f23-32-fail-property-not-residential`                | F23/32: fails, property not residential                                |
| `f23-33-fail-property-not-allowed`                    | F23/33: fails, property type not allowed                               |
| `f23-34-fail-date-before-2013`                        | F23/34: fails, date before 2013                                        |
| `f23-35-fail-date-before-2013`                        | F23/35: fails, date before 2013                                        |
| `f23-36-fail-before-window`                           | F23/36: fails, date before the relief window                           |
| `f23-36-pass-inside-window`                           | F23/36: passes, date inside the relief window                          |
| `f23-36-fail-after-window`                            | F23/36: fails, date after the relief window                            |
| `f23-37-fail-before-window`                           | F23/37: fails, date before the relief window                           |
| `f23-37-fail-after-window`                            | F23/37: fails, date after the relief window                            |
| `f23-38-fail-date-before-2025-03-19`                  | F23/38: fails, date before 19 Mar 2025                                 |
| `f24-additional-res`                                  | F24: additional residential                                            |
| `f25-fail-contract-date-null`                         | F25: fails, contract date null                                         |
| `f25-fail-contract-date-on-cutoff`                    | F25: fails, contract date on the cut off                               |
| `f25-fail-contract-date-after-cutoff`                 | F25: fails, contract date after the cut off                            |
| `f25-fail-effective-date-on-cutoff`                   | F25: fails, effective date on the cut off                              |
| `f25-fail-effective-date-after-cutoff`                | F25: fails, effective date after the cut off                           |
| `f28-cap500k-original-window-fail`                    | F28: fails, total premium payable over 500k cap in the original window |
| `f28-cap625k-middle-window-fail`                      | F28: fails, total premium payable over 625k cap in the middle window   |
| `f28-cap500k-post-2025-fail`                          | F28: fails, total premium payable over 500k cap post 2025              |
| `f28-cap500k-original-window-fail-totalConsideration` | F28: fails, total consideration over 500k cap in the original window   |
| `f28-cap625k-middle-window-fail-totalConsideration`   | F28: fails, total consideration over 625k cap in the middle window     |
| `f28-cap500k-post-2025-fail-totalConsideration`       | F28: fails, total consideration over 500k cap post 2025                |
| `F30-Cf-5a`                                           | F30: crossflow check 5a                                                |
| `F30-Cf-5b`                                           | F30: crossflow check 5b                                                |
| `F30-Cf-5c`                                           | F30: crossflow check 5c                                                |
| `F30-CF6`                                             | F30: crossflow check 6                                                 |
| `F30-CF6-with-cf5a`                                   | F30: crossflow check 6 combined with 5a                                |
| `F30-CF6-with-CF17`                                   | F30: crossflow check 6 combined with CF17                              |
| `CF-17`                                               | Crossflow check 17                                                     |
| `full-lease-transaction-type-f`                       | Full lease, transaction type F - **no fixture exists, returns 404**    |

### PDF generation

| Return ID | Scenario |
|-----------|----------|
| `sdlt1a-pdf-gen` | Complete SDLT1a PDF section |
| `sdlt1c-pdf-gen` | Complete SDLT1c PDF section |
| `sdlt1d-pdf-gen` | Complete SDLT1d PDF section |
| `sdlt2Purchaser-pdf-gen` | Complete SDLT2 purchaser PDF section |
| `sdlt4-pdf-answers-leased-3-lands` | Complete SDLT4 PDF section - leased, 3 lands |
| `sdlt4a-pdf-answers-freehold-f48` | Complete SDLT4a PDF section - freehold, F48 |
| `sdlt4a-pdf-answers-leased-1-land` | Complete SDLT4a PDF section - leased, 1 land |

### Task list

| Return ID                                         | Scenario                                                          |
|---------------------------------------------------|-------------------------------------------------------------------|
| `all-sections-incomplete`                         | All sections incomplete                                           |
| `all-sections-in-progress`                        | All sections in progress                                          |
| `all-sections-complete-individual`                | All sections complete, Individual type                            |
| `all-sections-complete-company`                   | All sections complete, Company type                               |
| `all-sections-complete-landCrossFlowError`        | All sections complete, with land authority code error             |
| `all-sections-complete-transactionCrossFlowError` | All sections complete, with land authority code error             |
| `all-sections-complete-multipleCrossFlowError`    | All sections complete, with land and transaction error            |
| `all-sections-complete-uk-residency`              | All sections complete, with UK residency                          |
| `only-mandatory-sections-complete`                | Only mandatory sections complete                                  |
| `only-mandatory-sections-incomplete`              | Only mandatory sections incomplete                                |
| `section-agent-incomplete`                        | Agents incomplete - represented by agent                          |
| `section-agent-complete-yes`                      | Agents complete - represented by agent                            |
| `section-agent-complete-no`                       | Agents complete - not represented by agent, without agent details |
| `section-agent-complete-no-with-agent-details`    | Agents complete - not represented by agent, with agent details    |

### Submission

| Return ID | Scenario |
|-----------|----------|
| `submission-complete` | Submission complete - `SUBMITTED_NO_RECEIPT` |
| `submission-complete-with-agent` | Submission complete, with agent |
| `submission-complete-multiples` | Submission complete, multiple purchasers, vendors and lands |
| `submission-failed` | Submission failed - `DEPARTMENTAL_ERROR` |
| `submission-status-null` | Submission object present but status `None` |
| `no-receipt` | Submitted, no receipt (`SUBMITTED_NO_RECEIPT`) |
| `success-no-utrn` | Success but no UTRN (`FATAL` / AF11) |
| `rejected` | Departmental error - business reject 3001 |
| `recoverable-1000` | Recoverable 1000 (`STARTED`) |
| `retryable` | Retryable - recoverable 2005 (`STARTED`) |
| `recoverable-3000` | Recoverable 3000 (`STARTED`) |
| `schema-error-1001` | ChRIS schema error 1001 (`FATAL`) |
| `failed` | Fatal error - other (`FATAL`) |
| `multi-error` | Multiple errors (`FATAL` plus error details) |
| `http-retryable-503` | HTTP 503 retryable (`STARTED`) |
| `http-fatal-400` | HTTP 400 fatal transport error |
| `timeout` | ChRIS timeout (`STARTED`) |
| `malformed-xml` | Malformed ChRIS XML (`FATAL`) |
| `delete-not-found` | Submitted; delete leg 2000 not found |
| `lock-error` | Return lock 409 conflict |
| `create-submission-error` | `createSubmission` 500 |
| `update-submission-error` | `updateSubmission` 500 (before ChRIS) |
| `govtalk-insert-error` | GovTalk insert 500 |
| `govtalk-lock-error` | GovTalk lock 500 |
| `govtalk-update-error` | GovTalk protocol update 500 (after ChRIS) |
| `govtalk-statistics-error` | GovTalk statistics 500 (after ChRIS) |
| `govtalk-reset-error` | GovTalk reset path plus reset 500 |
| `submission-error-detail-error` | Reject plus error-detail write 500 |
| `delete-error-detail-error` | Re-submittable; delete error detail 500 |
| `schema-invalid` | SDLT schema validation failure (400) |
| `missing-context` | Missing submission context |
| `started` | Submission block with `submissionStatus` = `STARTED` |
| `pending` | Submission block with `submissionStatus` = `PENDING` |
| `fatal` | Submission block with `submissionStatus` = `FATAL_ERROR` |
| `departmental` | Submission block with `submissionStatus` = `SUBMITTED` and a UTRN |
| `submitted` | Return with `returnInfo.status` = `STARTED`, no submission block |
| `acknowledged` | Same fixture as `submitted`, different `returnResourceRef` |

### End to end

| Return ID | Scenario |
|-----------|----------|
| `e2e-from-uk-residency-to-tax-calculation` | UK residency through to tax calculation journey |

## Tool to generate random returns data assuming we have local Oracle Db installed / running:

** Get data access operation status (if any in progress):

http://localhost:10914/stamp-duty-land-tax-stub/returns/getStatus

** Delete all returns data:
http://localhost:10914/stamp-duty-land-tax-stub/returns/deleteAll

* Generate random returns:
http://localhost:10914/stamp-duty-land-tax-stub/returns/createData?storn=STN001&returnType=inprogress&records=49


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").