package com.mhmmad.setramahjong.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhmmad.setramahjong.expansion.GardenEngine
import java.time.LocalDate

/** New fully integrated, localized puzzle journeys. Never replaces the original Classic game. */
@Composable internal fun gardenLabel(en:String,ar:String,pt:String):String {
    val language=LocalConfiguration.current.locales[0].language
    return when(language){"ar"->ar;"pt"->pt;else->en}
}

@Composable fun MiniGardenPromo(onOpen:()->Unit) {
    val heading=gardenLabel("NEW · THREE PUZZLE WORLDS","جديد · ثلاثة عوالم ألغاز","NOVO · TRÊS MUNDOS DE ENIGMAS")
    val detail=gardenLabel("Routes • Memory • Daily mystery","المسارات • الذاكرة • لغز اليوم","Caminhos • Memória • Desafio diário")
    Surface(onClick=onOpen,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(23.dp),
        color=Color(0xFF124B42),border=BorderStroke(1.3.dp,Gold.copy(alpha=.8f)),shadowElevation=10.dp) {
        Row(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF155D4B),Color(0xFF07332D)))).padding(13.dp),
            verticalAlignment=Alignment.CenterVertically) {
            BloomMedallion(2,Modifier.size(64.dp))
            Column(Modifier.weight(1f).padding(horizontal=12.dp)) {
                Text(heading,color=Gold,fontSize=13.sp,fontWeight=FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text(detail,color=Ivory,fontSize=12.sp,lineHeight=17.sp)
            }
            Text("›",color=Gold,fontSize=29.sp)
        }
    }
}

@Composable fun MiniGardenHub(onPick:(String)->Unit,onBack:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=17.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,null,tint=Gold)}
            Text(gardenLabel("THE PUZZLE ATELIER","مشغل الألغاز","ATELIÊ DE ENIGMAS"),
                style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f),color=Gold)
        }
        Box(Modifier.fillMaxWidth().height(185.dp).clip(RoundedCornerShape(27.dp))
            .border(1.dp,Gold.copy(alpha=.6f),RoundedCornerShape(27.dp))) {
            GardenBackdrop(Modifier.fillMaxSize(),2,.3f)
            Row(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Emerald.copy(alpha=.7f),Emerald.copy(alpha=.1f))))
                .padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(gardenLabel("Beyond classic Mahjong","أبعد من الماجونغ الكلاسيكية","Além do Mahjong clássico"),
                        style=MaterialTheme.typography.headlineMedium,color=Ivory)
                    Spacer(Modifier.height(10.dp))
                    Text(gardenLabel("Three fresh ways to think, explore and unwind.","ثلاث طرق جديدة للتفكير والاستكشاف والاسترخاء.","Três novas formas de pensar, explorar e relaxar."),
                        fontSize=12.sp,color=Mist,lineHeight=18.sp)
                }
                BloomMedallion(7,Modifier.size(107.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        GardenModeCard("mini_routes",5,
            gardenLabel("GARDEN ROUTES","مسارات الحديقة","CAMINHOS DO JARDIM"),
            gardenLabel("Trace a path between twin stones. Only two bends allowed.","اربط حجرين متطابقين بمسار لا يتجاوز انعطافين.","Conecte pedras iguais usando no máximo duas curvas."),
            onPick)
        Spacer(Modifier.height(12.dp))
        GardenModeCard("mini_memory",1,
            gardenLabel("MEMORY GROVE","بستان الذاكرة","BOSQUE DA MEMÓRIA"),
            gardenLabel("Flip botanical tiles, recall the hidden pairs.","اكشف البلاطات النباتية وتذكّر أماكن الأزواج.","Vire peças botânicas e memorize os pares ocultos."),
            onPick)
        Spacer(Modifier.height(12.dp))
        GardenModeCard("mini_daily",11,
            gardenLabel("DAILY SEED","بذرة اليوم","SEMENTE DIÁRIA"),
            gardenLabel("A new fixed puzzle every day, playable offline.","لغز مختلف وثابت يوميًا، يعمل دون إنترنت.","Um desafio novo por dia, mesmo sem internet."),
            onPick)
        Spacer(Modifier.height(25.dp))
    }
}

@Composable private fun GardenModeCard(mode:String,symbol:Int,heading:String,detail:String,onPick:(String)->Unit) {
    Surface(onClick={onPick(mode)},color=Color(0xFF104A40).copy(alpha=.96f),
        shape=RoundedCornerShape(24.dp),border=BorderStroke(1.dp,Gold.copy(alpha=.5f)),shadowElevation=6.dp) {
        Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(76.dp).clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF4EA78B),Color(0xFF164E42)))),
                contentAlignment=Alignment.Center){BloomMedallion(symbol,Modifier.size(68.dp))}
            Column(Modifier.weight(1f).padding(horizontal=15.dp)){
                Text(heading,color=Gold,fontWeight=FontWeight.Bold,fontSize=16.sp)
                Spacer(Modifier.height(5.dp))
                Text(detail,color=Mist,fontSize=12.sp,lineHeight=18.sp)
            }
            Text("›",color=Gold,fontSize=27.sp)
        }
    }
}

