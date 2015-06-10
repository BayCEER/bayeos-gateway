<html>
<head>
<meta name="layout" content="main" />
<title>Upload</title>
</head>
<body>
	<div class="container">
		<div class="block">
			<div class="block-header">Data Upload</div>
			<g:uploadForm action="save" class="form" role="form">
				<div class="form-group">
					<label><span style="color: #f00;">* </span>Board Origin:</label>
					<g:remoteField value="${board?.origin}" class="form-control" name="board_origin" url="[controller:'board',action:'findByOrigin']"/>	          
        </div>
				<div class="form-group">
					<label class="control-label"><span style="color: #f00;">*
					</span>File Format:</label>
					<g:select class="form-control" name="format_name"
						from="${formatNames}" />
				</div>
				<div class="form-group">
					<label>Zipped:</label>
					<g:checkBox name="zipped" />
				</div>
				<div class="form-group">
					<label><span style="color: #f00;">* </span>File:</label> <input
						type="file" name="file" />
				</div>
				<div class="form-group block-action">
					<button type="submit" class="btn btn-primary">
						<span class="glyphicon glyphicon-ok"></span> <g:message code="default.button.ok.label" default="Ok" />
					</button>
				</div>
								
			</g:uploadForm>
		</div>
	</div>
</body>
</html>
