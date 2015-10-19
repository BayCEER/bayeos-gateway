<!doctype html>
<html>
 <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	 	<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title><g:layoutTitle/></title>
	 	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	 	
	 	<!--  <asset:stylesheet src="application.css"/>  -->	 	
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'bootstrap.min.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'bootstrap-switch.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'bootstrap-theme.min.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'buttons.bootstrap.min.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'buttons.dataTables.min.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'dataTables.bootstrap.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'main.css')}">
	 	<link rel="stylesheet"	href="${resource(dir:'stylesheets',file:'responsive.bootstrap.min.css')}">
	 	 	
	 	
	 	<!--   <asset:javascript src="application.js"/> -->
	 	<script src="${resource(dir:'javascripts',file:'application.js')}"></script>
	 	<script src="${resource(dir:'javascripts',file:'jquery-2.1.3.js')}"></script>		 	
		<script src="${resource(dir:'javascripts',file:'bootstrap.min.js')}"></script>
		<script src="${resource(dir:'javascripts',file:'bootstrap-switch.min.js')}"></script>
		
		<script src="${resource(dir:'javascripts',file:'jquery.dataTables.js')}"></script>
		<script src="${resource(dir:'javascripts',file:'dataTables.bootstrap.js')}"></script>
		<script src="${resource(dir:'javascripts',file:'dataTables.responsive.min.js')}"></script>
		
	 	
	 	
	    <!--   <asset:link rel="shortcut icon" href="gateway.png" type="image/png" /> -->
	    <link rel="shortcut icon" href="${resource(dir:'images',file:'gateway.png')}" type="image/png" />	    
		<g:layoutHead/>		
</head>
<body>
		<g:render template="/layouts/navigation" />
		<div class="container">
		<g:if test="${flash.message}">
			<div class="container">
				<bootstrap:alert class="alert-${flash.level ?: 'info'}">${flash.message}</bootstrap:alert>
			</div>
		</g:if>
		<g:layoutBody/>
		</div>
		<g:render template="/layouts/footer" />
</body>
</html>