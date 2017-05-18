args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
 #path <-
 # "C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//ethereum//60//csv"
 setwd(path)

options(scipen=5)

times <- read.csv(file = "time.csv", sep = ",", head = TRUE)
resources <- read.csv(file = "resources.csv", sep = ",", head = TRUE)
latency <- read.csv(file = "transLatencies.csv", sep = ",", head = TRUE)
latencies <- read.csv(file = "latencyQuartils.csv", sep = ",", head = FALSE)

overTimePlot <- function(data, name) {
  data <- data.frame(data["time"], data[name])
  bmp(filename = paste(name,"OverTime.bmp", sep = ""))
  plot(data, type = "o" )
  dev.off()
}

histogramPlot <- function(data, name) {
  data <- data[[name]]
  bmp(filename = paste(name,"Histogram.bmp", sep = ""))
  hist(data, col = "blue", xlab = name, main = paste ("Histogram of", name))
  dev.off()
}

createDir <- function(name) {
  setwd(path)
  dir.create(file.path(path, paste("..\\graphs\\", name)))
  setwd(paste("..\\graphs\\", name))
}

dir.create(file.path(path, "..\\graphs"))

plotMetric <- function(data, metric) {
  createDir(metric)
  overTimePlot(data, metric)
  histogramPlot(data, metric)

}

plotMetric(times, "intensity")
plotMetric(times, "blockSize")
plotMetric(times, "transactionQueue")
plotMetric(times, "transactionSize")
plotMetric(times, "blockGeneration")
plotMetric(times, "throughput")

createDir("latency")
histogramPlot(latency, "latency")
overTimePlot(times, "latency")

means<-c()
names<-c()

for(i in c(1:length(latencies))) {
  means[[i]] <-mean(latencies[[1]])
  names[[i]] <- 1+i
}

data <- data.frame(names, means)

bmp(filename = "distributionLatency.bmp")
plot(data, type = 'o',xlab = "numNodes", ylab = "distribution, ms", xaxt = "n")
axis(1, at = names)
dev.off()



# data <- data.frame(transactions["transactionCreationTime"], transactions["latency"])
# bmp(filename = paste(name,"OverTime.bmp", sep = ""))
# plot(data, type = "o" )
# dev.off()

# boxplot(latencies,outline=FALSE, names = c("2","3","4"))


# overTimePlot(times, "throughput")
# overTimePlot(times, "transactionQueue")
# overTimePlot(times, "latency")
# overTimePlot(times, "transactionSize")
# overTimePlot(times, "blockSize")
# overTimePlot(times, "numberTransactionsInBlock")



createDir("resources")
printNode <- function(resources, nodeId) {
  createDir(paste("resources\\", nodeId))
  data<-resources
  correctElements <-  as.character(data[, 2]) %in% c(nodeId)
  data <- data[correctElements, ]
  overTimePlot(data, "cpu")
  overTimePlot(data, "usedMemory")
  overTimePlot(data, "downloaded")
  overTimePlot(data, "uploaded")
  setwd("..")
}

nodes<-resources$nodeId
nodes<-unique(nodes)
node<-as.character(nodes)[2]
for(node in as.character(nodes)) {
  printNode(resources, node)
}

# plotMetric <- function(file, colomn) {
#   
#   hist(data[[3]], col ="red")
#   hist(data[[4]]) 
# }
# 
# setwd(fabricDir)
# setwd( "tr50to100delay65//csv")
# param <-
#   read.csv(
#     file = paste("time", ".csv", sep = ""),
#     sep = ",",
#     head = TRUE
#   )
# param <- param[["intensity"]]
# param <- t(c(param))
# int <- signif(mean(param), 4)

# path <- "C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//ethereum//tr50to100delay40//csv"
# setwd(path)
# 
# latencies <- read.csv(file = "latencyQuartils.csv", sep = ",", head = FALSE) #, head = TRUE)
# 
# boxplot(latencies,outline=FALSE, names = c("2","3","4"), xlab = "numNodes", ylab = "distribution, ms")
# 
# setwd("C:")
