args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
#path <-
#"C://Users//�����//Documents//Course//git//blockchain-benchmarking//post-processing//src//main//resources//results//general//csv"
setwd(path)

intensities <-
read.csv(file = "intensities.csv", sep = ",", head = TRUE)
sizes <- read.csv(file = "sizes.csv", sep = ",", head = TRUE)

dir.create(file.path(path, "..\\graphs\\intensities"))
setwd("..\\graphs\\intensities")

uniqueDispersion <- unique(intensities[, 2])
uniqueNodeTypes <- unique(intensities[, 3])
uniqueSizes  <- unique(intensities[, 4])
for (i in uniqueDispersion) {
    for (j in uniqueNodeTypes) {
        for (k in uniqueSizes) {
            # select elements of only one type
            correctDisp <-  intensities[, 2] %in% c(i)
            correctNodeType <-  intensities[, 3] %in% c(j)
            correctSize <-  intensities[, 4] %in% c(k)
            correctElements <- correctDisp * correctSize * correctNodeType
            correctElements <- correctElements %in% 1
            intens <- intensities[correctElements,]
            # separate differrent plots
            distr <-
            data.frame(intens["intensity"], intens["mediumDistributionTime"])
            verification <-
            data.frame(intens["intensity"], intens["mediumVerificationTime"])
            unverified <-
            data.frame(intens["intensity"], intens["numberOfUnverifiedTransactions"])
            # plot
            bmp(
            filename = paste(
            "distribution__dispersion_",
            i,
            "_size_",
            k,
            "_nodeType_",
            j,
            ".bmp"
            )
            )
            plot(distr)
            dev.off()
            bmp(
            filename = paste(
            "verification__dispersion_",
            i,
            "_size_",
            k,
            "_nodeType_",
            j,
            ".bmp"
            )
            )
            plot(verification)
            dev.off()
            bmp(filename = paste(
            "unverified__dispersion_",
            i,
            "_size_",
            k,
            "_nodeType_",
            j,
            ".bmp"
            ))
            plot(unverified)
            dev.off()
        }
    }
}

dir.create(file.path(path, "..\\graphs\\sizes"))
setwd("..\\sizes")

uniqueDispersion <- unique(sizes[, 2])
uniqueNodeTypes <- unique(sizes[, 3])
uniqueIntensity  <- unique(sizes[, 4])
for (i in uniqueDispersion) {
  for (j in uniqueNodeTypes) {
    for (k in uniqueIntensity) {
      # select elements of only one type
      correctDisp <-  sizes[, 2] %in% c(i)
      correctNodeType <-  sizes[, 3] %in% c(j)
      correctIntensity <-  sizes[, 4] %in% c(k)
      correctElements <- correctDisp * correctIntensity * correctNodeType
      correctElements <- correctElements %in% 1
      new_sizes <- sizes[correctElements,]
      # separate differrent plots
      distr <-
        data.frame(new_sizes["size"], new_sizes["mediumDistributionTime"])
      verification <-
        data.frame(new_sizes["size"], new_sizes["mediumVerificationTime"])
      unverified <-
        data.frame(new_sizes["size"], new_sizes["numberOfUnverifiedTransactions"])
      # plot
      bmp(
        filename = paste(
          "distribution__dispersion_",
          i,
          "_intensity_",
          k,
          "_nodeType_",
          j,
          ".bmp"
        )
      )
      plot(distr)
      dev.off()
      bmp(
        filename = paste(
          "verification__dispersion_",
          i,
          "_intensity_",
          k,
          "_nodeType_",
          j,
          ".bmp"
        )
      )
      plot(verification)
      dev.off()
      bmp(filename = paste(
        "unverified__dispersion_",
        i,
        "_intensity_",
        k,
        "_nodeType_",
        j,
        ".bmp"
      ))
      plot(unverified)
      dev.off()
    }
  }
}
