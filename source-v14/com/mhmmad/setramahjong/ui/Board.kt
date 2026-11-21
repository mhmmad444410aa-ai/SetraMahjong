package com.mhmmad.setramahjong.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import com.mhmmad.setramahjong.R
import com.mhmmad.setramahjong.engine.*
import kotlin.math.*

@Composable fun GameBoard(s:Session,hint:PairStep?,reduceMotion:Boolean,background:Int,busy:Boolean,onTap:(Int)->Unit,modifier:Modifier=Modifier,matchStyle:Int=0) {
    val atlas=gardenAtlas()
    var viewport by remember {mutableStateOf(IntSize(1,1))}
    var zoom by remember(s.key){mutableFloatStateOf(1f)}
    var pan by remember(s.key){mutableStateOf(Offset.Zero)}
    var previous by remember(s.key){mutableStateOf(s.tiles)}
    var lastShuffle by remember(s.key){mutableIntStateOf(s.shuffles)}
    var leaving by remember(s.key){mutableStateOf(emptyList<Tile>())}
    var beforeShuffle by remember(s.key){mutableStateOf(s.tiles)}
    var returning by remember(s.key){mutableStateOf(emptySet<Int>())}
    val match=remember(s.key){Animatable(1f)}
    val shuffle=remember(s.key){Animatable(1f)}
    val deal=remember(s.key){Animatable(if(reduceMotion)1f else 0f)}
    val restore=remember(s.key){Animatable(1f)}
    val lift by animateFloatAsState(if(s.selectedId!=null)1f else 0f,spring(dampingRatio=.58f,stiffness=420f),label="tile lift")
    val pulse=if(reduceMotion)1f else rememberInfiniteTransition(label="free-pair glimmer").animateFloat(.55f,1f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="hint glimmer").value
    LaunchedEffect(s.key,reduceMotion){if(reduceMotion)deal.snapTo(1f)else deal.animateTo(1f,tween(520,easing=FastOutSlowInEasing))}
    LaunchedEffect(s.tiles,s.shuffles,reduceMotion) {
        val active=s.tiles.filterNot(Tile::removed).map(Tile::id).toSet()
        val oldActive=previous.filterNot(Tile::removed).map(Tile::id).toSet()
        val removed=previous.filter {it.id in oldActive-active}
        val restored=active-oldActive
        beforeShuffle=previous;previous=s.tiles
        val shuffled=s.shuffles!=lastShuffle;lastShuffle=s.shuffles
        if(reduceMotion){leaving=emptyList();match.snapTo(1f);shuffle.snapTo(1f);restore.snapTo(1f)}
        else if(removed.size==2) {leaving=removed;match.snapTo(0f);match.animateTo(1f,tween(620,easing=LinearEasing));leaving=emptyList()}
        else if(shuffled) {shuffle.snapTo(0f);shuffle.animateTo(1f,tween(580,easing=FastOutSlowInEasing))}
        else if(restored.isNotEmpty()){returning=restored;restore.snapTo(0f);restore.animateTo(1f,spring(.6f,360f));returning=emptySet()}
    }
    val density=LocalDensity.current
    val minX=s.tiles.minOfOrNull { it.x*50f+it.z*6f }?:0f
    val maxX=s.tiles.maxOfOrNull { it.x*50f+it.z*6f+100f }?:100f
    val minY=s.tiles.minOfOrNull { it.y*62f-it.z*9f }?:0f
    val maxY=s.tiles.maxOfOrNull { it.y*62f-it.z*9f+132f }?:132f
    val bw=maxX-minX;val bh=maxY-minY
    val unitMin=with(density){42.dp.toPx()}/100f
    val fit=min((viewport.width-32f)/bw,(viewport.height-24f)/bh).coerceAtLeast(.01f)
    val base=max(fit,unitMin);val scale=base*zoom
    fun clamp(p:Offset,z:Float):Offset {
        val x=max(0f,(bw*base*z-viewport.width)/2)+25f;val y=max(0f,(bh*base*z-viewport.height)/2)+25f
        return Offset(p.x.coerceIn(-x,x),p.y.coerceIn(-y,y))
    }
    val free=remember(s.tiles){Rules.freeIds(s.tiles).toSet()}
    val description=stringResource(R.string.board_accessibility)
    val zoomInLabel=stringResource(R.string.zoom_in);val zoomOutLabel=stringResource(R.string.zoom_out)
    val tapsLocked=busy||(!reduceMotion&&(match.value<1f||shuffle.value<1f||deal.value<.85f))
    Column(modifier) {
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onSizeChanged{viewport=it}) {
            GardenBackdrop(Modifier.fillMaxSize(),background,.22f)
            Canvas(Modifier.fillMaxSize().semantics { contentDescription=description }
                .pointerInput(s.key,scale,pan,s.tiles,tapsLocked) {
                    detectTapGestures { screen ->
                        if(tapsLocked)return@detectTapGestures
                        val model=(screen-Offset(size.width/2f,size.height/2f)-pan)/scale+Offset((minX+maxX)/2,(minY+maxY)/2)
                        val hit=s.tiles.filterNot(Tile::removed).sortedWith(compareByDescending<Tile>{it.z}.thenByDescending{it.y}.thenByDescending{it.x}).firstOrNull {
                            val x=it.x*50f+it.z*6f;val y=it.y*62f-it.z*9f
                            model.x in x..x+100f && model.y in y..y+119f
                        };hit?.let {onTap(it.id)}
                    }
                }
                .pointerInput(s.key,base,viewport) {
                    detectTransformGestures { centroid,delta,factor,_ ->
                        val old=zoom;val next=(zoom*factor).coerceIn(.75f,3.5f);val c=Offset(size.width/2f,size.height/2f)
                        pan=clamp((pan+c-centroid)*(next/old)-(c-centroid)+delta,next);zoom=next
                    }
                }) {
                val centreModel=Offset((minX+maxX)/2,(minY+maxY)/2)
                drawRect(Brush.verticalGradient(listOf(Emerald.copy(alpha=.06f),Color(0xFF042C29).copy(alpha=.24f),Emerald.copy(alpha=.08f))))
                withTransform({translate(size.width/2+pan.x,size.height/2+pan.y);scale(scale,scale,Offset.Zero);translate(-centreModel.x,-centreModel.y)}) {
                    s.tiles.sortedWith(compareBy<Tile>{it.z}.thenBy{it.y}.thenBy{it.x}).filterNot(Tile::removed).forEach { t ->
                        val sh=shuffle.value;val isShuffle=sh<1f
                        val shown=if(isShuffle&&sh<.5f)beforeShuffle.firstOrNull{it.id==t.id}?:t else t
                        val enter=if(reduceMotion)1f else ((deal.value-(t.id%7)*.012f)/.91f).coerceIn(0f,1f)
                        val x=t.x*50f+t.z*6f+(if(isShuffle)sin(sh*PI).toFloat()*sin(t.id*2.4f)*32f else 0f)
                        val y=t.y*62f-t.z*9f-(1f-enter)*65f-(if(isShuffle)sin(sh*PI).toFloat()*50f else 0f)
                        val pop=if(t.id in returning)restore.value.coerceAtLeast(.05f)else 1f
                        withTransform({translate(x,y);if(t.id==s.selectedId)translate(0f,-7f*lift);scale(if(isShuffle)abs(cos(sh*PI)).toFloat().coerceAtLeast(.06f)else if(t.id==s.selectedId)1f+.045f*lift else pop,if(t.id==s.selectedId)1f+.045f*lift else pop,Offset(50f,60f));if(t.id==s.selectedId)rotate(-2.5f*lift,Offset(50f,60f))}) {
                            tileFace(shown,t.id==s.selectedId,t.id==hint?.a||t.id==hint?.b,t.id in free,enter,atlas)
                            if((t.id==hint?.a||t.id==hint?.b)&&!reduceMotion)drawRoundRect(Color(0xFFB9FFE6).copy(alpha=.35f*pulse),Offset(-3f,-3f),Size(106f,127f),CornerRadius(14f),style=Stroke(3f))
                        }
                    }
                    if(leaving.isNotEmpty()) {
                        val p=match.value;val q=FastOutSlowInEasing.transform((p/.76f).coerceIn(0f,1f))
                        leaving.forEachIndexed {index,t->
                            val sign=if(index==0)-1f else 1f
                            val from=Offset(t.x*50f+t.z*6f,t.y*62f-t.z*9f)
                            val target=centreModel-Offset(50f,65f)
                            val pos=when(matchStyle){
                                1->{val a=p*PI.toFloat()*2.6f+index*PI.toFloat();val orbit=Offset(cos(a),sin(a))*45f*(1f-p);from*(1f-q)+(target+orbit)*q}
                                2->from+Offset(sign*sin(p*PI).toFloat()*66f,-p*190f)
                                else->from*(1f-q)+(target+Offset(sign*22f,-sin(p*PI).toFloat()*45f))*q
                            }
                            val a=if(p<.72f)1f else ((1f-p)/.28f).coerceIn(0f,1f)
                            val z=(1f+.13f*sin(p*PI).toFloat())*(if(p>.76f)(1f-(p-.76f)/.24f)else 1f).coerceAtLeast(.02f)
                            withTransform({translate(pos.x,pos.y);rotate(sign*sin(p*PI).toFloat()*if(matchStyle==1)110f else 17f,Offset(50f,60f));scale(z,z,Offset(50f,60f))}){tileFace(t,true,false,true,a,atlas)}
                        }
                        val burst=((p-.4f)/.6f).coerceIn(0f,1f)
                        if(burst>0f)repeat(26){i->val a=i*2.39996f;val r=(28f+burst*125f)*(if(i%3==0)1.2f else .8f);val pos=centreModel+Offset(cos(a)*r,sin(a)*r-burst*35f)
                            withTransform({translate(pos.x,pos.y);rotate(i*37f+burst*140f)}) {
                                drawOval((if(i%3==0)Color(0xFFFFB4CF)else if(i%3==1)Color(0xFFA7FFE3)else Gold).copy(alpha=(1f-burst)*.85f),Offset.Zero,Size(if(i%3==0)8f else 4f,if(i%3==0)15f else 4f))
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=1.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            TextButton(modifier=Modifier.semantics{contentDescription=zoomOutLabel},onClick={zoom=(zoom*.8f).coerceAtLeast(.75f);pan=clamp(pan,zoom)}){Text("−",color=Gold,fontSize=24.sp)}
            TextButton(onClick={zoom=1f;pan=Offset.Zero}){Text(stringResource(R.string.recenter),color=Mist,style=MaterialTheme.typography.labelMedium)}
            TextButton(modifier=Modifier.semantics{contentDescription=zoomInLabel},onClick={zoom=(zoom*1.25f).coerceAtMost(3.5f);pan=clamp(pan,zoom)}){Text("+",color=Gold,fontSize=24.sp)}
        }
    }
}
