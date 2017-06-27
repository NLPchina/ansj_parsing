ansj_parsing

本句法分析器按照ctb标注规范

````
词类标记33个
AD 副词
AS 体标记
BA 把字句标记
CC coordination conj联合短语的标记“和、或”
CD 数字
CS subordinating conjunction从属连词“如果，即使”
DEC 关系从句中的“的” 一个星期的访问   修饰性
DEG associative 定语“的”  上海的工业设施   领属
DER 得
DEV 地
DT 限定词
ETC 等，等等
FW 外来词
IJ interjection感叹词“嗯”等
JJ 名词做定语
LB 被字句标记
LC localizer方位词（上下左右内外）
M 度量衡（包括量词）
MSP 一些particles虚词，实际为（来，所，以）
NN 普通名词
NR 专有名词
NT 时间名词
OD ordinal 序数词
ON onomatopoeia 拟声词
P 介词
PN 代词
PU 标点
SB 短被动句
SP 句末particle “了”等。
VA 谓词性形容词
VC copula 系动词是
VE “有”作为动词
VV 其他动词
````

````
句法标记23个
短语标记17个
ADJP 形容词短语
ADVP 副词短语
CLP 量词短语
CP 补足语核心的小句
DNP “XP+DEG的”构成的短语
DP 限定短语
DVP “XP+DEV地”构成的短语
FRAG fragment碎片
IP 核心为I（INFL）的短语，InflectionPhrase.
LCP “XP+LC”方位短语
LST list标记（如讲稿中的，一、二、三）
NP 名词短语
PP 介词短语
PRN parenthetical插入语
QP 数量短语
UCP unidentical coordination phrase非对等同位语短语
VP 动词短语
动词复合6个标记
VCD 并列动词复合(VCD (VV 投资)    (VV 办厂))
VCP VV+VC 动词+是
VNV A不A，A一A
VPT V的R，或V不R (VPT (VV 得)   (AD 不)   (VV 到))
VRD 动词结果复合(VRD (VV 呈现) (VV 出))
VSB 定语+核心复合VSB (VV 加速) (VV 建设))

````

````
功能标记26个
ADV 副词
APP appositive 同位语
BNF beneficiary 受益
CND 条件
DIR 方向
EXT 范围
FOC 焦点
HLN 标题
IJ interjective插入语
IMP imperative祈使句
IO 间接宾语
LGS 逻辑主语
LOC 处所
MNR 方式
OBJ 直接宾语
PN 专有名词
PRD 谓词
PRP 目的或理由
Q 疑问
SBJ 主语
SHORT 缩略形式
TMP 时间
TPC 话题
TTL 标题
WH WH短语
VOC vocative呼格
````

````
空范畴标记7个
*OP* 在relative constructions相关结构中的操作符
*pro* 丢掉的论元
*PRO* 受控结构中使用
*PNR* 右部结点提升的空范畴
*T* A’移动的虚迹，话题化
* A移动的虚迹
*？* 其他未知的空范畴
````