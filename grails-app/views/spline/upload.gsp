<html>
<head>
<meta name="layout" content="main" />
</head>
<body>
	<div class="container">
		<div class="block">
		<div class="block-header">
			Spline Upload 
		</div>		  
		<g:uploadForm role="form" action="uploadFile">
			<div class="form-group">			
				<label for="payload">File:</label></td>
				<input type="file" id="payload" name="payload"/>			
			<div class="form-group block-action">
				<g:submitButton name="upload" class="btn btn-primary" value="Upload" />
			</div>
			</div>
		</g:uploadForm>
		</div>
	</div>
</body>
</html>