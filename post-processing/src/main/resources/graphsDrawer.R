#args = commandArgs(trailingOnly=TRUE)
#if (length(args)==0) {
#  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
#} 
#path <- args[1]
path <- "C://Users//Юыќур//Documents//Course//git//blockchain-benchmarking//post-processing//src//main//resources//results//current scenario//csv"
setwd(path)

intensities <-
  read.csv(file = "intensity.csv", sep = ",", head = TRUE)
blocks <- read.csv(file = "blocks.csv", sep = ",", head = TRUE)
distribution <-
  read.csv(file = "distributionsWithSize.csv", sep = ",", head = TRUE)
unverified <-
  read.csv(file = "timeToUnverifiedTransactions.csv", sep = ",", head = TRUE)

setwd("..")
dir.create(file.path(path, "..\\graphs"))

setwd("graphs")
bmp(filename = "intensities.bmp")
plot(intensities) # intensities to time
dev.off()

bmp(filename = "unverifiedTransactions.bmp")
plot(unverified) # intensities to time
dev.off()

uniqueSizes <- unique(distribution[, 2:3])
for(i in 1:nrow(uniqueSizes)) {
  i<-151
  size <- uniqueSizes[i,]
  #select only one size elements
  correctElements <-  as.numeric(distribution[, 2]) %in% c(size[1])
  distr <- distribution[correctElements, ]
  #remove all useless colomns
  distr95 <- data.frame(distr["time"], distr["distributionTime95"])
  distr100 <-
    data.frame(distr["time"], distr["maxDistributionTime"])
  #merge distribution and intensityInfo
  result95 <- merge(distr95, intensities, by = "time")[, 2:3]
  result100 <- merge(distr100, intensities, by = "time")[, 2:3]
  bmp(filename = paste("distribution",size[1],"to",size[2], "Mb.bmp"))
  plot(result95, col = c("red"), pch = 16)
  points(result100, col = c("green"), pch = 10)
  dev.off()
}



