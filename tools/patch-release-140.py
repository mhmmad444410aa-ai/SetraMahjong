#!/usr/bin/env python3
"""Setra Mahjong 1.4.0 — integrate real new modes into the original premium Compose game.
Run AFTER patch-release-121.py, -130.py and -131.py. Never restore ad-choice dialogs.
"""
from pathlib import Path
import sys,shutil,xml.etree.ElementTree as ET
root=Path(sys.argv[1]).resolve()
here=Path(__file__).resolve().parent.parent
screen=root/"app/src/main/java/com/mhmmad/setramahjong/ui/Screens.kt"
text=screen.read_text(encoding="utf-8")

def once(old,new):
    global text
    n=text.count(old)
    if n!=1:raise RuntimeError("Expected one insertion point, got %d: %r"%(n,old[:120]))
    text=text.replace(old,new,1)

once('when(ui.page){"game"->vm.pause();"pause"->vm.resume();"privacy"->vm.navigate("about");"how","about"->vm.navigate("settings");else->vm.navigate("home")}',
    'when(ui.page){"game"->vm.pause();"pause"->vm.resume();"mini_routes","mini_memory","mini_daily"->vm.navigate("mini_hub");"privacy"->vm.navigate("about");"how","about"->vm.navigate("settings");else->vm.navigate("home")}')
once('"home"->Home(vm,ui,prefs,completions,totals)',
    '''"home"->Home(vm,ui,prefs,completions,totals)
                                 "mini_hub"->MiniGardenHub(onPick={vm.navigate(it)},onBack={vm.navigate("home")})
                                 "mini_routes","mini_memory","mini_daily"->MiniGardenPlay(ui.page,onBack={vm.navigate("mini_hub")})''')
once('GoldButton(if(u.sessions["campaign"]?.won==false)R.string.continue_game else R.string.begin_journey,enabled=!u.busy){if(!p.guideDone)vm.start(1,"tutorial",true)else vm.continueGame()}\n        Spacer(Modifier.height(16.dp))',
    '''GoldButton(if(u.sessions["campaign"]?.won==false)R.string.continue_game else R.string.begin_journey,enabled=!u.busy){if(!p.guideDone)vm.start(1,"tutorial",true)else vm.continueGame()}
        Spacer(Modifier.height(12.dp))
        MiniGardenPromo(onOpen={vm.navigate("mini_hub")})
        Spacer(Modifier.height(16.dp))''')
once('BloomMedallion(1,Modifier.size(132.dp).padding(5.dp))',
    '''val quiet=LocalQuietMotion.current
                 val floatY=if(quiet)0f else rememberInfiniteTransition(label="floating emblem")
                     .animateFloat(initialValue=-6f,targetValue=6f,
                         animationSpec=infiniteRepeatable(tween(1900,easing=FastOutSlowInEasing),RepeatMode.Reverse),
                         label="emblem breath").value
                 BloomMedallion(1,Modifier.size(132.dp).padding(5.dp).graphicsLayer{translationY=floatY})''')
once('Text(title(R.string.hero_title),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center)',
    '''Text(title(R.string.hero_title),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center)
                 Surface(color=Gold.copy(alpha=.16f),shape=RoundedCornerShape(12.dp),border=BorderStroke(.7.dp,Gold.copy(alpha=.65f))){
                     Text(gardenLabel("1.4 · THE PUZZLE ATELIER","١.٤ · مشغل الألغاز","1.4 · ATELIÊ DE ENIGMAS"),
                         modifier=Modifier.padding(horizontal=11.dp,vertical=4.dp),
                         color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold)
                 }''')
if "consentDialog" in text or "adPrivacy.review" in text or '"adprivacy"' in text:
    raise RuntimeError("Do not restore the custom ad-choice UI removed in v1.2.1")
screen.write_text(text,encoding="utf-8")
source=here/"source-v14/com/mhmmad/setramahjong/ui/MiniGarden.kt"
assert source.is_file()
shutil.copy2(source,screen.with_name("MiniGarden.kt"))

# Make the enhanced ORIGINAL Compose app the only launcher in both main and Huawei Petal manifests.
android="{http://schemas.android.com/apk/res/android}"
ET.register_namespace("android","http://schemas.android.com/apk/res/android")
ET.register_namespace("tools","http://schemas.android.com/tools")
count=0
for rel in ("app/src/main/AndroidManifest.xml","app/src/petal/AndroidManifest.xml"):
    path=root/rel
    tree=ET.parse(path)
    app=tree.getroot().find("application")
    if app is None:raise RuntimeError("No application in "+rel)
    for activity in app:
        if activity.tag not in ("activity","activity-alias"):continue
        for filt in list(activity.findall("intent-filter")):
            actions=[a.get(android+"name") for a in filt.findall("action")]
            categories=[c.get(android+"name") for c in filt.findall("category")]
            if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories:
                activity.remove(filt)
        if rel.endswith("main/AndroidManifest.xml") and activity.get(android+"name") in (".MainActivity","com.mhmmad.setramahjong.MainActivity"):
            f=ET.SubElement(activity,"intent-filter")
            ET.SubElement(f,"action",{android+"name":"android.intent.action.MAIN"})
            ET.SubElement(f,"category",{android+"name":"android.intent.category.LAUNCHER"})
            count+=1
    tree.write(path,encoding="utf-8",xml_declaration=True)
if count!=1:raise RuntimeError("Expected exactly one original MainActivity launcher")

for rel in ("app/build.gradle.kts","tools/verify_release.py","tools/check_ads_integration.py"):
    path=root/rel
    src=path.read_text(encoding="utf-8")
    if "1.3.1" not in src:raise RuntimeError("Expected 1.3.1 in "+rel)
    src=src.replace("1.3.1","1.4.0")
    if rel=="app/build.gradle.kts":
        if src.count("versionCode = 6")!=1:raise RuntimeError("Unexpected versionCode")
        src=src.replace("versionCode = 6","versionCode = 7")
    elif rel=="tools/verify_release.py":
        src=src.replace("versionCode='6'","versionCode='7'").replace("'versionCode':6","'versionCode':7")
    elif rel=="tools/check_ads_integration.py":
        src=src.replace("versionCode = 6","versionCode = 7").replace("versionCode='6'","versionCode='7'")
    path.write_text(src,encoding="utf-8")
print("PASS: Setra 1.4.0 (7) integrates new illustrated mini modes in original Compose home; no ad-choice; original exclusive launcher")
