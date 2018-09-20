# REST Endpoint for Grafana

This API allows you to access observation data in [Grafana Server](https://grafana.com/) by using the Grafana [SimpleJson data source plugin](https://grafana.com/plugins/grafana-simple-json-datasource) or by your own HTTP client implementation. A typical use case would be  the display of observation data on a web page.
## Common Properties
**Main context**
The controller listens on the following context path:    ```https://<host>:<port>/gateway/grafana```   

**Authentication**
Every request must include an [authorization header](https://tools.ietf.org/html/rfc2617) composed of a valid gateway username and password. The user must be in role ADMIN,USER or IMPORT to query the endpoint. A sample header entry for user root with password 'bayeos' looks like this: ```Authorization: Basic cm9vdDpiYXllb3M=```

**Content type**        
The only accepted content type is JSON encoded in UTF-8. Please add the following information to every request header: ```Content-Type: application/json;charset=UTF-8```

## Connection test
**GET /**  
**Response**   
status-code: 200  
message-body: {\"msg\":\"This is a grafana Endpoint.\"}

## Search
Probably the first statement a client sends to the gateway. The returned path information can be used to [query](#query) the gateway. 

**POST /search**        
message-body: ```{}```  
**Sample response**   
status-code: 200  
message-body:
```javascript
[
"MyBoard/1",
"MyBoard/2",
"MyBoard/3",
"MyBoard/4",
"MyBoard/5"
]
```
       
The response body contains a list of channel paths. A path is constructed as follows:
    ```COALESCE(board.name, board.origin) || '/') || COALESCE(channel.name,channel.nr)```
    
## Query
Retrieve observation values as a list of value/timestamp pairs for each channel. The time range and interval information is used to filter records.

**POST /query**        
message-body:   
```javascript
{
    "targets": [
        { "target": "MyBoard/1" },
        { "target": "MyBoard/2" }
    ],
    "interval" : "1h",
    "range": {
        "from": "2018-09-19T06:33:44Z",
        "to": "2018-09-19T12:33:44Z"
    }
}
```
**Properties**
* _targets_: array of targets
* _target:_ channel path as returned by [search request](#search)
* _interval:_ aggregation interval in [PostgreSQL format](https://www.postgresql.org/docs). Quantity is a number (possibly signed). Unit is microsecond, millisecond, second, minute, hour, day, week, month, year, decade, century, millennium, or abbreviations or plurals of these units.
* _range_: from and to as ISO datetime values. UTC time is defined with a capital letter Z. If you want to modify the time relative to UTC, remove the Z and add +HH or -HH instead.    

**Sample response**   
status-code: 200  
message-body: 
```javascript
[
  {
    "target": "MyBoard/1",
    "datapoints":[[0.50373834, 1537336800000 ], [0.50892884, 1537340400000 ]]
  },
  {
    "target": "MyBoard/2",
    "datapoints":[[0.49592498, 1537336800000 ], [0.50154513, 1537340400000 ]]
  }
]
```
**Properties**
* _target_: channel path information as returned by [search request](#search)
* _datapoints_: list of value/timestamp (number of milliseconds since January 1, 1970, 00:00:00 GMT) pairs. This function returns raw/not aggregated values, if the specified query interval is less than the channel sampling interval. The average function is used for aggregation if no aggregation function is specified on the channel.





