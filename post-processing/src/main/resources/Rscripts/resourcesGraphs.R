args = commandArgs(trailingOnly = TRUE)
if (length(args) == 0) {
  stop("At least one argument must be supplied (path to results directory)",
       call. = FALSE)
}
path <- args[1]

#path <-
  #"C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//general//resources//csv"
setwd(path)
  
plotAverage <- function(filename, data) {
  correctElements <-  as.numeric((data[, 1])) %in% c(1)
  line1 <- data[correctElements,]
  correctElements <-  as.numeric((data[, 1])) %in% c(2)
  line2 <- data[correctElements,]
  if (nrow(line1) == 0 &&  nrow(line2) == 0) {
    return()
  }
  
  if(nrow(line1) == 0 ||  nrow(line2) == 0){
    if(nrow(line1) == 0) {
    c<- line1
    line1<-line2
    line2<-line1
    }
    line1name = as.character(line1[1,1])
    line1 <- aggregate(line1[3], line1[2], mean)
    bmp(filename = paste(filename, ".bmp"))
    plot(
      line1,
      col = c("red"),
      ylim = range(c(line1[2], line2[2])),
      xlim = range(c(line1[1], line2[1])),
      lwd = 3,
      type = "l"
    )
    legend(
      "topleft",
      inset = .02,
      legend = c(line1name),
      col = c("red"),
      lty = c(1),
      lwd = 3
    )
    dev.off()
    return()
    }
  
  line1name = as.character(line1[1,1])
  line2name = as.character(line2[1,1])
  
  line1 <- aggregate(line1[3], line1[2], mean)
  line2 <- aggregate(line2[3], line2[2], mean)
  bmp(filename = paste(filename, ".bmp"))
  plot(
    line1,
    col = c("red"),
    ylim = range(c(line1[2], line2[2])),
    xlim = range(c(line1[1], line2[1])),
    lwd = 3,
    type = "l"
  )
  lines(line2, col = c("black"), lwd = 3)
  legend(
    "topleft",
    inset = .02,
    legend = c(line1name, line2name),
    col = c("red", "black"),
    lty = c(1, 1),
    lwd = 3
  )
  dev.off()
}

intensity <-
  read.csv(file = "resources_intensity.csv", sep = ",", head = TRUE)
size <- read.csv(file = "resources_size.csv", sep = ",", head = TRUE)
scalability <-
  read.csv(file = "resources_scalability.csv", sep = ",", head = TRUE)

dir.create(file.path(path, "..\\graphs"))

allPlotsfromParam <- function(data, paramVec, paramName) {
  dir.create(file.path(path, paste("..\\graphs\\", paramName)))
  setwd(paste("..\\graphs\\", paramName))
  
if (nrow(data) != 0) {
  data <- data[(order(paramVec)), ]
  data <- setNames(
    aggregate(
      data[c(3:7)],
      by = list(data$blockchainType, paramVec),
      mean
    ),
    c(
      "blockchainType",
      paramName,
      "averageCPU",
      "averageMem",
      "averageMemPercent",
      "averageIn",
      "averageOut"
    )
  )
  
  plotAverage(filename = "averageCPU",
              data =  data.frame(data[1], data[paramName],
                                 data["averageCPU"]))
  plotAverage(filename = "averageMem",
              data =  data.frame(data[1], data[paramName],
                                 data["averageMem"]))
  plotAverage(filename = "averageIn",
              data =  data.frame(data[1], data[paramName],
                                 data["averageIn"]))
  plotAverage(filename = "averageOut",
              data =  data.frame(data[1], data[paramName],
                                 data["averageOut"]))
  
}
  setwd(path)
}

allPlotsfromParam(intensity, intensity$intensity, "intensity")
allPlotsfromParam(size, size$transactionSize, "transactionSize")
allPlotsfromParam(scalability, scalability$numberOfNodes, "numberOfNodes")
