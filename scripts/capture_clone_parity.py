#!/usr/bin/env python3
"""Capture repeatable light/dark Keep clone states on the compact Android 17 guest."""

import json
import argparse
import subprocess
import time
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "verification/rebuilt/ci-run"


def adb(*args: str, check: bool = True) -> str:
    result = subprocess.run(["adb", *args], check=check, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return result.stdout.decode("utf-8", errors="replace").strip()


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"PREREQUISITE FAILED: {message}")


def tap(x: int, y: int) -> None:
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(1)


def dump_ui() -> str:
    adb("shell", "uiautomator", "dump", "/sdcard/keepclone-window.xml", check=False)
    return adb("exec-out", "cat", "/sdcard/keepclone-window.xml", check=False)


def wait_for_text(text: str) -> str:
    for _ in range(8):
        xml = dump_ui()
        if text in xml:
            return xml
        time.sleep(0.5)
    raise SystemExit(f"CAPTURE FAILED: expected visible UI text {text!r}")


def capture(name: str, required_text: str) -> None:
    xml = wait_for_text(required_text)
    activity = adb("shell", "dumpsys", "activity", "activities")
    require("dev.arcturus.memo/.MainActivity" in activity, "clone is not the resumed activity")
    (OUT / f"{name}.xml").write_text(xml, encoding="utf-8")
    image = subprocess.run(["adb", "exec-out", "screencap", "-p"], check=True, stdout=subprocess.PIPE).stdout
    (OUT / f"{name}.png").write_bytes(image)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--settings-only", action="store_true", help="stop after the settings light/dark captures")
    args = parser.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    require(adb("get-state") == "device", "ADB device is not connected")
    require(adb("shell", "getprop", "ro.build.version.sdk") == "37", "Android 17 / API 37 guest required")
    require(adb("shell", "getprop", "ro.product.cpu.abi") == "x86_64", "x86_64 guest required")
    require(adb("shell", "wm", "size").endswith("1080x2220"), "compact profile requires 1080x2220 display")
    require(adb("shell", "wm", "density").endswith("440"), "compact profile requires 440 dpi")
    apk = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
    require(apk.is_file(), f"debug APK not found: {apk}")
    adb("install", "-r", str(apk))
    adb("shell", "pm", "clear", "dev.arcturus.memo")
    adb("shell", "cmd", "uimode", "night", "no")
    adb("shell", "am", "start", "-n", "dev.arcturus.memo/.MainActivity")
    time.sleep(2)
    capture("home-empty-light", "Search Keep")

    tap(100, 165)
    wait_for_text("Settings")
    tap(270, 1069)
    wait_for_text("Display options")
    capture("settings-system-light", "System default")
    tap(900, 800)
    capture("theme-picker-system-light", "Choose theme")
    tap(232, 846)
    capture("settings-light", "Light")
    tap(960, 1460)
    capture("settings-sharing-off-light", "Light")
    tap(960, 1460)

    tap(900, 800)
    wait_for_text("Choose theme")
    tap(232, 978)
    capture("settings-dark", "Dark")
    for x, y in [(960, 408), (960, 540), (960, 672), (960, 1085), (960, 1460)]:
        tap(x, y)
    capture("settings-toggled-dark", "Dark")

    if args.settings_only:
        profile = {
            "device": "KeepRef_Compact_API37",
            "api": 37,
            "abi": "x86_64",
            "wm_size": adb("shell", "wm", "size"),
            "wm_density": adb("shell", "wm", "density"),
            "font_scale": adb("shell", "settings", "get", "system", "font_scale"),
            "locale": adb("shell", "settings", "get", "system", "system_locales"),
            "timezone": adb("shell", "getprop", "persist.sys.timezone"),
            "night_mode_after_capture": adb("shell", "cmd", "uimode", "night"),
            "captures": 7,
        }
        (OUT / "profile.json").write_text(json.dumps(profile, indent=2) + "\n", encoding="utf-8")
        print(f"Captured settings states to {OUT}")
        return

    for x, y in [(960, 408), (960, 540), (960, 672), (960, 1085), (960, 1460)]:
        tap(x, y)
    tap(75, 154)
    wait_for_text("Search Keep")
    tap(100, 165)
    capture("drawer-dark", "Arcturus Memo")

    tap(270, 1069)
    wait_for_text("Display options")
    tap(900, 800)
    wait_for_text("Choose theme")
    tap(232, 1110)
    capture("settings-system-day", "System default")
    adb("shell", "cmd", "uimode", "night", "yes")
    time.sleep(2)
    capture("settings-system-night", "System default")
    adb("shell", "cmd", "uimode", "night", "no")
    time.sleep(2)
    tap(75, 154)

    profile = {
        "device": "KeepRef_Compact_API37",
        "api": 37,
        "abi": "x86_64",
        "wm_size": adb("shell", "wm", "size"),
        "wm_density": adb("shell", "wm", "density"),
        "font_scale": adb("shell", "settings", "get", "system", "font_scale"),
        "locale": adb("shell", "settings", "get", "system", "system_locales"),
        "timezone": adb("shell", "getprop", "persist.sys.timezone"),
        "night_mode_after_capture": adb("shell", "cmd", "uimode", "night"),
        "captures": 10,
    }
    (OUT / "profile.json").write_text(json.dumps(profile, indent=2) + "\n", encoding="utf-8")
    print(f"Captured clone states to {OUT}")


if __name__ == "__main__":
    main()
