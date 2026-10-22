#!/usr/bin/env python3
"""Generate the full zh_cn.json for the tweelix_hints resource pack.

Source : run/exports/command_keys.json  ({"keys": {key: "Please translate: ..."}})
Target : src/main/resources/resourcepacks/command_hints/assets/tweelix_hints/lang/zh_cn.json

Rules:
  * key set is copied verbatim (no additions, no removals)
  * every value is non-empty Chinese
  * gamerule: "游戏规则：<name>" and "<值>" for .value.description
  * existing translations from the old pack are reused when the key matches
"""
import json
import re
import shutil
from datetime import datetime
from pathlib import Path

TREE = Path("run/exports/command_keys.json")
TARGET = Path("src/main/resources/resourcepacks/command_hints/assets/tweelix_hints/lang/zh_cn.json")
BACKUP_SRC = Path("tools/backup/zh_cn.json.bak")

ROOT_MAP = {
    "advancement": "进度", "attribute": "属性", "execute": "执行命令", "bossbar": "Boss 栏",
    "clear": "清除物品", "clone": "克隆方块", "compute": "计算", "damage": "造成伤害",
    "data": "获取或修改数据", "datapack": "数据包", "debug": "调试",
    "defaultgamemode": "默认游戏模式", "dialog": "对话框", "difficulty": "难度",
    "effect": "状态效果", "me": "发送动作消息", "enchant": "附魔", "experience": "经验",
    "xp": "经验（别名）", "fill": "填充方块", "fillbiome": "填充生物群系",
    "forceload": "强制加载区块", "function": "函数", "gamemode": "游戏模式",
    "gamerule": "游戏规则", "give": "给予物品", "help": "帮助", "item": "物品栏操作",
    "kick": "踢出玩家", "kill": "杀死实体", "list": "列出玩家",
    "locate": "定位结构、生物群系或兴趣点", "loot": "战利品", "msg": "发送私信",
    "tell": "发送私信（别名）", "w": "发送私信（别名）", "swing": "挥动手臂",
    "particle": "粒子", "place": "放置地物、结构或模板", "playsound": "播放声音",
    "posteffect": "后处理效果", "random": "随机", "reload": "重新加载", "recipe": "配方",
    "fetchprofile": "获取玩家档案", "return": "返回值", "ride": "骑乘或下骑",
    "rotate": "旋转实体", "say": "发送消息", "schedule": "调度函数",
    "scoreboard": "计分板", "seed": "世界种子", "version": "版本",
    "setblock": "设置方块", "spawnpoint": "设置出生点", "setworldspawn": "设置世界出生点",
    "spectate": "旁观", "spreadplayers": "分散玩家", "stopsound": "停止声音",
    "stopwatch": "秒表", "summon": "召唤实体", "tag": "标签", "team": "队伍",
    "teammsg": "发送队伍消息", "tm": "发送队伍消息（别名）", "teleport": "传送",
    "tp": "传送（别名）", "tellraw": "发送原始 JSON 消息", "test": "测试", "tick": "刻",
    "time": "时间", "title": "标题", "trigger": "触发计分板目标", "waypoint": "路径点",
    "weather": "天气", "worldborder": "世界边界", "jfr": "JFR 性能分析", "raid": "袭击",
    "debugpath": "调试路径", "debugmobspawning": "调试生物生成",
    "warden_spawn_tracker": "监守者生成追踪器", "spawn_armor_trims": "生成盔甲纹饰",
    "serverpack": "服务器资源包", "publish": "发布到局域网", "unpublish": "取消发布",
    "fcc": "Fabric 客户端命令", "fabric-command-api-v2:client": "Fabric 客户端命令 API",
}

