#!/usr/bin/env python3
"""Apply the v1.3.0 expansion to the original v1.2.0 archive after patch-release-121.py."""
from pathlib import Path
import shutil, sys, xml.etree.ElementTree as ET
root=Path(sys.argv[1]).resolve()
here=Path(__file__).resolve().parent.parent
ns="http://schemas.android.com/apk/res/android"
ET.register_namespace("android",ns)
a=lambda name:"{"+ns+"}"+name
manifest=root/"app/src/main/AndroidManifest.xml"
doc=ET.parse(manifest); app=doc.getroot().find("application")
if app is None: raise RuntimeError("Missing application")
launchers=[]
for activity in app.findall("activity")+app.findall("activity-alias"):
    for f in activity.findall("intent-filter"):
        if any(x.get(a("name"))=="android.intent.action.MAIN" for x in f.findall("action")) and any(x.get(a("name"))=="android.intent.category.LAUNCHER" for x in f.findall("category")):
            launchers.append((activity,f))
if len(launchers)!=1: raise RuntimeError("Expected exactly one original launcher")
old,f=launchers[0]
if old.tag!="activity": raise RuntimeError("Unexpected launcher alias: inspect manually")
name=old.get(a("name"))
if not name: raise RuntimeError("Missing launcher class")
package_name="com.mhmmad.setramahjong"
if name.startswith("."):name=package_name+name
elif "." not in name:name=package_name+"."+name
if not name.startswith(package_name+"."):raise RuntimeError("Unexpected original package")
if f.findall("data") or len(f.findall("action"))!=1 or len(f.findall("category"))!=1:raise RuntimeError("Unexpected original launcher filter")
old.remove(f)
new=ET.SubElement(app,"activity",{a("name"):package_name+".expansion.GardenActivity",a("exported"):"true"})
f=ET.SubElement(new,"intent-filter");ET.SubElement(f,"action",{a("name"):"android.intent.action.MAIN"});ET.SubElement(f,"category",{a("name"):"android.intent.category.LAUNCHER"})
doc.write(manifest,encoding="utf-8",xml_declaration=True)
dest=root/"app/src/main/java/com/mhmmad/setramahjong/expansion"
dest.mkdir(parents=True,exist_ok=False)
for src in (here/"expansion").glob("*.java"):
    if src.name!="GardenEngineTest.java":shutil.copy2(src,dest/src.name)
(dest/"ClassicTarget.java").write_text("package com.mhmmad.setramahjong.expansion;\nfinal class ClassicTarget { static final String NAME = "+repr(name).replace("'","\"")+"; }\n",encoding="utf-8")
for rel in ["app/build.gradle.kts","tools/verify_release.py","tools/check_ads_integration.py"]:
    p=root/rel;s=p.read_text(encoding="utf-8")
    if "1.2.1" not in s:raise RuntimeError("Missing expected 1.2.1 markers in "+rel)
    s=s.replace("1.2.1","1.3.0").replace("versionCode = 4","versionCode = 5").replace("versionCode='4'","versionCode='5'").replace("'versionCode':4","'versionCode':5")
    p.write_text(s,encoding="utf-8")
print("Expansion integrated; original activity retained:",name)
