# Gateway REST API

## Table of Contents

- [Gateway REST API](#gateway-rest-api)
  - [Table of Contents](#table-of-contents)
  - [Document History](#document-history)
  - [General Notes](#general-notes)
  - [Board](#board)
    - [Get board by ID](#get-board-by-id)
    - [Get boards by IDs](#get-boards-by-ids)
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
    - [Delete board command by id](#delete-board-command-by-id)

<!-- pagebreak -->

## Document History

| Date       | User           | Note                                            |
| ---------- | -------------- | ----------------------------------------------- |
| 2021-02-15 | Oliver Archner | Comment API POST,PUT,DELETE,GET request support |
| 2021-03-18 | Oliver Archner | User API GET request support                    |
| 2022-05-10 | Oliver Archner | Board command API added                         |
| 2022-06-19 | Oliver Archner | Missing BoardCommand methods documented         |
| 2023-11-06 | Oliver Archner | New BoardRestController.getBoard method added   |
| 2023-11-09 | Oliver Archner | New BoardRestController.getBoards method added  |
| 2023-11-24 | Oliver Archner | BoardResctController enhanced 					|

## General Notes

All example requests use an authorization header with default credentials on localhost. Please adapt these settings according to your BayEOS Gateway situation.

## Board

### Get board by id

```javascript
GET /gateway/rest/board/4 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
{
    "id": 4,
    "domain": null,
    "name": "Wind Vane",
    "channels": [
        {
            "id": 21,
            "nr": "1",
            "name": "WindDirection",
            "phenomena": "Wind Direction",
            "lastResultTime": "2023-11-20T14:05:25.057+00:00",
            "lastResultValue": 1.0,
            "unit": "Degree",
            "dbSeriesId": null
        },
        {
            "id": 20,
            "nr": "2",
            "name": "Speed",
            "phenomena": "Wind Speed",
            "lastResultTime": "2023-11-20T14:05:25.057+00:00",
            "lastResultValue": 2.0,
            "unit": "Meter per second",
            "dbSeriesId": 12345678
        }
    ],
    "origin": "ME",
    "dbFolderId": null,
    "domainId": null
}

```

### Get boards by ids

```javascript
GET /gateway/rest/boards?id=2&id=4 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=

Response Body:
[
    {
        "id": 2,
        "domain": null,
        "name": null,
        "channels": [
            {
                "id": 10,
                "nr": "millis",
                "name": null,
                "phenomena": null,
                "lastResultTime": "2023-11-24T11:53:12.274+00:00",
                "lastResultValue": 1.0,
                "unit": null,
                "dbSeriesId": null
            },
            {
                "id": 2,
                "nr": "records",
                "name": null,
                "phenomena": null,
                "lastResultTime": "2023-11-24T11:53:12.274+00:00",
                "lastResultValue": 0.0,
                "unit": null,
                "dbSeriesId": null
            },
            {
                "id": 4,
                "nr": "exit",
                "name": null,
                "phenomena": null,
                "lastResultTime": "2023-11-24T11:53:12.274+00:00",
                "lastResultValue": -1.0,
                "unit": null,
                "dbSeriesId": null
            }
        ],
        "origin": "$SYS/ExportThread",
        "dbFolderId": null,
        "domainId": null
    },
    {
        "id": 4,
        "domain": null,
        "name": "Wind Vane",
        "channels": [
            {
                "id": 21,
                "nr": "1",
                "name": "WindDirection",
                "phenomena": "Wind Direction",
                "lastResultTime": "2023-11-20T14:05:25.057+00:00",
                "lastResultValue": 1.0,
                "unit": "Degree",
                "dbSeriesId": null
            },
            {
                "id": 20,
                "nr": "2",
                "name": "Speed",
                "phenomena": "Wind Speed",
                "lastResultTime": "2023-11-20T14:05:25.057+00:00",
                "lastResultValue": 2.0,
                "unit": "Meter per second",
                "dbSeriesId": 12345678
            }
        ],
        "origin": "ME",
        "dbFolderId": null,
        "domainId": null
    }
]
```

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

### Delete board command by id

```javascript
DELETE /gateway/rest/boardcommand/13 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
Response Code: 200
```