RULE_MAP = {
    "universal_anger": "通用愤怒", "ender_pearls_vanish_on_death": "末影珍珠死亡消失",
    "entity_drops": "实体掉落", "fall_damage": "摔落伤害", "respawn_radius": "重生半径",
    "pvp": "玩家对战", "fire_spread_radius_around_player": "玩家周围火焰蔓延半径",
    "random_tick_speed": "随机刻速度", "mob_drops": "生物掉落", "advance_time": "时间流逝",
    "forgive_dead_players": "原谅死亡玩家", "spectators_generate_chunks": "旁观者生成区块",
    "spawn_mobs": "生成生物", "max_block_modifications": "最大方块修改数",
    "max_command_sequence_length": "最大命令序列长度", "lava_source_conversion": "熔岩源转化",
    "limited_crafting": "限制合成", "max_entity_cramming": "最大实体挤压",
    "tnt_explosion_drop_decay": "TNT 爆炸掉落消失", "command_block_output": "广播命令方块输出",
    "command_blocks_work": "命令方块工作", "projectiles_can_break_blocks": "弹射物可破坏方块",
    "mob_griefing": "生物破坏", "locator_bar": "定位栏", "player_movement_check": "玩家移动检查",
    "reduced_debug_info": "简化调试信息", "players_sleeping_percentage": "玩家入睡百分比",
    "block_drops": "方块掉落", "max_command_forks": "最大命令分支数",
    "immediate_respawn": "立即重生", "block_explosion_drop_decay": "方块爆炸掉落消失",
    "spawner_blocks_work": "刷怪笼工作", "fire_damage": "火焰伤害",
    "show_advancement_messages": "显示进度消息", "show_death_messages": "显示死亡消息",
    "drowning_damage": "溺水伤害", "global_sound_events": "全局声音事件",
    "allow_entering_nether_using_portals": "允许使用下界传送门",
    "max_snow_accumulation_height": "最大积雪高度", "spread_vines": "藤蔓蔓延",
    "keep_inventory": "保留物品栏", "spawn_patrols": "生成巡逻队",
    "mob_explosion_drop_decay": "生物爆炸掉落消失", "tnt_explodes": "TNT 爆炸",
    "players_nether_portal_creative_delay": "创造模式传送门延迟",
    "spawn_monsters": "生成怪物", "players_nether_portal_default_delay": "默认传送门延迟",
    "spawn_wardens": "生成监守者", "natural_health_regeneration": "自然生命恢复",
    "elytra_movement_check": "鞘翅移动检查", "raids": "袭击",
    "spawn_wandering_traders": "生成流浪商人", "freeze_damage": "冰冻伤害",
    "send_command_feedback": "发送命令反馈", "water_source_conversion": "水源转化",
    "log_admin_commands": "通告管理员命令", "advance_weather": "天气变化",
    "spawn_phantoms": "生成幻翼",
}

MOD_MAP = {
    "batchplayer": "批量控制假人玩家", "crosshairCopy": "复制准星", "delay": "延迟执行",
    "emptyInventory": "清空物品栏", "exportkeys": "导出翻译键",
    "openConfigGui": "打开配置界面", "vault": "保险库", "stop": "停止",
}

# 末段节点翻译不够自然的少数键
SPECIAL = {
    "tweelix.command.vault.at.description": "坐标",
    "tweelix.command.batchplayer.prefix.start.description": "起始编号",
    "tweelix.command.delay.all.description": "命令与时间",
    # 介词/上下文节点
    "commands.execute.at.description": "坐标",
    "commands.execute.in.description": "维度",
    "commands.execute.on.description": "实体关系",
    "commands.damage.target.amount.damageType.at.description": "坐标",
    "commands.time.of.description": "时钟",
    "commands.raid.start.description": "开始袭击",
    # 用词统一
    "commands.waypoint.list.description": "列出路径点",
    "commands.waypoint.modify.description": "修改路径点",
    "commands.team.modify.team.collisionRule.pushOtherTeams.description": "推其他队伍",
    "commands.team.modify.team.collisionRule.pushOwnTeam.description": "推己方队伍",
    "commands.spawn_armor_trims.*_lag_my_game.description": "通配",
    # clone 的 from/to 是坐标端点
    "commands.clone.from.description": "起点",
    "commands.clone.begin.end.to.description": "终点",
    "commands.clone.from.sourceDimension.begin.end.to.description": "终点",
}

