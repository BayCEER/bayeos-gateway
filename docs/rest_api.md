# Gateway REST API

## Table of Contents

- [Gateway REST API](#gateway-rest-api)
  - [Table of Contents](#table-of-contents)
  - [Document History](#document-history)
  - [General Notes](#general-notes)
  - [Board Comment](#board-comment)
    - [Create board comment](#create-board-comment)
    - [Update board comment](#update-board-comment)
    - [Delete comment](#delete-comment)
    - [Get all board comments](#get-all-board-comments)
  - [User](#user)
    - [Get user by ID](#get-user-by-id)
    - [Get all users sorted by name](#get-all-users-sorted-by-name)
    - [Get all domain users sorted by name](#get-all-domain-users-sorted-by-name)
  - [Channel](#channel)
    - [Get channel values during the last 24h sorted by time ascending](#get-channel-values-during-the-last-24h-sorted-by-time-ascending)
    - [Get channel values > lastRowId sorted by time ascending](#get-channel-values-gt-lastrowid-sorted-by-time-ascending)
    - [Get channel values during a time period sorted by time ascending](#get-channel-values-during-a-time-period-sorted-by-time-ascending)
  - [Board command](#board-command)
    - [Create a new board command](#create-a-new-board-command)
    - [Find first pending board command by board origin](#find-first-pending-board-command-by-board-origin)
    - [Find all board commands by board origin](#find-all-board-commands-by-board-origin)
    - [Find board command by id](#find-board-command-by-id)
    - [Update board command response](#update-board-command-response)
    - [Delete board command by id](#delete-board-command-by-id)

<!-- pagebreak -->

## Document History

| Date       | User           | Note                                            |
| ---------- | -------------- | ----------------------------------------------- |
| 2021-02-15 | Oliver Archner | Comment API POST,PUT,DELETE,GET request support |
| 2021-03-18 | Oliver Archner | User API GET request support                    |
| 2022-05-10 | Oliver Archner | Board command API added                         |
| 2022-06-19 | Oliver Archner | Missing BoardCommand methods documented         |

## General Notes

All example requests use an authorization header with default credentials on localhost. Please adapt these settings according to your BayEOS Gateway situation.

## Board Comment

### Create board comment

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

### Update board comment

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

### Delete comment

```javascript
DELETE /gateway/rest/comments/52 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
```

### Get all board comments

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

## User

### Get user by ID

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

### Get all users sorted by name

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

### Get all domain users sorted by name

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

## Channel

### Get channel values during the last 24h sorted by time ascending

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

### Get channel values gt lastRowId sorted by time ascending

```javascript
GET /gateway/rest/channel?id=4&lastRowId=587 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[{"id": 588, "millis": 1562588481171, "value": 0.1756716},…]
```

### Get channel values during a time period sorted by time ascending

```javascript
GET /gateway/rest/channel/?id=4&startTime=1552588481171&endTime=1562588481171 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[{"id": 588, "mill's": 1562588481171, "value": 0.1756716},…]
```

## Board command

### Create a new board command

```javascript
POST /gateway/rest/boardcommand HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json
{
    "origin": "ME",
    "value": "adfasdfasf",
    "description": "Toggle A",
    "kind": 2
}
Response Code: 200
Response Body:
{
    "id": 4,
    "origin": "ME",
    "value": "adfasdfasf",
    "kind": 2,
    "description": "Toggle A",
    "response": null,
    "responseStatus": null,
    "insertTime": "2022-09-28T13:20:15.176+00:00",
    "responseTime": null
}
```

### Find first pending board command by board origin

```javascript
GET /gateway/rest/boardcommand?origin=ME HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json

Response Code: 200
Response Body:
{
    "id": 4,
    "origin": "ME",
    "value": "adfasdfasf",
    "kind": 2,
    "description": "Toggle A",
    "response": null,
    "responseStatus": null,
    "insertTime": "2022-09-28T13:20:15.176+00:00",
    "responseTime": null
}
```

### Find all board commands by board origin

```javascript
GET /gateway/rest/boardcommands?origin=ME HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json

Response Code: 200
Response Body:
[
    {
        "id": 2,
        "origin": "ME",
        "value": "1",
        "kind": 1,
        "description": "PIN 1 HIGH",
        "response": "[]",
        "responseStatus": 1,
        "insertTime": "2022-09-28T13:08:53.657+00:00",
        "responseTime": "2022-09-28T13:08:56.118+00:00"
    },
    {
        "id": 4,
        "origin": "ME",
        "value": "adfasdfasf",
        "kind": 2,
        "description": "Toggle A",
        "response": null,
        "responseStatus": null,
        "insertTime": "2022-09-28T13:20:15.176+00:00",
        "responseTime": null
    }
]
```

### Find board command by id

```javascript
GET /gateway/rest/boardcommand/1 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json

Response Code: 200
Response Body:
{
    "id": 1,
    "origin": "DESKTOP-VC82HHI",
    "value": "1",
    "kind": 4,
    "description": "PIN 4 HIGH",
    "response": null,
    "responseStatus": null,
    "insertTime": "2022-09-27T15:21:58.886+00:00",
    "responseTime": null
}
```

### Update board command response

```javascript
PATCH /gateway/rest/boardcommand/1/response HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Content-Type: application/json
{
    "response": "asjfklfsdjiofjids",
    "responseStatus" : 0    
}
Response Code: 200
Response Body: {
    "id": 1,
    "origin": "DESKTOP-VC82HHI",
    "value": "1",
    "kind": 4,
    "description": "PIN 4 HIGH",
    "response": "asjfklfsdjiofjids",
    "responseStatus": 0,
    "insertTime": "2022-09-27T15:21:58.886+00:00",
    "responseTime": "2022-09-28T13:39:43.858+00:00"
}
```

### Delete board command by id

```javascript
DELETE /gateway/rest/boardcommand/13 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Response Code: 200
```
