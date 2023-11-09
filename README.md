# BayEOS Gateway Influx

A sensor gateway to receive, check, aggregate and export observation data to InfluxDB

Main characteristics:

- Import service for observation data ([BayEOS Frames](https://www.bayceer.uni-bayreuth.de/bayeos/frames) over HTTP)
- Completeness and value checks
- Interpolation and aggregation of observation data
- Live charts of measured values
- Export service to [InfluxDB](https://docs.influxdata.com/influxdb/v2/)

## Getting Started

### Prerequisites

- Debian 11 (bullseye)
- Root login
- [InfluxDB v2](https://docs.influxdata.com/influxdb/v2/)

### Installing

- Login as root
- Install basic tools for installation  
  `apt-get update`  
  `apt-get install wget gnupg lsb-release`
- Import the repository key  
  `wget -O - http://www.bayceer.uni-bayreuth.de/repos/apt/conf/bayceer_repo.gpg.key |apt-key add -`
- Add the BayCEER Debian repository  
  `echo "deb http://www.bayceer.uni-bayreuth.de/repos/apt/debian $(lsb_release -c -s) main" | tee /etc/apt/sources.list.d/bayceer.list`
- Update your repository cache  
  `apt-get update`
- Install the package  
  `apt-get install bayeos-gateway-influx`

### Configuration

- Open the URL `http://localhost/gateway-influx` and log in as user 'root' with password 'bayeos'
- Open the administration menu and change the default root password


## Authors

- **Dr. Stefan Holzheu** - _Project lead_ - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)
- **Oliver Archner** - _Developer_ - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)

## History
### Version 1.0.1, November 2023
- Fix: Wrong path in notification and file import messages 
- Fix: Dropped dependencies to bayeos-password and bayeos-xmlrpc

### Version 1.0.0, November 2023
- Initial Version with InfluxDB export 

## License

GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1, February 1999
