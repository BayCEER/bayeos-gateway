package gateway

import org.apache.log4j.Logger
import org.apache.log4j.Level
import static org.apache.log4j.Level.*


class LoggerService {
	
	def debug() {
		Logger.rootLogger.setLevel(Level.DEBUG)
	}
	
	def info() {
		Logger.rootLogger.setLevel(Level.INFO)
	}
	
	def warn() {
		Logger.rootLogger.setLevel(Level.WARN)
		
	}
	
	def error() {
		Logger.rootLogger.setLevel(Level.ERROR)
	}


	def getRootLogger() {
		Logger.rootLogger
	}
	def getAllLoggers() {
		rootLogger.loggerRepository.currentLoggers.toList().sort { it.name
		} }
	def getActiveLoggers() {
		allLoggers.findAll { it.level
		} }
	def getLogger(String logName) {
		rootLogger.getLogger(logName)
	}
	def setLevel(String logName, Level level) {
		rootLogger.getLogger(logName).level = level
	}

	def printLogger(logger) {
		println "${logger.name} -> ${logger.level}"
	}
	def printAllLoggers() {
		allLoggers.each { printLogger(it)
		} }
	def printActiveLoggers() {
		activeLoggers.each { printLogger(it)
		} }
}
