<nav class="navbar navbar-default">
	<div class="container">		
		<div class="navbar-header">
		<sec:ifLoggedIn>
			<button type="button" class="navbar-toggle" data-toggle="collapse"		data-target=".navbar-ex1-collapse">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
		</sec:ifLoggedIn>
		<g:link class="navbar-brand" controller="board"><strong>BayEOS</strong> Gateway</g:link>			 			
		</div>
		
		<div class="collapse navbar-collapse navbar-ex1-collapse">
		<sec:ifLoggedIn>
			<ul class="nav navbar-nav">
				<li ${controllerName.equals('board') ? 'class="active"' : ''}>
					<g:link controller="board"><span class="glyphicon glyphicon-hdd"></span> Boards</g:link>
				</li>
				<li ${controllerName.equals('boardGroup') ? 'class="active"' : ''}>
					<g:link	controller="boardGroup"><span class="glyphicon glyphicon-flag"></span> Groups</g:link>
				</li>
				<li class="dropdown"><a href="#" class="dropdown-toggle"
					data-toggle="dropdown"><span class="glyphicon glyphicon-wrench"></span>
						Admin<b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li ${controllerName.equals('boardTemplate') ? 'class="active"' : ''}>
							<g:link	controller="boardTemplate"><span class="glyphicon glyphicon-hdd"></span> Board Templates</g:link>
						</li>
						
						<li ${controllerName.equals('user') ? 'class="active"' : ''}>
							<g:link controller="user"><span class="glyphicon glyphicon-user"></span> Users</g:link>
						</li>

						<li class="divider"></li>

						<li class="dropdown-submenu"><a href="#"><span
								class="glyphicon glyphicon-resize-small"></span> Aggregate</a>
							<ul class="dropdown-menu">
								<li ${controllerName.equals('function') ? 'class="active"' : ''}>
									<g:link controller="function"> Functions</g:link>
								</li>
								<li ${controllerName.equals('interval') ? 'class="active"' : ''}>
									<g:link controller="interval"> Intervals</g:link>
								</li>
							</ul></li>
						<li ${controllerName.equals('spline') ? 'class="active"' : ''}>
							<g:link controller="spline"><span class="glyphicon glyphicon-resize-horizontal"></span> Splines</g:link>
						</li>
						<li ${controllerName.equals('unit') ? 'class="active"' : ''}>
							<g:link controller="unit"><span class="glyphicon glyphicon-tag"></span> Units</g:link>
						</li>
						<li class="dropdown-submenu"><a href="#"><span
								class="glyphicon glyphicon-time"></span> Jobs</a>
							<ul class="dropdown-menu">
								<li ${controllerName.equals('exportJob') ? 'class="active"' : ''}>
									<g:link controller="exportJob" action="edit"> Export</g:link>
								</li>
								<li ${controllerName.equals('deleteJob') ? 'class="active"' : ''}>
									<g:link controller="deleteJob" action="edit"> Delete</g:link>
								</li>
							</ul></li>
						<li ${controllerName.equals('fileUpload') ? 'class="active"' : ''}>
							<g:link controller="fileUpload" action="upload">
							<span class="glyphicon glyphicon-upload"></span> Upload</g:link>
						</li>
						<li ${controllerName.equals('file') ? 'class="active"' : ''}>
							<g:link controller="file" action="show" params="[file:'/var/log/tomcat6/gateway.log', title:'Gateway Log']"><span class="glyphicon glyphicon-file"></span>
								Log</g:link>
						</li>

					</ul></li>
			</ul>
		</sec:ifLoggedIn>
			<div class="navbar-right">
				<sec:ifLoggedIn>
					<p class="navbar-text">Signed in as <sec:username/> </p>													
					<g:link controller="logout">
					<button type="button" class="btn btn-default navbar-btn">Logout</button>
					</g:link>
				</sec:ifLoggedIn>							
			</div>
		</div>

	</div>


</nav>