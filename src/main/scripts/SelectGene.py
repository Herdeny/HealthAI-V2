import sys

import pandas as pd
import numpy as np
import scanpy as sc


data_path = sys.argv[1]
file_name = sys.argv[2]
print("Loading data...", flush=True)
data = pd.read_csv(data_path + file_name, index_col=0, header=0)
print("Data loaded.", flush=True)
# 创建 AnnData 对象
print("Creating AnnData object...", flush=True)
adata = sc.AnnData(data)
print("AnnData object created.", flush=True)
# 转换数据类型
print("Converting data type...", flush=True)
adata.X = adata.X.astype(np.float64)  # Ensure the data is in float64
print("Data type converted.", flush=True)
# 数据归一化
print("Normalizing data...", flush=True)
sc.pp.normalize_per_cell(adata, counts_per_cell_after=1e4)
print("Data normalized.", flush=True)
# 对数转换
print("Log transforming data...", flush=True)
sc.pp.log1p(adata)
print("Data log transformed.", flush=True)
# 选择高度可变基因
print("Selecting highly variable genes...", flush=True)
sc.pp.highly_variable_genes(adata, min_mean=0.0125, max_mean=3, min_disp=0.5, n_top_genes=1000)
print("Highly variable genes selected.", flush=True)
# 提取高变基因的表达矩阵
print("Extracting expression matrix of highly variable genes...", flush=True)
data_M6 = adata[:, adata.var['highly_variable']]
data_M6 = data_M6.to_df()
print("Expression matrix of highly variable genes extracted.", flush=True)
# 保存数据
print("Saving data...", flush=True)
data_M6.to_csv(data_path + file_name[:-4] + "_1000.csv")
print("Data saved.", flush=True)
