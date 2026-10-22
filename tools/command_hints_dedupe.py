#!/usr/bin/env python3
"""Command-hint resource-pack dedupe (dry-run by default).

Usage:
  python tools/command_hints_dedupe.py                     # dry-run, pack only
  python tools/command_hints_dedupe.py --tree run/exports/command_keys.json
  python tools/command_hints_dedupe.py --tree ... --apply  # rewrite pack (backup first)

What it considers:
  A. namespace duplicates: "minecraft:x" key that has an identical "x" twin.
     Safe to drop once the matcher normalizes the default namespace.
  B. argument-slot keys (needs --tree): keys whose tree node is an argument
     (the exporter marks argument placeholders as "<name>").
     The v1 matcher only looks up suggestion values, so these keys cannot be hit.
  C. merge candidates (report only): identical texts shared by many keys;
     turning them into generic keys is a separate, behaviour-changing step.
"""
import argparse
import json
import shutil
from datetime import datetime
from pathlib import Path

PACK = Path("src/main/resources/resourcepacks/command_hints/assets/tweelix_hints/lang/zh_cn.json")
REPORT = Path("tools/hint-dedupe-report.md")


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--pack", default=str(PACK))
    ap.add_argument("--tree", default=None, help="command_keys.json produced by /tweelix exportkeys")
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--out", default=str(REPORT))
    args = ap.parse_args()

    pack_path = Path(args.pack)
    pack = load(pack_path)

    ns_same, ns_diff = [], []
    for key in pack:
        if "minecraft:" not in key:
            continue
        stripped = key.replace("minecraft:", "", 1)
        if stripped in pack:
            (ns_same if pack[key] == pack[stripped] else ns_diff).append(key)

    # merge candidates: same text used by >= 2 keys
    by_text = {}
    for k, v in pack.items():
        by_text.setdefault(v, []).append(k)
    merge_groups = {v: ks for v, ks in by_text.items() if len(ks) > 1}

    slots, literals, unknown = [], [], []
    if args.tree:
        tree = load(Path(args.tree))
        keys = tree.get("keys", tree)
        slot_set, literal_set = set(), set()
        for key, val in keys.items():
            is_arg = isinstance(val, str) and val.startswith("Please translate: <")
            target = slot_set if is_arg else literal_set
            target.add(key)
            # 同一个节点可能以带/不带 minecraft: 两种形式出现在包里
            stripped = key.replace("minecraft:", "", 1)
            if stripped != key:
                target.add(stripped)

        slots = sorted(k for k in slot_set if k in pack)
        literals = sorted(k for k in literal_set if k in pack)
        known = set(slots) | set(literals)
        unknown = [k for k in pack if k not in known]

    report = []
    w = report.append
    w("# Command-hint dedupe report\n")
    w(f"- pack: `{pack_path}`")
    w(f"- total keys: **{len(pack)}**")
    w(f"- tree: `{args.tree or 'not provided'}`\n")

    w("## A. namespace duplicates (identical value)\n")
    w(f"- deletable: **{len(ns_same)}**")
    w(f"- conflicting value (keep both / choose manually): **{len(ns_diff)}**\n")
    if ns_diff:
        w("Conflicts:\n")
        for k in ns_diff:
            w(f"- `{k}` = `{pack[k]}`")
            w(f"  - stripped: `{pack[k.replace('minecraft:', '', 1)]}`")
        w("")
    w("### Deletable namespace keys\n")
    w("```text")
    for k in sorted(ns_same):
        w(k)
    w("```\n")

    w("## B. argument-slot keys (unreachable by the v1 matcher)\n")
    if args.tree:
        w(f"- deletable: **{len(slots)}**")
        w(f"- literal-node keys kept: **{len(literals)}**")
        w(f"- pack keys not present in tree (value keys, kept): **{len(unknown)}**\n")
        w("### Deletable slot keys\n")
        w("```text")
        for k in sorted(slots):
            w(k)
        w("```\n")
    else:
        w("- **needs --tree**; run `/tweelix exportkeys` and pass "
          "`run/exports/command_keys.json`\n")

    w("## C. merge candidates (report only)\n")
    w(f"- groups with identical text: **{len(merge_groups)}**")
    w(f"- potential saved keys if generic keys are introduced: "
      f"**{sum(len(v) - 1 for v in merge_groups.values())}**\n")
    for text, ks in sorted(merge_groups.items(), key=lambda kv: -len(kv[1]))[:10]:
        w(f"- `{text}` × {len(ks)}")
    w("")

    projected = len(pack) - len(set(ns_same) | set(slots))
    w(f"## Projected key count\n\n`{len(pack)}` → **`{projected}`**\n")

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text("\n".join(report), encoding="utf-8")
    print("\n".join(report[:40]))
    print(f"\n[report] {out}")

    if args.apply:
        if not args.tree:
            raise SystemExit("--apply requires --tree (slot classification)")
        backup_dir = Path("tools/backup")
        backup_dir.mkdir(parents=True, exist_ok=True)
        backup = backup_dir / f"{pack_path.name}.{datetime.now():%Y%m%d-%H%M%S}.bak"
        shutil.copy2(pack_path, backup)
        drop = set(ns_same) | set(slots)
        new_pack = {k: v for k, v in pack.items() if k not in drop}
        pack_path.write_text(json.dumps(new_pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"[apply] removed {len(drop)} keys, backup: {backup}")


if __name__ == "__main__":
    main()
