import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

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

# 读取 Excel 文件
# df = pd.read_excel('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/MultiProtocol.xlsx')
df = pd.read_excel(os.path.join(current_dir, 'MultiProtocol.xlsx'))

# 提取数据
topo = df['topo']
redis_time = df['Redistribution']
propagation_time = df['Propagation']
neighbor_time = df['Neighboring']
redis_first = df['FirstSimR']
prop_first = df['FirstSimP']
nei_first = df['FirstSimN']

# 设置柱状图宽度
bar_width = 0.13

# 设置颜色和填充样式
color_prop = '#BCF58D'
color_redis = '#F8D3BB'
color_nei = '#c5defa'
color_prop_f = '#2A9D8C'
color_redis_f = '#F09672'
color_nei_f = '#016190'
color_scalpel3 = '#A8DADB'
hatch_redis = '--'  # 斜杠填充
hatch_prop = "\\\\"
hatch_nei = "//"


fig, ax = plt.subplots()
bar1_bottom = ax.bar(np.arange(len(topo)), redis_first, width=bar_width, color = color_redis_f,label="Redistribution (Fir. Sim.)")
bar2_bottom = ax.bar(np.arange(len(topo)) + bar_width, prop_first, width=bar_width, color = color_prop_f,label="Propagation (Fir. Sim.)")
bar3_bottom = ax.bar(np.arange(len(topo)) + bar_width * 2, nei_first, width=bar_width, color = color_nei_f,label="Neighboring (Fir. Sim.)")

bar1 = ax.bar(np.arange(len(topo)), redis_time, width=bar_width, bottom=redis_first, color=color_redis, hatch=hatch_redis, label='Redistribution (Sec. Sim.)')
# bar2_bottom = ax.bar(np.arange(len(topo)) + bar_width, scalpel_time1, width=bar_width, color=color_scalpel1,  label='First Simulation Time (R)')
bar2 = ax.bar(np.arange(len(topo)) + bar_width, propagation_time, width=bar_width, bottom = prop_first, color=color_prop, hatch=hatch_prop, label='Propagation (Sec. Sim.)')
bar3 = ax.bar(np.arange(len(topo)) + bar_width * 2, neighbor_time, width=bar_width, bottom = nei_first, color=color_nei, hatch=hatch_nei, label='Neighboring (Sec. Sim.)')




# 添加标签和图例
# ax.set_xlabel('Topo',fontsize=15,weight='bold')
ax.set_ylabel('Time (s)',fontsize=19,weight='bold')
#ax.set_title('Bar Chart with Two Parts')
# ax.set_yticks(ticks)
ax.set_yticklabels(ax.get_yticklabels(),fontsize=14,weight='bold')
ax.set_xticks(np.arange(len(topo)) + bar_width)
ax.set_xticklabels(topo,fontsize=15,weight='bold')
font1 = {'weight' : 'bold','size' : 13}#图例加粗
# ax.legend(prop=font1,loc='upper left')
ax.legend(loc='upper left', bbox_to_anchor=(0, 1), prop=font1)
# 显示柱状图
# plt.show()
bwith = 2
ax.spines['bottom'].set_linewidth(bwith)
ax.spines['left'].set_linewidth(bwith)
ax.spines['top'].set_linewidth(bwith)
ax.spines['right'].set_linewidth(bwith)
fig.set_size_inches(7,4)
# plt.gca().spines['top'].set_visible(False)
# plt.gca().spines['right'].set_visible(False)
plt.tight_layout()
# plt.savefig('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/Multi-protocol-Bar.pdf')

plt.savefig(os.path.join(current_dir, 'Multi-protocol-Bar.pdf'), bbox_inches='tight', pad_inches=0.05)

# 最后关闭图表
# plt.close()