import matplotlib
matplotlib.use('Agg')

from matplotlib.ticker import LogLocator
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

import os
matplotlib.rcParams['font.family'] = 'sans-serif'
matplotlib.rcParams['font.sans-serif'] = ['DejaVu Sans']

# 获取当前脚本文件的绝对路径
current_file = os.path.abspath(__file__)

# 获取当前脚本所在的文件夹路径
current_dir = os.path.dirname(current_file)

print(current_dir)

# 读取 Excel 文件
# df = pd.read_excel(r'D:\Project\Draw\comparison-Time.xlsx', sheet_name='Sheet2')
df = pd.read_excel(os.path.join(current_dir, 'comparison-Time.xlsx'), sheet_name='Sheet2')

# 设置柱状图宽度
bar_width = 0.02
gap_within_group = 0
gap_between_groups = 0.1  # 组间距
topo_labels = []
topo_centers = []

# 提取数据
topos = df['topo']
cur_x = 0
for topo in topos:
    start_x = cur_x
    center_x = (start_x + cur_x - gap_within_group - bar_width) / 2
    topo_centers.append(center_x)
    topo_labels.append(topo)
    cur_x += gap_between_groups

scalpel_time_2 = df['S2Sim (2R)']
scalpel_time_6 = df['S2Sim (6R)']
scalpel_time_10 = df['S2Sim (10R)']
cpr_time_2 = df['CPR (2R)']
cpr_time_6 = df['CPR (6R)']
cpr_time_10 = df['CPR (10R)']
cel_time_2 = df['CEL (2R)']
cel_time_6 = df['CEL  (6R)']
cel_time_10 = df['CEL (10R)']

# 设置颜色和填充样式
color_origin = '#FF4500'
color_scalpel1 = '#3CB371'
color_scalpel2 = '#4169E1'
# color1 = '#FF8C00'
# color2 = '#20B2AA'
# color3 = '#4682B4'
color1 = '#FFDAB9'
color2 = '#C1FFC1'
color3 = '#B0E2FF'
color_red = '#FF0000'
hatch_pattern1 = '---'  # 填充
hatch_pattern2 = "\\\\\\\\"
hatch_pattern3 = "xxxx"

# 生成柱状图
fig, ax = plt.subplots(figsize=(9, 4))
bar1 = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups), scalpel_time_2, color=color1,  hatch=hatch_pattern1, width=bar_width, label=r'S$^{\mathbf{2}}$Sim')
bar1_bottom = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + bar_width, cpr_time_2, color=color2, hatch=hatch_pattern2, width=bar_width, label='CPR')
bar1_top = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 2*bar_width, cel_time_2, color=color3, hatch=hatch_pattern3, width=bar_width, label='CEL')
bar2 = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 3*bar_width, scalpel_time_6, color=color1, hatch=hatch_pattern1, width=bar_width)
bar2_bottom = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 4*bar_width, cpr_time_6, color=color2, hatch=hatch_pattern2, width=bar_width)
bar2_top = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 5*bar_width, cel_time_6, color=color3, hatch=hatch_pattern3, width=bar_width)
bar3 = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 6*bar_width, scalpel_time_10, color=color1, hatch=hatch_pattern1, width=bar_width)
bar3_bottom = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 7*bar_width, cpr_time_10, color=color2, hatch=hatch_pattern2, width=bar_width)
bar3_top = ax.bar(np.arange(len(topos)) * (6 * bar_width + gap_between_groups) + 8*bar_width, cel_time_10, color=color3, hatch=hatch_pattern3, width=bar_width)

for i, topo in enumerate(topos):
    group_center = i * (6 * bar_width + gap_between_groups) + (1 * bar_width)
    ax.text(group_center, -0.02, 'S1', ha='center', va='top', fontsize=14, transform=ax.get_xaxis_transform(), rotation=-30, fontweight='bold')

    group_center_k1 = i * (6 * bar_width + gap_between_groups) + (4 * bar_width)
    ax.text(group_center_k1, -0.02, 'S2', ha='center', va='top', fontsize=14, transform=ax.get_xaxis_transform(), rotation=-30, fontweight='bold')

    group_center_k1 = i * (6 * bar_width + gap_between_groups) + (7 * bar_width)
    ax.text(group_center_k1, -0.02, 'S3', ha='center', va='top', fontsize=14, transform=ax.get_xaxis_transform(), rotation=-30, fontweight='bold')

# 添加 Topo 的标签
for i, topo in enumerate(topos):
    group_center = i * (6 * bar_width + gap_between_groups) + (4 * bar_width)  # Topo 的中心位置
    ax.text(group_center, -0.14, topo, ha='center', va='top', fontsize=18, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cpr_time_2):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 1 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color2, hatch=hatch_pattern2)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos - 0.25 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cpr_time_6):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 4 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color2, hatch=hatch_pattern2)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos - 0.25 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cpr_time_10):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 7 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color2, hatch=hatch_pattern2)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos - 0.25 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cel_time_2):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 2 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color3, hatch=hatch_pattern3)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos + 0.2 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cel_time_6):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 5 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color3, hatch=hatch_pattern3)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos + 0.2 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

for i, value in enumerate(cel_time_10):
    if value == 0:
        # 计算柱体的 x 坐标
        x_pos = i * (6 * bar_width + gap_between_groups) + 8 * bar_width
        # 绘制一个长柱子
        ax.bar(x_pos, 2000000, width=bar_width, color=color3, hatch=hatch_pattern3)
        # 在柱体上方添加 ">24h" 标记
        ax.text(x_pos + 0.2 * bar_width, 0.96, '>2h', ha='center', va='bottom', fontsize=6, color=color_red, rotation=0, transform=ax.get_xaxis_transform(), fontweight='bold')

# 添加标签和图例
# ax.set_xlabel('Topo', fontsize=12)
ax.set_ylabel('Time (ms)', fontsize=18, weight='bold')
ax.set_xticks([])
ax.set_yticks(ax.get_yticks()) 
ax.set_yticklabels([label.get_text() for label in ax.get_yticklabels()],
                   fontsize=16, weight='bold')
ax.legend(prop={'size': 13, 'weight': 'bold', 'family': 'DejaVu Sans'}, loc='upper left')
# legend.get_frame().set_alpha(0.2)
plt.yscale('log')

# 显示柱状图
# plt.show()

plt.tight_layout(rect=[0, 0, 1, 1])
# plt.subplots_adjust(left=0.1, right=0.98, top=0.98, bottom=0.15)
bwith = 2
ax.spines['bottom'].set_linewidth(bwith)
ax.spines['left'].set_linewidth(bwith)
ax.spines['top'].set_linewidth(bwith)
ax.spines['right'].set_linewidth(bwith)

# plt.savefig(r'D:\Project\Draw\eval-comparison-k.pdf')
plt.savefig(os.path.join(current_dir, 'eval-comparison-k.pdf'))

# 最后关闭图表
plt.close()