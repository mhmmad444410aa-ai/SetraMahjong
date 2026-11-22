#!/usr/bin/env python3
"""Fix Huawei/Petal manifest overlay launcher and bump to 1.3.1.
Apply only after patch-release-121.py and patch-release-130.py.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]).resolve()
android = "{http://schemas.android.com/apk/res/android}"
ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
ET.register_namespace("tools", "http://schemas.android.com/tools")
launcher = "com.mhmmad.setramahjong.expansion.GardenActivity"
base = root / "app/src/main/AndroidManifest.xml"
petal = root / "app/src/petal/AndroidManifest.xml"

def is_launcher_filter(node):
    actions = [a.get(android+"name") for a in node.findall("action")]
    categories = [c.get(android+"name") for c in node.findall("category")]
    return "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories

def inspect_and_fix(path):
    tree = ET.parse(path)
    application = tree.getroot().find("application")
    assert application is not None, str(path)
    removed = 0
    found = 0
    for activity in list(application):
        if activity.tag not in ("activity", "activity-alias"): continue
        name = activity.get(android+"name","")
        for intent in list(activity.findall("intent-filter")):
            if not is_launcher_filter(intent): continue
            if name == launcher:
                found += 1
            else:
                activity.remove(intent)
                removed += 1
    tree.write(path, encoding="utf-8", xml_declaration=True)
    return removed,found

removed_main, found_main = inspect_and_fix(base)
removed_petal, found_petal = inspect_and_fix(petal)
# The Petal manifest is higher priority. Android manifest merger does not
# match intent-filter elements, so removing the filter from the overlay
# alone may still inherit the original MainActivity launcher from the ZIP.
# Replace that activity declaration in the merged manifest while retaining
# its Petal attributes; the new GardenActivity launcher remains in main.
petal_tree = ET.parse(petal)
petal_app = petal_tree.getroot().find("application")
old_activities = [a for a in petal_app.findall("activity")
                  if a.get(android+"name") == "com.mhmmad.setramahjong.MainActivity"]
assert len(old_activities) == 1, "Expected exactly one Petal MainActivity"
old_activities[0].set("{http://schemas.android.com/tools}node", "replace")
# The actual merged APK from CI had no GardenActivity at all, despite its
# declaration in src/main. Put the single launcher filter in the Petal
# overlay itself, and remove it from src/main to prevent duplicate filters.
base_tree = ET.parse(base)
base_app = base_tree.getroot().find("application")
garden_main = [a for a in base_app.findall("activity") if a.get(android+"name") == launcher]
assert len(garden_main) == 1, "Expected one base GardenActivity"
garden_filters = [f for f in garden_main[0].findall("intent-filter") if is_launcher_filter(f)]
assert len(garden_filters) == 1, "Expected one base GardenActivity launcher filter"
garden_main[0].remove(garden_filters[0])
base_tree.write(base, encoding="utf-8", xml_declaration=True)
garden_petal = [a for a in petal_app.findall("activity") if a.get(android+"name") == launcher]
assert not garden_petal, "Petal GardenActivity already exists"
new_garden = ET.SubElement(petal_app, "activity", {
    android+"name": launcher, android+"exported": "true",
    "{http://schemas.android.com/tools}node": "replace",
})
launch_filter = ET.SubElement(new_garden, "intent-filter")
ET.SubElement(launch_filter, "action", {android+"name": "android.intent.action.MAIN"})
ET.SubElement(launch_filter, "category", {android+"name": "android.intent.category.LAUNCHER"})
petal_tree.write(petal, encoding="utf-8", xml_declaration=True)
if (removed_main,found_main,removed_petal,found_petal) != (0,1,1,0):
    raise RuntimeError("Unexpected launcher state: main=%s petal=%s" % ((removed_main,found_main),(removed_petal,found_petal)))
# AAB flavor overlay must not re-add the old launcher. Android's merged manifest should have just one launchable activity.
for rel in ("app/build.gradle.kts", "tools/verify_release.py", "tools/check_ads_integration.py"):
    path = root/rel
    text = path.read_text(encoding="utf-8")
    if "1.3.0" not in text: raise RuntimeError("Expected version 1.3.0 in "+rel)
    text = text.replace("1.3.0","1.3.1")
    if rel == "app/build.gradle.kts":
        old = "versionCode = 5"
        assert text.count(old) == 1, "Unexpected versionCode structure"
        text = text.replace(old, "versionCode = 6")
    elif rel == "tools/verify_release.py":
        text = text.replace("versionCode='5'", "versionCode='6'").replace("'versionCode':5", "'versionCode':6")
    path.write_text(text, encoding="utf-8")
print("PASS: Huawei/Petal overlay old launcher removed, unique GardenActivity startup retained; version 1.3.1 (6)")
