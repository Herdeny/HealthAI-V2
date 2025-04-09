import sys
import pandas as pd
import networkx as nx

import random
import matplotlib.pyplot as plt
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
            if len(parts) >= 2:  # 至少有模块ID和一个基因
                module_id = parts[0]
                genes = parts[1:]  # 第二列开始的所有列都是基因
                # 过滤掉可能的空字符串
                genes = [gene for gene in genes if gene]
                if genes:  # 只有当有基因时才添加这个模块
                    modules[module_id] = genes
    return modules

# 读取PFN邻接表（CSV格式）
def read_pfn_csv(pfn_file):
    """读取PFN邻接表，CSV格式"""
    # 尝试按照提供的格式读取
    df = pd.read_csv(pfn_file)
    # 假设列名为 row, col, source, target, weight
    # 如果列名不一致，需要调整
    edges = []
    for _, row in df.iterrows():
        source = row[1]
        target = row[2]
        weight = row[3]
        edges.append((source, target, weight))
    return edges


# 为模块分配颜色
def assign_module_colors(modules):
    """为每个模块分配一个唯一的颜色"""
    colors = {}
    # 使用一组预定义的颜色，或者生成随机颜色
    color_list = list(plt.cm.tab20.colors) + list(plt.cm.tab20b.colors) + list(plt.cm.tab20c.colors)

    for i, module_id in enumerate(modules.keys()):
        if i < len(color_list):
            colors[module_id] = '#%02x%02x%02x' % tuple(int(255*j) for j in color_list[i][:3])
        else:
            # 如果模块数量超过预定义颜色数量，生成随机颜色
            colors[module_id] = "#{:06x}".format(random.randint(0, 0xFFFFFF))

    return colors

# 构建网络并为边分配颜色
def build_network_with_edge_colors(edges, modules, module_colors):
    """构建网络，并为边分配基于模块的颜色"""
    G = nx.Graph()

    # 添加所有边
    for source, target, weight in edges:
        G.add_edge(source, target, weight=weight)

    # 找出每个基因属于哪些模块
    gene_to_modules = defaultdict(list)
    for module_id, genes in modules.items():
        for gene in genes:
            gene_to_modules[gene].append(module_id)

    # 为边分配颜色
    edge_colors = {}
    edge_modules = {}

    for source, target in G.edges():
        # 查找两个节点共同的模块
        common_modules = set(gene_to_modules[source]) & set(gene_to_modules[target])

        if common_modules:
            # 如果有共同模块，选择第一个作为边的颜色
            # 这里可以根据需要修改选择逻辑，例如基于模块大小或重要性
            module_id = list(common_modules)[0]
            edge_colors[(source, target)] = module_colors[module_id]
            edge_modules[(source, target)] = module_id
        else:
            # 如果没有共同模块，使用默认颜色
            edge_colors[(source, target)] = "#CCCCCC"  # 灰色
            edge_modules[(source, target)] = "none"

    # 将颜色信息添加到边属性
    nx.set_edge_attributes(G, edge_colors, "color")
    nx.set_edge_attributes(G, edge_modules, "module")

    return G


def save_network_info(G, output_file):
    """保存网络信息到CSV文件，包括每条边的模块归属"""
    edge_data = []
    for source, target, data in G.edges(data=True):
        edge_data.append({
            "Source": source,
            "Target": target,
            "Weight": data.get('weight', 1.0),
            "Module": data.get('module', 'none'),
            "Color": data.get('color', '#CCCCCC')
        })

    df = pd.DataFrame(edge_data)
    df.to_csv(output_file, index=False)

# 主函数
def main(module_file, pfn_file, output_file):
    # 读取模块
    modules = read_modules(module_file)

    # 读取PFN边
    edges = read_pfn_csv(pfn_file)

    # 为模块分配颜色
    module_colors = assign_module_colors(modules)

    # 构建网络并为边分配颜色
    G = build_network_with_edge_colors(edges, modules, module_colors)

    # 保存网络信息
    save_network_info(G, output_file)

    print(f"MPath:{output_file}")
    print(f"Total edges: {G.number_of_edges()}")
    print(f"Total genes: {G.number_of_nodes()}")

    # 保存模块颜色映射
    with open(output_file + ".module_colors.txt", 'w') as f:
        f.write("ModuleID\tColor\n")
        for module_id, color in module_colors.items():
            f.write(f"{module_id}\t{color}\n")

    print(f"CPath:{output_file}.module_colors.txt")


if __name__ == "__main__":
    module_file = data_path + "multiscale_significant.modules.txt"
    pfn_file = data_path + file_name
    output_file = data_path + file_name[:-4] + f"_modules.csv"
    main(module_file, pfn_file, output_file)
