
<html>
    <head>        
        <meta name="layout" content="main" />
        <title>Log File</title>
    </head>
    
    <body>
    	               	
        <div class="container"> 
        	Level: ${level}            
         	<g:textArea rows="30" class="col-sm-12" cols="80" id="log" name="log" readonly="true">${file.encodeAsHTML()}</g:textArea>         	                                    
        </div>
    </body>
</html>
