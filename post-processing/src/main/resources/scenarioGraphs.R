args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
 # path <-
 # "C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//ethereum//olga_3//csv"
setwd(path)

options(scipen=5)

times <- read.csv(file = "time.csv", sep = ",", head = TRUE)
resources <- read.csv(file = "resources.csv", sep = ",", head = TRUE)

dir.create(file.path(path, "..\\graphs"))
dir.create(file.path(path, "..\\graphs\\overTime"))
setwd("..\\graphs\\overTime")

intensities <- data.frame(times["time"], times["intensity"])
bmp(filename = "intensities.bmp")
plot(intensities, type = "l")
dev.off()

throughput <- data.frame(times["time"], times["throughput"])
bmp(filename = "throughput.bmp")
plot(throughput, type = "l")
dev.off()

queue <- data.frame(times["time"], times["transactionQueue"])
bmp(filename = "transactionQueue.bmp")
plot(queue, type = "l")
dev.off()

# throughput <- general.frame(times["time"], times["throughputDistributed"])
# bmp(filename = "throughputDistributed.bmp")
# plot(throughput, type = "l")
# dev.off()

latency <- data.frame(times["time"], times["latency"])
bmp(filename = "latency.bmp")
plot(latency, type = "l")
dev.off()

transactionSize <- data.frame(times["time"], times["transactionSize"])
bmp(filename = "transactionSize.bmp")
plot(transactionSize, type = "l")
dev.off()

blockSize <- data.frame(times["time"], times["blockSize"])
bmp(filename = "blockSize.bmp")
plot(blockSize, type = "l")
dev.off()

number <- data.frame(times["time"], times["numberTransactionsInBlock"])
bmp(filename = "numberTransactionsInBlock.bmp")
plot(number, type = "l")
dev.off()

#resources graphs
dir.create(file.path(path, "..\\graphs\\resources"))
setwd("..\\resources")

printNode <- function(resources, nodeId) {
  dir.create(file.path(path, paste("..\\graphs\\resources\\", nodeId)))
  setwd(paste("..\\resources\\",nodeId))
  
  data<-resources
  correctElements <-  as.numeric(data[, 2]) %in% c(as.numeric(nodeId))
  data <- data[correctElements, ]
  printMonitor(data, "cpu")
  printMonitor(data, "usedMemory")
  printMonitor(data, "usedMemory.")
  printMonitor(data, "downloaded")
  printMonitor(data, "uploaded")

  setwd("..")
}

printMonitor <- function(data, name) {
  data <- data.frame(resources["time"], resources[name])
  bmp(filename = paste(name,".bmp"))
  plot(data, type = "l")
  dev.off()
}

nodes<-resources$nodeId
nodes<-unique(nodes)
for(node in nodes) {
  printNode(resources, node)
}

#throughput graphs
dir.create(file.path(path, "..\\graphs\\throughput"))
setwd("..\\throughput")


res <-aggregate(times["throughput"], list(times$intensity), mean)
bmp(filename = "throughputToIntensity.bmp")
plot(res, type = 'l', xlab = 'intensity')
dev.off()

#res <- general.frame(times["transactionSize"], times["throughput"])
res <-aggregate(times["throughput"], list(times$transactionSize), mean)
bmp(filename = "throughputToTrSize.bmp")
plot(res, type = 'l', xlab = 'transactionSize')
dev.off()

#res <- general.frame(times["blockSize"], times["throughput"])
res <-aggregate(times["throughput"], list(times$blockSize), mean)
bmp(filename = "troughputToBlockSize.bmp")
plot(res, type = 'l', xlab = 'blockSize')
dev.off()

res <-aggregate(times["throughput"], list(times$numberTransactionsInBlock), mean)
bmp(filename = "throughputToNumberTransactionsInBlock.bmp")
plot(res, type ='l', xlab = 'numberTransactionsInBlock')
dev.off()

# #throughput distributed graphs
# dir.create(file.path(path, "..\\graphs\\throughputDistributed"))
# setwd("..\\throughputDistributed")
#
#
# res <-aggregate(times["throughputDistributed"], list(times$intensity), mean)
# bmp(filename = "throughputDistributedToIntensity.bmp")
# plot(res, type = 'l', xlab = 'intensity')
# dev.off()
#
# #res <- general.frame(times["transactionSize"], times["throughput"])
# res <-aggregate(times["throughputDistributed"], list(times$transactionSize), mean)
# bmp(filename = "throughputDistributedToTrSize.bmp")
# plot(res, type = 'l', xlab = 'transactionSize')
# dev.off()
#
# #res <- general.frame(times["blockSize"], times["throughput"])
# res <-aggregate(times["throughputDistributed"], list(times$blockSize), mean)
# bmp(filename = "troughputDistributedToBlockSize.bmp")
# plot(res, type = 'l', xlab = 'blockSize')
# dev.off()
#
# res <-aggregate(times["throughputDistributed"], list(times$numberTransactionsInBlock), mean)
# bmp(filename = "throughputDistributedToNumberTransactionsInBlock.bmp")
# plot(res, type ='l', xlab = 'numberTransactionsInBlock')
# dev.off()

#latency graphs
dir.create(file.path(path, "..\\graphs\\latency"))
setwd("..\\latency")


#res <- general.frame(times["intensity"], times["distributionLatency"])
res <-aggregate(times["latency"], list(times$intensity), mean)
bmp(filename = "latencyToIntensity.bmp")
plot(res, type = 'l', xlab = 'intensity')
dev.off()

#res <- general.frame(times["transactionSize"], times["distributionLatency"])
res <-aggregate(times["latency"], list(times$transactionSize), mean)
bmp(filename = "latencyToTrSize.bmp")
plot(res, type = 'l', xlab = "transactionSize")
dev.off()

#res <- general.frame(times["blockSize"], times["distributionLatency"])
res <-aggregate(times["latency"], list(times$blockSize), mean)
bmp(filename = "latencyToBlockSize.bmp")
plot(res, type ='l', xlab = 'blockSize')
dev.off()

res <-aggregate(times["latency"], list(times$numberTransactionsInBlock), mean)
bmp(filename = "latencyToNumberTransactionsInBlock.bmp")
plot(res, type ='l', xlab = 'numberTransactionsInBlock')
dev.off()





