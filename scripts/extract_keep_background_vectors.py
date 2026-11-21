#!/usr/bin/env python3
"""Decode Keep note-background vectors from a private reference APK with AAPT2."""

from __future__ import annotations

import argparse
import html
import re
import subprocess
from pathlib import Path


ASSETS = (
    "celebration",
    "food",
    "groceries",
    "music",
    "notes",
    "places",
    "recipes",
    "travel",
    "video",
)


def run(*args: str) -> str:
    return subprocess.run(args, check=True, text=True, stdout=subprocess.PIPE).stdout


def resource_blocks(resource_dump: str) -> dict[str, str]:
    lines = resource_dump.splitlines()
    blocks: dict[str, str] = {}
    current: str | None = None
    current_lines: list[str] = []
    for line in lines:
        match = re.match(r"\s*resource 0x[0-9a-fA-F]+ drawable/(\S+)\s*$", line)
        if match:
            if current is not None:
                blocks[current] = "\n".join(current_lines)
            current = match.group(1)
            current_lines = []
        elif current is not None:
            current_lines.append(line)
    if current is not None:
        blocks[current] = "\n".join(current_lines)
    return blocks


def resource_path(blocks: dict[str, str], name: str, night: bool = False) -> str:
    block = blocks.get(name)
    if block is None:
        raise RuntimeError(f"APK has no drawable/{name}")
    wanted = r"\(night\)" if night else r"\(\)"
    match = re.search(rf"(?m)^\s*{wanted} \(file\) (res/\S+\.xml)", block)
    if match is None:
        raise RuntimeError(f"drawable/{name} has no {'night' if night else 'default'} vector XML")
    return match.group(1)


def drawable_files(resource_dump: str) -> dict[str, str]:
    result: dict[str, str] = {}
    current_id: str | None = None
    current_lines: list[str] = []
    for line in resource_dump.splitlines():
        match = re.match(r"\s*resource (0x[0-9a-fA-F]+) drawable/\S+\s*$", line)
        if match:
            if current_id is not None:
                file_match = re.search(r"\(\) \(file\) (res/\S+\.xml)", "\n".join(current_lines))
                if file_match:
                    result[current_id.lower()] = file_match.group(1)
            current_id = match.group(1).lower()
            current_lines = []
        elif current_id is not None:
            current_lines.append(line)
    if current_id is not None:
        file_match = re.search(r"\(\) \(file\) (res/\S+\.xml)", "\n".join(current_lines))
        if file_match:
            result[current_id] = file_match.group(1)
    return result


def gradient_xml(tree: str) -> str:
    lines = tree.splitlines()
    gradient_start = next((i for i, line in enumerate(lines) if re.match(r"\s*E: gradient\b", line)), None)
    if gradient_start is None:
        raise RuntimeError("fillColor resource is not a gradient")
    base_indent = len(lines[gradient_start]) - len(lines[gradient_start].lstrip())

    def attrs(line: str) -> dict[str, str]:
        return {
            match.group(1): match.group(2)
            for match in re.finditer(r"(?:android:)?([A-Za-z][A-Za-z0-9]*)\([^)]*\)=([^\s]+)", line)
        }

    def enum(name: str, value: str) -> str:
        return {"type": {"0": "linear", "1": "radial", "2": "sweep"}}.get(name, {}).get(value, value)

    gradient_attrs: dict[str, str] = {}
    item_xml: list[str] = []
    index = gradient_start + 1
    while index < len(lines):
        line = lines[index]
        stripped = line.lstrip()
        indent = len(line) - len(stripped)
        if stripped.startswith("E:") and indent <= base_indent:
            break
        if stripped.startswith("A: ") and indent == base_indent + 2:
            gradient_attrs.update(attrs(stripped))
        elif stripped.startswith("E: item") and indent == base_indent + 4:
            item_attrs: dict[str, str] = {}
            child_index = index + 1
            while child_index < len(lines):
                child = lines[child_index]
                child_stripped = child.lstrip()
                child_indent = len(child) - len(child_stripped)
                if child_indent <= indent:
                    break
                if child_stripped.startswith("A: ") and child_indent == indent + 2:
                    item_attrs.update(attrs(child_stripped))
                child_index += 1
            item_xml.append("<item " + " ".join(f'android:{k}="{html.escape(v, quote=True)}"' for k, v in item_attrs.items()) + " />")
        index += 1

    if not item_xml:
        raise RuntimeError("gradient has no color stops")
    gradient_attrs = {name: enum(name, value) for name, value in gradient_attrs.items()}
    attr_text = " ".join(f'android:{k}="{html.escape(v, quote=True)}"' for k, v in gradient_attrs.items())
    return f'<aapt:attr name="android:fillColor"><gradient {attr_text}>{"".join(item_xml)}</gradient></aapt:attr>'


