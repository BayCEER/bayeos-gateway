# CREST API 
__2021-02-15, Oliver Archner, Comment Basic POST,PUT,DELETE,GET request support__
__2021-03-18, Oliver Archner, User GET request support__

All example requests use an authorization header with default credentials on localhost. 
Please adapt these settings according to your BayEOS Gateway situation.

## Create Board Comment
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

## Update Board Comment
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

## Delete Comment
```javascript
DELETE /gateway/rest/comments/52 HTTP/1.1
Host: localhost:5533
Accept: application/json
Authorization: Basic cm9vdDpiYXllb3M=
```

## Get All Board Comments
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

## Get User By ID
```javascript
GET /gateway/rest/users/4 HTTP/1.1
Host: localhost:5533
Accept: application/json

Response Body:
{
    "id": 4,
    "name": "oliver",
    "fullName": "Oliver Archner",
    "domain": "REZ"
}
```

## Get All Users Sorted by Name
```javascript
GET /gateway/rest/users HTTP/1.1
Host: localhost:5533
Accept: application/json
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

## Get channel values during the last 24h sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4'
[{"id": 635,"millis": 1562678971850,"value": 0.59041363},{"id": 640,"millis": 1562678992717,"value": 0.57821494},{"id": 645,"millis": 1562679012765,"value": 0.84613395}]

## Get channel values > lastRowId sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4&lastRowId=587'
[{"id": 588, "mill's": 1562588481171, "value": 0.1756716},…]

## Get channel values during a time period sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4&startTime=1552588481171&endTime=1562588481171'
[{"id": 576, "millis": 1562066857108, "value": 0.49066883},…]