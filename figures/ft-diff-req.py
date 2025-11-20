import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
import pandas as pd

import os
import matplotlib
matplotlib.use("Agg")
matplotlib.rcParams['font.family'] = 'sans-serif'
matplotlib.rcParams['font.sans-serif'] = ['DejaVu Sans']

# 获取当前脚本文件的绝对路径
current_file = os.path.abspath(__file__)

# 获取当前脚本所在的文件夹路径
current_dir = os.path.dirname(current_file)

print(current_dir)

# df = pd.read_excel('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/ft-diff-req.xlsx')

# # 提取数据
# # topo = df['topo'][:8]#到fattree18
# req = df['req']
# k0 = df['k=0']
# k1 = df['k=1']

# 数据
req = [70, 210, 350, 490, 630, 770, 910, 1050, 1190, 1330, 1470]
k0 =  [4.759, 7.201, 9.619, 12.750, 14.948, 17.756, 19.991, 22.603, 25.394, 28.033, 30.609]
k1 =  [4.714, 8.411, 12.354, 15.079, 18.101, 21.654, 25.247, 27.786, 34.547, 35.134, 38.669]

# 创建图形
fig, ax = plt.subplots(figsize=(6, 5))

# 折线图
ax.plot(req, k0, marker='o', label='RCH (K=0)', linewidth=2.5)
ax.plot(req, k1, marker='s', label='RCH (K=1)', linewidth=2.5)

# 设置横坐标间隔为140
ax.set_xticks(range(min(req), max(req)+1, 140))

# 横向参考线（y 方向网格）
ax.yaxis.grid(True, linestyle='--', linewidth=1.5)
ax.xaxis.grid(False)

# 字体大小和粗细
label_font = {'fontsize': 20, 'fontweight': 'bold'}
tick_font = {'fontsize': 15, 'fontweight': 'bold'}

# 坐标轴标签
ax.set_xlabel('Intents', **label_font)
ax.set_ylabel('Time (s)', **label_font)
# ax.set_title('Request vs Time', fontsize=16, fontweight='bold')

# 坐标轴刻度字体
ax.tick_params(axis='both', labelsize=tick_font['fontsize'], width=1.8)
for label in ax.get_xticklabels() + ax.get_yticklabels():
    label.set_fontweight('bold')

# 强制Y轴整数刻度
ax.yaxis.set_major_locator(MaxNLocator(integer=True))

# 图例
font1 = {'weight' : 'bold','size' : 20}#图例加粗
ax.legend(loc='upper left', bbox_to_anchor=(0, 1), prop=font1)

# 设置坐标轴轮廓线粗度
for spine in ax.spines.values():
    spine.set_linewidth(2)
    
fig.set_size_inches(9,5)

# plt.tight_layout()
# plt.show()


# plt.savefig('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/ft-diff-req-line-plot.pdf')
plt.savefig(os.path.join(current_dir, 'ft-diff-req-line-plot.pdf'), bbox_inches='tight', pad_inches=0.05)