def vector_xml(tree: str, resource_ids: dict[str, str], aapt2: Path, apk: Path) -> str:
    if re.search(r"(?m)^\s+E: group\b|^\s+E: clip-path\b", tree):
        raise RuntimeError("A group or clip-path needs explicit conversion support")

    def root_attr(name: str) -> str:
        match = re.search(rf"android:{name}\([^)]*\)=([^\s]+)", tree)
        if match is None:
            raise RuntimeError(f"vector has no android:{name}")
        return match.group(1)

    attrs = {
        name: root_attr(name)
        for name in ("width", "height", "viewportWidth", "viewportHeight")
    }
    paths: list[str] = []
    for block in re.split(r"(?m)^\s+E: path\b[^\n]*\n", tree)[1:]:
        path_data_match = re.search(r'android:pathData\([^)]*\)="([^"]*)"', block)
        fill_match = re.search(r"android:fillColor\([^)]*\)=([^\s]+)", block)
        if path_data_match is None:
            continue
        path_attrs = []
        nested_fill = None
        if fill_match is not None:
            fill_value = fill_match.group(1)
            reference = re.fullmatch(r"@?(0x[0-9a-fA-F]{8})", fill_value)
            if reference:
                resource_path = resource_ids.get(reference.group(1).lower())
                if resource_path is None:
                    raise RuntimeError(f"cannot resolve vector fill resource {fill_value}")
                fill_tree = run(str(aapt2), "dump", "xmltree", "--file", resource_path, str(apk))
                nested_fill = gradient_xml(fill_tree)
            else:
                path_attrs.append(("fillColor", fill_value))
        for name in (
            "fillAlpha",
            "fillType",
            "strokeColor",
            "strokeAlpha",
            "strokeWidth",
            "strokeLineCap",
            "strokeLineJoin",
            "strokeMiterLimit",
        ):
            match = re.search(rf"android:{name}\([^)]*\)=([^\s]+)", block)
            if match is not None:
                value = match.group(1)
                if name == "fillType":
                    value = {"0": "nonZero", "1": "evenOdd"}.get(value, value)
                path_attrs.append((name, value))
        path_attrs.append(("pathData", path_data_match.group(1)))
        rendered = " ".join(
            f'android:{name}="{html.escape(value, quote=True)}"' for name, value in path_attrs
        )
        if nested_fill:
            paths.append(f"    <path {rendered}>{nested_fill}</path>")
        else:
            paths.append(f"    <path {rendered} />")

    if not paths:
        raise RuntimeError("vector contains no path elements")
    root = " ".join(f'android:{name}="{value}"' for name, value in attrs.items())
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android" '
        'xmlns:aapt="http://schemas.android.com/aapt" '
        f"{root}>\n" + "\n".join(paths) + "\n</vector>\n"
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--aapt2", required=True, type=Path)
    parser.add_argument("--out", required=True, type=Path)
    args = parser.parse_args()

    dump = run(str(args.aapt2), "dump", "resources", str(args.apk))
    blocks = resource_blocks(dump)
    resource_ids = drawable_files(dump)
    args.out.mkdir(parents=True, exist_ok=True)
    for asset in ASSETS:
        for mode, is_night in (("day", False), ("night", False)):
            # *_day and *_night are separate drawable resources without night qualifiers.
            resource_name = f"{asset}_{mode}"
            path = resource_path(blocks, resource_name)
            tree = run(str(args.aapt2), "dump", "xmltree", "--file", path, str(args.apk))
            (args.out / f"keep_background_{asset}_{mode}.xml").write_text(vector_xml(tree, resource_ids, args.aapt2, args.apk))
        for suffix, night in (("thumb_day", False), ("thumb_night", True)):
            path = resource_path(blocks, f"{asset}_thumbnail", night=night)
            tree = run(str(args.aapt2), "dump", "xmltree", "--file", path, str(args.apk))
            (args.out / f"keep_background_{asset}_{suffix}.xml").write_text(vector_xml(tree, resource_ids, args.aapt2, args.apk))


if __name__ == "__main__":
    main()
