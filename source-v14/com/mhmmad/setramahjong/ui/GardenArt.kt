package com.mhmmad.setramahjong.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.mhmmad.setramahjong.R
import kotlin.math.*

val LocalQuietMotion=staticCompositionLocalOf { false }
@Composable fun gardenAtlas():ImageBitmap=ImageBitmap.imageResource(R.drawable.symbol_atlas)
fun DrawScope.atlasSymbol(atlas:ImageBitmap,symbol:Int,topLeft:Offset=Offset.Zero,iconSize:Size=size,opacity:Float=1f) {
    val w=atlas.width/4;val h=atlas.height/4;val i=symbol.mod(16)
    drawImage(atlas,srcOffset=IntOffset((i%4)*w,(i/4)*h),srcSize=IntSize(w,h),dstOffset=IntOffset(topLeft.x.roundToInt(),topLeft.y.roundToInt()),dstSize=IntSize(iconSize.width.roundToInt().coerceAtLeast(1),iconSize.height.roundToInt().coerceAtLeast(1)),alpha=opacity,filterQuality=FilterQuality.Medium)
}
@Composable fun GardenBackdrop(modifier:Modifier=Modifier,variant:Int=0,dim:Float=.38f) {
    val quiet=LocalQuietMotion.current
    val drift=if(quiet)0f else rememberInfiniteTransition(label="garden atmosphere").animateFloat(0f,1f,infiniteRepeatable(tween(16000,easing=LinearEasing)),label="pollen drift").value
    Box(modifier) {
        Image(painterResource(R.drawable.garden_scene),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
        Box(Modifier.matchParentSize().background(GardenBackgrounds[variant.coerceIn(0,3)].copy(alpha=if(variant==0).14f else .44f)))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color(0xFF042C2A).copy(alpha=dim*.8f),Color(0xFF052D2C).copy(alpha=dim),Color(0xFF032522).copy(alpha=(dim+.12f).coerceAtMost(.9f))))))
        if(!quiet)Canvas(Modifier.matchParentSize()) {
            repeat(21){i->
                val x=((i*.618034f+sin(drift*6.283f+i)*.026f)%1f)*size.width
                val y=(1f-((i*.137f+drift*(.32f+i%3*.12f))%1f))*size.height
                val a=(.22f+.25f*sin(drift*6.283f+i*1.3f)).coerceIn(.06f,.5f)
                drawCircle(Gold.copy(alpha=a*.18f),7f,Offset(x,y));drawCircle(Color(0xFFFFEEB5).copy(alpha=a),1.5f+i%3,Offset(x,y))
            }
            repeat(5){i->val a=drift*6.283f+i;val x=if(i%2==0)size.width*.08f else size.width*.92f;val y=size.height*((i*.23f+drift*.3f)%1f)
                withTransform({translate(x+sin(a)*14f,y);rotate(sin(a)*22f)}){drawOval(Color(0xFFFFB8C5).copy(alpha=.34f),Offset.Zero,Size(8f,15f))}
            }
        }
    }
}
@Composable fun BloomMedallion(symbol:Int,modifier:Modifier=Modifier,animate:Boolean=true) {
    val atlas=gardenAtlas();val quiet=LocalQuietMotion.current
    val f=if(quiet||!animate)0f else rememberInfiniteTransition(label="living ornament").animateFloat(-1f,1f,infiniteRepeatable(tween(2400,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="ornament float").value
    Canvas(modifier) {
        val r=min(size.width,size.height)*.45f
        drawCircle(Color.Black.copy(alpha=.28f),r,center+Offset(0f,7f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFF7ACDBC),Color(0xFF146E62),Color(0xFF063C36)),center=center),r,center)
        drawCircle(Brush.linearGradient(listOf(Color(0xFFFFEDBE),Gold,Color(0xFF916020))),r,center,style=Stroke(3.dp.toPx()))
        drawCircle(Gold.copy(alpha=.22f),r*.87f,center,style=Stroke(1.dp.toPx()))
        repeat(12){i->val a=(i*30f-90f)*PI/180;drawCircle(Gold.copy(alpha=.8f),1.6.dp.toPx(),center+Offset(cos(a).toFloat(),sin(a).toFloat())*(r*.94f))}
        val side=r*1.45f
        atlasSymbol(atlas,symbol,center-Offset(side/2,side/2+f*3.dp.toPx()),Size(side,side))
    }
}
@Composable fun VictoryBloom(modifier:Modifier=Modifier) {
    val quiet=LocalQuietMotion.current
    var appeared by remember{mutableStateOf(quiet)}
    LaunchedEffect(Unit){appeared=true}
    val grow by animateFloatAsState(if(appeared)1f else .1f,spring(dampingRatio=.55f,stiffness=140f),label="bloom opening")
    val phase=if(quiet)1f else rememberInfiniteTransition(label="victory pollen").animateFloat(0f,1f,infiniteRepeatable(tween(4200,easing=LinearEasing)),label="victory trails").value
    Box(modifier,contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            repeat(28){i->val a=(i*137.5f)*PI/180;val d=min(size.width,size.height)*(.25f+.25f*((phase+i*.071f)%1f));val p=center+Offset(cos(a).toFloat(),sin(a).toFloat())*d
                drawCircle(if(i%3==0)Color(0xFFFFBFCD).copy(alpha=.65f)else Gold.copy(alpha=.5f),if(i%4==0)3.dp.toPx()else 1.5.dp.toPx(),p)
            }
        }
        BloomMedallion(1,Modifier.fillMaxSize(.74f).graphicsLayer{scaleX=grow;scaleY=grow})
    }
}