@Composable fun MiniGardenPlay(mode:String,onBack:()->Unit) {
    val context=LocalContext.current
    val prefs=remember(context){context.getSharedPreferences("setra_puzzle_atelier_v14",Context.MODE_PRIVATE)}
    val today=LocalDate.now().toEpochDay()
    val isDaily=mode=="mini_daily"
    val routes=if(isDaily) today%2L==0L else mode=="mini_routes"
    var level by remember(mode){mutableIntStateOf(1)}
    var revision by remember(mode){mutableIntStateOf(0)}
    val rows=if(isDaily||!routes)4 else if(level>=5)6 else 4
    val cols=if(isDaily||level>=3)6 else 4
    val count=rows*cols
    val seed=if(isDaily)today*982451653L else 142857L+level*7919L
    var board by remember(mode,level,revision,today){mutableStateOf(
        if(routes)GardenEngine.routes(rows,cols,seed).tiles else GardenEngine.memory(count,seed))}
    var selected by remember(mode,level,revision,today){mutableIntStateOf(-1)}
    var revealSecond by remember(mode,level,revision,today){mutableIntStateOf(-1)}
    var locked by remember(mode,level,revision,today){mutableStateOf(false)}
    var moves by remember(mode,level,revision,today){mutableIntStateOf(0)}
    var hintPair by remember(mode,level,revision,today){mutableStateOf(listOf<Int>())}
    var message by remember(mode,level,revision,today){mutableStateOf("")}
    val won=board.all{it==0}
    val key=if(isDaily)"daily-"+today else mode+"-"+level

    LaunchedEffect(locked,selected,revealSecond){
        if(locked&&selected>=0&&revealSecond>=0){
            kotlinx.coroutines.delay(850)
            selected=-1;revealSecond=-1;locked=false
        }
    }
    LaunchedEffect(won,key){
        if(won&&!prefs.getBoolean(key,false))prefs.edit().putBoolean(key,true)
            .putInt("completed",prefs.getInt("completed",0)+1).apply()
    }
    fun choose(i:Int){
        if(locked||won||board[i]==0)return
        hintPair=emptyList();message=""
        if(selected<0){selected=i;return}
        if(selected==i){selected=-1;return}
        moves++
        val correct=if(routes)GardenEngine.connects(board,rows,cols,selected,i)
                    else board[selected]==board[i]
        if(correct){
            val updated=board.clone()
            updated[selected]=0;updated[i]=0
            board=updated;selected=-1;revealSecond=-1
        } else if(routes){
            selected=i
            message=""
        } else {
            revealSecond=i;locked=true
        }
    }
    val completed=prefs.getInt("completed",0)+(if(won&&!prefs.getBoolean(key,false))1 else 0)
    val head=if(isDaily)gardenLabel("TODAY'S SEED","بذرة اليوم","SEMENTE DE HOJE")
       else if(routes)gardenLabel("GARDEN ROUTES","مسارات الحديقة","CAMINHOS DO JARDIM")
       else gardenLabel("MEMORY GROVE","بستان الذاكرة","BOSQUE DA MEMÓRIA")
    val prompt=if(routes)gardenLabel("Match twins through a clear path with no more than 2 turns.",
        "اربط الأزواج عبر مسار لا يتجاوز انعطافين.","Ligue pares com um caminho de até duas curvas.")
       else gardenLabel("Find the pairs hidden among the leaves.","تذكّر مواقع الأزواج المخفية.","Encontre pares escondidos entre as folhas.")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=13.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=5.dp),verticalAlignment=Alignment.CenterVertically){
            IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,null,tint=Gold)}
            Column(Modifier.weight(1f)){Text(head,color=Ivory,fontWeight=FontWeight.Bold,fontSize=19.sp)
                Text(if(isDaily)LocalDate.now().toString() else gardenLabel("STAGE ","المرحلة ","FASE ")+level,
                    color=Gold,fontSize=12.sp)}
            BloomMedallion(if(routes)5 else 1,Modifier.size(50.dp))
        }
        Spacer(Modifier.height(5.dp))
        Surface(color=Color(0xFF104E43).copy(alpha=.94f),shape=RoundedCornerShape(20.dp),
            border=BorderStroke(1.dp,Gold.copy(alpha=.4f))) {
            Column(Modifier.fillMaxWidth().padding(15.dp)){
                Text(prompt,fontSize=13.sp,lineHeight=20.sp,color=Mist)
                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){
                    Text(gardenLabel("MOVES ","الحركات ","JOGADAS ")+moves,fontSize=12.sp,color=Gold)
                    Text(gardenLabel("PAIRS ","الأزواج ","PARES ")+board.count{it!=0}/2,fontSize=12.sp,color=Ivory)
                    Text(gardenLabel("SOLVED ","مكتملة ","CONCLUÍDOS ")+completed,fontSize=12.sp,color=Mist)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress={1f-board.count{it!=0}.toFloat()/count},
                    modifier=Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),color=Gold,trackColor=Emerald)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
            .background(Color(0xFF0E3D36).copy(alpha=.88f))
            .border(1.dp,Gold.copy(alpha=.5f),RoundedCornerShape(23.dp)).padding(9.dp)) {
            Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
                repeat(rows){r->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        repeat(cols){c->
                            val i=r*cols+c;val value=board[i]
                            val selectedTile=i==selected||i==revealSecond
                            val shown=routes||selectedTile
                            val highlighted=i in hintPair
                            val label=if(value==0)gardenLabel("Empty tile","بلاطة فارغة","Peça vazia")
                               else if(shown)gardenLabel("Tile ","بلاطة ","Peça ")+value
                               else gardenLabel("Hidden tile ","بلاطة مخفية ","Peça oculta ")+(i+1)
                            val cardColor=if(value==0)Color(0xFF0A312B)
                               else if(selectedTile)Color(0xFFFFE0A0)
                               else if(highlighted)Color(0xFFB1EED1)
                               else if(shown)Color(0xFFF7EFDA)
                               else Color(0xFF276A56)
                            Surface(onClick={choose(i)},enabled=value!=0&&!locked&&!won,
                                modifier=Modifier.weight(1f).aspectRatio(.78f)
                                    .semantics{contentDescription=label},
                                shape=RoundedCornerShape(11.dp),
                                color=cardColor,
                                border=BorderStroke(if(selectedTile||highlighted)2.dp else 1.dp,
                                    if(selectedTile)Color(0xFFBB8541) else Gold.copy(alpha=.75f)),
                                shadowElevation=if(value!=0)4.dp else 0.dp) {
                                Box(contentAlignment=Alignment.Center,modifier=Modifier.fillMaxSize()) {
                                    if(value>0){
                                        if(shown){
                                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                                Ornament(value%16,Modifier.fillMaxWidth(.7f).aspectRatio(1f))
                                                if(cols==4)Text("✦",color=Color(0xFFB89045),fontSize=10.sp)
                                            }
                                        }else{
                                            Ornament(3,Modifier.fillMaxWidth(.62f).aspectRatio(1f),Gold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp));Text(message,color=Gold,fontSize=12.sp)
        }
        AnimatedVisibility(won){
            Column(Modifier.fillMaxWidth().padding(top=16.dp),horizontalAlignment=Alignment.CenterHorizontally){
                BloomMedallion(7,Modifier.size(94.dp))
                Text(gardenLabel("GARDEN COMPLETE!","اكتملت الحديقة!","JARDIM CONCLUÍDO!"),
                    color=Gold,style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center)
                Text(gardenLabel("A fresh challenge awaits.","تحدٍ جديد بانتظارك.","Um novo desafio espera por você."),
                    color=Mist,fontSize=13.sp)
                Spacer(Modifier.height(9.dp))
                Button(onClick={if(isDaily)revision++ else level++},
                    colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Emerald),
                    modifier=Modifier.fillMaxWidth()) {
                    Text(if(isDaily)gardenLabel("Play again","العب مجددًا","Jogar novamente")
                         else gardenLabel("Next stage","المرحلة التالية","Próxima fase"))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            if(routes)OutlinedButton(onClick={
                val pair=GardenEngine.hint(board,rows,cols)
                hintPair=if(pair[0]>=0)listOf(pair[0],pair[1]) else emptyList()
            },enabled=!won,modifier=Modifier.weight(1f),
                border=BorderStroke(1.dp,Gold.copy(alpha=.65f))){
                Icon(Icons.Default.Search,null,Modifier.size(17.dp),tint=Gold)
                Spacer(Modifier.width(5.dp))
                Text(gardenLabel("Hint","تلميح","Dica"),fontSize=12.sp,color=Ivory)
            }
            OutlinedButton(onClick={revision++},modifier=Modifier.weight(1f),
                border=BorderStroke(1.dp,Gold.copy(alpha=.65f))){
                Icon(Icons.Default.Refresh,null,Modifier.size(17.dp),tint=Gold)
                Spacer(Modifier.width(5.dp))
                Text(gardenLabel("Restart","إعادة","Reiniciar"),fontSize=12.sp,color=Ivory)
            }
            if(!isDaily)OutlinedButton(onClick={level++;revision=0},modifier=Modifier.weight(1f),
                border=BorderStroke(1.dp,Gold.copy(alpha=.65f))){
                Text(gardenLabel("More","التالي","Mais"),fontSize=12.sp,color=Ivory)
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}
