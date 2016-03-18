grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml'],
	xls:		   ['application/vnd.ms-excel']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/stylesheets/*', '/javascripts/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

grails.gorm.default.mapping = {
	id generator:'identity'
	name sqlType:'text'
	label sqlType:'text'
	version false
}


log4j = {
	
	// Set level for all application artifacts
	info "grails.app"	
	info "grails.app.services"
	info "bayeos"
	
}

environments {
	development {
        grails.logging.jul.usebridge = true		
    }	
    production {
        grails.logging.jul.usebridge = false	
		grails.config.locations = [ "classpath:/resources/LogConfig.groovy" ]
    }
}


			    
    	   

// import grails.plugins.springsecurity.
grails.plugins.springsecurity.securityConfigType = 'InterceptUrlMap'
grails.plugins.springsecurity.interceptUrlMap = [
	'/static/**'	:     	['IS_AUTHENTICATED_ANONYMOUSLY'], 	// public
	'/login/*'  :     		['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/frame/**' :	  		['ROLE_IMPORT'],					// save frames
	'/deleteService/*': 	['ROLE_ADMIN'],						// start and stop service
	'/logger/**': 			['ROLE_ADMIN'],						// logging 
	'/exportService/*': 	['ROLE_ADMIN'],						// start and stop service 
	'/nagios/**' :	  		['ROLE_CHECK'],						// check services	 
	'/**':         	  		['ROLE_USER']						// all users 
]


// Erlaube admins alles was user d�rfen
// Erlaube normalen usern alles was import darf 
grails.plugins.springsecurity.roleHierarchy = '''
   ROLE_ADMIN > ROLE_USER 
   ROLE_USER  > ROLE_IMPORT 
   ROLE_IMPORT > ROLE_CHECK 
'''

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'gateway.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'gateway.UserRole'
grails.plugins.springsecurity.authority.className = 'gateway.Role'

grails.plugins.springsecurity.password.algorithm='SHA'
grails.plugins.springsecurity.password.encodeHashAsBase64 = true

grails.plugins.springsecurity.useBasicAuth = true
grails.plugins.springsecurity.basic.realmName = "Login Please:"

grails.plugins.springsecurity.filterChain.chainMap = [
	'/nagios/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
	'/grafana/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
	'/frame/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
	'/board/**': 'JOINED_FILTERS,-exceptionTranslationFilter',	
	'/message/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
	'/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
 ]

grails.plugins.twitterbootstrap.fixtaglib = true
grails.plugins.twitterbootstrap.defaultBundle = 'bundle_bootstrap'

