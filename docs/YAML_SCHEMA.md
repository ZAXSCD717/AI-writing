# 剧本 YAML Schema 设计文档

## 概述

本文档定义了 AI 小说转剧本工具所使用的剧本结构化 Schema（JSON Schema / YAML Schema），
描述了从小说文本到剧本的标准化转换格式。

---

## 设计原则

### 1. 忠实于剧本创作规范

本 Schema 参照了主流剧本创作规范（Screenplay Format / 分场剧本格式），
保留了「幕 → 场 → 对白/动作/旁白」的经典层级结构，使生成的剧本可直接用于实际拍摄或排演。

### 2. 易于机器生成与人工编辑

采用 YAML 格式（而非 JSON）作为主要序列化格式，原因：

- YAML 可读性更强，适合作者直接在文本编辑器中修改
- 支持注释，作者可在剧本中添加标注
- 比 JSON 更简洁，减少括号噪音
- 天然支持多行字符串（用于大段旁白/对白）

### 3. 保留改编可追溯性

- 保留 `source` 字段记录原著信息，方便对照
- 角色表（`characters`）独立于剧情结构之外，便于统一管理
- 角色通过 `id` 引用，确保同一角色在不同场次中一致

### 4. 扩展性

- 所有 `type` 字段均设计为可扩展枚举
- 可以方便地添加新的剧本元素类型（如 `monologue`、`voice_over`、`flashback`）
- 场景设置（`setting`）支持自定义属性

---

## Schema 完整定义

```yaml
# 剧本根节点
script:
  type: object
  required: [metadata, characters, acts]
  properties:
    metadata:
      type: object
      description: 剧本元信息
      required: [title, source, author, version, created_at]
      properties:
        title:
          type: string
          description: 剧本标题（通常为「原著名称·剧本版」）
          example: "《三体》·剧本版"
        source:
          type: string
          description: 原著小说标题
          example: "三体"
        source_author:
          type: string
          description: 原著作者
          example: "刘慈欣"
        adapter:
          type: string
          description: 改编者（可由 AI 生成后人工确认）
          example: "AI 辅助改编"
        version:
          type: string
          description: 剧本版本号，遵循语义化版本
          pattern: "^\d+\.\d+\.\d+$"
          example: "1.0.0"
        created_at:
          type: string
          format: date
          description: 创建日期
          example: "2026-06-05"
        description:
          type: string
          description: 剧本简介，概括剧情主线
          example: "文化大革命时期，天体物理学家叶文洁..."
        tags:
          type: array
          items:
            type: string
          description: 标签（如：科幻/悬疑/爱情）
          example: ["科幻", "史诗"]

    characters:
      type: array
      description: 角色表
      items:
        type: object
        required: [id, name]
        properties:
          id:
            type: string
            description: 角色唯一标识，供对话中引用
            pattern: "^char_\d+$"
            example: "char_001"
          name:
            type: string
            description: 角色名称
            example: "叶文洁"
          alias:
            type: array
            items:
              type: string
            description: 别名/昵称列表
            example: ["叶老师", "统帅"]
          gender:
            type: string
            enum: [男, 女, 未知]
            description: 性别
          age:
            type: string
            description: 年龄或年龄段
            example: "28岁"
          personality:
            type: array
            items:
              type: string
            description: 性格特征标签
            example: ["沉稳", "理性", "坚毅"]
          description:
            type: string
            description: 角色详细描述（外貌/背景/动机）
            example: "天体物理学家，对人类文明失望..."

    acts:
      type: array
      description: 幕（剧本的一级结构单元，通常对应故事的开端/发展/高潮/结局）
      items:
        type: object
        required: [id, title, order, scenes]
        properties:
          id:
            type: string
            pattern: "^act_\d+$"
            example: "act_001"
          title:
            type: string
            description: 幕标题
            example: "第一幕：三体游戏"
          order:
            type: integer
            description: 幕的序号，决定播放/阅读顺序
            minimum: 1
          summary:
            type: string
            description: 本幕剧情概要
          scenes:
            type: array
            description: 场（幕下的二级结构单元，代表连续时空中的一段剧情）
            items:
              type: object
              required: [id, title, order, elements]
              properties:
                id:
                  type: string
                  pattern: "^scene_\d+$"
                  example: "scene_001"
                title:
                  type: string
                  description: 场景标题
                  example: "红岸基地·日落"
                order:
                  type: integer
                  description: 场次序号
                  minimum: 1
                setting:
                  type: object
                  description: 场景设置
                  properties:
                    location:
                      type: string
                      description: 地点
                      example: "红岸基地·发射控制室"
                    time:
                      type: string
                      description: 时间
                      example: "1971年·秋·黄昏"
                    atmosphere:
                      type: string
                      description: 氛围渲染
                      example: "压抑、沉闷，仪器指示灯忽明忽暗"
                    props:
                      type: array
                      items:
                        type: string
                      description: 关键道具
                      example: ["无线电接收器", "星图"]
                elements:
                  type: array
                  description: |-
                    剧本元素列表（核心内容）。
                    支持以下 type：
                    - narration:   旁白/场景描写
                    - dialogue:    对白（含 speaker 引用 + 动作指示）
                    - action:      动作/行为描述
                    - transition:  转场效果
                  items:
                    type: object
                    oneOf:
                      # --- 旁白/场景描写 ---
                      - properties:
                          type:
                            type: string
                            enum: [narration]
                          content:
                            type: string
                            description: 旁白内容或场景描写文本
                            example: "夕阳透过百叶窗在控制台上投下条纹状的阴影。"
                        required: [type, content]

                      # --- 对白 ---
                      - properties:
                          type:
                            type: string
                            enum: [dialogue]
                          speaker:
                            type: string
                            description: 说话角色 ID（引用 characters[].id）
                            example: "char_001"
                          line:
                            type: string
                            description: 对白文本
                            example: "你相信有外星文明吗？"
                          direction:
                            type: string
                            description: 舞台指示/动作表情描述
                            example: "凝视着远方，语气低沉"
                        required: [type, speaker, line]

                      # --- 动作描述 ---
                      - properties:
                          type:
                            type: string
                            enum: [action]
                          content:
                            type: string
                            description: 动作/行为描述
                            example: "他缓缓站起身，走到窗前，推开了那扇尘封已久的窗户。"
                        required: [type, content]

                      # --- 转场 ---
                      - properties:
                          type:
                            type: string
                            enum: [transition]
                          content:
                            type: string
                            description: 转场效果
                            enum: [淡入, 淡出, 切, 划, 黑屏, 闪回, 闪前]
                        required: [type, content]
```

