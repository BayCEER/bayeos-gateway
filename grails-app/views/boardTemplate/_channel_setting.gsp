<div class="block">
	<div class="block-header">
	 	${header}
	</div>
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${channel}" property="criticalMin" />
					</div>
					<div class="col-sm-6">
						<f:field bean="${channel}" property="criticalMax" />
					</div>
				</div>
				
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${channel}" property="warningMin" />
					</div>
					<div class="col-sm-6">
						<f:field bean="${channel}" property="warningMax" />
					</div>					
				</div>		
				
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${channel}" property="samplingInterval" />
					</div>
					<div class="col-sm-6">
						<f:field bean="${channel}" property="checkDelay" label="Check Delay [sec]"/>
					</div>					
				</div>				
</div>