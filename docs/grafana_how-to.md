# Grafana Howto
This is a short instruction on how to use BayEOS Gateway observation data in Grafana. This combination allows you to produce all types of good looking dashboards based on your own live data.

## Requirements
- Debian/Ubuntu machine with root privileges
- [BayEOS Gateway](https://github.com/BayCEER/bayeos-gateway)

## Installation 
1. Grafana Server 
Please follow the installation instruction on the [Grafana page](https://grafana.com/) to install the latest grafana server on your machine.
1. Grafana JSON Plugin
    - The Grafana server communicates with the BayEOS Gateway over a [JSON based REST interface](https://github.com/BayCEER/bayeos-gateway/blob/master/docs/grafana_endpoint.md). Please go to the grafana plugin site and install the latest [SimpleJson data source plugin](https://grafana.com/plugins/grafana-simple-json-datasource).
    - Create a new user with role IMPORT on the gateway.
1. Gateway Datasource
    - On a running grafana server login as admin user
    - Create a new SimpleJson data source with your new user:
    ![grafana_datasource](./grafana_datasource.png)

## Dashbord Example
