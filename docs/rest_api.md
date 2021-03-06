# Gateway REST API 

## Document History 
|Date|User|Note|
|----|----|----|
|2021-02-15|Oliver Archner|Comment API POST,PUT,DELETE,GET request support|
|2021-03-18|Oliver Archner|User API GET request support|

## General Notes 
All example requests use an authorization header with default credentials on localhost. Please adapt these settings according to your BayEOS Gateway situation.

## Create board comment
```javascript
POST /gateway/rest/comments HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json
{
    "boardID":26,
    "userID": 3,
    "insertTime": 1613396534237,
    "content":"This is my 26 board comment"    
}

Response Body:
{
    "id": 52,
    "boardID": 26,
    "userID": 3,
    "insertTime": 1613396534237,
    "content": "This is my 26 board comment"
}
```

## Update board comment
```javascript
PUT /gateway/rest/comments HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json
{    
    "id":52,
    "boardID": 26,
    "userID": 3,
    "insertTime": 1613396623619,
    "content": "This is an updated comment"
}

Response Body:
{
    "id": 52,
    "boardID": 26,
    "userID": 3,
    "insertTime": 1613396623619,
    "content": "This is an updated comment"
}
```

## Delete comment
```javascript
DELETE /gateway/rest/comments/52 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
```

## Get all board comments
```javascript
GET /gateway/rest/comments/board/26 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[
    {
        "id": 49,
        "boardID": 26,
        "userID": 3,
        "insertTime": 1613343600000,
        "content": "This is an updated comment"
    },
    {
        "id": 50,
        "boardID": 26,
        "userID": 3,
        "insertTime": 1613343600000,
        "content": "This is my 26 board comment"
    }
]
```

## Get user by ID
```javascript
GET /gateway/rest/users/4 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
{
    "id": 4,
    "name": "oliver",
    "fullName": "Oliver Archner",
    "domain": "REZ"
}
```

## Get all users sorted by name
```javascript
GET /gateway/rest/users HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[
    {
        "id": 3,
        "name": "import",
        "fullName": "import",
        "domain": null
    },
    {
        "id": 2,
        "name": "nagios",
        "fullName": "nagios",
        "domain": null
    },
    {
        "id": 4,
        "name": "oliver",
        "fullName": "Oliver Archner",
        "domain": "REZ"
    },
    {
        "id": 1,
        "name": "root",
        "fullName": "root",
        "domain": null
    }
]
```

## Get all domain users sorted by name
```javascript
GET /gateway/rest/users?domainName=REZ HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[
    {
        "id": 4,
        "name": "oliver",
        "fullName": "Oliver Archner",
        "domain": "REZ"
    }
]
```

## Get channel values during the last 24h sorted by time ascending
```javascript
GET /gateway/rest/channel?id=4 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[{"id": 635,"millis": 1562678971850,"value": 0.59041363},
{"id": 640,"millis": 1562678992717,"value": 0.57821494},
{"id": 645,"millis": 1562679012765,"value": 0.84613395}]
```

## Get channel values > lastRowId sorted by time ascending
```javascript
GET /gateway/rest/channel?id=4&lastRowId=587 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[{"id": 588, "millis": 1562588481171, "value": 0.1756716},…]
``` 

## Get channel values during a time period sorted by time ascending
```javascript
GET /gateway/rest/channel/?id=4&startTime=1552588481171&endTime=1562588481171 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[{"id": 588, "mill's": 1562588481171, "value": 0.1756716},…]
``` 
