from bayeosgatewayclient import BayEOSWriter, BayEOSSender
from time import sleep
from os import path
import tempfile
import psutil

PATH = path.join(tempfile.gettempdir(),'bayeos') 
NAME = 'MyFirstBoard'
URL = 'http://localhost/gateway/frame/saveFlat'

writer = BayEOSWriter(PATH)
writer.save_msg('Writer was started.')

sender = BayEOSSender(PATH, NAME, URL)
sender.start() 	

while True:
    print 'Adding frame'
    writer.save(psutil.cpu_percent(percpu=True))
    sleep(5)