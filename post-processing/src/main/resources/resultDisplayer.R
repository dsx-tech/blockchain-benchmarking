args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
 stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
#path <-
#"C://Users//Ольга//Documents//Course//git//blockchain-benchmarking//post-processing//src//main//resources//results//csv"
setwd(path)

times <- read.csv(file = "time.csv", sep = ",", head = TRUE)

dir.create(file.path(path, "..\\graphs\\intensities"))
setwd("..\\graphs\\intensities")

intensities <- data.frame(times["time"], times["intensity"])
bmp(filename = "intensities.bmp")
plot(intensities, type = "l")
dev.off()

throughput <- data.frame(times["time"], times["throughput"])
bmp(filename = "throughput.bmp")
plot(throughput, type = "l")
dev.off()

latency <- data.frame(times["time"], times["distributionLatency"])
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

#throughput graphs
#todo считать среднее значение для совпадающих аргументов
res <- data.frame(times["intensity"], times["throughput"])
bmp(filename = "throughputToIntensity.bmp")
plot(res)
dev.off()

res <- data.frame(times["transactionSize"], times["throughput"])
bmp(filename = "throughputToTrSize.bmp")
plot(res)
dev.off()

res <- data.frame(times["blockSize"], times["throughput"])
bmp(filename = "troughputToBlockSize.bmp")
plot(res)
dev.off()

#latency graphs
res <- data.frame(times["intensity"], times["distributionLatency"])
bmp(filename = "latencyToIntensity.bmp")
plot(res)
dev.off()

res <- data.frame(times["transactionSize"], times["distributionLatency"])
bmp(filename = "latencyToTrSize.bmp")
plot(res)
dev.off()

res <- data.frame(times["blockSize"], times["distributionLatency"])
bmp(filename = "latencyToBlockSize.bmp")
plot(res)
dev.off()


