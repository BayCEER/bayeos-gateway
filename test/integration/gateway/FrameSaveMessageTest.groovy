import java.nio.ByteBuffer
import java.util.prefs.Base64


host = "localhost:8080"
user = "admin"
password = "xbee"
sender = "Foc1"
message = "This is a test message"

   
for (type in [0x4, 0x5]){           
      System.out.println("Sending message ${type}")
      
      url = new URL ("http://${host}/bayeos-gateway/frame/saveFlat")
      con = url.openConnection()
      con.setRequestMethod("POST")
      header = "Basic " + (user + ":" + password).bytes.encodeBase64().toString()
      con.setRequestProperty("Authorization", header)
      con.doOutput = true

      ByteBuffer bf = ByteBuffer.allocate(message.length() + 1)
      bf.put((byte)type);
      bf.put(message.getBytes());    
      msg = Base64.byteArrayToBase64(bf.array());        
            
      data = "sender=${sender}&bayeosframes[]=${msg}"
      Writer wr = new OutputStreamWriter(con.outputStream)
      wr.write(data)
      wr.flush()            
      println(con.content.text)
      wr.close()
}
  


