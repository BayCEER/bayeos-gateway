<html>
<head>
<title>Add boards</title>
<meta name="layout" content="main" />
</head>
<body>
	<bootstrap:alert class="alert-info">Please click on a row to add a board to the group: ${boardGroupInstance.name}</bootstrap:alert>
	
	<g:form class="form" id="form" role="form" action="addToGroup" id="${boardGroupInstance.id}">
			
				<table id="table" class="table">
					<thead>
						<tr>
							<th>Id</th>
							<th>Name</th>
							<th>Origin</th>
							<th>Last Result Time</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${gateway.Board.findAllByBoardGroupIsNull()}" var="b">
							<tr>
								<td>
									${b.id}
								</td>
								<td>
									${(b.name==null)?'Not set':b.name}
								</td>
								<td>
									${b.origin}
								</td>
								<td><g:formatDate date="${b.lastResultTime}" format="dd.MM.yyyy HH:mm:ss z"/></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			

			

			<button type="submit" id="submit" class="btn btn-primary">
				<span class="glyphicon glyphicon-save"></span>
				<g:message code="default.button.save.label" default="Save" />
			</button>							
					
			<g:hiddenField name="boards" id="boards" value=""/>

   </g:form>

		<script type="text/javascript" language="javascript" class="init">
			$(document).ready(function() {
				var dt = $('#table').DataTable({
					"lengthChange" : false,
					"info" : false,
					"searching" : true,

					"columnDefs" : [ 
					{	"targets" : 0,
						"visible" : false,
						"searchable" : false
					}					 
					]
				});

				$('#table tbody').on('click', 'tr', function() {
					$(this).toggleClass('selected');
				});

				$('#submit').click( function() {
					var ids = [];
					dt.rows('.selected').data().each( function(row) {
						ids.push(row[0])
					});									
					$('#boards').val(ids)
				});
				

			});
		</script>
</body>
</html>