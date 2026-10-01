package com.example.wifilocalization
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
data class DrawAnchor(val name:String,val x:Double,val y:Double,val distance:Double?)
class LocalizationView(c:Context,a:AttributeSet?):View(c,a){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG); var anchors:List<DrawAnchor> = emptyList(); var position:Pair<Double,Double>?=null
 override fun onDraw(cv:Canvas){ super.onDraw(cv); cv.drawColor(Color.WHITE); if(anchors.isEmpty())return
  val m=55f; var mx=max(10.0,anchors.maxOf{it.x}+2); var my=max(8.0,anchors.maxOf{it.y}+2)
  position?.let{mx=max(mx,it.first+2);my=max(my,it.second+2)}
  val s=min((width-2*m)/mx.toFloat(),(height-2*m)/my.toFloat()); fun X(x:Double)=m+x.toFloat()*s; fun Y(y:Double)=height-m-y.toFloat()*s
  p.color=0xffe0e0e0.toInt();p.strokeWidth=1f
  for(i in 0..mx.toInt())cv.drawLine(X(i.toDouble()),m,X(i.toDouble()),height-m,p)
  for(i in 0..my.toInt())cv.drawLine(m,Y(i.toDouble()),width-m,Y(i.toDouble()),p)
  anchors.forEach{q-> q.distance?.let{d->p.style=Paint.Style.STROKE;p.color=0xff78909c.toInt();p.strokeWidth=3f;cv.drawCircle(X(q.x),Y(q.y),d.toFloat()*s,p)}
   p.style=Paint.Style.FILL;p.color=0xff263238.toInt();cv.drawCircle(X(q.x),Y(q.y),10f,p);p.textSize=28f;cv.drawText("${q.name} (${q.x},${q.y})",X(q.x)+13,Y(q.y)-12,p)}
  position?.let{p.color=0xffd32f2f.toInt();cv.drawCircle(X(it.first),Y(it.second),13f,p);p.textSize=30f;cv.drawText("Phone",X(it.first)+15,Y(it.second),p)}
 }
}