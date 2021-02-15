# CommentREST API 
__2021-02-15, Oliver Archner, Basic POST,PUT,DELETE,GET request support__

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