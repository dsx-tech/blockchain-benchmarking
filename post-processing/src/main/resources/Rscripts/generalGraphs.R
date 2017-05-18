args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
# path <- "C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//"

ethereumDir <- paste(path, "/ethereum/", sep="")
fabricDir <- paste(path, "/fabric/", sep="")
generalDir <- paste(path, "/general/", sep="")


getCases <-function(dir, param) {
  setwd(dir)
  cases <- read.csv(file = paste(param, "ScenarioList.csv", sep = ""), sep = ",", head = TRUE)
  if(nrow(cases) == 0) {
    return(c())
  }
  cases <- as.character(cases[[1]])
  return(cases)
}
#cas <- getCases(fabricDir, "size")


plotMetricFromScenarioGroup <-
  function(
    currentDir,      
    metricFile,
           metricColomn,
           metricUnits,
           cases,
           paramFile,
           paramColomn,
           paramUnits,
           resultDir) {
    data <- c()
    names <- c()
    
    i <- 1
    for (case in cases) {
      pathCase <- paste(currentDir, as.character(case), "//csv", sep = "")
      setwd(pathCase)
      
      latencies <-
        read.csv(
          file = paste(metricFile, ".csv", sep = ""),
          sep = ",",
          head = TRUE
        )
      lat <- latencies[metricColomn]
      data[i] <- t(c(lat))
      param <-
        read.csv(
          file = paste(paramFile, ".csv", sep = ""),
          sep = ",",
          head = TRUE
        )
      
      param <- param[[paramColomn]]
      int <- signif(mean(param), 4)
      names[[i]] <- int
      i <- i + 1
    }
    setwd(resultDir)
    bmp(filename = paste(metricColomn, ".bmp", sep = ""))
    boxplot(
      data,
      outline = FALSE,
      names = names,
      xlab = paste(paramColomn, ", ", paramUnits, sep = ""),
      ylab = paste(metricColomn, ", ", metricUnits, sep = "")
    )
    dev.off()
  }

plotBlockchain <- function(blockchainDir, param, paramUnits) {
  dir.create(file.path(blockchainDir, paste("generalGraphs_", param, sep = "")))
  resultDir <- paste(blockchainDir,"generalGraphs_", param, sep = "")
  cases <-getCases(blockchainDir, param)

  if(length(cases)==0) {
    return()
  }
  
  plotMetricFromScenarioGroup(blockchainDir, "time", "throughput", "tr/sec",
                            cases, "time", param, paramUnits, resultDir)
  plotMetricFromScenarioGroup(blockchainDir, "time", "blockGeneration", "blocks/sec",
                            cases, "time", param, paramUnits, resultDir)
  plotMetricFromScenarioGroup(blockchainDir, "time", "blockSize", "bytes",
                              cases, "time", param, paramUnits, resultDir)
  plotMetricFromScenarioGroup(blockchainDir, "time", "transactionQueue", "tr",
                              cases, "time", param, paramUnits, resultDir)
  plotMetricFromScenarioGroup(blockchainDir, "transLatencies", "latency", "sec",
                              cases, "time", param, paramUnits, resultDir)
}
plotBlockchain(ethereumDir, "intensity", "tr/s")
plotBlockchain(ethereumDir, "transactionSize", "bytes")

plotBlockchain(fabricDir, "transactionSize", "bytes")
plotBlockchain(fabricDir, "intensity", "tr/s")
#plotBlockchain(fabricDir, "scalability", "bytes")