TOKEN_MAP = {
    # 通用/参数
    "targets": "目标", "target": "目标", "entity": "实体", "entities": "实体",
    "players": "玩家", "player": "玩家", "pos": "坐标", "positioned": "定位",
    "block": "方块", "blocks": "方块区域", "biome": "生物群系", "dimension": "维度",
    "source": "来源", "provider": "提供者", "sourcePath": "来源路径", "sourceSlots": "来源槽位",
    "sourceObjective": "来源计分项", "sourceDimension": "来源维度",
    "targetPath": "目标路径", "targetObjective": "目标计分项",
    "targetDimension": "目标维度", "targetPos": "目标坐标",
    "compute": "计算", "computePos": "计算坐标", "computeTarget": "计算目标",
    "store": "存储", "result": "结果", "success": "成功", "storage": "存储",
    "name": "名称", "id": "标识", "value": "值", "string": "字符串",
    "float": "浮点", "integer": "整数", "int": "整数", "short": "短整", "long": "长整",
    "double": "双精度", "byte": "字节", "scale": "倍率", "base": "基础",
    "set": "设置", "reset": "重置", "modifier": "修饰符", "add": "添加",
    "remove": "移除", "run": "运行", "if": "如果", "unless": "除非",
    "loaded": "已加载", "score": "分数", "matches": "匹配", "range": "范围",
    "start": "起点", "end": "终点", "destination": "目标位置", "all": "全部",
    "masked": "遮蔽", "predicate": "谓词", "function": "函数", "stopwatch": "秒表",
    "path": "路径", "items": "物品", "item": "物品", "slots": "槽位",
    "as": "作为", "at": "在", "store_result": "存储结果", "max": "最大",
    "positioned_over": "定位在上方", "heightmap": "高度图", "rotated": "旋转",
    "rot": "旋转", "facing": "朝向", "anchor": "锚点", "align": "对齐",
    "axes": "轴", "anchored": "锚定", "in": "在", "summon": "召唤", "on": "在",
    "owner": "主人", "leasher": "拴绳者", "attacker": "攻击者", "vehicle": "载具",
    "controller": "控制者", "origin": "原点", "passengers": "乘客", "list": "列表",
    "color": "颜色", "style": "样式", "progress": "进度", "visible": "可见",
    "begin": "开始", "replace": "替换", "force": "强制", "move": "移动",
    "normal": "普通", "filtered": "过滤", "filter": "过滤器", "strict": "严格",
    "to": "到…", "default": "默认", "amount": "数量", "location": "位置",
    "by": "由", "cause": "原因", "merge": "合并", "nbt": "NBT", "modify": "修改",
    "insert": "插入", "index": "索引", "prepend": "前插", "append": "追加",
    "enable": "启用", "disable": "禁用", "available": "可用", "enabled": "已启用",
    "create": "创建", "description": "描述", "existing": "现有", "last": "最后",
    "first": "第一", "peaceful": "和平", "easy": "简单", "hard": "困难",
    "clear": "清除", "give": "给予", "infinite": "无限", "amplifier": "倍率",
    "hideParticles": "隐藏粒子", "action": "动作", "enchantment": "附魔",
    "level": "等级", "points": "点数", "levels": "等级", "outline": "轮廓",
    "hollow": "空心", "destroy": "破坏", "keep": "保留", "query": "查询",
    "arguments": "参数", "with": "使用", "notched_6": "缺口 6", "notched_10": "缺口 10",
    "notched_12": "缺口 12", "notched_20": "缺口 20", "pink": "粉色", "blue": "蓝色",
    "red": "红色", "green": "绿色", "yellow": "黄色", "purple": "紫色", "white": "白色",
    "mainhand": "主手", "offhand": "副手", "fish": "钓鱼", "loot": "战利品",
    "loot_table": "战利品表", "kill": "击杀", "mine": "挖掘", "tool": "工具",
    "slot": "槽位", "count": "数量", "message": "消息", "reason": "原因",
    "uuid": "UUID", "hash": "哈希", "url": "URL", "allowCommands": "允许命令",
    "port": "端口", "pattern": "图案", "warning_level": "警告等级",
    "omenlvl": "不祥之兆等级", "type": "类型", "monster": "怪物", "creature": "生物",
    "ambient": "环境", "axolotls": "美西螈", "water_creature": "水生生物",
    "water_ambient": "水下环境", "underground_water_creature": "地下水生生物",
    "misc": "杂项", "spawnleader": "生成队长", "setomen": "设置不祥之兆",
    "glow": "发光", "sound": "声音", "check": "检查", "spawn": "生成",
    "take": "取出", "fail": "失败", "mount": "骑乘", "dismount": "下骑",
    "rotation": "旋转", "schedule": "调度", "time": "时间", "objectives": "目标",
    "objective": "目标", "criteria": "准则", "criterion": "准则",
    "displayName": "显示名称", "displayname": "显示名称", "rendertype": "渲染类型",
    "hearts": "心", "displayautoupdate": "自动更新显示", "numberformat": "数字格式",
    "blank": "空白", "fixed": "固定", "contents": "内容", "styled": "样式化",
    "setdisplay": "设置显示", "operation": "操作", "seed": "种子", "version": "版本",
    "spectate": "旁观", "master": "主音量", "music": "音乐", "record": "唱片",
    "weather": "天气", "hostile": "敌对", "neutral": "中立", "voice": "语音",
    "ui": "界面", "restart": "重启", "tag": "标签", "team": "队伍",
    "members": "成员", "leave": "离开", "join": "加入", "empty": "清空",
    "friendlyFire": "友伤", "allowed": "允许", "seeFriendlyInvisibles": "看见友方隐身",
    "nametagVisibility": "名称标签可见性", "never": "从不",
    "hideForOtherTeams": "对其他队伍隐藏", "hideForOwnTeam": "对己方队伍隐藏",
    "always": "始终", "deathMessageVisibility": "死亡消息可见性",
    "collisionRule": "碰撞规则", "pushOwnTeam": "推己方队伍",
    "pushOtherTeams": "推其他队伍", "prefix": "前缀", "suffix": "后缀",
    "verify": "验证", "resetclosest": "重置最近的", "resetthese": "重置这些",
    "resetthat": "重置那个", "clearthat": "清除那个", "clearthese": "清除这些",
    "clearall": "清除全部", "radius": "半径", "var": "变量", "width": "宽度",
    "height": "高度", "depth": "深度", "export": "导出", "tick": "刻",
    "rate": "速率", "step": "步进", "sprint": "冲刺", "unfreeze": "解冻",
    "freeze": "冻结", "pause": "暂停", "resume": "恢复", "timemarker": "时间标记",
    "timeline": "时间线", "repetition": "重复", "gametime": "游戏时间",
    "of": "的", "clock": "时钟", "subtitle": "副标题", "actionbar": "动作栏",
    "times": "时间", "fadeIn": "淡入", "stay": "停留", "fadeOut": "淡出",
    "trigger": "触发", "waypoint": "路径点", "hex": "十六进制",
    "worldborder": "世界边界", "distance": "距离", "damagePerBlock": "每方块伤害",
    "buffer": "缓冲", "warning": "警告", "jfr": "JFR", "raid": "袭击",
    "push": "推送", "pop": "弹出", "publish": "发布", "unpublish": "取消发布",
    "openConfigGui": "打开配置界面", "crosshairCopy": "复制准星",
    "emptyInventory": "清空物品栏", "exportkeys": "导出翻译键", "delay": "延迟",
    "batchplayer": "批量玩家", "vault": "保险库", "stop": "停止",
    "direction": "方向", "startNumber": "起始编号", "subcommand": "子命令",
    # 缺失词表
    "*": "全部", "*_lag_my_game": "延迟我的游戏（通配）", "<": "小于", "<=": "小于等于",
    "=": "等于", ">": "大于", ">=": "大于等于",
    "add_multiplied_base": "乘以基础值", "add_multiplied_total": "乘以总值",
    "add_value": "加上值", "animation": "动画", "attacker": "攻击者",
    "by": "由", "cause": "原因", "check": "检查", "contents": "内容",
    "controller": "控制者", "damageType": "伤害类型", "delta": "增量",
    "display": "显示", "end": "终点", "everything": "全部",
    "exportclosest": "导出最近的", "exportthat": "导出那个", "exportthese": "导出这些",
    "feature": "地物", "fillbiome": "填充生物群系", "fish": "钓鱼", "fixed": "固定",
    "force": "强制", "grant": "授予", "hearts": "心", "height": "高度",
    "includeSequenceId": "包含序列 ID", "includeWorldSeed": "包含世界种子",
    "integrity": "完整度", "item_predicate": "物品谓词", "jigsaw": "拼图",
    "leasher": "拴绳者", "maxCount": "最大数量", "maxHeight": "最大高度",
    "maxRange": "最大范围", "max_depth": "最大深度", "minVolume": "最小音量",
    "mirror": "镜像", "numberOfTimes": "次数", "only": "仅",
    "onlyRequiredTests": "仅必需测试", "over": "在上方", "override": "覆盖",
    "owner": "主人", "pitch": "音调", "pool": "池", "prepend": "前插",
    "progress": "进度", "respectTeams": "尊重队伍", "revoke": "撤销",
    "roll": "掷出", "rotationSteps": "旋转步数", "runclosest": "运行最近的",
    "runfailed": "运行失败的", "runmultiple": "运行多个", "runthat": "运行那个",
    "runthese": "运行这些", "sequence": "序列", "serverpack": "服务器资源包",
    "sourceDimension": "来源维度", "spreadDistance": "分散距离",
    "targetDimension": "目标维度", "template": "模板", "tests": "测试",
    "testsPerRow": "每行测试数", "through": "穿过", "timemarker": "时间标记",
    "tool": "工具", "under": "在下方", "until": "直到", "untilFailed": "直到失败",
    "url": "URL", "uuid": "UUID", "verify": "验证", "viewers": "查看者",
    "volume": "音量", "warning_level": "警告等级", "with": "使用",
    "x": "X 坐标", "y": "Y 坐标", "z": "Z 坐标",
    # 二轮补齐（纯英文残留）
    "advancement": "进度", "attribute": "属性", "bossbar": "Boss 栏", "center": "中心",
    "data": "数据", "duration": "持续时间", "fill": "填充", "from": "从",
    "get": "获取", "locate": "定位", "position": "位置", "posteffect": "后处理效果",
    "recipe": "配方", "speed": "速度", "structure": "结构", "test": "测试",
    "command": "命令", "help": "帮助", "from": "从…",
}

