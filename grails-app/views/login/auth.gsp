<html>
<head>
	<title>Login</title>
	<link rel="stylesheet"	href="${resource(dir:'stylesheets', file:'login.css')}">
	<meta name="layout" content="main" />
</head>
<body>
		<form class="form-signin" action='${postUrl}' method='POST'	id='loginForm' autocomplete='off'>
			<p class="lead strong">Please sign in</p>
			<input id="username" name="j_username" class="form-control"
				type="text" autofocus="" placeholder="Username"> 
			<input id="password" name="j_password" class="form-control" type="password"
				placeholder="Password"> 			
			<button class="btn btn-lg btn-primary btn-block" type="submit"
				value="Login">Sign in</button>
		</form>
</body>
</html>