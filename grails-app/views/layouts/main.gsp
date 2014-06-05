<!DOCTYPE html>
<html>
 <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title><g:layoutTitle/></title>
		<g:javascript library="jquery" plugin="jquery" />
		<g:layoutHead/>		
		<r:require modules="bootstrap" />
		<link rel="stylesheet"	href="${resource(dir:'css',file:'bootstrap-switch.css')}">
		<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
		<link rel="shortcut icon" href="${resource(dir:'images',file:'gateway.png')}" type="image/png" />				
		<r:layoutResources />
		<script src="${resource(dir:'js',file:'bootstrap-switch.js')}"></script>
</head>
<body>
	<div id="wrap">
		<g:render template="/layouts/navigation" />
		<g:if test="${flash.message}">
			<div class="container">
				<bootstrap:alert class="alert-${flash.level ?: 'info'}">${flash.message}</bootstrap:alert>				
			</div>
		</g:if>
		<g:layoutBody />
		<r:layoutResources />
	</div>
	<g:render template="/layouts/footer" />
</body>
</html>