
try {
  host = "localhost"
  user = "admin"
  password = "xbee"  
  url = new URL ("http://${host}/gateway/frame/saveFlat")	
  con = url.openConnection()
  con.setRequestMethod("POST")	
  header = "Basic " + (user + ":" + password).bytes.encodeBase64().toString()
  con.setRequestProperty("Authorization", header)	
  data = "sender=TEST&bayeosframes[]=BEp1c3QgYSBtZXNzYWdlIQ"		
  con.doOutput = true		
  Writer wr = new OutputStreamWriter(con.outputStream)
  wr.write(data)
  wr.flush()
  wr.close()		
  con.connect()
  println(con.content.text)

} catch (Exception e) {
	println(e.getMessage())
	System.exit(3)
}
