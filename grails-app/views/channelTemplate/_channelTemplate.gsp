<div class="block">
				<div class="block-header">${header}</div>

				<f:with bean="${channel}">
					<div class="row">
						<div class="col-sm-6">
							<f:field property="nr" />
						</div>
						<div class="col-sm-6">
							<f:field property="label" />
						</div>
					</div>
					<div class="row">
						<div class="col-sm-6">
							<f:field property="phenomena" />
						</div>
						<div class="col-sm-6">
							<f:field property="unit" />
						</div>
					</div>
					<div class="row">
						<div class="col-sm-4">
							<f:field property="aggrInterval" />
						</div>
						<div class="col-sm-4">
							<f:field property="aggrFunction" />
						</div>
						<div class="col-sm-4">
							<f:field property="spline" />
						</div>
					</div>
				</f:with>
			</div>