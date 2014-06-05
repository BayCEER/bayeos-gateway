<div class="block">
	<div class="block-header">
	 	${header}
	</div>
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${template}" property="name" />
					</div>
					<div class="col-sm-6">
						<f:field bean="${template}" property="description" />
					</div>
				</div>
				
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${template}" property="revision" />
					</div>
					<div class="col-sm-6">
						<f:field bean="${template}" property="dataSheet" />
					</div>
				</div>				
</div>