package gateway

class LogFile {
	
	public static String getFileContents(String fileName, int maxLines) {
								
		File file = new File(fileName)
		if( file.exists() ){			
			List lines = file.readLines()
			
			if (lines.size>maxLines) {
				int si = lines.size() - maxLines
				lines = lines[-1..si]
			} else {
			    lines = lines[-1..0]
			}
			
			
			return lines.join('\r')
		} else {
			return file.absolutePath + " not found."
		}
	}
	
	 

}
