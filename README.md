# BayEOS Gateway
A sensor gateway to receive, check and aggregate observation data 

Main characteristics:
- Import service for observation data (BayEOS Frames over HTTP)
- Configuration of boards, channels, checks, interpolations and aggregates
- Observation charts 
- Export service to [BayEOS Server}(BayCEER/bayeos-server)

## Getting Started
### Prerequisites
You need a Debian/Ubuntu Linux Server to run the Gateway. We recommend a minimum of 2GB RAM.

### Installing
- Import the repository key `wget -O - http://www.bayceer.uni-bayreuth.de/repos/apt/conf/bayceer_repo.gpg.key |apt-key add -`
- Add the following repositories to /etc/apt/sources.list `deb http://www.bayceer.uni-bayreuth.de/repos/apt/debian stretch main`
- Update your repository cache `apt-get update`
- Install the package `apt-get install bayeos-gateway`

### Configuration
- Open the URL  `http://<hostname>/gateway` and log in as user 'root' with password 'bayeos'
- Open the administration menu and change the default root password
- Activate the import user

### Sample Data Producer
- Install the bayeosgatewayclient
- Run the follwing python code
```python
from bayeosgatewayclient import BayEOSWriter, BayEOSSender

PATH = '/tmp/bayeosgateway/'
NAME = 'MyFirst Board'
URL = 'http://<host>/gateway/frame/saveFlat'

writer = BayEOSWriter(PATH)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL)
sender.start() 	# sender runs in a concurrent thread

while True:
    print 'adding frame'
    writer.save([2.1, 3, 20.5])
    sleep(5)
```
- Open the gateway web interface `http://<hostname>/gateway` to identify your first board  
- Refine the sample to send real world sensor data according to your needs 
- Set the board name and channel names 
- Turn the board export option on to export the observations


## Authors 
* **Oliver Archner** - *Programmer* - [University of Bayreuth](https://www.bayceer.uni-bayreuth.de)
* **Dr. Stefan Holzheu** - *Project lead* - [University of Bayreuth](https://www.bayceer.uni-bayreuth.de)

## History
### Version 2.2.20, March 6, 2018
- Enhanced usabilitity in main board list 
- Fixed incomplete aggreagation definition bug

### Version 2.1, March 30, 2017
- Virtual channels: user defined functions to calculate new channels based on exisiting values 
- JRE 1.8

### Version 2.0, March 1, 2017
- Multilanguage support 
- Browser time zones 
- Grafana interface 
- First release on JRE 1.7
- Embedded web server
- Migration to Spring 4.0

### Version 1.9.36, Dec 1, 2017
- Final Grails version

## License
GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1, February 1999





