
# stamp-duty-land-tax-stub

This is a repository used to stub https://github.com/hmrc/formp-proxy

To start the server locally on `port 10914`: `sbt 'run 10914'`

## SDLT Stub data

| **STORN** | **Agents** | **In progress Returns** | **Submitted Returns** | **Deleted Returns** |
|-----------|------------|-------------------------|-----------------------|---------------------|
| STN001    | 35         | 51                      | 62                    | 57                  |
| STN002    | 0          | 31                      | 0                     | 11                  |
| STN003    | 25         | 0                       | 62                    | 0                   |
| STN004    | 24         | 0                       | 0                     | 0                   |


## Tool to generate random returns data assuming we have local Oracle Db installed / running:

** Get data access operation status (if any in progress):

http://localhost:10914/stamp-duty-land-tax-stub/returns/getStatus

** Delete all returns data:
http://localhost:10914/stamp-duty-land-tax-stub/returns/deleteAll

* Generate random returns:
http://localhost:10914/stamp-duty-land-tax-stub/returns/createData?storn=STN001&returnType=inprogress&records=49


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").