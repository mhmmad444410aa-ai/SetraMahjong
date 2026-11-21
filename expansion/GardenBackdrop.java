package com.mhmmad.setramahjong.expansion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Lightweight original garden artwork behind the new modes, drawn fully offline. */
public final class GardenBackdrop extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public GardenBackdrop(Context context) { super(context); }
    private void color(int value) { p.reset(); p.setAntiAlias(true); p.setStyle(Paint.Style.FILL); p.setColor(value); }
    private void leaf(Canvas canvas,float x,float y,float angle,float size,int green) {
        canvas.save(); canvas.translate(x,y); canvas.rotate(angle);
        color(green);
        Path leaf=new Path(); leaf.moveTo(0,0);
        leaf.cubicTo(-9*size,-13*size,-11*size,-25*size,0,-33*size);
        leaf.cubicTo(12*size,-25*size,10*size,-12*size,0,0); leaf.close();
        canvas.drawPath(leaf,p);
        p.setColor(0x6679c6a8);p.setStrokeWidth(size);canvas.drawLine(0,0,0,-29*size,p);
        canvas.restore();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()<=0||getHeight()<=0)return;
        canvas.save(); canvas.scale(getWidth()/360f,getHeight()/760f);
        color(0xff092d2a);
        p.setShader(new LinearGradient(0,0,280,760,new int[]{0xff0a2928,0xff0e5145,0xff061f20},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,360,760,p); p.setShader(null);
        color(0x355abda1);
        p.setShader(new RadialGradient(176,155,310,new int[]{0x665fc0a5,0x220ba18b,0x000ba18b},null,Shader.TileMode.CLAMP));
        canvas.drawCircle(176,155,310,p);p.setShader(null);
        color(0x44d8c182);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);
        Path arch=new Path();arch.moveTo(22,760);arch.lineTo(22,116);arch.cubicTo(22,-8,338,-8,338,116);arch.lineTo(338,760);
        canvas.drawPath(arch,p);
        p.setStrokeWidth(1.4f);p.setColor(0x889bcb99);canvas.drawPath(arch,p);
        color(0x1fcee6aa); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(10);
        canvas.drawRoundRect(12,26,348,790,160,160,p);
        for(int side=0;side<2;side++) {
            canvas.save(); if(side==1){canvas.translate(360,0);canvas.scale(-1,1);}
            color(0x5541a277);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
            Path vine=new Path(); vine.moveTo(-4,25);vine.cubicTo(65,112,18,210,47,288);
            vine.cubicTo(2,360,52,480,-8,597); canvas.drawPath(vine,p);
            for(int i=0;i<17;i++){
                float y=50+i*37, x=12+(float)Math.sin(i*1.15)*19;
                leaf(canvas,x,y,(i%2==0?70:-75),0.74f,i%3==0?0xff3b8b68:0xff286c52);
            }
            canvas.restore();
        }
        for(int i=0;i<18;i++){
            float x=(i*97)%328+16, y=(i*137)%690+28;
            color(i%3==0?0x80f6d38d:0x447bc8ac);canvas.drawCircle(x,y,i%4==0?1.8f:0.9f,p);
        }
        color(0x22549a7b);canvas.drawOval(-50,675,192,820,p);canvas.drawOval(145,684,398,850,p);
        canvas.restore();
    }
}
