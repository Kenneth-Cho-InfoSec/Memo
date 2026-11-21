#!/usr/bin/env python3
"""Create unmasked, native-size screenshot comparisons from a JSON pair manifest."""

import argparse
import json
import shutil
import sys
from pathlib import Path

from PIL import Image, ImageChops


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = ROOT / "verification/comparison/pairs.json"


def compare_pixels(left: Image.Image, right: Image.Image) -> dict:
    if left.size != right.size:
        return {"same_dimensions": False, "left_size": list(left.size), "right_size": list(right.size)}
    left_bytes = left.convert("RGB").tobytes()
    right_bytes = right.convert("RGB").tobytes()
    differing = 0
    absolute_sum = 0
    max_channel_delta = 0
    for index in range(0, len(left_bytes), 3):
        dr = abs(left_bytes[index] - right_bytes[index])
        dg = abs(left_bytes[index + 1] - right_bytes[index + 1])
        db = abs(left_bytes[index + 2] - right_bytes[index + 2])
        if dr or dg or db:
            differing += 1
        absolute_sum += dr + dg + db
        max_channel_delta = max(max_channel_delta, dr, dg, db)
    total = left.width * left.height
    return {
        "same_dimensions": True,
        "width": left.width,
        "height": left.height,
        "total_pixels": total,
        "differing_pixels": differing,
        "exact_pixel_match_percent": round(100.0 * (total - differing) / total, 6),
        "mean_absolute_channel_error": round(absolute_sum / (total * 3), 6),
        "max_channel_delta": max_channel_delta,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--reference-root", type=Path, default=ROOT / "verification/reference/compact")
    parser.add_argument("--rebuilt-root", type=Path, default=ROOT / "verification/rebuilt/compact")
    parser.add_argument("--output-root", type=Path, default=ROOT / "verification/comparison/results")
    parser.add_argument("--strict", action="store_true", help="return nonzero when any compared pixel differs")
    args = parser.parse_args()

    pairs = json.loads(args.manifest.read_text(encoding="utf-8"))
    failures = []
    for pair in pairs:
        state_id = pair["state_id"]
        reference = args.reference_root / pair["reference_file"]
        rebuilt = args.rebuilt_root / pair["rebuilt_file"]
        if not reference.is_file() or not rebuilt.is_file():
            missing = [str(path) for path in (reference, rebuilt) if not path.is_file()]
            print(f"ERROR {state_id}: missing input(s): {', '.join(missing)}", file=sys.stderr)
            failures.append(state_id)
            continue

        out = args.output_root / state_id
        out.mkdir(parents=True, exist_ok=True)
        shutil.copy2(reference, out / "reference.png")
        shutil.copy2(rebuilt, out / "rebuilt.png")
        ref_image = Image.open(reference).convert("RGB")
        clone_image = Image.open(rebuilt).convert("RGB")
        metrics = compare_pixels(ref_image, clone_image)
        result = {
            **pair,
            "reference_file": str(reference),
            "rebuilt_file": str(rebuilt),
            "comparison": metrics,
            "masking": {"applied": False, "excluded_regions": []},
            "app_content_region_px": [0, 66, ref_image.width, max(66, ref_image.height - 40)],
        }
        app_exact = False
        if metrics.get("same_dimensions"):
            diff = ImageChops.difference(ref_image, clone_image)
            diff.point(lambda value: min(255, value * 4)).save(out / "diff-amplified-4x.png")
            Image.blend(ref_image, clone_image, 0.5).save(out / "overlay-50.png")
            x1, y1, x2, y2 = result["app_content_region_px"]
            app_metrics = compare_pixels(ref_image.crop((x1, y1, x2, y2)), clone_image.crop((x1, y1, x2, y2)))
            result["app_content_comparison"] = app_metrics
            app_exact = app_metrics["differing_pixels"] == 0
        else:
            app_exact = False
        result["strict_app_content_exact"] = app_exact
        result["full_frame_exact"] = bool(metrics.get("same_dimensions") and metrics.get("differing_pixels") == 0)
        result["disposition"] = "passed" if app_exact else "failed"
        (out / "result.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
        print(f"{result['disposition'].upper()} {state_id}: {metrics}")
        if not app_exact:
            failures.append(state_id)

    summary = {"pair_count": len(pairs), "failed_state_ids": failures, "exact_match": not failures}
    args.output_root.mkdir(parents=True, exist_ok=True)
    (args.output_root / "summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    return 1 if args.strict and failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