WORD_MAP = {
    "source": "来源", "target": "目标", "path": "路径", "slot": "槽位", "slots": "槽位",
    "dimension": "维度", "objective": "计分项", "pos": "坐标", "compute": "计算",
    "max": "最大", "min": "最小", "count": "数量", "height": "高度", "range": "范围",
    "depth": "深度", "rotation": "旋转", "steps": "步数", "spread": "分散",
    "distance": "距离", "tests": "测试", "per": "每", "row": "行", "include": "包含",
    "sequence": "序列", "world": "世界", "seed": "种子", "only": "仅", "required": "必需",
    "number": "数量", "of": "", "times": "次", "until": "直到", "failed": "失败",
    "display": "显示", "auto": "自动", "update": "更新", "name": "名称",
    "item": "物品", "predicate": "谓词", "loot": "战利品", "table": "表",
    "run": "运行", "closest": "最近的", "these": "这些", "that": "那个",
    "multiple": "多个", "export": "导出", "reset": "重置", "clear": "清除",
    "all": "全部", "start": "起始", "end": "结束", "subcommand": "子命令",
    "lag": "延迟", "my": "我的", "game": "游戏",
}


def split_words(token: str):
    parts = re.split(r"[_\s]+", token)
    out = []
    for p in parts:
        out.extend(re.findall(r"[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+", p) or [p])
    return [x for x in out if x]


