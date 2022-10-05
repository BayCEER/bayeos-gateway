"""Creates an example writer and sender using threads."""
from time import sleep
from bayeosgatewayclient import BayEOSWriter, BayEOSSender
import logging
import tempfile
from os import path
import math 


logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s:%(message)s")                

NAME = 'FramePump'
URL = 'http://localhost:5533/gateway/frame/saveFlat'
USER= 'root'
PASSWORD = 'bayeos'
PATH = path.join(tempfile.gettempdir(),'bayeos-device')
BACKUP_PATH =  path.join(tempfile.gettempdir(),'bayeos-device-backup')

writer = BayEOSWriter(PATH,max_time=5,log_level=logging.INFO)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL,backup_path=BACKUP_PATH,log_level=logging.INFO,user=USER,password=PASSWORD)
sender.start()

nr=0
angle = 0
while True:
    writer.save([nr,angle ,10*math.sin(angle)])    
    nr+=1
    angle+=0.1
    if angle > 360:
        angle = 0
    sleep(0.5)