---

## 完整示例

以下是一个符合 Schema 的完整 YAML 剧本示例，展示三章小说内容转换后的剧本结构：

```yaml
script:
  metadata:
    title: "《三体》·剧本版·节选"
    source: "三体"
    source_author: "刘慈欣"
    adapter: "AI 辅助改编"
    version: "1.0.0"
    created_at: "2026-06-05"
    description: "天体物理学家叶文洁在红岸基地发现了外星文明的存在..."
    tags: ["科幻", "悬疑"]

  characters:
    - id: char_001
      name: "叶文洁"
      gender: "女"
      age: "28岁"
      personality: ["沉稳", "理性", "坚毅"]
      description: "天体物理学家，红岸基地研究员"

    - id: char_002
      name: "杨卫宁"
      gender: "男"
      age: "45岁"
      personality: ["严谨", "谨慎"]
      description: "红岸基地总工程师"

    - id: char_003
      name: "雷志成"
      gender: "男"
      age: "50岁"
      personality: ["深谋远虑", "强权"]
      description: "基地政委"

  acts:
    - id: act_001
      title: "第一幕：红岸往事"
      order: 1
      summary: "叶文洁在红岸基地发现外星文明信号，面临重大抉择"
      scenes:
        - id: scene_001
          title: "红岸基地·控制室·夜"
          order: 1
          setting:
            location: "红岸基地·地下控制室"
            time: "1971年·秋·深夜"
            atmosphere: "幽暗封闭，只有仪器屏幕的微光闪烁"
            props: ["监听耳机", "信号记录仪", "星图"]
          elements:
            - type: narration
              content: "控制室深处，叶文洁戴着监听耳机，专注地调整着频率旋钮。墙上的时钟指向凌晨三点。"
            - type: dialogue
              speaker: char_001
              line: "（自言自语）这个频率……不像是自然辐射。"
              direction: "皱眉，将音量调大"

            - type: action
              content: "她迅速在记录本上写下几组数字，手指微微颤抖。"
            - type: dialogue
              speaker: char_001
              line: "有规律……这是调制信号！"
              direction: "猛地摘下耳机，眼中充满震惊"

            - type: transition
              content: "切"

        - id: scene_002
          title: "红岸基地·通讯室"
          order: 2
          setting:
            location: "通讯室"
            time: "次日清晨"
            atmosphere: "紧张而凝重"
          elements:
            - type: narration
              content: "杨卫宁和雷志成站在 decoding 设备前，纸条上打印出一串串编码。"
            - type: dialogue
              speaker: char_002
              line: "你确定这不是来自某个大国的秘密通讯？"
              direction: "怀疑地看着叶文洁"
            - type: dialogue
              speaker: char_001
              line: "我检查过所有已知频段，扫描了三个月。这个信号的频率特征不属于人类已知的任何发射源。"
              direction: "冷静地展开频谱分析图"
            - type: dialogue
              speaker: char_003
              line: "这件事到此为止。对谁都别说。"
              direction: "语气不容置疑"

    - id: act_002
      title: "第二幕：回应的代价"
      order: 2
      summary: "叶文洁做出改变人类命运的决定"
      scenes:
        - id: scene_003
          title: "红岸基地·发射室"
          order: 1
          setting:
            location: "大功率发射室"
            time: "深夜"
            atmosphere: "孤独而决绝"
          elements:
            - type: action
              content: "叶文洁独自一人走入发射室，反锁了厚重的铁门。"
            - type: narration
              content: "她的目光在「发射」按钮上停留了很久。窗外是无尽的星空。"
            - type: dialogue
              speaker: char_001
              line: "文明间的善意，有时需要巨大的代价才能证明。"
              direction: "低声自语，眼神从犹豫变为坚定"
            - type: action
              content: "她按下了发射按钮。巨大功率以光速奔向宇宙深处。"
            - type: transition
              content: "淡出"
```

