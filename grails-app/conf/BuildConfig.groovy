grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.enable.native2ascii = false

grails.war.resources = { stagingDir ->
	// Servlet JAR conflicts with Tomcat's version. See: http://jira.grails.org/browse/GRAILS-9483
	delete(file:"${stagingDir}/WEB-INF/lib/javax.servlet-api-3.0.1.jar")
}

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
       
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
		compile 'de.unibayreuth.bayceer.bayeos:bayeos-frame:1.1.2'
		compile 'de.unibayreuth.bayceer.bayeos:bayeos-xmlrpc:1.9.5'
		compile 'de.unibayreuth.bayceer.bayeos:bayeos-password-file:1.0.0'		
		test 	'org.codehaus.groovy.modules.http-builder:http-builder:0.6'
    }

    plugins {		
        runtime ":hibernate:$grailsVersion"
        runtime ":resources:1.2"		
		runtime ":spring-security-acl:1.1.1"
		runtime ":spring-security-core:1.2.7.3"
		runtime ":twitter-bootstrap:3.0.1"
		runtime ':fields:1.3'			
		build 	":tomcat:$grailsVersion"
               
    }
}
