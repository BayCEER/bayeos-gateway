import frame.channel.DataFrame;
import frame.channel.IndexFrame
import frame.wrapped.DelayedFrame;
import frame.wrapped.MillisecondTimestampFrame;
import frame.BayEOS
import org.apache.commons.codec.binary.Base64

int FRAME_COUNT = 10;

try {
  host = "bayeos-master"
  user = "admin"
  password = "xbee"  
  url = new URL ("http://${host}/gateway/frame/saveFlat")	
  con = url.openConnection()
  con.setRequestMethod("POST")	
  header = "Basic " + (user + ":" + password).bytes.encodeBase64().toString()
  con.setRequestProperty("Authorization", header)
  
    StringBuffer data = new StringBuffer("sender=TEST")	  	  	  
	for(short i=1;i<=10;i++){				
		IndexFrame<Short> f = new IndexFrame<Float>().putAll(1,2,3);
		DelayedFrame df = new DelayedFrame(i*10000, f.getBytes(BayEOS.Number.UInt8))
		data.append("&bayeosframes[]=")
		data.append(new String(Base64.encodeBase64String(df.getBytes())))	  
	}
	    		
  con.doOutput = true		
  Writer wr = new OutputStreamWriter(con.outputStream)
  wr.write(data.toString())
  wr.flush()
  wr.close()		
  con.connect()
  println(con.content.text)

} catch (Exception e) {
	println(e.getMessage())
	System.exit(3)
}