---

## Schema 版本记录

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2026-06-05 | 初始版本，定义剧本核心结构 |

---

## 设计决策记录 (ADR)

### ADR-1: 使用 YAML 而非 JSON

**决策**：采用 YAML 作为剧本的序列化格式。
**理由**：
- YAML 允许注释，作者可以在剧本中加入创作说明
- YAML 的多行字符串语法 (`|`、`>`) 更适合大段对白和旁白
- 减少括号噪音，人类可读性更好
- 主流的剧本编辑场景中，人类直接编辑 YAML 的体验优于 JSON

### ADR-2: 角色通过 ID 引用而非内联

**决策**：角色在 `characters` 数组中集中定义，对话中通过 `speaker: char_001` 引用。
**理由**：
- 确保同一角色在不同场次中的属性完全一致
- 便于 AI 解析小说时先提取角色表，再逐场生成内容
- 支持别名搜索和角色关系图的自动生成

### ADR-3: 幕（Acts）和场（Scenes）的二级结构

**决策**：采用「幕 → 场」两级结构，而非三级（幕→场→镜头）或单级。
**理由**：
- 小说改编场景下，「幕」对应故事的发展阶段（开端/发展/高潮/结局）
- 「场」对应连续时空中的剧情单元，与小说中的「章节」有较好的映射关系
- 避免「镜头」级别过于细碎，给作者留出创作空间
- 太粗（单级）无法表达剧本的层级关系，太细（三级）对 AI 生成精度要求过高

### ADR-4: 元素类型的 oneOf 设计

**决策**：`elements` 数组中的元素通过 `type` 字段区分不同类型，每种类型有独立的必填字段。
**理由**：
- 对比将所有字段扁平化（`speaker`/`line`/`direction`/`content` 全混在一起），
  oneOf 设计更清晰，每种元素类型只包含自己需要的字段
- 便于前端渲染：根据 `type` 选择不同的 UI 组件
- 便于 AI 生成：模型只需选择 type 并填充对应字段，减少幻觉
