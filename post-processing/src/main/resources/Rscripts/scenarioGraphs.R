args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
  # path <-
  #"C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//ethereum//olga_3//csv"
setwd(path)

options(scipen=5)

times <- read.csv(file = "time.csv", sep = ",", head = TRUE)
resources <- read.csv(file = "resources.csv", sep = ",", head = TRUE)

overTimePlot <- function(data, name) {
  data <- data.frame(data["time"], data[name])
  bmp(filename = paste(name,".bmp"))
  plot(data, type = "l")
  dev.off()
}

createDir <- function(name) {
  setwd(path)
  dir.create(file.path(path, paste("..\\graphs\\", name)))
  setwd(paste("..\\graphs\\", name))
}

dir.create(file.path(path, "..\\graphs"))

createDir("overTime")
overTimePlot(times, "intensity")
overTimePlot(times, "throughput")
overTimePlot(times, "transactionQueue")
overTimePlot(times, "latency")
overTimePlot(times, "transactionSize")
overTimePlot(times, "blockSize")
overTimePlot(times, "numberTransactionsInBlock")

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

plotMean<- function(data, xVec, xName, yName) {
  res <-aggregate(data[yName], list(xVec), mean)
  bmp(filename = paste(yName, "_to_", xName, ".bmp"))
  plot(res, type = 'l', xlab = xName)
  dev.off()
}

createDir("throughput")
plotMean(times, times$intensity,"intensity", "throughput")
plotMean(times, times$transactionSize,"transactionSize", "throughput")
plotMean(times, times$blockSize,"blockSize", "throughput")
plotMean(times, times$numberTransactionsInBlock,"numberTransactionsInBlock", "throughput")

createDir("latency")
plotMean(times, times$intensity,"intensity", "latency")
plotMean(times, times$transactionSize,"transactionSize", "latency")
plotMean(times, times$blockSize,"blockSize", "latency")
plotMean(times, times$numberTransactionsInBlock,"numberTransactionsInBlock", "latency")






