package com.mhmmad.setramahjong.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhmmad.setramahjong.*
import com.mhmmad.setramahjong.R
import com.mhmmad.setramahjong.data.*
import com.mhmmad.setramahjong.engine.*
import com.mhmmad.setramahjong.ads.*
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable private fun title(id:Int)=stringResource(id)
@Suppress("DEPRECATION")
@Composable private fun currentLocale():Locale {val c=LocalConfiguration.current;return if(android.os.Build.VERSION.SDK_INT>=24)c.locales[0]else c.locale}
@Composable fun SetraApp(vm:GameViewModel,activity:Activity) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val completions by vm.completions.collectAsStateWithLifecycle()
    val totals by vm.totals.collectAsStateWithLifecycle()
    val context=LocalContext.current
    val device=Locale.getDefault()
    val tag=when(prefs.language){"ar"->"ar";"pt-BR"->"pt-BR";"en-GB"->"en-GB";else->when(device.language){"ar"->"ar";"pt"->"pt-BR";"en"->"en-GB";else->"en-GB"}}
    val locale=remember(tag){Locale.forLanguageTag(tag)}
    val config=Configuration(LocalConfiguration.current).apply {setLocale(locale);setLayoutDirection(locale)}
    val local=remember(context,tag,config.fontScale){context.createConfigurationContext(config)}
    val adProvider=remember(activity){createAdProvider(activity)}
    val adPrivacy by adProvider.privacy.collectAsStateWithLifecycle()
    DisposableEffect(adProvider){onDispose{adProvider.close()}}
    LaunchedEffect(prefs.consent) {adProvider.permission(prefs.consent=="granted")}
    LaunchedEffect(ui.page,ui.session?.key) {
        if(ui.page=="game"&&ui.session?.mode!="tutorial")adProvider.prepare()
        if(ui.page=="result"&&ui.session?.mode=="campaign") {
            val s=ui.session!!
            if(AdFrequency.eligible(s.level,completions.count{it.mode=="campaign"}-1,totals.sinceAd,System.currentTimeMillis(),totals.lastAd))adProvider.transition {vm.markAd()}
        }
    }
    CompositionLocalProvider(LocalQuietMotion provides prefs.reduceMotion,LocalContext provides local,LocalConfiguration provides config,LocalLayoutDirection provides if(tag=="ar")LayoutDirection.Rtl else LayoutDirection.Ltr) {
        MaterialTheme(colorScheme=darkColorScheme(primary=Gold,onPrimary=Emerald,background=Emerald,onBackground=Ivory,surface=Color(0xFF16483E),onSurface=Ivory,secondary=Mist),typography=Typography(
            headlineLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=34.sp,lineHeight=40.sp,color=Ivory),
            headlineMedium=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=27.sp,lineHeight=34.sp,color=Ivory),
            titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=22.sp,lineHeight=28.sp,color=Ivory))) {
            var confirm by remember{mutableStateOf("")}
            var consentDialog by remember{mutableStateOf(false)}
            // Privacy permission is separate from ad-display settings. Present it before gameplay.
            LaunchedEffect(ui.ready,ui.page,prefs.consent) {
                if(BuildConfig.PETAL_ENABLED&&ui.ready&&ui.page=="home"&&prefs.consent=="unknown")consentDialog=true
            }
            val rootPages=setOf("home","map","album","stats")
            BackHandler(ui.page!="home") {
                when(ui.page){"game"->vm.pause();"pause"->vm.resume();"mini_routes","mini_memory","mini_daily"->vm.navigate("mini_hub");"privacy","adprivacy"->vm.navigate("about");"how","about"->vm.navigate("settings");else->vm.navigate("home")}
            }
            Surface(Modifier.fillMaxSize(),color=Emerald) {
                Box(Modifier.fillMaxSize()) {
                GardenBackdrop(Modifier.fillMaxSize(),prefs.background,if(ui.page=="home").62f else .70f)
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    if(!ui.ready)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Gold)}
                    else {
                        Box(Modifier.weight(1f)) {
                            when(ui.page) {
                                "home"->Home(vm,ui,prefs,completions,totals)
                                 "mini_hub"->MiniGardenHub(onPick={vm.navigate(it)},onBack={vm.navigate("home")})
                                 "mini_routes","mini_memory","mini_daily"->MiniGardenPlay(ui.page,onBack={vm.navigate("mini_hub")})
                                "map"->MapScreen(vm,completions)
                                "game"->ui.session?.let {s->PlayScreen(vm,ui,prefs,adProvider)}
                                "pause"->PauseScreen(vm,onRestart={confirm="restart"})
                                "result"->ResultScreen(vm,ui,prefs,completions)
                                "daily"->DailyScreen(vm,completions,locale)
                                "album"->AlbumScreen(vm,prefs,completions,totals)
                                "stats"->StatsScreen(vm,completions,totals)
                                "settings"->SettingsScreen(vm,prefs,onReset={confirm="reset"})
                                "how"->InfoPage(vm,R.string.how_to_play) {Text(title(R.string.how_body),lineHeight=26.sp);Spacer(Modifier.height(24.dp));GoldButton(R.string.tutorial){vm.start(1,"tutorial",true)}}
                                "privacy"->InfoPage(vm,R.string.privacy){PrivacyContent()}
                                "about"->InfoPage(vm,R.string.about){Ornament(1,Modifier.size(100.dp));Spacer(Modifier.height(18.dp));Text(title(R.string.about_body),lineHeight=26.sp);Spacer(Modifier.height(20.dp));Text(stringResource(R.string.version_info,BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE),color=Gold);Text(BuildConfig.APPLICATION_ID,color=Mist,style=MaterialTheme.typography.labelSmall);Spacer(Modifier.height(16.dp));GoldButton(R.string.privacy){vm.navigate("privacy")};if(BuildConfig.PETAL_ENABLED)TextButton(onClick={vm.navigate("adprivacy")}){Text(title(R.string.privacy_choices))}}
                                "adprivacy"->InfoPage(vm,R.string.privacy_choices) {
                                    Text(title(if(BuildConfig.PETAL_ENABLED)R.string.privacy_ads_on else R.string.no_ad_configuration),lineHeight=26.sp)
                                    if(BuildConfig.PETAL_ENABLED){Spacer(Modifier.height(20.dp));Text(stringResource(R.string.consent_status,title(if(prefs.consent=="granted")R.string.consent_granted else R.string.consent_none)));Spacer(Modifier.height(16.dp));GoldButton(if(prefs.consent=="granted")R.string.consent_withdraw else R.string.consent_title){if(prefs.consent=="granted")vm.setText("consent","declined")else consentDialog=true}}
                                }
                            }
                        }
                        if(ui.page in rootPages) BottomNav(ui.page){vm.navigate(it)}
                    }
                }
                }
                if(ui.error!=null) AlertDialog(onDismissRequest={vm.clearError()},title={Text(title(R.string.app_name))},text={Text(title(ui.error!!))},confirmButton={TextButton(onClick={vm.clearError()}){Text(title(R.string.confirm))}})
                if(confirm.isNotEmpty())AlertDialog(onDismissRequest={confirm=""},title={Text(title(if(confirm=="reset")R.string.reset_title else R.string.restart_title))},text={Text(title(if(confirm=="reset")R.string.reset_body else R.string.restart_body))},confirmButton={TextButton(onClick={if(confirm=="reset")vm.reset()else vm.restart();confirm=""}){Text(title(R.string.confirm))}},dismissButton={TextButton(onClick={confirm=""}){Text(title(R.string.cancel))}})
                if(adPrivacy.review)AlertDialog(onDismissRequest={adProvider.acceptPartners(false);vm.setText("consent","declined")},title={Text(title(R.string.consent_title))},text={
                    val uriHandler=LocalUriHandler.current
                    Column(Modifier.verticalScroll(rememberScrollState())){Text(title(R.string.consent_body));adPrivacy.partners.forEach { partner ->TextButton(onClick={runCatching{uriHandler.openUri(partner.privacyUrl)}}){Text(partner.name+" · "+partner.privacyUrl,fontSize=12.sp)}}}
                },confirmButton={TextButton(onClick={adProvider.acceptPartners(true);vm.setText("consent","granted")}){Text(title(R.string.consent_allow))}},dismissButton={TextButton(onClick={adProvider.acceptPartners(false);vm.setText("consent","declined")}){Text(title(R.string.consent_decline))}})
                if(consentDialog)AlertDialog(onDismissRequest={vm.setText("consent","declined");consentDialog=false},title={Text(title(R.string.consent_title))},text={Column{Text(title(R.string.consent_body));TextButton(onClick={consentDialog=false;vm.navigate("privacy")}){Text(title(R.string.privacy))}}},confirmButton={TextButton(onClick={vm.setText("consent","pending");adProvider.requestPrivacy();consentDialog=false}){Text(title(R.string.consent_allow))}},dismissButton={TextButton(onClick={vm.setText("consent","declined");consentDialog=false}){Text(title(R.string.consent_decline))}})
            }
        }
    }
}
@Composable fun GoldButton(text:Int,modifier:Modifier=Modifier,enabled:Boolean=true,onClick:()->Unit) {
    Button(onClick=onClick,enabled=enabled,modifier=modifier.fillMaxWidth().heightIn(min=54.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Emerald)) {Text(title(text),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center,modifier=Modifier.padding(vertical=4.dp))}
}
@Composable private fun Header(text:Int,onBack:(()->Unit)?=null,extra:@Composable RowScope.()->Unit={}) {
    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
        if(onBack!=null)IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,title(R.string.back),tint=Gold)}
        Text(title(text),Modifier.weight(1f).padding(horizontal=8.dp),style=MaterialTheme.typography.titleLarge)
        extra()
    }
}
@Composable private fun BottomNav(page:String,onGo:(String)->Unit) {
    Surface(color=Color(0xFF052D29).copy(alpha=.96f),shadowElevation=16.dp,shape=RoundedCornerShape(topStart=26.dp,topEnd=26.dp),border=BorderStroke(.7.dp,Gold.copy(alpha=.25f))) {
        Row(Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
            listOf(Triple("home",R.string.home,13),Triple("map",R.string.journey,2),Triple("album",R.string.collection,1),Triple("stats",R.string.statistics,4)).forEach{(route,label,symbol)->
                val active=route==page
                Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(if(active)Color(0xFF287B68).copy(alpha=.7f)else Color.Transparent).clickable{onGo(route)}.padding(vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Ornament(symbol,Modifier.size(32.dp),if(active)Gold else Mist.copy(alpha=.65f));Spacer(Modifier.height(4.dp));Text(title(label),fontSize=11.sp,fontWeight=if(active)FontWeight.Bold else FontWeight.Normal,color=if(active)Gold else Mist,textAlign=TextAlign.Center)
                }
            }
        }
    }
}
@Composable private fun Home(vm:GameViewModel,u:GameUi,p:Preferences,c:List<CompletionRow>,t:TotalsRow) {
    val count=c.count{it.mode=="campaign"}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically) {
            Image(painterResource(R.mipmap.ic_launcher),null,Modifier.size(46.dp).shadow(8.dp,RoundedCornerShape(13.dp)).clip(RoundedCornerShape(13.dp)))
            Column(Modifier.weight(1f).padding(horizontal=11.dp)){Text(title(R.string.app_name),style=MaterialTheme.typography.titleLarge);Text(title(R.string.brand_tag),color=Gold,fontSize=8.sp,letterSpacing=1.2.sp)}
            IconButton(onClick={vm.navigate("settings")}){Icon(Icons.Default.Settings,title(R.string.settings),tint=Gold)}
        }
        Box(Modifier.fillMaxWidth().heightIn(min=290.dp).clip(RoundedCornerShape(28.dp)).border(1.dp,Gold.copy(alpha=.64f),RoundedCornerShape(28.dp))) {
            GardenBackdrop(Modifier.matchParentSize(),p.background,.10f)
            Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent,Emerald.copy(alpha=.2f),Emerald.copy(alpha=.94f)))).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Surface(color=Emerald.copy(alpha=.8f),shape=CircleShape,border=BorderStroke(.7.dp,Gold.copy(alpha=.55f))) {
                    Text(stringResource(R.string.level_number,vm.nextLevel()),Modifier.padding(horizontal=18.dp,vertical=7.dp),color=Gold,fontSize=12.sp,fontWeight=FontWeight.Bold)
                }
                val quiet=LocalQuietMotion.current
                 val floatY=if(quiet)0f else rememberInfiniteTransition(label="floating emblem")
                     .animateFloat(initialValue=-6f,targetValue=6f,
                         animationSpec=infiniteRepeatable(tween(1900,easing=FastOutSlowInEasing),RepeatMode.Reverse),
                         label="emblem breath").value
                 BloomMedallion(1,Modifier.size(132.dp).padding(5.dp).graphicsLayer{translationY=floatY})
                Text(title(R.string.hero_title),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center)
                 Surface(color=Gold.copy(alpha=.16f),shape=RoundedCornerShape(12.dp),border=BorderStroke(.7.dp,Gold.copy(alpha=.65f))){
                     Text(gardenLabel("1.4 · THE PUZZLE ATELIER","١.٤ · مشغل الألغاز","1.4 · ATELIÊ DE ENIGMAS"),
                         modifier=Modifier.padding(horizontal=11.dp,vertical=4.dp),
                         color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold)
                 }
                Spacer(Modifier.height(7.dp));Text(title(R.string.hero_body),fontSize=12.sp,lineHeight=18.sp,color=Ivory.copy(alpha=.88f),textAlign=TextAlign.Center)
            }
        }
        Spacer(Modifier.height(16.dp))
        GoldButton(if(u.sessions["campaign"]?.won==false)R.string.continue_game else R.string.begin_journey,enabled=!u.busy){if(!p.guideDone)vm.start(1,"tutorial",true)else vm.continueGame()}
        Spacer(Modifier.height(12.dp))
        MiniGardenPromo(onOpen={vm.navigate("mini_hub")})
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=3.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Text(stringResource(R.string.level_progress,count),fontSize=11.sp,color=Ivory)
            Row(verticalAlignment=Alignment.CenterVertically){Ornament(7,Modifier.size(21.dp));Spacer(Modifier.width(6.dp));Text(String.format(currentLocale(),"%d",t.petals),color=Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)}
        }
        Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={count/120f},Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),color=Gold,trackColor=Emerald.copy(alpha=.75f))
        Spacer(Modifier.height(17.dp))
        FeatureCard(R.string.campaign,R.string.campaign_body,2,{vm.navigate("map")}) {}
        Spacer(Modifier.height(11.dp));FeatureCard(R.string.daily,R.string.daily_body,11,{vm.navigate("daily")}) {}
        Spacer(Modifier.height(12.dp));Text(title(R.string.saved),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,fontSize=10.sp,color=Mist.copy(alpha=.8f));Spacer(Modifier.height(14.dp))
    }
}
@Composable private fun FeatureCard(name:Int,body:Int,symbol:Int,onClick:()->Unit,extra:@Composable ()->Unit) {
    Surface(onClick=onClick,color=Color(0xFF0C483E).copy(alpha=.91f),shape=RoundedCornerShape(23.dp),border=BorderStroke(.8.dp,Gold.copy(alpha=.35f)),shadowElevation=7.dp) {
        Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically) {
            BloomMedallion(symbol,Modifier.size(62.dp),false);Column(Modifier.weight(1f).padding(start=14.dp)){Text(title(name),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.height(3.dp));Text(title(body),fontSize=12.sp,color=Mist,lineHeight=18.sp);extra()}
            Icon(Icons.AutoMirrored.Filled.ArrowBack,null,Modifier.size(19.dp).graphicsLayer{rotationZ=180f},tint=Gold)
        }
    }
}
@Composable private fun MapScreen(vm:GameViewModel,c:List<CompletionRow>) {
    val names=stringArrayResource(R.array.chapter_names)
    val completed=c.filter{it.mode=="campaign"}.associateBy{it.level}
    var expanded by remember {mutableIntStateOf((vm.nextLevel()-1)/10)}
    Column {
        Header(R.string.campaign)
        LazyColumn(contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=22.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
            items(12){chapter->
                val count=(chapter*10+1..chapter*10+10).count{it in completed};val opened=expanded==chapter
                Surface(color=Color(0xFF073C35).copy(alpha=.91f),shape=RoundedCornerShape(26.dp),border=BorderStroke(if(opened)1.2.dp else .7.dp,Gold.copy(alpha=if(opened).68f else .25f)),shadowElevation=9.dp) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().clickable{expanded=if(opened)-1 else chapter}.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                            BloomMedallion(chapter,Modifier.size(73.dp),opened)
                            Column(Modifier.weight(1f).padding(start=15.dp)) {
                                Text(stringResource(R.string.chapter_number,chapter+1),fontSize=10.sp,color=Gold,letterSpacing=1.sp)
                                Text(names[chapter],style=MaterialTheme.typography.titleLarge)
                                Text(stringResource(R.string.chapter_progress,count),color=Mist,fontSize=12.sp)
                            }
                            Icon(Icons.Default.PlayArrow,null,Modifier.size(21.dp).graphicsLayer{rotationZ=if(opened)90f else 0f},tint=Gold)
                        }
                        AnimatedVisibility(opened,enter=expandVertically(tween(if(LocalQuietMotion.current)0 else 260))+fadeIn(),exit=shrinkVertically(tween(if(LocalQuietMotion.current)0 else 180))+fadeOut()) {
                            Column {
                                Box(Modifier.fillMaxWidth().height(112.dp).padding(horizontal=16.dp).clip(RoundedCornerShape(20.dp))) {
                                    GardenBackdrop(Modifier.matchParentSize(),chapter%4,.20f)
                                    MiniLayout(remember(chapter){Layouts.tiles(chapter*10+5)},Modifier.fillMaxSize().padding(12.dp))
                                }
                                ChapterTrail(chapter,vm.nextLevel(),completed){vm.start(it)}
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun ChapterTrail(chapter:Int,next:Int,completed:Map<Int,CompletionRow>,onPlay:(Int)->Unit) {
    val rtl=LocalLayoutDirection.current==LayoutDirection.Rtl
    Box(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=13.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val points=(0..9).map{i->val row=i/3;val col=if(row%2==0)i%3 else 2-i%3;var x=(col+.5f)/3;if(rtl)x=1f-x;Offset(size.width*x,size.height*(row+.5f)/4)}
            val trail=Path().apply{moveTo(points[0].x,points[0].y);points.drop(1).forEach{lineTo(it.x,it.y)}}
            drawPath(trail,Gold.copy(alpha=.25f),style=Stroke(2.dp.toPx(),cap=StrokeCap.Round,pathEffect=PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(),6.dp.toPx()))))
        }
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            repeat(4){row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                repeat(3){col->val slot=row*3+if(row%2==0)col else 2-col;val level=chapter*10+slot+1
                    Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {
                        if(slot<10){val enabled=level<=next;val done=completed[level];val active=level==next
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(enabled=enabled){onPlay(level)}.padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                Box(Modifier.size(54.dp).shadow(if(active)10.dp else 3.dp,CircleShape).background(Brush.verticalGradient(if(enabled)listOf(if(active)Color(0xFFFFE8A6)else Color(0xFF51A98B),if(active)Color(0xFFBD8735)else Color(0xFF175D4D))else listOf(Color(0xFF31594E),Color(0xFF123A33))),CircleShape).border(if(active)2.dp else 1.dp,if(enabled)Gold else Mist.copy(alpha=.25f),CircleShape),contentAlignment=Alignment.Center) {
                                    if(enabled)Text(String.format(currentLocale(),"%d",level),color=if(active)Emerald else Ivory,fontWeight=FontWeight.Bold,fontSize=21.sp)
                                    else Icon(Icons.Default.Lock,null,Modifier.size(18.dp),tint=Mist.copy(alpha=.45f))
                                }
                                Spacer(Modifier.height(5.dp));Stars(done?.stars?:0,Modifier.height(13.dp),true)
                            }
                        }else Spacer(Modifier.height(80.dp))
                    }
                }
            }}
        }
    }
}
@Composable private fun Stars(count:Int,modifier:Modifier=Modifier,tiny:Boolean=false) {
    Row(modifier,horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){repeat(3){i->Icon(Icons.Default.Star,null,Modifier.size(if(tiny)10.dp else 37.dp),tint=if(i<count)Gold else Gold.copy(alpha=.18f))}}
}
@Composable private fun PlayScreen(vm:GameViewModel,u:GameUi,p:Preferences,ad:AdProvider) {
    val s=u.session?:return
    var deadDismissed by remember(s.tiles){mutableStateOf(false)}
    val guide=s.mode=="tutorial"
    val compactHud=LocalConfiguration.current.fontScale>1.2f || LocalConfiguration.current.screenHeightDp<560
    val step=if(s.remaining==8)1 else if(s.remaining>2)2 else 3
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={vm.pause()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,title(R.string.pause),tint=Gold)}
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text(if(guide)title(R.string.tutorial)else if(s.mode=="daily")title(R.string.daily)else stringResource(R.string.level_number,s.level),style=MaterialTheme.typography.titleLarge);if(p.showTimer||!compactHud)Text(if(p.showTimer)"${u.elapsed/60}:${(u.elapsed%60).toString().padStart(2,'0')}"else title(R.string.calm_mode),color=Mist,fontSize=11.sp)}
            IconButton(onClick={vm.pause()}){Icon(painterResource(R.drawable.ic_pause),title(R.string.pause),tint=Gold)}
        }
        if(guide)Surface(color=Color(0xFF245748),modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=6.dp),shape=RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(14.dp)){Text(stringResource(R.string.guide_step,step),color=Gold,fontSize=10.sp);Text(title(if(compactHud)R.string.guide_compact else when(step){1->R.string.guide_one;2->R.string.guide_two;else->R.string.guide_three}),fontSize=13.sp,lineHeight=19.sp)}
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=10.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            val pairCount=Rules.availablePairs(s.tiles).size
            Text(pluralStringResource(R.plurals.pair_count,pairCount,pairCount),Modifier.weight(1f),color=Gold,fontSize=12.sp)
            Text(pluralStringResource(R.plurals.tiles_remaining,s.remaining,s.remaining),Modifier.weight(1f),color=Mist,fontSize=12.sp,textAlign=TextAlign.End)
        }
        LinearProgressIndicator(progress={1f-s.remaining.toFloat()/s.tiles.size},modifier=Modifier.fillMaxWidth().padding(horizontal=22.dp).height(4.dp).clip(CircleShape),color=Gold,trackColor=Ivory.copy(alpha=.13f))
        Spacer(Modifier.height(6.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            GameBoard(s,if(guide) s.solution.firstOrNull{p->s.tiles.any{it.id==p.a&&!it.removed}}else u.hint,p.reduceMotion,p.background,u.busy,vm::tap,Modifier.weight(1f),p.matchStyle)
        }
        if(!compactHud)Text(title(R.string.board_tip),Modifier.fillMaxWidth().padding(bottom=9.dp),textAlign=TextAlign.Center,color=Mist.copy(alpha=.7f),fontSize=10.sp)
        val roomierControls=LocalConfiguration.current.fontScale>1.2f || LocalConfiguration.current.screenWidthDp<340
        if(roomierControls) {
            Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=7.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    ToolButton(R.string.hint,Icons.Default.Search,Modifier.weight(1f),!u.busy){vm.hint()}
                    ToolButton(R.string.undo,Icons.AutoMirrored.Filled.ArrowBack,Modifier.weight(1f),!u.busy&&s.history.isNotEmpty()){vm.undo()}
                }
                ToolButton(R.string.shuffle,Icons.Default.Refresh,Modifier.fillMaxWidth(),!u.busy&&!guide){vm.shuffle()}
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=7.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                ToolButton(R.string.hint,Icons.Default.Search,Modifier.weight(1f),!u.busy){vm.hint()}
                ToolButton(R.string.undo,Icons.AutoMirrored.Filled.ArrowBack,Modifier.weight(1f),!u.busy&&s.history.isNotEmpty()){vm.undo()}
                ToolButton(R.string.shuffle,Icons.Default.Refresh,Modifier.weight(1f),!u.busy&&!guide){vm.shuffle()}
            }
        }
        if(!guide)ad.Banner(Modifier.fillMaxWidth())
    }
    if(!guide&&!u.busy&&!deadDismissed&&Rules.noMoves(s))AlertDialog(onDismissRequest={deadDismissed=true},title={Text(title(R.string.no_moves))},text={Text(title(R.string.no_moves_body))},confirmButton={Column{TextButton(onClick={deadDismissed=true;vm.shuffle()}){Text(title(R.string.shuffle))};TextButton(onClick={deadDismissed=true;vm.restart()}){Text(title(R.string.restart))}}},dismissButton={TextButton(onClick={deadDismissed=true;vm.undo()},enabled=s.history.isNotEmpty()){Text(title(R.string.undo))}})
}
@Composable private fun ToolButton(text:Int,icon:ImageVector,modifier:Modifier,enabled:Boolean,onClick:()->Unit) {
    OutlinedButton(onClick=onClick,enabled=enabled,modifier=modifier.heightIn(min=60.dp),shape=RoundedCornerShape(19.dp),colors=ButtonDefaults.outlinedButtonColors(containerColor=Color(0xFF246E5A).copy(alpha=.66f),disabledContainerColor=Emerald.copy(alpha=.7f)),contentPadding=PaddingValues(horizontal=4.dp,vertical=9.dp),border=BorderStroke(1.dp,Gold.copy(alpha=if(enabled).65f else .18f))) {
        Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,Modifier.size(18.dp),tint=if(enabled)Gold else Mist.copy(alpha=.3f));Text(title(text),fontSize=11.sp,textAlign=TextAlign.Center,color=if(enabled)Ivory else Mist.copy(alpha=.3f))}
    }
}
@Composable private fun PauseScreen(vm:GameViewModel,onRestart:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        BloomMedallion(4,Modifier.size(122.dp));Spacer(Modifier.height(30.dp));Text(title(R.string.paused_title),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center);Spacer(Modifier.height(12.dp));Text(title(R.string.paused_body),color=Mist,textAlign=TextAlign.Center);Spacer(Modifier.height(35.dp));GoldButton(R.string.resume){vm.resume()};Spacer(Modifier.height(12.dp));OutlinedButton(onClick=onRestart,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(title(R.string.restart))};TextButton(onClick={vm.navigate("home")}){Text(title(R.string.go_home))}
    }
}
@Composable private fun ResultScreen(vm:GameViewModel,u:GameUi,p:Preferences,c:List<CompletionRow>) {
    val s=u.session?:return
    var appeared by remember(s.key){mutableStateOf(p.reduceMotion)}
    LaunchedEffect(s.key){appeared=true}
    val reveal by animateFloatAsState(if(appeared)1f else 0f,tween(if(p.reduceMotion)0 else 260),label="garden complete")
    Column(Modifier.fillMaxSize().graphicsLayer{alpha=reveal;scaleX=.97f+.03f*reveal;scaleY=.97f+.03f*reveal}.verticalScroll(rememberScrollState()).padding(26.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp));VictoryBloom(Modifier.size(188.dp));Spacer(Modifier.height(8.dp));Stars(s.stars);Spacer(Modifier.height(16.dp));Text(title(if(s.mode=="tutorial")R.string.guide_done else R.string.win_title),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Text(title(R.string.win_body),color=Mist,textAlign=TextAlign.Center);Spacer(Modifier.height(24.dp))
        Surface(color=Color(0xFF0C4439).copy(alpha=.93f),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                ResultLine(R.string.moves,s.moves.toString());ResultLine(R.string.time_played,"${s.elapsedSeconds/60}:${(s.elapsedSeconds%60).toString().padStart(2,'0')}");ResultLine(R.string.assists,(s.hints+s.undos+s.shuffles).toString())
                if(s.mode!="tutorial") {HorizontalDivider(color=Gold.copy(alpha=.2f));Text(if(u.resultFirst)stringResource(R.string.reward_points,30)else title(R.string.reward_repeat),color=Gold,fontSize=13.sp)}
            }
        }
        Spacer(Modifier.height(16.dp));Text(title(R.string.star_rules),fontSize=11.sp,color=Mist,textAlign=TextAlign.Center);Spacer(Modifier.height(26.dp))
        GoldButton(if(s.mode=="campaign"&&s.level<120)R.string.next_level else R.string.go_home){if(s.mode=="campaign"&&s.level<120)vm.start(s.level+1)else vm.navigate("home")}
        if(s.mode!="tutorial")TextButton(onClick={vm.navigate("album")}){Text(title(R.string.collection))}
        TextButton(onClick={vm.navigate("home")}){Text(title(R.string.go_home))}
    }
}
@Composable private fun ResultLine(label:Int,value:String) {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(title(label),Modifier.weight(1f),color=Mist);Text(value,color=Ivory,fontWeight=FontWeight.SemiBold)}}
@Composable private fun DailyScreen(vm:GameViewModel,c:List<CompletionRow>,locale:Locale) {
    val done=c.any{it.key=="daily:${vm.dailyDate()}"}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(26.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Header(R.string.daily,{vm.navigate("home")});Spacer(Modifier.height(22.dp));BloomMedallion(11,Modifier.size(130.dp));Spacer(Modifier.height(24.dp));Text(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy",locale)),color=Gold,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(20.dp));Text(title(if(done)R.string.daily_done else R.string.daily_start),style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center);Spacer(Modifier.height(20.dp));MiniLayout(remember(vm.dailyDate()){Layouts.tiles(vm.dailyLevel())},Modifier.fillMaxWidth().height(170.dp));Spacer(Modifier.height(20.dp));Text(title(R.string.daily_explanation),color=Mist,lineHeight=25.sp,textAlign=TextAlign.Center);Spacer(Modifier.height(28.dp));GoldButton(R.string.play){vm.start(vm.dailyLevel(),"daily")}
    }
}
@Composable private fun AlbumScreen(vm:GameViewModel,p:Preferences,c:List<CompletionRow>,t:TotalsRow) {
    val bgNames=stringArrayResource(R.array.background_names);val chapters=stringArrayResource(R.array.chapter_names)
    Column {
        Header(R.string.collection)
        LazyColumn(contentPadding=PaddingValues(horizontal=22.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
            item{Text(title(R.string.collection_title),style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(8.dp));Text(title(R.string.collection_body),color=Mist,lineHeight=23.sp);Spacer(Modifier.height(12.dp));Text(stringResource(R.string.petal_balance,t.petals),color=Gold)}
            item{Text(title(R.string.backgrounds),style=MaterialTheme.typography.titleLarge)}
            items(4){i->val required=listOf(0,90,300,900)[i];val unlocked=t.petals>=required
                Surface(color=GardenBackgrounds[i],shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,if(p.background==i)Gold else Gold.copy(alpha=.15f))) {
                    Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){Ornament(i+2,Modifier.size(49.dp));Column(Modifier.weight(1f).padding(horizontal=14.dp)){Text(bgNames[i],fontSize=17.sp);Text(if(unlocked)title(if(p.background==i)R.string.equipped else R.string.unlocked)else stringResource(R.string.unlock_at,required),fontSize=11.sp,color=Mist)};if(unlocked&&p.background!=i)TextButton(onClick={vm.setNumber("background",i)}){Text(title(R.string.apply),fontSize=11.sp)}}
                }
            }
            item{Spacer(Modifier.height(8.dp));Text(title(R.string.ornaments),style=MaterialTheme.typography.titleLarge)}
            items(6){row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {repeat(2){col->val chapter=row*2+col;val unlocked=(chapter*10+1..chapter*10+10).all{l->c.any{it.mode=="campaign"&&it.level==l}}
                Surface(Modifier.weight(1f),color=Color(0xFF0D473C).copy(alpha=.91f),shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,Gold.copy(alpha=.1f))){Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){Ornament(chapter,Modifier.size(65.dp),if(unlocked)Gold else Mist.copy(alpha=.23f));Spacer(Modifier.height(10.dp));Text(chapters[chapter],fontSize=13.sp,textAlign=TextAlign.Center);Spacer(Modifier.height(5.dp));Text(if(unlocked)title(R.string.unlocked)else stringResource(R.string.unlock_chapter,chapter+1),fontSize=10.sp,color=Mist,textAlign=TextAlign.Center)}}
            }}}
        }
    }
}
@Composable private fun StatsScreen(vm:GameViewModel,c:List<CompletionRow>,t:TotalsRow) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Header(R.string.statistics);Ornament(4,Modifier.size(70.dp).align(Alignment.CenterHorizontally));Spacer(Modifier.height(14.dp));Text(title(R.string.stats_body),color=Mist,lineHeight=24.sp);Spacer(Modifier.height(23.dp))
        val data=listOf(R.string.levels_cleared to "${c.count{it.mode=="campaign"}} / 120",R.string.total_stars to "${c.filter{it.mode=="campaign"}.sumOf{it.stars}} / 360",R.string.daily_cleared to c.count{it.mode=="daily"}.toString(),R.string.total_pairs to t.pairs.toString(),R.string.hint to t.hints.toString(),R.string.undo to t.undos.toString(),R.string.shuffle to t.shuffles.toString(),R.string.time_played to "${t.seconds/60}:${(t.seconds%60).toString().padStart(2,'0')}",R.string.reward_points to t.petals.toString())
        data.forEach{(label,value)->Surface(color=Color(0xFF0D473C).copy(alpha=.91f),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().padding(bottom=9.dp)){Row(Modifier.padding(17.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(if(label==R.string.reward_points)stringResource(R.string.petal_balance,t.petals)else title(label),Modifier.weight(1f),color=Mist);if(label!=R.string.reward_points)Text(value,color=Gold,fontWeight=FontWeight.SemiBold)}}}
    }
}
@Composable private fun SettingsScreen(vm:GameViewModel,p:Preferences,onReset:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(R.string.settings,{vm.navigate("home")})
        Column(Modifier.padding(horizontal=22.dp)) {
            Text(title(R.string.language),style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(10.dp))
            listOf("system" to R.string.language_system,"en-GB" to R.string.language_english,"ar" to R.string.language_arabic,"pt-BR" to R.string.language_portuguese).forEach{(key,label)->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable{vm.setText("language",key)}.padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(p.language==key,{vm.setText("language",key)},colors=RadioButtonDefaults.colors(selectedColor=Gold));Text(title(label),Modifier.weight(1f))}
            }
            Spacer(Modifier.height(14.dp));HorizontalDivider(color=Gold.copy(alpha=.2f));Spacer(Modifier.height(12.dp))
            SettingSwitch(R.string.sound,p.sound){vm.setBool("sound",it)};SettingSwitch(R.string.haptics,p.haptics){vm.setBool("haptics",it)};SettingSwitch(R.string.reduce_motion,p.reduceMotion){vm.setBool("reduceMotion",it)};SettingSwitch(R.string.show_timer,p.showTimer){vm.setBool("showTimer",it)}
            Spacer(Modifier.height(16.dp));Text(title(R.string.match_motion),style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(7.dp))
            val movementNames=stringArrayResource(R.array.match_motion_names)
            movementNames.forEachIndexed {index,name->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable{vm.setNumber("matchStyle",index)}.padding(vertical=1.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(p.matchStyle==index,{vm.setNumber("matchStyle",index)},colors=RadioButtonDefaults.colors(selectedColor=Gold));Text(name,Modifier.weight(1f))}}
            Text(title(R.string.preferences_body),fontSize=12.sp,color=Mist,lineHeight=20.sp);Spacer(Modifier.height(18.dp))
            listOf(R.string.tutorial to "tutorial",R.string.how_to_play to "how",R.string.about to "about").forEach{(label,page)->OutlinedButton(onClick={if(page=="tutorial")vm.start(1,"tutorial",true)else vm.navigate(page)},modifier=Modifier.fillMaxWidth().padding(bottom=8.dp).heightIn(min=49.dp),shape=RoundedCornerShape(14.dp)){Text(title(label))}}
            TextButton(onClick=onReset,modifier=Modifier.fillMaxWidth()){Text(title(R.string.reset),color=Color(0xFFEEB7A4))};Spacer(Modifier.height(20.dp))
        }
    }
}
@Composable private fun SettingSwitch(label:Int,value:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth().padding(vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Text(title(label),Modifier.weight(1f));Switch(value,onChange,colors=SwitchDefaults.colors(checkedThumbColor=Emerald,checkedTrackColor=Gold))}}
@Composable private fun InfoPage(vm:GameViewModel,title:Int,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize()){Header(title,{vm.navigate(if(title==R.string.privacy||title==R.string.privacy_choices)"about"else"settings")});Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}}
@Composable private fun PrivacyContent(){
    Text(title(R.string.privacy_local),lineHeight=26.sp);Spacer(Modifier.height(10.dp));Text(title(if(BuildConfig.PETAL_ENABLED)R.string.privacy_ads_on else R.string.privacy_ads_off),lineHeight=26.sp)
    Spacer(Modifier.height(10.dp));Text(if(BuildConfig.DEVELOPER_NAME.isBlank()||BuildConfig.CONTACT_EMAIL.isBlank())title(R.string.contact_pending)else stringResource(R.string.contact_details,BuildConfig.DEVELOPER_NAME,BuildConfig.CONTACT_EMAIL),color=Gold,lineHeight=24.sp)
    if(BuildConfig.PETAL_ENABLED) {
        val uriHandler=LocalUriHandler.current
        if(BuildConfig.PRIVACY_URL.startsWith("https://"))TextButton(onClick={runCatching{uriHandler.openUri(BuildConfig.PRIVACY_URL)}}){Text(BuildConfig.PRIVACY_URL,fontSize=12.sp,color=Gold)}
        TextButton(onClick={runCatching{uriHandler.openUri("https://consumer.huawei.com/en/privacy/")}}){Text("Petal Ads privacy",fontSize=12.sp,color=Mist)}
    }
    Text(stringResource(R.string.version_info,BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE),color=Mist,fontSize=12.sp)
}
