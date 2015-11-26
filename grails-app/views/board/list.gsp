
<html>
    <head>
    	<title>Boards</title>            
        <meta name="layout" content="main" />         		                           
    </head>
    <body>
	 <div class="row">
		<div class="col-xs-12">
        <table id="table" class="table table-hover nowrap">
        <thead>        	                        
                <tr>
                   <th class="min-tablet-l">Group</th>
                   <th class="min-tablet-l">Origin</th>
                   <th>Name</th>
                   <th class="min-tablet-l">Rssi</th>
                   <th>Last Result Time</th>
                   <th>Status</th>
                    <th></th>
                   </tr>
                    </thead>
                    <tbody>
                    </tbody>
		</table>
		</div>
	</div>

       
        <script type="text/javascript" language="javascript" class="init">
 		$(document).ready(function(){
 			var dt = $('#table').DataTable( {
 				"responsive": true,
        		"autoWidth": false,
                "processing": true,
                "serverSide": true,
                "lengthChange": false,
                "columns": [
					{data: 'group_name'},
                    {data: 'origin'},
                    {data: 'name'},
                    {data: 'last_rssi'},
                    {data: 'last_result_time'},
                    {data: 'status'}                    
                ],
                "ajax": {
                    "url": '${createLink(action:'listData')}'
                },
                "columnDefs": [
                    // Group Name
                   	{ "targets": 0 , render:
                   		function ( data, type, row ) {   
                   		if (row.group_name != null){
                   			return '<a href="${createLink(controller:'boardGroup', action:'edit')}/' + row.group_id +'">' + row.group_name + '</>' ;
                        }  else {
							return "";
                        }       			                		
            		}
                    },
					                                                       
                    // Origin
            		{ "targets": 1 , "render":
            			function ( data, type, row ) {            			
                    		return '<a href="${createLink(action:'edit')}/' + row.id +'">' + row.origin + '</>' ;
                		}
            		},
            		// Name
            		{ "targets": 2 , "render":
            			function ( data, type, row ) {
            			    if (row.name != null) {
            			            return '<a href="${createLink(action:'edit')}/' + row.id + '">' + row.name + '</>' ;
            			    } else {
            			        return '' ;
            			    }
                		}
            		},

            		// Rssi
            		{ "targets": 3, "render":
            			function ( data, type, row ) {
            			    if (row.last_rssi != null) {
            			    	return '<img src="${resource(dir:'images')}/level_' + rssiLevel(row.last_rssi) + '.gif"/>';             			        
            			    } else {
            			        return '';
            			    }
                		}
            		},
            		// LRT
            		{"targets": 4, "render":
            			function ( data, type, row ) {
            			 	return getDateString(new Date(row.last_result_time));
                		}

            		},
            		// Status
            		{ "targets": 5,"render":
            		    function ( data, type, row ) {
            			   if (row.status != null) {
            			        return '<span class="label label-' + statusClass(row.status) + '">' + statusText(row.status) + '</span>';
            			   } else {
            			    return '';
            			   }
                		}
            		},
            		// Controls
            		{ "targets": 6,"render":
            		    function ( data, type, row ) {
            		        var ret = '<td class="link">';
            		        ret += '<a class="btn btn-xs btn-default" href="${createLink(action:'edit')}/' + row.id + '"><span class="glyphicon glyphicon-edit"></span> Edit</a>';
            		        ret += '<a class="btn btn-xs btn-default" href="${createLink(action:'chart')}/' + row.id + '"><span class="glyphicon glyphicon-stats"></span> Chart</a>';
            		        ret += '<a class="btn btn-xs btn-default" href="${createLink(action:'remove')}/' + row.id + '" onclick="return confirm(\'Are you sure?\');"><span class="glyphicon glyphicon-remove"></span> Remove</a>';
            			    return ret += '</td>';
                		},
                		"sortable" : false
            		}


        		]
            } );

            
		});
    
	</script>
</body>
</html>
