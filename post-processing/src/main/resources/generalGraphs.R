args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]

# path <-
#   "C://Users//Olga//Desktop//git//blockchain-benchmarking//post-processing//src//main//resources//results//general//csv"
setwd(path)


plotAverage <-function(filename, data) {
  correctElements <-  as.numeric((data[, 1])) %in% c(1)
  line1 <- data[correctElements, ]
  line1 <- data.frame(line1[2], line1[3])
  correctElements <-  as.numeric((data[, 1])) %in% c(2)
  line2 <- data[correctElements, ]
  line2 <- data.frame(line2[2], line2[3])
  if (nrow(line1) == 0 || nrow(line2) == 0) {
    return()
  }
  bmp(filename = paste(filename, ".bmp"))
  plot(line1,
  col = c("red"),
  pch = 30,
  type = "l")
  lines(line2, col = c("black"), pch = 30)
  legend("topleft",
  inset = .02,
  legend = c("ethereum", "fabric"),
  col = c("red", "black"), lty = c(1,1))
  dev.off()
}




plotBlockchain <- function(data, name, num, param) {
  correctElements <-  as.numeric((data[, 1])) %in% c(num)
  data <- data[correctElements,]
  if (nrow(data) == 0) {
    return()
  }
  plotMaxMinAverage(
  x = c(t(data[param])),
  yMax = c(t(data["maxThroughput"])),
  y90Max = c(t(data["per90Throughput"])),
  yAvrg = c(t(data["averageThroughput"])),
  name = paste("Throughput in ", name),
  xlab = param,
  ylab = "throughput",
  filename = paste(name, "Throughput")
  )

  plotMaxMinAverage(
  x = c(t(data[param])),
  yMax = c(t(data["maxLatency"])),
  y90Max = c(t(data["per90Latency"])),
  yAvrg = c(t(data["averageLatency"])),
  name = paste("Latency in ", name),
  xlab = param,
  ylab = "throughput",
  filename = paste(name, "Latency")
  )
}



plotMaxMinAverage <- function(x,
y90Max,
yAvrg,
yMax,
name = "",
xlab = "X",
ylab = "Y",
filename) {
  stopifnot(
  length(y90Max) == length(yAvrg)
  &&
    length(y90Max) == length(yMax) && length(y90Max) == length(x)
  )

  bmp(filename = paste(filename, ".bmp"))
  plot(
  x = c(min(x), max(x)),
  y = c(0, max(yMax)),
  type = "n",
  main = name,
  xlab = xlab,
  ylab = ylab
  )
  polygon(
  x = c(x, rev(x)),
  y = c(yMax, rev(y90Max)),
  col = "LightBlue",
  border = NA
  )
  polygon(
  x = c(x, rev(x)),
  y = c(y90Max, rev(yAvrg)),
  col = "Blue",
  border = NA
  )
  polygon(
  x = c(x, rev(x)),
  y = c(rep(0, length(x)), rev(yAvrg)),
  col = "DarkBlue",
  border = NA
  )
  legend(
  "topleft",
  inset = .02,
  legend = c("max", "90-percentile", "average"),
  fill = c("LightBlue", "blue", "DarkBlue")
  )
  dev.off()
}


intensity <-
  read.csv(file = "intensity.csv", sep = ",", head = TRUE)
size <- read.csv(file = "size.csv", sep = ",", head = TRUE)

dir.create(file.path(path, "..\\graphs"))
setwd("..\\graphs")

if (nrow(intensity) != 0) {
  intensity <- intensity[(order(intensity$intensity)),]
  plotBlockchain(intensity, "ETHEREUM", 1, "intensity")
  plotBlockchain(intensity, "FABRIC", 2, "intensity")
  
  plotAverage(filename = "averageTroughputfromIntensity",
     data =  data.frame(intensity[1], intensity["intensity"], 
                               intensity["averageThroughput"]))
  plotAverage(filename = "averageLatencyfromIntensity",
              data =  data.frame(intensity[1], intensity["intensity"], 
                                 intensity["averageLatency"]))
}

if (nrow(size) != 0) {
  size <- size[(order(size$size)),]
  plotBlockchain(size, "ETHEREUM", 1, "size")
  plotBlockchain(size, "FABRIC", 2, "size")
  
  plotAverage(filename = "averageTroughputfromSize",
              data =  data.frame(intensity[1], intensity["intensity"], 
                                 intensity["averageThroughput"]))
  plotAverage(filename = "averageLatencyfromSize",
              data =  data.frame(intensity[1], intensity["intensity"], 
                                 intensity["averageLatency"]))
}

