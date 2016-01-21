dataSource {
    pooled = true
    driverClassName = "org.postgresql.Driver"
    dialect = "org.hibernate.dialect.PostgreSQLDialect"
	properties {
		maxActive = 50
		maxIdle = 10
		maxWait = 10000
	}
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

environments {
    development {
        dataSource {           
            url = "jdbc:postgresql://bayeos-dev/xbee" 			
			username="xbee"
			password="0b64debf8fae4239a7ca845f39878a3d"
			loggingSql = true
        }
    }
	
	
	test {
        dataSource {           
        	url = "jdbc:postgresql://bayeos-dev/xbee"       
			username="xbee"
			password="0b64debf8fae4239a7ca845f39878a3d"
			loggingSql = true
        }
    }
	
			
	production {
		dataSource {			
			url = "jdbc:postgresql://localhost/xbee" 
			username="xbee"
			password="0b64debf8fae4239a7ca845f39878a3d"
			loggingSql = false
		}
	}
	
    
}


