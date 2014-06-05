package gateway

import org.springframework.dao.DataIntegrityViolationException

class KnotPointController {


	def create() {
		KnotPoint np = new KnotPoint(params)
		Spline t = Spline.get(params.splineId)
		np.spline = t


		switch (request.method) {
			case 'GET':
				[knotPointInstance: np]
				break
			case 'POST':

				if (!np.save(flush: true)) {
					render view: 'create', model: [knotPointInstance: np]
					return
				}

				flash.message = message(code: 'default.created.message', args: [
					message(code: 'knotPoint.label', default: 'KnotPoint'),
					np.id
				])
				redirect controller:'spline', action: 'edit', id: np.spline.id
				break
		}
	}

	def edit() {
		def np = KnotPoint.get(params.id)
		if (!np) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'knotPoint.label', default: 'KnotPoint'),
				params.id
			])
			flash.level = 'warning'
			return
		}


		switch (request.method) {
			case 'GET':
				[knotPointInstance: np]
				break
			case 'POST':
				if (params.version) {
					def version = params.version.toLong()
					if (np.version > version) {
						np.errors.rejectValue('version', 'default.optimistic.locking.failure',
						[message(code: 'knotPoint.label', default: 'KnotPoint')] as Object[],"Another user has updated this KnotPoint while you were editing")
						render view: 'edit', model: [knotPointInstance: np]
						return
					}
				}

				np.properties = params
				if (!np.save(flush: true)) {
					render view: 'edit', model: [knotPointInstance: np]					
					return
				}
				flash.message = message(code: 'default.updated.message', args: [
					message(code: 'knotPoint.label', default: 'KnotPoint'),
					np.id
				])
				redirect(controller: 'spline', action: 'edit', params:[id:np.spline.id])
				break
		}
	}

	def delete() {
		def knotPointInstance = KnotPoint.get(params.id)
		if (!knotPointInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'knotPoint.label', default: 'KnotPoint'),
				params.id
			])
			return
		}

		try {
			knotPointInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [
				message(code: 'knotPoint.label', default: 'KnotPoint'),
				params.id
			])
		}
		catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [
				message(code: 'knotPoint.label', default: 'KnotPoint'),
				params.id
			])
			flash.level = 'danger'
		}
		redirect(controller: 'spline', action: 'edit', params:[id:knotPointInstance.spline.id])
	}
}