def fallback(token: str) -> str:
    parts = split_words(token)
    zh = "".join(WORD_MAP.get(p.lower(), "") for p in parts)
    return zh or token


def last_token(key: str) -> str:
    return key[:-len(".description")].split(".")[-1]


def main():
    tree = json.load(open(TREE, encoding="utf-8"))["keys"]
    old = json.load(open(BACKUP_SRC, encoding="utf-8")) if BACKUP_SRC.exists() else {}

    # rule name -> Chinese, preferring the old pack's human translation
    rules = dict(RULE_MAP)
    for k, v in old.items():
        m = re.match(r"commands\.gamerule\.([^.]+)\.description$", k)
        if m and m.group(1) not in rules:
            rules[m.group(1)] = v.removeprefix("游戏规则：")

    out = {}
    for key in tree:
        if key == "tweelix.command.description":
            out[key] = "Tweelix 命令"
            continue

        if key in SPECIAL:
            out[key] = SPECIAL[key]
            continue

        if key.startswith("tweelix.command."):
            parts = key[:-len(".description")].split(".")
            if len(parts) == 3:
                name = parts[2]
                out[key] = MOD_MAP.get(name) or TOKEN_MAP.get(name) or fallback(name)
            else:
                tok = parts[-1]
                out[key] = TOKEN_MAP.get(tok) or MOD_MAP.get(tok) or fallback(tok)
            continue

        if key.startswith("commands.gamerule."):
            if key.endswith(".value.description"):
                out[key] = "<值>"
                continue
            m = re.match(r"commands\.gamerule\.(minecraft:)?([^.]+)\.description$", key)
            if m:
                rule = m.group(2)
                out[key] = "游戏规则：" + rules.get(rule, fallback(rule))
                continue

        if key.startswith("commands.") and key.count(".") == 2:
            root = key.split(".")[1]
            out[key] = ROOT_MAP.get(root) or TOKEN_MAP.get(root) or fallback(root)
            continue

        if key in old:
            out[key] = old[key]
            continue

        tok = last_token(key)
        out[key] = TOKEN_MAP.get(tok) or fallback(tok)

    # 合并旧包里「不在命令树」的值层 key（如 time.set.day）。
    # gamerule.<规则>.value 在 v1 匹配里不可达，直接舍弃。
    tree_keys = set(tree)
    for key, value in old.items():
        if key in tree_keys or key in out:
            continue
        if key.startswith("commands.gamerule.") and key.endswith(".value.description"):
            continue
        out[key] = value

    # safety: never empty
    for k, v in out.items():
        if not isinstance(v, str) or not v.strip():
            out[k] = fallback(last_token(k)) or "参数"

    # backup current target, then write
    if TARGET.exists():
        bdir = Path("tools/backup")
        bdir.mkdir(parents=True, exist_ok=True)
        shutil.copy2(TARGET, bdir / f"zh_cn.before-full.{datetime.now():%Y%m%d-%H%M%S}.bak")

    TARGET.write_text(json.dumps(out, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print("source keys =", len(tree))
    print("written keys =", len(out))
    print("missing =", len(set(tree) - set(out)), " extra =", len(set(out) - set(tree)))
    print("empty values =", sum(1 for v in out.values() if not v.strip()))
    ascii_only = [k for k, v in out.items() if re.fullmatch(r"[ -~]+", v)]
    print("ascii-only values =", len(ascii_only), ascii_only[:10])


if __name__ == "__main__":
    main()
