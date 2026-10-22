#!/usr/bin/env python3
"""Collapse same-token, same-value hint keys into commands.generic.<token>.

The v1 matcher already falls back to
    commands.generic.<suggestion>.description
when no path-specific key exists, so replacing N identical keys with one
generic key keeps the displayed text unchanged while shrinking the file.
"""
import json
import re
import shutil
from datetime import datetime
from pathlib import Path

PACK = Path("src/main/resources/resourcepacks/command_hints/assets/tweelix_hints/lang/zh_cn.json")
REPORT = Path("tools/hint-simplify-report.md")

# 统一措辞后再折叠（这些 token 的旧翻译混入了上下文后缀）
NORMALIZE = {
    "entity": "实体",
    "replace": "替换",
    "strict": "严格",
    "block": "方块",
}

# 这些 token 在不同命令里语义不同，保留逐路径翻译
KEEP_CONTEXT = {"clear", "start", "set", "add", "give", "time"}


def main():
    pack = json.loads(PACK.read_text(encoding="utf-8"))

    # 1) normalize
    changed = 0
    for k in list(pack):
        tok = k[:-len(".description")].split(".")[-1]
        if tok in NORMALIZE and pack[k] != NORMALIZE[tok]:
            pack[k] = NORMALIZE[tok]
            changed += 1

    # 2) group non-root, non-gamerule keys by last token
    groups = {}
    for k, v in pack.items():
        if k.startswith("commands.") and k.count(".") == 2:
            continue  # root command keys
        if k.startswith("commands.gamerule."):
            continue
        groups.setdefault(k[:-len(".description")].split(".")[-1], []).append(k)

    # 3) collapse groups with a single distinct value
    collapsed, added = [], []
    for tok, keys in groups.items():
        if tok in KEEP_CONTEXT or len(keys) < 2:
            continue
        values = {pack[k] for k in keys}
        if len(values) != 1:
            continue

        value = values.pop()
        gkey = f"commands.generic.{tok}.description"
        if gkey in pack:
            continue
        for k in keys:
            del pack[k]
        pack[gkey] = value
        collapsed.extend(keys)
        added.append((tok, value, len(keys)))

    # 4) backup + write
    bdir = Path("tools/backup")
    bdir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(PACK, bdir / f"zh_cn.before-generic.{datetime.now():%Y%m%d-%H%M%S}.bak")
    PACK.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    # report
    lines = ["# Hint generic-key simplification\n",
             f"- normalized values: **{changed}**",
             f"- collapsed groups: **{len(added)}**, removed keys: **{len(collapsed)}**",
             f"- resulting keys: **{len(pack)}**\n",
             "## Generic keys added\n"]
    for tok, value, n in sorted(added, key=lambda x: -x[2]):
        lines.append(f"- `commands.generic.{tok}.description` = `{value}`  (replaced {n} keys)")
    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(f"normalized={changed} collapsed_groups={len(added)} removed={len(collapsed)} keys={len(pack)}")
    print(f"report: {REPORT}")


if __name__ == "__main__":
    main()
