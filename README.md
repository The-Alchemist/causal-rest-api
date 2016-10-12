# Causal REST API V1

This RESTful API is designed for causal web. And it implements the [JAX-RS](https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) specifications using Jersey.

## Installation

The following installation instructions are supposed to be used by the server admin who deploys this API server. API users can skip this section and just start reading from the [API Usage and Examples](https://github.com/bd2kccd/causal-rest-api#api-usage-and-examples) section. 

### Prerequisites 

You must have the following installed to build/install Causal REST API:

- [Oracle Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3.x](https://maven.apache.org/download.cgi)

### Dependencies

If you want to run this API server and expose the API to your users, you'll first need to have the [Causal Web Application](https://github.com/bd2kccd/causal-web) installed and running. Your API users will use this web app to create their user accounts before they can consume the API.

In order to build the API server, you'll need the released version of [ccd-commons-0.3.1](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.1) by going to the repo and checkout this specific release version:

````
git clone https://github.com/bd2kccd/ccd-commons.git
cd ccd-commons
git checkout tags/v0.3.1
mvn clean install
````

You'll also need to download [ccd-db-0.6.2](https://github.com/bd2kccd/ccd-db) branch:

````
git clone https://github.com/bd2kccd/ccd-db.git
cd ccd-db
git checkout v0.6.2
mvn clean install
````

**Note: we'll use the the 0.6.2 tagged release once it's released, only use the branch for now.**

Then you can go get and install `causal-rest-api`:

````
git clone https://github.com/bd2kccd/causal-rest-api.git
cd causal-rest-api
mvn clean package
````

### Configuration

There are 4 configuration files to configure located at `causal-rest-api/src/main/resources`:
- **application-hsqldb.properties**: HSQLDB database configurations (for testing only).
- **application-mysql.properties**: MySQL database configurations
- **application.properties**: Spring Boot application settings
- **causal.properties**: Data file directory path and folder settings

Befor editing the `causal.properties` file, you need to create a workspace for the application to work in. Create a directory called workspace, for an example `/home/zhy19/ccd/workspace`. Inside the workspace directory, create another folder called `lib`. Then build the jar file of Tetred using the [6.0-alpha-20160930](https://github.com/cmu-phil/tetrad/releases/tag/6.0-alpha-20160930) pre-release version. After that, copy the jar file to the `lib` folder created earlier.

### Start the API Server

Once you have all the settings configured, go to `causal-rest-api/target` and you will find the jar file named `causal-rest-api.jar`. Then simply run 

```bash
java -jar causal-rest-api.jar
```

## API Usage and Examples

In the following sections, we'll demonstrate the API usage with examples using the API server that is running on Amazon Web Services EC2 instance. The API base URI is https://cloud.ccd.pitt.edu/ccd-api.

This API requires user to be authenticated. Before using this API, the user will need to go to [Causal-Web App](https://cloud.ccd.pitt.edu/ccd-web/) and create an account. 

### Getting JSON Web Token(JWT)

After registration in Causal Web App, the username and password can be used to authenticate against the Causal REST API to get the access token (we use JWT) via **HTTP Basic Auth**. 

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/jwt
````

In basic auth, the user provides the username and password, which the HTTP client concatenates (username + ":" + password), and base64 encodes it. This encoded string is then sent using a `Authorization` header with the "Basic" schema. For instance user `demouser` whose password is `123`.

````
POST /ccd-api/jwt HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Basic ZGVtb3VzZXI6MTIz
````

Once the request is processed successfully, the user ID together with a JWT will be returned in the response for further API queries.

````
{
  "userId": 22,
  "jwt": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA0Mjg1OTcsImlhdCI6MTQ3NTg0NjgyODU5N30.FcE7aEpg0u2c-gUVugIjJkzjhlDu5qav_XHtgLu3c6E",
  "issuedTime": 1475846828597,
  "lifetime": 3600,
  "expireTime": 1475850428597
}
````

We'll need to use this `userId` in the URI path of all subsequent requests. And this `jwt` expires in 3600 seconds(1 hour), so the API consumer will need to request for another JWT otherwise the API query to other API endpoints will be denied. And this JWT will need to be sent via the HTTP `Authorization` header as well, but using the `Bearer` schema.

Note: querying the JWT endpoint again before the current JWT expires will generate a new JWT, which makes the old JWT expired automatically. And this newly generated JWT will be valid in another hour unless there's another new JWT being queried.

Since this API is developed with Jersey, which supports [WADL](https://en.wikipedia.org/wiki/Web_Application_Description_Language). So you can view the generated WADL by going to `https://cloud.ccd.pitt.edu/ccd-api/application.wadl?detail=true` and see all resource available in the application. Accessing to this endpoint doesn't require authentication.

Basically, all the API usage examples are grouped into three categories: 

1. Data Management
2. Causal Discovery
3. Result Management

And all the following examples will be issued by user `22` whose password is `123`.

### 1. Data Management

#### Upload small data file

At this point, you can upload two types of data files: tabular dataset file(either tab delimited or comma delimited) and prior knowledge file.

API Endpoint URI pattern:

````
POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/upload/dataset
````

This is a multipart file upload via an HTML form, and the client is required to use `name="file"` to name their file upload field in their form.

Generated HTTP request code example:

````
POST /ccd-api/22/upload/dataset HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename=""
Content-Type: 


----WebKitFormBoundary7MA4YWxkTrZu0gW
````

If the Authorization header is not provided, the response will look like this:

````javascript
{
  "timestamp": 1465414501443,
  "status": 401,
  "error": "Unauthorized",
  "message": "User credentials are required.",
  "path": "/22/upload/dataset"
}
````

This POST request will upload the dataset file to the target server location and add corresponding records into database. And the response will contain the following pieces:

````javascript
{
    "id": 6,
    "name": "Lung-tetrad_hv.txt",
    "creationTime": 1466622267000,
    "lastModifiedTime": 1466622267000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
      "variableType": null,
      "fileDelimiter": null,
      "numOfRows": null,
      "numOfColumns": null
    }
  }
````

The prior knowledge file upload uses a similar API endpoint:

````
POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/upload/priorknowledge
````

Due to there's no need to summarize a prior knowledge file, the response of a successful prior knowledge file upload will look like:


````javascript
{
    "id": 6,
    "name": "Lung-tetrad_hv.txt",
    "creationTime": 1466622267000,
    "lastModifiedTime": 1466622267000,
    "fileSize": 3309465,
    "md5checkSum": "ugdb7511rt293d29ke3055d9a7b46c9k"
  }
````

#### Resumable data file upload

In addition to the regular file upload described in Example 6, we also provide the option of stable and resumable large file upload. It requires the client side to have a resumable upload implementation. We currently support client integrated with [Resumable.js](http://resumablejs.com/), whihc provides multiple simultaneous, stable 
and resumable uploads via the HTML5 File API. You can also create your own client as long as al the following parameters are set correctly.

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/upload/chunk

POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/upload/chunk
````

In this example, the data file is splited into 3 chunks. The upload of each chunk consists of a GET request and a POST request. To handle the state of upload chunks, a number of extra parameters are sent along with all requests:

* `resumableChunkNumber`: The index of the chunk in the current upload. First chunk is `1` (no base-0 counting here).
* `resumableChunkSize`: The general chunk size. Using this value and `resumableTotalSize` you can calculate the total number of chunks. Please note that the size of the data received in the HTTP might be lower than `resumableChunkSize` of this for the last chunk for a file.
* `resumableCurrentChunkSize`: The size of the current resumable chuck.
* `resumableTotalSize`: The total file size.
* `resumableType`: The file type of the resumable chuck, e.e., "text/plain".
* `resumableIdentifier`: A unique identifier for the file contained in the request.
* `resumableFilename`: The original file name (since a bug in Firefox results in the file name not being transmitted in chunk multipart posts).
* `resumableRelativePath`: The file's relative path when selecting a directory (defaults to file name in all browsers except Chrome).
* `resumableTotalChunks`: The total number of chunks.  

Generated HTTP request code example:

````
GET /ccd-api/22/upload/chunk?resumableChunkNumber=2&resumableChunkSize=1048576&resumableCurrentChunkSize=1048576&resumableTotalSize=3309465&resumableType=text%2Fplain&resumableIdentifier=3309465-large-datatxt&resumableFilename=large-data.txt&resumableRelativePath=large-data.txt&resumableTotalChunks=3 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This GET request checks if the data chunk is already on the server side. If the target file chunk is not found on the server, the client will issue a POST request to upload the actual data.

Generated HTTP request code example:

````
POST /ccd-api/22/upload/chunk HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryMFjgApg56XGyeTnZ

------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableChunkNumber"

2
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableChunkSize"

1048576
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableCurrentChunkSize"

1048576
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableTotalSize"

3309465
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableType"

text/plain
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableIdentifier"

3309465-large-datatxt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableFilename"

large-data.txt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableRelativePath"

large-data.txt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableTotalChunks"

3
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="file"; filename="blob"
Content-Type: application/octet-stream


------WebKitFormBoundaryMFjgApg56XGyeTnZ--
````

Each chunk upload POST will get a 200 status code from response if everything works fine.


And finally the md5checkSum string of the reassemabled file will be returned once the whole file has been uploaded successfully. In this example, the POST request that uploads the third chunk will response this:

````
b1db7511ee293d297e3055d9a7b46c5e
````

#### List all dataset files of a user

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/dataset
````

Generated HTTP request code example:

````
GET /ccd-api/22/dataset HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/json
````

A `JSON` formatted list of all the input dataset files that are associated with user `22` will be returned.

````javascript
[
  {
    "id": 8,
    "name": "data_small.txt",
    "creationTime": 1467132449000,
    "lastModifiedTime": 1467132449000,
    "fileSize": 278428,
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382",
    "fileSummary": {
      "variableType": "continuous",
      "fileDelimiter": "tab",
      "numOfRows": 302,
      "numOfColumns": 123
    }
  },
  {
    "id": 10,
    "name": "large-data.txt",
    "creationTime": 1467134048000,
    "lastModifiedTime": 1467134048000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
      "variableType": null,
      "fileDelimiter": null,
      "numOfRows": null,
      "numOfColumns": null
    }
  },
  {
    "id": 11,
    "name": "Lung-tetrad_hv (copy).txt",
    "creationTime": 1467140415000,
    "lastModifiedTime": 1467140415000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
      "variableType": "continuous",
      "fileDelimiter": "tab",
      "numOfRows": 302,
      "numOfColumns": 608
    }
  }
]
````

You can also specify the response format as XML in your request

Generated HTTP request code example:

````
GET /ccd-api/22/dataset HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/xml
````

And the response will look like this:

````xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<datasetFileDTOes>
    <datasetFile>
        <id>8</id>
        <name>data_small.txt</name>
        <creationTime>2016-06-28T12:47:29-04:00</creationTime>
        <lastModifiedTime>2016-06-28T12:47:29-04:00</lastModifiedTime>
        <fileSize>278428</fileSize>
        <md5checkSum>ed5f27a2cf94fe3735a5d9ed9191c382</md5checkSum>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <numOfColumns>123</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </datasetFile>
    <datasetFile>
        <id>10</id>
        <name>large-data.txt</name>
        <creationTime>2016-06-28T13:14:08-04:00</creationTime>
        <lastModifiedTime>2016-06-28T13:14:08-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
        <fileSummary>
            <variableType xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <fileDelimiter xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfRows xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfColumns xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
        </fileSummary>
    </datasetFile>
    <datasetFile>
        <id>11</id>
        <name>Lung-tetrad_hv (copy).txt</name>
        <creationTime>2016-06-28T15:00:15-04:00</creationTime>
        <lastModifiedTime>2016-06-28T15:00:15-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <numOfColumns>608</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </datasetFile>
</datasetFileDTOes>
````

Form the above output, we can also tell that data file with ID 10 doesn't have all the `fileSummary` field values set, we'll cover this in the dataset summarization section.

#### Get the deatil information of a dataset file based on ID

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/dataset/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/dataset/8 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And the resulting response looks like this:

````javascript
{
  "id": 8,
  "name": "data_small.txt",
  "creationTime": 1467132449000,
  "lastModifiedTime": 1467132449000,
  "fileSize": 278428,
  "fileSummary": {
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382",
    "variableType": "continuous",
    "fileDelimiter": "tab",
    "numOfRows": 302,
    "numOfColumns": 123
  }
}
````

#### Delete physical dataset file and all records from database for a given file ID

API Endpoint URI pattern:

````
DELETE https://cloud.ccd.pitt.edu/ccd-api/{userId}/dataset/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/dataset/8 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.


#### Summarize dataset file

So from the first example we can tell that file with ID 10 doesn't have `variableType`, `fileDelimiter`, `numOfRows`, and `numOfColumns` specified under `fileSummary`. Among these attributes, variableType` and `fileDelimiter` are the ones that users will need to provide during this summarization process.

Before we can go ahead to run the desired algorithm with the newly uploaded data file, we'll need to summarize the data by specifing the variable type and file delimiter.

| Required Fields | Description |
| --- | --- |
| id | The data file ID |
| variableType | discrete or continuous |
| fileDelimiter | tab or comma |

API Endpoint URI pattern:

````
POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/dataset/summarize
````

Generated HTTP request code example:

````
POST /ccd-api/22/dataset/summarize HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

{
    "id": 1,
    "variableType": "continuous",
    "fileDelimiter": "comma"
}
````

This POST request will summarize the dataset file and generate a response (JSON or XML) like below:

````javascript
{
  "id": 10,
  "name": "large-data.txt",
  "creationTime": 1467134048000,
  "lastModifiedTime": 1467134048000,
  "fileSize": 3309465,
  "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
  "fileSummary": {
    "variableType": "continuous",
    "fileDelimiter": "tab",
    "numOfRows": 302,
    "numOfColumns": 608
  }
}
````

#### List all prior knowledge files of a given user

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/priorknowledge
````

Generated HTTP request code example:

````
GET /ccd-api/22/priorknowledge HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/json
````

A `JSON` formatted list of all the input dataset files that are associated with user `22` will be returned.

````javascript
[
  {
    "id": 9,
    "name": "data_small.prior",
    "creationTime": 1467132449000,
    "lastModifiedTime": 1467132449000,
    "fileSize": 278428,
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382"
  },
  {
    "id": 12,
    "name": "large-data.prior",
    "creationTime": 1467134048000,
    "lastModifiedTime": 1467134048000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e"
  }
]
````

#### Get the deatil information of a prior knowledge file based on ID

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/priorknowledge/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/priorknowledge/9 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And the resulting response looks like this:

````javascript
{
  "id": 9,
  "name": "data_small.prior",
  "creationTime": 1467132449000,
  "lastModifiedTime": 1467132449000,
  "fileSize": 278428,
  "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382"
}
````

#### Delete physical prior knowledge file and all records from database for a given file ID

API Endpoint URI pattern:

````
DELETE https://cloud.ccd.pitt.edu/ccd-api/{userId}/priorknowledge/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/priorknowledge/9 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.

### 2. Causal Discovery

Once the data file is uploaded and summaried, you can start running a Causal Discovery Algorithm on the uploaded data file.

#### List all the available causal discovery algorithms

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/algorithms
````

Generated HTTP request code example:

````
GET /ccd-api/22/algorithms HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````
Currently we support "FGS continuous" and "FGS discrete".

````javascript
[
  {
    "id": 1,
    "name": "fgsc",
    "description": "FGS continuous"
  },
  {
    "id": 2,
    "name": "fgsd",
    "description": "FGS discrete"
  },
  {
    "id": 3,
    "name": "gfcic",
    "description": "GFCI continuous"
  }
]
````

Currently we support "FGS continuous", "FGS discrete" and "GFCI continuous". They also share a common JSON structure as of their input, for example:

| Input JSON Fields | Description |
| --- | --- |
| `datasetFileId` | The dataset file ID, integer |
| `priorKnowledgeFileId` | The prior knowledge file ID, integer |
| `dataValidation` | Algorithm specific input data validation flags, JSON object |
| `algorithmParameters` | Algorithm specific parameters, JSON object |
| `jvmOptions` | Advanced Options For Java Virtual Machine (JVM), JSON object. Currently only support `maxHeapSize` (Gigabyte, max value is 100) |

Below are the data validation flags and parameters that you can use for each algorithm.

**FGS continuous** 

Data validation:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `nonZeroVariance`      | Non-zero Variance. Ensure that each variable has non-zero variance | true |
| `uniqueVarName`      | Unique Variable Name. Ensure that there are no duplicated variable names      |  true |

Algorithm parameters:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `depth`      | Search depth. Integer value |  |
| `faithfulnessAssumed`      | Yes if (one edge) faithfulness should be assumed      |   false |
| `maxDegree`      | The maximum degree of the output graph      |   5 |
| `penaltyDiscount`      | Penalty discount      |   4.0 |
| `ignoreLinearDependence` | Ignore linear dependence      |    true |
| `heuristicSpeedup` | Heuristic speedup. All conditional independence relations that hold in the distribution are entailed by the Causal Markov Assumption      |    true |
| `verbose` | Print additional information      |    true |

**FGS discrete** 

Data validation:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `uniqueVarName`      | Unique Variable Name. Ensure that there are no duplicated variable names      |  true |
| `limitNumOfCategory`      | Limit Number of Categories - ensure the number of categories of a variable does not exceed 10 | true |


Algorithm parameters:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `depth`      | Search depth. Integer value |  |
| `faithfulnessAssumed`      | Yes if (one edge) faithfulness should be assumed      |   false |
| `maxDegree`      | The maximum degree of the output graph      |   5 |
| `structurePrior`      | Penalty discount      |  |
| `samplePrior` | Sample prior      |  |
| `heuristicSpeedup` | Heuristic speedup. All conditional independence relations that hold in the distribution are entailed by the Causal Markov Assumption      |    true |
| `verbose` | Print additional information      |    true |

**GFCI continuous** 

Data validation:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `uniqueVarName`      | Unique Variable Name. Ensure that there are no duplicated variable names      |  true |

Algorithm parameters:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `alpha`      | Search depth. Integer value |  1.0 | 
| `faithfulnessAssumed`      | Yes if (one edge) faithfulness should be assumed      |   false |
| `maxInDegree`      | Maximum indegree of graph      |   100 |
| `penaltyDiscount`      | Penalty discount      |   4.0 |
| `verbose` | Print additional information      |    true |

#### Add a new job to run the desired algorithm on a given data file

This is a POST request and the algorithm details and data file id will need to be specified in the POST body as a JSON when you make the request.

API Endpoint URI pattern:

````
POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/jobs/fgsc
````

Generated HTTP request code example:

````
POST /ccd-api/22/jobs/fgsc HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

{
    "datasetFileId": 8,
    "priorKnowledgeFileId": 9,
    "dataValidation": {
      "nonZeroVariance": false,
      "uniqueVarName": false
    },
    "algorithmParameters": {
      "depth": 3,
      "penaltyDiscount": 5.0
    },
    "jvmOptions": {
      "maxHeapSize": 100
    }
}
````

In this example, we are running the "FGS continuous" algorithm on the file with ID 8. And this call will return the job info with a 201 Created response status code.

````
{
  "id": 5,
  "algorithmName": "fgsc",
  "addedTime": 1472742564355,
  "resultFileName": "fgs_data_small.txt_1472742564353.txt",
  "errorResultFileName": "error_fgs_data_small.txt_1472742564353.txt"
}
````

From this response we can tell that the job ID is 5, and the result file name will be `fgs_data_small.txt_1472742564353.txt` if everything goes well. If something is wrong an error result file with name `error_fgs_data_small.txt_1472742564353.txt` will be created.

When you need to run "FGS discrete", just send the request to a different endpont URI:

API Endpoint URI pattern:

````
POST https://cloud.ccd.pitt.edu/ccd-api/{userId}/jobs/fgsd
````

Generated HTTP request code example:

````
POST /ccd-api/22/jobs/fgsd HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

{
    "datasetFileId": 10,
    "priorKnowledgeFileId": 12,
    "dataValidation": {
      "uniqueVarName": false,
      "limitNumOfCategory": false
    },
    "algorithmParameters": {
      "depth": 3,
      "structurePrior": 1.0,
      "samplePrior": 1.0
    },
    "jvmOptions": {
      "maxHeapSize": 100
    }
}
````

#### List all running jobs

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/jobs
````

Generated HTTP request code example:

````
GET /ccd-api/22/jobs/ HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

````

Then you'll see the information of all jobs that are currently running:

````javascript
[
  {
    "id": 32,
    "algorithmName": "fgsc",
    "addedTime": 1468436085000
  },
  {
    "id": 33,
    "algorithmName": "fgsd",
    "addedTime": 1468436087000
  }
]
````

#### Check the job status for a given job ID

Once the new job is submitted, it takes time and resources to run the algorithm on the server. During the waiting, you can check the status of a given job ID:

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/jobs/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/jobs/32 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This will either return "Pending" or "Completed".

#### Cancel a running job

Sometimes you may want to cancel a submitted job.

API Endpoint URI pattern:

````
DELETE https://cloud.ccd.pitt.edu/ccd-api/{userId}/jobs/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/jobs/8 HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This call will response either "Job 8 has been canceled" or "Unable to cancel job 8". It's not guranteed that the system can always cencal a job successfully.

### 3. Result Management

#### List all result files generated by the algorithm

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/results
````

Generated HTTP request code example:

````
GET /ccd-api/22/results HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

The response to this request will look like this:

````javascript
[
  {
    "name": "fgs_sim_data_20vars_100cases.csv_1466171729046.txt",
    "creationTime": 1466171732000,
    "lastModifiedTime": 1466171732000,
    "fileSize": 1660
  },
  {
    "name": "fgs_data_small.txt_1466172140585.txt",
    "creationTime": 1466172145000,
    "lastModifiedTime": 1466172145000,
    "fileSize": 39559
  }
]
````

#### Download a speific result file generated by the algorithm based on file name

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/results/{result_file_name}
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/fgs_data_small.txt_1466172140585.txt HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````
On success, you will get the result file back as text file content. If there's a typo in file name of the that file doesn't exist, you'll get either a JSON or XML message based on the `accept` header in your request:

The response to this request will look like this:

````javascript
{
  "timestamp": 1467210996233,
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found.",
  "path": "/22/results/fgs_data_small.txt_146172140585.txt"
}
````


#### Compare algorithm result files

Since we can list all the algorithm result files, based on the results, we can also choose multiple files and run a comparison. 

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/results/compare/{result_file_name}!!{another_result_file_name}
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/compare/fgs_sim_data_20vars_100cases.csv_1466171729046.txt!!fgs_data_small.txt_1467305104859.txt HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````
When you specify multiple file names, use the `!!` as a delimiter. This request will generate a result comparison file with the following content (shortened version):

````
fgs_sim_data_20vars_100cases.csv_1466171729046.txt	fgs_data_small.txt_1467305104859.txt
Edges	In All	Same End Point
NR4A2,FOS	0	0
X5,X17	0	0
MMP11,ASB5	0	0
X12,X8	0	0
hsa_miR_654_3p,hsa_miR_337_3p	0	0
RND1,FGA	0	0
HHLA2,UBXN10	0	0
HS6ST2,RND1	0	0
SCRG1,hsa_miR_377	0	0
CDH3,diag	0	0
SERPINI2,FGG	0	0
hsa_miR_451,hsa_miR_136_	0	0
````

From this comparison, you can see if the two algorithm graphs have common edges and endpoints.

#### List all the comparison files

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/results/comparisons
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/comparisons HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

The response will show a list of comparison files:

````javascript
[
  {
    "name": "result_comparison_1467385923407.txt",
    "creationTime": 1467385923000,
    "lastModifiedTime": 1467385923000,
    "fileSize": 7505
  },
  {
    "name": "result_comparison_1467387034358.txt",
    "creationTime": 1467387034000,
    "lastModifiedTime": 1467387034000,
    "fileSize": 7505
  },
  {
    "name": "result_comparison_1467388042261.txt",
    "creationTime": 1467388042000,
    "lastModifiedTime": 1467388042000,
    "fileSize": 7533
  }
]
````

#### Download a speific comparison file based on file name

API Endpoint URI pattern:

````
GET https://cloud.ccd.pitt.edu/ccd-api/{userId}/results/comparisons/{comparison_file_name}
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/comparisons/result_comparison_1467388042261.txt HTTP/1.1
Host: cloud.ccd.pitt.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

Then it returns the content of that comparison file (shorted version):

````
fgs_sim_data_20vars_100cases.csv_1466171729046.txt	fgs_data_small.txt_1467305104859.txt
Edges	In All	Same End Point
NR4A2,FOS	0	0
X5,X17	0	0
MMP11,ASB5	0	0
X12,X8	0	0
hsa_miR_654_3p,hsa_miR_337_3p	0	0
RND1,FGA	0	0
HHLA2,UBXN10	0	0
HS6ST2,RND1	0	0
SCRG1,hsa_miR_377	0	0
CDH3,diag	0	0
SERPINI2,FGG	0	0
````

