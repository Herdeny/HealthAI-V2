import pandas as pd
import sys
from tqdm import tqdm

# data_path = sys.argv[1]
# file_name = sys.argv[2]
data_path = "D:/ACM/HealthAIV2/data/"
file_name = "Data_Correlation-M24.txt"
print("Loading data...", flush=True)
df = pd.read_csv(data_path + file_name, sep="\t", index_col=None)
print("Data loaded.", flush=True)
# 提取基因列表
print("Extracting gene list...", flush=True)
genes = sorted(list(set(df['row']) | set(df['col'])))
print("Gene list extracted.", flush=True)
# 构建邻接矩阵
print("Building adjacency matrix...", flush=True)
adj_matrix = pd.DataFrame(0, columns=genes, index=genes)
print(1)
try:
    # 填充邻接矩阵
    for index, row in df.iterrows():
        row_gene = row['row']
        col_gene = row['col']
        rho = row['rho']
        adj_matrix.at[row_gene, col_gene] = rho
        adj_matrix.at[col_gene, row_gene] = rho  # 如果需要对称矩阵
except Exception as e:
    print(f"Error occurred: {e}", flush=True)
    sys.exit(1)  # 退出脚本
print("Adjacency matrix built.", flush=True)
# 可选：将邻接矩阵保存到CSV文件
print("Saving data...", flush=True)
adj_matrix.to_csv(data_path + file_name + "_adj_matrix.csv")
print("Data saved.", flush=True)
