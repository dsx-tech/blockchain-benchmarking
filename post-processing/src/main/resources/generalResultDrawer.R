args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to results directory)", call.=FALSE)
}
path <- args[1]
#path <-
#"C://Users//�����//Documents//Course//git//blockchain-benchmarking//post-processing//src//main//resources//results//general//csv"
setwd(path)

intensities1 <-
read.csv(file = "intensities.csv", sep = ",", head = TRUE)

setwd("..//graphs")

uniqueDispersion <- unique(intensities1[, 2])
uniqueNodeTypes <- unique(intensities1[, 3])
uniqueSizes  <- unique(intensities1[, 4])
for (i in uniqueDispersion) {
    for (j in uniqueNodeTypes) {
        for (k in uniqueSizes) {
            # select elements of only one type
            correctDisp <-  intensities1[, 2] %in% c(i)
            correctNodeType <-  intensities1[, 3] %in% c(j)
            correctSize <-  intensities1[, 4] %in% c(k)
            correctElements <- correctDisp * correctSize * correctNodeType
            correctElements <- correctElements %in% 1
            intens <- intensities1[correctElements,]
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
