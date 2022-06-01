"""Creates an example writer and sender using threads."""
from time import sleep
from bayeosgatewayclient import BayEOSWriter, BayEOSSender
import logging
import tempfile
from os import path


logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s:%(message)s")                

NAME = 'FramePump'
URL = 'http://localhost:5533/frame/saveFlat'
USER= 'root'
PASSWORD = 'bayeos'
PATH = path.join(tempfile.gettempdir(),'bayeos-device')
BACKUP_PATH =  path.join(tempfile.gettempdir(),'bayeos-device-backup')

writer = BayEOSWriter(PATH,max_time=10,log_level=logging.DEBUG)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL,backup_path=BACKUP_PATH,log_level=logging.DEBUG,user=USER,password=PASSWORD)
sender.start()

nr=0
while True:
    writer.save([nr, 3, 20.5])
    writer.flush()
    nr+=1
    sleep(1)