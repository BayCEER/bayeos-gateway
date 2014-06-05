
try {

  host = "localhost:8080"
  user = "admin"
  password = "xbee"
  
  url = new URL ("http://${host}/bayeos-gateway/frame/saveMatrix")		
  con = url.openConnection()
  con.setRequestMethod("POST")	
  header = "Basic " + (user + ":" + password).bytes.encodeBase64().toString()
  con.setRequestProperty("Authorization", header)	
  data = "sender=MATRIX&bayeosframes[]=ASIBAAAAAgAAAAMAAAAEAAAA"		
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
