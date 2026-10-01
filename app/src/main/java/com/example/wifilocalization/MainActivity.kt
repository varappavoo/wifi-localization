package com.example.wifilocalization
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.*

data class Anchor(val name:String,val bssid:String,val x:Double,val y:Double)
data class Measurement(val a:Anchor,val rssi:Int,val d:Double)

class MainActivity:AppCompatActivity(){
 // EDIT THESE. Coordinates are physical positions in METRES.
 private val anchors=listOf(
  Anchor("AP1","D2:C4:DF:9C:6A:D9",0.0,0.0),
  Anchor("AP2","BA:4D:BF:8B:DF:2D",3.0,0.0),
  Anchor("AP3","E6:F7:87:54:59:76",0.0,3.0))
 private val rssiAt1m=-40.0
 private val pathLossN=2.5
 private lateinit var wifi:WifiManager
 private lateinit var status:TextView
 private lateinit var results:TextView
 private lateinit var map:LocalizationView

 private val receiver=object:BroadcastReceiver(){
  override fun onReceive(c:Context?,i:Intent?){showResults()}
 }

 override fun onCreate(b:Bundle?){
  super.onCreate(b); setContentView(R.layout.activity_main)
  wifi=applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  status=findViewById(R.id.statusText); results=findViewById(R.id.resultsText); map=findViewById(R.id.localizationView)
  findViewById<Button>(R.id.scanButton).setOnClickListener{if(hasPermissions())scan() else requestPermissionsNow()}
  ContextCompat.registerReceiver(this,receiver,IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),ContextCompat.RECEIVER_NOT_EXPORTED)
  if(!hasPermissions())requestPermissionsNow()
 }

 private fun hasPermissions():Boolean{
  val fine=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
  val nearby=Build.VERSION.SDK_INT<33||ContextCompat.checkSelfPermission(this,Manifest.permission.NEARBY_WIFI_DEVICES)==PackageManager.PERMISSION_GRANTED
  return fine&&nearby
 }

 private fun requestPermissionsNow(){
  val p=mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
  if(Build.VERSION.SDK_INT>=33)p.add(Manifest.permission.NEARBY_WIFI_DEVICES)
  ActivityCompat.requestPermissions(this,p.toTypedArray(),10)
 }

 @Suppress("DEPRECATION")
 private fun scan(){
  status.text="Scanning..."
  if(!wifi.startScan()){status.text="Scan throttled; using latest results.";showResults()}
 }

 private fun distance(rssi:Int)=10.0.pow((rssiAt1m-rssi.toDouble())/(10.0*pathLossN))

 @Suppress("DEPRECATION")
 private fun showResults(){
  if(!hasPermissions())return
  val scans=wifi.scanResults
  val ms=anchors.mapNotNull{a->scans.firstOrNull{it.BSSID.equals(a.bssid,true)}?.let{Measurement(a,it.level,distance(it.level))}}
  results.text=anchors.joinToString("\n"){a->
   ms.firstOrNull{it.a.name==a.name}?.let{"${a.name}  ${a.bssid}   ${it.rssi} dBm   %.2f m".format(it.d)}?:"${a.name}  ${a.bssid}   not visible"
  }
  val pos=if(ms.size==3)trilaterate(ms[0],ms[1],ms[2]) else null
  map.anchors=anchors.map{a->DrawAnchor(a.name,a.x,a.y,ms.firstOrNull{it.a.name==a.name}?.d)}
  map.position=pos; map.invalidate()
  status.text=pos?.let{"Estimated phone position: (%.2f, %.2f) m".format(it.first,it.second)}?:"All three configured APs must be visible."
 }

 private fun trilaterate(m1:Measurement,m2:Measurement,m3:Measurement):Pair<Double,Double>?{
  val a=m1.a; val b=m2.a; val c=m3.a
  val A=2*(b.x-a.x); val B=2*(b.y-a.y)
  val C=m1.d*m1.d-m2.d*m2.d-a.x*a.x+b.x*b.x-a.y*a.y+b.y*b.y
  val D=2*(c.x-a.x); val E=2*(c.y-a.y)
  val F=m1.d*m1.d-m3.d*m3.d-a.x*a.x+c.x*c.x-a.y*a.y+c.y*c.y
  val det=A*E-B*D
  return if(abs(det)<1e-9)null else Pair((C*E-B*F)/det,(A*F-C*D)/det)
 }

 override fun onDestroy(){unregisterReceiver(receiver);super.onDestroy()}
}
