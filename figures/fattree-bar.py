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
# df = pd.read_excel('/home/gaohan/Scalpel-batfish/eval_data/Fat tree Time.xlsx')
df = pd.read_excel(os.path.join(current_dir, 'Fat tree Time1.xlsx'))

# 提取数据
# topo = df['topo'][:8]#到fattree18
topo = df['topo']
# origin_time = df['Batfish Execution Time']
scalpel_time1 = df['First Simulation Time']
scalpel_time2 = df['Second Simulation Time']
scalpel_time3 = df['First Simulation Time(K)']
scalpel_time4 = df['Second Simulation Time(K)']

# 设置柱状图宽度
bar_width = 0.3

# 设置颜色和填充样式
color_origin = '#2A9D8C'
color_scalpel1 = '#2A9D8C'
color_scalpel2 = '#BCF58D'
color_scalpel3 = '#016190'
color_scalpel4 = '#c5defa'
hatch_pattern1 = '--'  # 斜杠填充
hatch_pattern2 = "xxx"
hatch_pattern3 = "\\\\\\"
hatch_pattern4 = "///"

# 生成两个柱状图
fig, ax = plt.subplots()
# bar1 = ax.bar(np.arange(len(topo)), origin_time, width=bar_width, color=color_origin, label='Batfish Execution Time')
scalpel_time2_bottom = ax.bar(np.arange(len(topo)), scalpel_time1, width=bar_width, color=color_scalpel1, label='RCH (K=0) (Fir. Sim.)',zorder=1)
scalpel_time2_top = ax.bar(np.arange(len(topo)), scalpel_time2, bottom=scalpel_time1, width=bar_width, hatch = hatch_pattern3,color=color_scalpel2, label='RCH (K=0) (Sec. Sim.)',zorder=2)
bar2_bottom = ax.bar(np.arange(len(topo)) + bar_width, scalpel_time3, width=bar_width, color=color_scalpel3,  label='RCH (K=1) (Fir. Sim.)',zorder=1)
bar2_top = ax.bar(np.arange(len(topo)) + bar_width, scalpel_time4, bottom=scalpel_time3, width=bar_width, hatch = hatch_pattern4, color=color_scalpel4, label='RCH (K=1) (Sec. Sim.)',zorder=2)

# scalpel_time2 = scalpel_time2_top+scalpel_time2_bottom
time2 = scalpel_time2 +scalpel_time1

# 添加标签和图例
# ax.set_ylim(bottom=1)
# ax.set_xlabel('Topo',fontsize=15,weight='bold')
ax.set_ylabel('Time (ms)',fontsize=19,weight='bold')
#ax.set_title('Bar Chart with Two Parts')
# ax.set_yticks(ticks)
ax.set_yticklabels(ax.get_yticklabels(),fontsize=15,weight='bold')
ax.set_xticks(np.arange(len(topo)) + bar_width / 2)
ax.set_xticklabels(topo,rotation=0,fontsize=19,weight='bold')
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


# for i in range(len(topo)):
#     plt.text(i, origin_time[i] + 0.5, str(origin_time[i]), ha='center',fontsize=7,weight='bold')
for i in range(len(topo)):
    plt.text(i, scalpel_time2[i]+scalpel_time1[i] , str(scalpel_time2[i]), ha='center',fontsize=5,weight='bold')
for i in range(len(topo)):
    plt.text(i+bar_width, scalpel_time3[i]+scalpel_time4[i] , str(scalpel_time4[i]), ha='center',fontsize=5,weight='bold')
plt.yscale('log')
fig.set_size_inches(9,4)
# plt.gca().spines['top'].set_visible(False)
# plt.gca().spines['right'].set_visible(False)
# plt.show()
plt.tight_layout()
# plt.savefig('/home/gaohan/Scalpel-batfish/eval_data/BGP-Fattree-Bar.pdf')
plt.savefig(os.path.join(current_dir, 'BGP-Fattree-Bar.pdf'), bbox_inches='tight', pad_inches=0.05)

# 最后关闭图表
plt.close()