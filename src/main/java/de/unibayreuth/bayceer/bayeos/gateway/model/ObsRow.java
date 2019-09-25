package de.unibayreuth.bayceer.bayeos.gateway.model;
public  class ObsRow {
		private Long id;		
		private Long millis;
		private Float value;

		public ObsRow(Long id, Long millis, Float value) {
			this.id = id;			
			this.millis = millis;
			this.value = value;		
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
		
		public Long getMillis() {
			return millis;
		}

		public void setMillis(Long millis) {
			this.millis = millis;
		}

		public Float getValue() {
			return value;
		}

		public void setValue(Float value) {
			this.value = value;
		}
		
	}