package com.mhmmad.setramahjong.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.mhmmad.setramahjong.engine.Tile
import kotlin.math.*

val Emerald=Color(0xFF062E2B)
val Gold=Color(0xFFF1D18B)
val Ivory=Color(0xFFF6F0DF)
val Mist=Color(0xFFCEE1D5)
val GardenBackgrounds=listOf(Color(0xFF103E35),Color(0xFF102F40),Color(0xFF3F4930),Color(0xFF70533E))
private val inks=listOf(Color(0xFF27745B),Color(0xFFA35460),Color(0xFF477786),Color(0xFF9F7B3A))
fun DrawScope.ornament(symbol:Int, colour:Color?=null) {
    val c=colour?:inks[symbol%4]
    val stroke=Stroke(width=4f,cap=StrokeCap.Round,join=StrokeJoin.Round)
    fun path(build:Path.()->Unit,fill:Boolean=false){drawPath(Path().apply(build),c,style=if(fill)Fill else stroke)}
    when(symbol%16) {
        0 -> {
            path({moveTo(23f,79f);cubicTo(10f,43f,46f,12f,80f,19f);cubicTo(89f,53f,64f,88f,23f,79f);close()},true)
            drawLine(Ivory.copy(alpha=.9f),Offset(25f,78f),Offset(70f,29f),3f,StrokeCap.Round)
            drawLine(Ivory.copy(alpha=.7f),Offset(43f,59f),Offset(41f,38f),2f)
            drawLine(Ivory.copy(alpha=.7f),Offset(51f,50f),Offset(71f,51f),2f)
        }
        1 -> {
            for(a in listOf(-42f,0f,42f)) rotate(a,Offset(50f,77f)) {path({moveTo(50f,77f);cubicTo(20f,56f,31f,29f,50f,13f);cubicTo(69f,29f,80f,56f,50f,77f);close()})}
            path({moveTo(16f,79f);quadraticTo(50f,96f,84f,79f)})
        }
        2 -> {
            path({for(i in 0..15){val a=(-90+i*22.5)*PI/180;val r=if(i%2==0)39 else 21;val x=50+r*cos(a);val y=50+r*sin(a);if(i==0)moveTo(x.toFloat(),y.toFloat()) else lineTo(x.toFloat(),y.toFloat())};close()},true)
            drawCircle(Ivory.copy(alpha=c.alpha),10f,Offset(50f,50f));drawCircle(c,5f,Offset(50f,50f))
        }
        3 -> {path({moveTo(22f,85f);lineTo(22f,48f);cubicTo(22f,9f,78f,9f,78f,48f);lineTo(78f,85f);close()});path({moveTo(38f,84f);lineTo(38f,50f);cubicTo(38f,30f,62f,30f,62f,50f);lineTo(62f,84f)});drawLine(c,Offset(15f,86f),Offset(85f,86f),4f)}
        4 -> {path({moveTo(50f,88f);lineTo(50f,16f)});for(y in listOf(34f,56f,75f)){path({moveTo(50f,y);quadraticTo(20f,y-1,24f,y-23);quadraticTo(47f,y-23,50f,y);close()},true);path({moveTo(50f,y-8);quadraticTo(78f,y-8,78f,y-31);quadraticTo(54f,y-31,50f,y-8);close()},true)}}
        5 -> {repeat(8){i->rotate(i*45f,Offset(50f,50f)){drawOval(c,Offset(42f,12f),Size(16f,37f),style=stroke)}};drawCircle(c,10f,Offset(50f,50f))}
        6 -> {path({moveTo(50f,9f);lineTo(86f,50f);lineTo(50f,91f);lineTo(14f,50f);close()});path({moveTo(50f,28f);lineTo(68f,50f);lineTo(50f,72f);lineTo(32f,50f);close()},true)}
        7 -> {path({moveTo(50f,84f);lineTo(50f,55f)});path({moveTo(22f,23f);lineTo(39f,32f);lineTo(50f,14f);lineTo(61f,32f);lineTo(78f,23f);cubicTo(82f,68f,18f,68f,22f,23f);close()},true);path({moveTo(50f,78f);quadraticTo(72f,77f,76f,58f);quadraticTo(53f,59f,50f,78f)})}
        8 -> {for(i in 0..2){val k=18f+i*23;path({moveTo(k,14f);lineTo(k+26,50f);lineTo(k,86f)});path({moveTo(100-k,14f);lineTo(74-k,50f);lineTo(100-k,86f)})}}
        9 -> {path({moveTo(50f,88f);quadraticTo(45f,43f,50f,15f)});for(i in 0..3){val y=30f+i*14;path({moveTo(50f,y+11);quadraticTo(18f,y+5,17f,y-10)});path({moveTo(50f,y+11);quadraticTo(82f,y+5,83f,y-10)})}}
        10 -> {path({moveTo(18f,60f);quadraticTo(50f,94f,82f,60f);close()},true);path({moveTo(50f,59f);lineTo(50f,19f)});path({moveTo(50f,31f);cubicTo(28f,8f,19f,31f,20f,43f)});path({moveTo(50f,31f);cubicTo(72f,8f,81f,31f,80f,43f)});drawLine(c,Offset(27f,86f),Offset(73f,86f),4f)}
        11 -> {drawCircle(c,23f,Offset(50f,50f),style=stroke);repeat(12){i->rotate(i*30f,Offset(50f,50f)){drawLine(c,Offset(50f,10f),Offset(50f,19f),4f,StrokeCap.Round)}};drawCircle(c,8f,Offset(50f,50f))}
        12 -> {path({moveTo(25f,84f);quadraticTo(69f,53f,71f,18f)});repeat(3){i->val x=36f+i*12;val y=70f-i*19;drawOval(c,Offset(x-15,y-19),Size(15f,23f));drawOval(c,Offset(x+8,y-8),Size(17f,12f))}}
        13 -> {path({moveTo(15f,85f);lineTo(15f,39f);lineTo(50f,12f);lineTo(85f,39f);lineTo(85f,85f);close()});path({moveTo(34f,84f);lineTo(34f,48f);quadraticTo(50f,20f,66f,48f);lineTo(66f,84f)});drawCircle(c,4f,Offset(50f,18f))}
        14 -> {repeat(4){i->rotate(i*90f,Offset(50f,50f)){path({moveTo(50f,50f);cubicTo(17f,45f,19f,12f,50f,9f);cubicTo(81f,12f,83f,45f,50f,50f);close()},true)}};drawCircle(Ivory.copy(alpha=c.alpha),7f,Offset(50f,50f))}
        else -> {rotate(45f,Offset(50f,50f)){drawRoundRect(c,Offset(17f,32f),Size(66f,36f),CornerRadius(8f),style=stroke)};rotate(-45f,Offset(50f,50f)){drawRoundRect(c,Offset(17f,32f),Size(66f,36f),CornerRadius(8f),style=stroke)}}
    }
}
fun DrawScope.tileFace(tile:Tile,selected:Boolean=false,hinted:Boolean=false,free:Boolean=true,alpha:Float=1f,atlas:ImageBitmap?=null) {
    val face=if(free)listOf(Color(0xFFFFFEF5),Color(0xFFF9F0D9),Color(0xFFE7D9B4))else listOf(Color(0xFFDAE1CD),Color(0xFFC2CFB8),Color(0xFFA8BAA4))
    if(selected||hinted) {
        drawRoundRect((if(selected)Gold else Color(0xFF81FFE4)).copy(alpha=.17f*alpha),Offset(-7f,-7f),Size(114f,143f),CornerRadius(19f))
    }
    drawRoundRect(Color.Black.copy(alpha=.36f*alpha),Offset(3f,12f),Size(102f,125f),CornerRadius(14f))
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF70BC9F).copy(alpha=alpha),Color(0xFF0D6455).copy(alpha=alpha),Color(0xFF06463F).copy(alpha=alpha))),Offset(0f,7f),Size(100f,126f),CornerRadius(13f))
    drawRoundRect(Color(0xFFB9A064).copy(alpha=alpha),Offset(0f,3f),Size(100f,121f),CornerRadius(13f))
    drawRoundRect(Brush.linearGradient(face.map{it.copy(alpha=alpha)},start=Offset(10f,0f),end=Offset(85f,120f)),Offset.Zero,Size(100f,118f),CornerRadius(12f))
    drawRoundRect(Color.White.copy(alpha=.88f*alpha),Offset(2.5f,2.5f),Size(95f,110f),CornerRadius(10f),style=Stroke(2f))
    drawRoundRect(Color(0xFFB08A3C).copy(alpha=.23f*alpha),Offset(7f,7f),Size(86f,101f),CornerRadius(7f),style=Stroke(.85f))
    drawLine(Color.White.copy(alpha=.8f*alpha),Offset(17f,5f),Offset(80f,5f),2f,StrokeCap.Round)
    if(atlas!=null)atlasSymbol(atlas,tile.symbol,Offset(9f,15f),Size(82f,82f),alpha*if(free)1f else .58f)
    else withTransform({translate(14f,17f);scale(.72f,.72f,Offset.Zero)}) { ornament(tile.symbol,inks[tile.symbol%4].copy(alpha=alpha*if(free)1f else .5f)) }
    drawCircle(Gold.copy(alpha=.75f*alpha),1.6f,Offset(50f,105f))
    if(selected||hinted)drawRoundRect(if(selected)Color(0xFFFFDE87).copy(alpha=alpha)else Color(0xFF85FFE4).copy(alpha=alpha),Offset(-1f,-1f),Size(102f,121f),CornerRadius(13f),style=Stroke(3f))
}
@Composable fun Ornament(symbol:Int,modifier:Modifier=Modifier,colour:Color=Gold) {
    val atlas=gardenAtlas()
    Canvas(modifier){atlasSymbol(atlas,symbol,opacity=colour.alpha)}
}
@Composable fun MiniLayout(tiles:List<Tile>,modifier:Modifier=Modifier) {
    val atlas=gardenAtlas()
    Canvas(modifier) {
        if(tiles.isEmpty())return@Canvas
        val minX=tiles.minOf{it.x*50f+it.z*6f};val maxX=tiles.maxOf{it.x*50f+it.z*6f+100f}
        val minY=tiles.minOf{it.y*62f-it.z*9f};val maxY=tiles.maxOf{it.y*62f-it.z*9f+134f}
        val unit=min(size.width/(maxX-minX),size.height/(maxY-minY))
        withTransform({translate((size.width-(maxX-minX)*unit)/2,(size.height-(maxY-minY)*unit)/2);scale(unit,unit,Offset.Zero);translate(-minX,-minY)}) {
            tiles.sortedWith(compareBy<Tile>{it.z}.thenBy{it.y}.thenBy{it.x}).forEach {t->withTransform({translate(t.x*50f+t.z*6f,t.y*62f-t.z*9f)}){tileFace(t,atlas=atlas)}}
        }
    }
}
