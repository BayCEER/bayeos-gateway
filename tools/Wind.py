"""Creates an example writer and sender using threads."""
from time import sleep
from bayeosgatewayclient import BayEOSWriter, BayEOSSender
import logging
import tempfile
from os import path
import csv

from datetime import datetime


logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s:%(message)s")                

NAME = 'Wind5'
# URL = 'http://localhost:5533/gateway/frame/saveFlat'
URL = 'http://develop/gateway/frame/saveFlat'
USER= 'root'
PASSWORD = 'bayeos'
PATH = path.join(tempfile.gettempdir(),'bayeos-device')
BACKUP_PATH =  path.join(tempfile.gettempdir(),'bayeos-device-backup')

writer = BayEOSWriter(PATH,max_time=5,log_level=logging.INFO)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL,backup_path=BACKUP_PATH,log_level=logging.INFO,user=USER,password=PASSWORD)
sender.start()

with open ("Wind5.csv") as f:
    d = csv.DictReader(f,delimiter=';')    
    for row in d:        
        values = {k:float(v) for k,v in row.items() if k != 'date'}        
        writer.save(values,timestamp=datetime.fromisoformat(row['date']).timestamp())                       
writer.flush()  
sleep(10)