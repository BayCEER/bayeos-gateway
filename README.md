﻿# ![BayEOS Gateway](docs/gateway_logo_medium.png)
A sensor gateway to receive, check and aggregate observation data

Main characteristics:
- Import service for observation data ([BayEOS Frames](https://www.bayceer.uni-bayreuth.de/bayeos/frames) over HTTP)
- Completeness and value checks  
- Interpolation and aggregation of observation data 
- Live charts of measured values 
- Export service to [BayEOS Server](https://github.com/BayCEER/bayeos-server)

## Getting Started
### Prerequisites
- Debian Linux with a minimum of 2GB RAM
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
- Set the board name 
- Set the channel names and units 
- Configure 10min average aggregation on each channel
- Turn the board auto export option on 
- Access the board data on a [BayEOS Server](https://github.com/BayCEER/bayeos-server)
 
## Authors 
* **Dr. Stefan Holzheu** - *Project lead* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)
* **Oliver Archner** - *Developer* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)

## History

### Version 2.6.1, March, 2020
- Import feedback by e-mail 
- Enhanced upload list display

### Version 2.6.0, March, 2020
- Binary Logger file uploads 

### Version 2.5.2, Dec, 2019
- Fixed: Default binding on localhost only 
- Fixed: Channel path view 

### Version 2.5.1, Sep, 2019
- Fixed wrong column name in view nagios_status

### Version 2.5.0, July, 2019
- State calculation and alerting for devices and device groups
- DelayedSecondFrame support 
- Enhanced login dialog with domain drop down
- REST Interface for channel observations 
- [Fixed: Incomplete spline function crashes export job](https://github.com/BayCEER/bayeos-gateway/issues/49)

### Version 2.4.10, April 15, 2019
- [Fixed:  Message not shown after board has been moved to a new domainissue](https://github.com/BayCEER/bayeos-gateway/issues/51) 

### Version 2.4.9, December 13, 2018
- Enhanced metrics with board, domain and user counts and calculated frames/h

### Version 2.4.8, November 28, 2018
- [Fixed: Message not shown in GUI bug](https://github.com/BayCEER/bayeos-gateway/issues/50)
  
### Version 2.4.7, November 22, 2018
- Improved DeleteJob performance
- LDAP: User search 
- Grafana search and query for non domain users 

### Version 2.4.5, November 13, 2018
- New attributes for users    
- LDAP: Fetch empty user attributes
- LDAP: Optional user password

### Version 2.4.3, November 11, 2018
- Report metrics data to redis
- Linux like configuration in /etc/bayeos-gateway/application.properties 
- Constant values in virtual channel functions 
- Fixed performance bug in group and device list
 
### Version 2.4.0, October 17, 2018
- [Multitenancy based on domains](https://github.com/BayCEER/bayeos-gateway/issues/34)
- [LDAP Authentication](https://github.com/BayCEER/bayeos-gateway/issues/44)

### Version 2.3.0, July 30, 2018
- [Enhanced sync of objects between gateway and server](https://github.com/BayCEER/bayeos-gateway/issues/33)
- [Exclude critical values from export](https://github.com/BayCEER/bayeos-gateway/issues/27)
- Create NaN values for out of range numeric spline values
- Internal configuration table for export job dropped

### Version 2.2.21, March 20, 2018
- Fixed delete_obs() procedure bug caused by wrong transaction isolation level

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

### Version 1.8.0, 2015
- [Responsive design](https://getbootstrap.com/) for mobile devices 
- Debian package

### Version 1.0, 2012
- Initial release 

## License
GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1, February 1999

