library(MEGENA)
library(Seurat)
args = commandArgs(trailingOnly = TRUE)
datapath <- args[1]
filename <- args[2]
setwd(datapath)  # 设置当前工作目录为指定的目录
filepath <- paste0(datapath, filename)
myData = read.csv(filepath,header=T,row.names=1,sep=",",check.names=F)
datExpr <- as.matrix(t(myData))
n.cores<-1
doPar<-FALSE
methos="pearson"
FDR.cutoff=0.05
module.pval=0.05
hub.pval=0.05
cor.perm = 2 # 用于计算所有相关对的FDRs
hub.perm = 20 # 用于计算连通显著性p值
annot.table=NULL
id.col=1
symbol.col=2
ijw<-calculate.correlation(datExpr,doPerm = 3,num.cores = 8,saveto ="./")
el <- calculate.PFN(ijw[,1:3])
output_path_suffix <- "_PFN.csv"
output_path <- paste0(tools::file_path_sans_ext(filepath), output_path_suffix)
write.csv(el, output_path)
