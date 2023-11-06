"""Creates an example writer and sender using threads."""
from time import sleep
from bayeosgatewayclient import BayEOSWriter, BayEOSSender
import logging
import tempfile
from os import path
import math 


logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s:%(message)s")                

NAME = 'Modem1'
URL = 'http://localhost:5535/gateway-influx/frame/saveFlat'
USER= 'root'
PASSWORD = 'bayeos'
PATH = path.join(tempfile.gettempdir(),'bayeos-device')
BACKUP_PATH =  path.join(tempfile.gettempdir(),'bayeos-device-backup')

writer = BayEOSWriter(PATH,max_time=5,log_level=logging.INFO)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL,backup_path=BACKUP_PATH,log_level=logging.INFO,user=USER,password=PASSWORD)
sender.start()

theta=0
u = 2
while True:
    writer.save({"theta":theta,"u":u},origin="Modem1/WindVane/P0")        
    theta+=10    
    if theta > 360:
        theta = 0
    sleep(10)