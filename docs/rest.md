# All channel values during the last 24h sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4'
[{"id": 635,"millis": 1562678971850,"value": 0.59041363},{"id": 640,"millis": 1562678992717,"value": 0.57821494},{"id": 645,"millis": 1562679012765,"value": 0.84613395}]

# All channel values > lastRowId sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4&lastRowId=587'
[{"id": 588, "mill's": 1562588481171, "value": 0.1756716},…]

# All channel values during a time period sorted by time ascending
curl -i -X GET -H "Authorization:Basic cm9vdDpiYXllb3M=" 'http://localhost:5533/gateway/rest/channel/?id=4&startTime=1552588481171&endTime=1562588481171'
[{"id": 576, "millis": 1562066857108, "value": 0.49066883},…]