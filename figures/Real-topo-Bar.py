import os

import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
import numpy as np

matplotlib.rcParams['font.family'] = 'sans-serif'
matplotlib.rcParams['font.sans-serif'] = ['DejaVu Sans']
# 获取当前脚本文件的绝对路径
current_file = os.path.abspath(__file__)

# 获取当前脚本所在的文件夹路径
current_dir = os.path.dirname(current_file)

print(current_dir)

# 读取 Excel 文件
df = pd.read_excel(os.path.join(current_dir, 'Real-topo.xlsx'))
# df = pd.read_excel('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/Real-topo.xlsx')

# 提取数据
topo = df['topo']
sec_sim = df['Second Sim']
sec_sim_k = df['Second Sim（K）']
sec_sim_w = df['Second Sim（W）']
fir_sim = df['First Sim']
fir_sim_k = df['First Sim（K）']
fir_sim_w = df['First Sim（W）']

# 设置柱状图宽度
bar_width = 0.25

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
bar1_bottom = ax.bar(np.arange(len(topo)), fir_sim, width=bar_width, color = color_redis_f,label="RCH (K=0) (Fir. Sim.)")
bar2_bottom = ax.bar(np.arange(len(topo)) + bar_width, fir_sim_k, width=bar_width, color = color_prop_f,label="RCH (K=1) (Fir. Sim.)")
bar3_bottom = ax.bar(np.arange(len(topo)) + bar_width * 2, fir_sim_w, width=bar_width, color = color_nei_f,label="WPT (Fir. Sim.)")

bar1 = ax.bar(np.arange(len(topo)), sec_sim, width=bar_width, bottom=fir_sim, color=color_redis, hatch=hatch_redis, label='RCH (K=0) (Sec. Sim.)')
# bar2_bottom = ax.bar(np.arange(len(topo)) + bar_width, scalpel_time1, width=bar_width, color=color_scalpel1,  label='First Simulation Time (R)')
bar2 = ax.bar(np.arange(len(topo)) + bar_width, sec_sim_k, width=bar_width, bottom = fir_sim_k, color=color_prop, hatch=hatch_prop, label='RCH (K=1) (Sec. Sim.)')
bar3 = ax.bar(np.arange(len(topo)) + bar_width * 2, sec_sim_w, width=bar_width, bottom = fir_sim_w, color=color_nei, hatch=hatch_nei, label='WPT (Sec. Sim.)')




# 添加标签和图例
# ax.set_xlabel('Topo',fontsize=15,weight='bold')
ax.set_ylabel('Time (s)',fontsize=19,weight='bold')
#ax.set_title('Bar Chart with Two Parts')
# ax.set_yticks(ticks)
# 纵坐标只显示整数
ax.yaxis.set_major_locator(MaxNLocator(integer=True))
ax.set_yticklabels(ax.get_yticklabels(),fontsize=16,weight='bold')
ax.set_xticks(np.arange(len(topo)) + bar_width/2)
ax.set_xticklabels(topo,fontsize=16,weight='bold')
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
fig.set_size_inches(7.5,4)
# plt.gca().spines['top'].set_visible(False)
# plt.gca().spines['right'].set_visible(False)
plt.tight_layout()
# plt.savefig('/home/gaohan/Scalpel-batfish/eval_data_nsdi26/Real-topo.pdf')
plt.savefig(os.path.join(current_dir, 'Real-topo.pdf'), bbox_inches='tight', pad_inches=0.05)

# 最后关闭图表
# plt.close()