library(openair)
data = read.csv('Wind5.csv',header=TRUE,sep=";")
data$date <- as.POSIXct(strptime(data$date,"%Y-%m-%d %H:%M", tz="UTC"))
timeAverage(data,avg.time = 'hour')