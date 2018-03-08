# ![BayEOS Gateway](docs/gateway_logo_medium.png)
A sensor gateway to receive, check and aggregate observation data

Main characteristics:
- Import service for observation data ([BayEOS Frames](https://www.bayceer.uni-bayreuth.de/bayeos/frames) over HTTP)
- Completeness and value checks 
- Interpolation and aggregation of observation data 
- Live charts of measured values 
- Export service to [BayEOS Server](https://github.com/BayCEER/bayeos-server)

## Getting Started
### Prerequisites
- Debian/Ubuntu Linux Server with a minimum of 2GB RAM is recommended
- Raspian on a Raspberry Pi (Model >=3) is working

### Installing
- Import the repository key  
`wget -O - http://www.bayceer.uni-bayreuth.de/repos/apt/conf/bayceer_repo.gpg.key |apt-key add -`
- Add the following repository to /etc/apt/sources.list  
`deb http://www.bayceer.uni-bayreuth.de/repos/apt/debian stretch main`
- Update your repository cache  
`apt-get update`
- Install the package  
`apt-get install bayeos-gateway`

### Configuration
- Open the URL `http://localhost/gateway` and log in as user 'root' with password 'bayeos'
- Open the administration menu and change the default root password

### My First Data Producer
Let's send the cpu load of our pc to a local gateway:
- Install the [python-bayeosgatewayclient](https://github.com/BayCEER/bayeosgatewayclient) module
- Install the python [psutils](https://pypi.python.org/pypi/psutil) module
- Run [MyFirstBoard.py](docs/MyFirstBoard.py)
- Open `http://localhost/gateway` to identify our new board record

![board list](docs/MyFirstBoard.png)
- Set the board name and the channel names 
- Turn the board export option on
- Configure 10min average aggregation 
- Access the board data on a [BayEOS Server](https://github.com/BayCEER/bayeos-server) 

## Authors 
* **Dr. Stefan Holzheu** - *Project lead* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)
* **Oliver Archner** - *Programmer* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)

## History
### Version 2.2.20, March 6, 2018
- Enhanced usability in board list 
- Fixed incomplete aggregation bug

### Version 2.1, March 30, 2017
- Virtual channels 
- Live board list 

### Version 2.0, March 1, 2017
- Rewrite and migration to Spring 4.0
- Multilanguage support 
- Browser time zones 
- Grafana interface 
- Board groups 
- Embedded web server
- Usability enhancements based on web flows 

### Version 1.9.36, Dec 1, 2017
- Final Grails version

## License
GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1, February 1999





