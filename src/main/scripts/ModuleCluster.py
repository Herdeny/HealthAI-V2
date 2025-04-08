import sys
import pandas as pd
import networkx as nx
from collections import defaultdict

data_path = sys.argv[1]
file_name = sys.argv[2]


# 读取MEGENA聚类结果
def read_modules(module_file):
    """读取模块文件，格式为每行第一列是模块ID，后续列是基因名"""
    modules = {}
    with open(module_file, 'r') as f:
        for line in f:
            parts = line.strip().split('\t')
            if len(parts) >= 2:
                module_id = parts[0]
                genes = [gene for gene in parts[1:] if gene]
                if genes:
                    modules[module_id] = genes
    return modules


# 读取PFN邻接表（CSV格式）
def read_pfn_csv(pfn_file):
    """读取PFN邻接表，CSV格式"""
    df = pd.read_csv(pfn_file)
    edges = []
    for _, row in df.iterrows():
        source = row[1]
        target = row[2]
        weight = row[3]
        edges.append((source, target, weight))
    return edges


# 构建网络并为边分配模块标签
def build_network_with_module_labels(edges, modules):
    """构建网络，并为边分配基于模块的标签"""
    G = nx.Graph()
    for source, target, weight in edges:
        G.add_edge(source, target, weight=weight)

    gene_to_modules = defaultdict(list)
    for module_id, genes in modules.items():
        for gene in genes:
            gene_to_modules[gene].append(module_id)

    edge_modules = {}
    for source, target in G.edges():
        common_modules = set(gene_to_modules[source]) & set(gene_to_modules[target])
        if common_modules:
            module_id = list(common_modules)[0]
        else:
            module_id = "none"
        edge_modules[(source, target)] = module_id

    nx.set_edge_attributes(G, edge_modules, "module")
    return G


# 保存网络信息到CSV文件
def save_network_info(G, output_file):
    """保存网络信息到CSV文件"""
    edge_data = []
    for source, target, data in G.edges(data=True):
        edge_data.append({
            "Source": source,
            "Target": target,
            "Weight": data.get('weight', 1.0),
            "Module": data.get('module', 'none')
        })

    df = pd.DataFrame(edge_data)
    df.to_csv(output_file, index=False)


# 主函数
def main(module_file, pfn_file, output_file):
    modules = read_modules(module_file)
    edges = read_pfn_csv(pfn_file)
    G = build_network_with_module_labels(edges, modules)
    save_network_info(G, output_file)
    print(f"Cluster results has been saved to {output_file}")
    print(f"total edges: {G.number_of_edges()}")
    print(f"total genes: {G.number_of_nodes()}")


if __name__ == "__main__":
    module_file = data_path + "multiscale_significant.modules.txt"
    pfn_file = data_path + file_name
    output_file = data_path + file_name[:-4] + f"_modules.csv"
    main(module_file, pfn_file, output_file)
