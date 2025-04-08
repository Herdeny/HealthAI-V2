rm(list = ls())
library(MEGENA)
library(ggplot2)
library(Seurat)
library(reticulate)
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


cor.perm = 2
hub.perm = 20

annot.table=NULL
id.col=1
symbol.col=2

ijw<-calculate.correlation(datExpr,doPerm = 3,num.cores = 8,saveto ="./")
#ijw<-calculate.correlation(datExpr,doPerm = 5,doPar = FALSE,num.cores = 8,method = "pearson",
#                           FDR.cutoff = 0.05,n.increment = 100,is.signed = FALSE,
#                           output.permFDR = TRUE,output.corTable = TRUE,saveto ="./")


el <- calculate.PFN(ijw[,1:3])
output_path_suffix <- "_PFN.csv"
output_path <- paste0(tools::file_path_sans_ext(filepath), output_path_suffix)
write.csv(el, output_path)

g <- graph_from_data_frame(el,directed = FALSE)
MEGENA.output <- do.MEGENA(g = g,remove.unsig = FALSE,doPar = FALSE,n.perm = 10,save.output=TRUE)
output.summary <- MEGENA.ModuleSummary(MEGENA.output,
                                       mod.pvalue = 0.05,hub.pvalue = 0.05,
                                       min.size = 10,max.size = 5000,
                                       annot.table = TRUE,id.col = TRUE,symbol.col = TRUE,
                                       output.sig = TRUE)
output.summary$modules


#################

pnet.obj <- plot_module(output = output.summary,PFN = g,subset.module = "comp1_2",
                        layout = "kamada.kawai",label.hubs.only = FALSE,
                        gene.set = list("hub.set" = c("CD3E","CD2")),color.code = c("red"),
                        output.plot = FALSE,out.dir = "modulePlot",col.names = c("grey","grey","grey"),
                        hubLabel.col = "black",hubLabel.sizeProp = 1,show.topn.hubs = Inf,show.legend = TRUE)
pnet.obj
###################

mdf= output.summary$module.table
mdf$heat.pvalue = runif(nrow(mdf),0,0.1)
sbobj = draw_sunburst_wt_fill(module.df = mdf,feat.col = "heat.pvalue",log.transform = TRUE,
                              fill.type = "continuous",
                              fill.scale = scale_fill_gradient2(low = "white",mid = "white",high = "red",
                                                                midpoint = -log10(0.05),na.value = "white"),
                              id.col = "module.id",parent.col = "module.parent")
sbobj
mdf$category = factor(sample(x = c("A","B"),size = nrow(mdf),replace = TRUE))
sbobj = draw_sunburst_wt_fill(module.df = mdf,feat.col = "category",
                              fill.type = "discrete",
                              fill.scale = scale_fill_manual(values = c("A" = "red","B" = "blue")),
                              id.col = "module.id",parent.col = "module.parent")
sbobj

get.union.cut(module.output = MEGENA.output$module.output,alpha.cut = 0.5,
              output.plot = TRUE,plotfname = NULL,module.pval = 0.05,remove.unsig = TRUE)

out <- get.DegreeHubStatistic(subnetwork = g,n.perm = 100,doPar = FALSE,n.core = 4)
out
pnet.obj <- plot_subgraph(module = output.summary$modules[[1]],
                          hub = c("CD3E","CD2"),PFN = g,node.default.color = "black",
                          gene.set = NULL,color.code = c("grey"),show.legend = TRUE,
                          label.hubs.only = TRUE,hubLabel.col = "red",hubLabel.sizeProp = 0.5,
                          show.topn.hubs = 10,node.sizeProp = 13,label.sizeProp = 13,
                          label.scaleFactor = 10,layout = "kamada.kawai")
pnet.obj[[1]]
pnet.obj[[2]]

module.table = output.summary$module.table
colnames(module.table)[1] <- "id"
output.obj <- plot_module_hierarchy(module.table = module.table,
                                    label.scaleFactor = 0.15,arrow.size = 0.005,node.label.color = "blue")
print(output.obj[[1]])




