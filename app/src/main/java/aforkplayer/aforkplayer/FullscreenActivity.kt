package aforkplayer.aforkplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_fullscreen.*
import android.os.Build
import android.view.KeyEvent
import android.net.MacAddress
import android.net.wifi.WifiManager
import android.R.string
import android.content.Context
import android.net.wifi.WifiInfo
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import android.support.v4.content.ContextCompat
import android.app.Activity
import android.Manifest.permission
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_PHONE_STATE
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.R.bool
import android.graphics.Bitmap
import android.util.Log.INFO
import android.webkit.*
import android.widget.Toast
import java.lang.reflect.Array

import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

import android.content.Intent
import android.os.Environment


import android.view.GestureDetector
import android.view.MotionEvent

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class FullscreenActivity : AppCompatActivity() {



    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var mVisible: Boolean = false
    //private var view: WebView? = null
    lateinit var view: WebView
    var initial: String =""
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?): Boolean {

        view.loadUrl("javascript: keyHandler({'keycode':'" + keyCode + "/" + e?.scanCode + "'});")
        return super.onKeyDown(keyCode, e)
    }

    override fun dispatchKeyEvent(e: KeyEvent?): Boolean {
        return super.dispatchKeyEvent(e)
        if (e?.keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (e.action == 0) view.loadUrl("javascript: tmf();")
            return true
        }
        //  else if (e.KeyCode == Keycode.DpadCenter || e.KeyCode == Keycode.DpadDown || e.KeyCode == Keycode.DpadLeft || e.KeyCode == Keycode.DpadRight || e.KeyCode == Keycode.DpadUp) return true;
        else
        {
            if (e?.action == 0) view.loadUrl("javascript: keyHandler({'keycode':'" + e.keyCode + "/" + e.scanCode + "'});")
        }

    }

    fun getMac(context: Context): String {
        var m_wlanMac: String = ""
        try {
            val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            m_wlanMac = wifi.connectionInfo.macAddress.replace(":", "").toLowerCase()
        }
        catch (e: Exception) {
        }

        return m_wlanMac
    }
    fun getBt(context: Context): String {
        try {
            var m_BluetoothAdapter: android.bluetooth.BluetoothAdapter? = null // Local Bluetooth adapter
            m_BluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            return m_BluetoothAdapter!!.address.trim().replace("-", "").replace(":", "").toLowerCase()
        } catch (e: Exception) {

        }
        return ""
    }

    val PHONE_STATE_PERMISSION = permission.READ_PHONE_STATE

    fun checkPermission(permission: String, activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_fullscreen)

        view = findViewById<WebView>(R.id.webView)
        val settings = view.settings
        settings.loadWithOverviewMode= true
        settings.useWideViewPort = true
        if (Build.VERSION.SDK_INT > 16) {
            print("SET MediaPlaybackRequiresUserGesture")
            settings.mediaPlaybackRequiresUserGesture = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if (Build.VERSION.SDK_INT > 15) {
            settings.allowUniversalAccessFromFileURLs = true
            settings.allowFileAccessFromFileURLs = true
        }
        if (Build.VERSION.SDK_INT > 25) settings.safeBrowsingEnabled = false
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        print("SDK INT="+Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT < 19) {
            print("SET DatabasePath")
            settings.databasePath = "/data/data/aforkPlayer.aforkPlayer/databases/"
        }
        settings.defaultTextEncodingName = "UTF-8"
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36"

        //view.webViewClient(WebViewClient())
        var serial = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && checkPermission
                (PHONE_STATE_PERMISSION, this)) {
            serial = Build.getSerial()

        } else {
            serial = Build.SERIAL
        }
        view.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.d("Js Error",error.toString())
                super.onReceivedError(view, request, error)
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                Log.d("Js UnhandledKeyEvent",event.toString())
                super.onUnhandledKeyEvent(view, event)
            }

        }
        //if(Build.VERSION.SDK_INT > 25) serial = Build.getSerial()
        //else serial = android.os.Build.SERIAL
        var tuid = android.provider.Settings.Secure.getString(this.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        var ru = RootUtil(this)
        initial = getMac(this) + "|" + getMac(this) + "|" + android.os.Build.MODEL + " sdk " + android.os.Build.VERSION.SDK_INT + "|" + "aForkPlayer2.57|6.0|" + serial+"|"+ getBt(this)+"|"+tuid+"|"+ru.isDeviceRooted
        println("addJavascriptInterface")
        view.addJavascriptInterface(andr(this,this), "andr")
        view.loadUrl("file:///android_asset/index.html")

        //view.loadUrl("http://operatv.obovse.ru/2.5/")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        hide()
        mVisible = true

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.d("myApp", "double tap")
                view.loadUrl("javascript: keyHandler({'keycode':VK_ENTER});");
                return true
            }
            var x1=0
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                x1++
                // Console.WriteLine("OnScroll " + distanceX + "/" + distanceY);
                if (distanceX > 4)
                {
                    if (x1 % 3 == 0) view.loadUrl("javascript: if(handler=='qual') keyHandler({'keycode':VK_RIGHT});");
                }
                else if (distanceX < -4)
                {
                    if (x1 % 3 == 0) view.loadUrl("javascript: if(handler=='qual') keyHandler({'keycode':VK_LEFT});");
                }
                if (distanceY > 4)
                {
                    if (x1 % 3 == 0) view.loadUrl("javascript: keyHandler({'keycode':VK_UP});");

                }
                else if (distanceY < -4)
                {
                    if (x1 % 3 == 0) view.loadUrl("javascript: keyHandler({'keycode':VK_DOWN});");

                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                Log.d("onFling", "$velocityX / $velocityY")
                if (velocityX > 2000)
                {
                    // Console.WriteLine(">2000");
                    view.loadUrl("javascript: if(insetSelect<(insetCount+2)&&handler=='menu') {insetSelect++;Main.showInset();Main.PlayInset();}");
                }
                else if (velocityX < -2000)
                {
                    // Console.WriteLine("<1000");
                    view.loadUrl("javascript: if(insetSelect>0&&handler=='menu') {insetSelect--;Main.showInset();Main.PlayInset();}");
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        // Set up the user interaction to manually show or hide the system UI.

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            //show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    inner class RootUtil(internal var context: Context) {
        val isDeviceRooted: Boolean
            get() = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()

        private fun checkRootMethod1(): Boolean {
            val buildTags = android.os.Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        private fun checkRootMethod2(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            for (i in 0 until paths.size) {
                if (java.io.File(paths[i]).exists()) return true
            }
            return false
        }

        private fun checkRootMethod3(): Boolean {
            val pkg = arrayOf("eu.chainfire.supersu")
            for (i in 0 until pkg.size) {
                try {
                    val info = this.context.packageManager.getApplicationInfo(pkg[i], 0)
                    if (info.enabled) return true

                } catch (ex: android.content.pm.PackageManager.NameNotFoundException) {
                }

            }

            return false
        }
    }

    private inner class andr(context: Context,activity: Activity)
    {
       // fun andr(context: Context, activity: Activity) {
           // context = context
           // this.activity = activity
        //}
        var thread = Thread()
        fun getMXPackageInfo(): String
        {
            val pkg = arrayOf("com.mxtech.videoplayer.pro","com.mxtech.videoplayer.ad")
            for ( i in pkg)
            {
                try
                {
                    var info = this@FullscreenActivity.packageManager.getApplicationInfo(i, 0)
                    if (info.enabled) return i
                }
                catch (e:java.lang.Exception)
                {
                    println(e.printStackTrace())
                    printToast(e.message)
                }
            }
            return ""
        }
        fun getXMPackageInfo(): String
        {
            val pkg = arrayOf("com.xmtvplayer.watch.live.streams")
            for ( i in pkg)
            {
                try
                {
                    var info = this@FullscreenActivity.packageManager.getApplicationInfo(i, 0)
                    if (info.enabled) return i
                }
                catch (e:java.lang.Exception)
                {
                    println(e.printStackTrace())
                    printToast(e.message)
                }
            }
            return ""
        }

        fun getVimuPackageInfo(): String
        {
            val pkg = arrayOf("net.gtvbox.videoplayer")
            for ( i in pkg)
            {
                try
                {
                    var info = this@FullscreenActivity.packageManager.getApplicationInfo(i, 0)
                    if (info.enabled) return i
                }
                catch (e:java.lang.Exception)
                {
                    println(e.printStackTrace())
                    printToast(e.message)
                }
            }

            return ""
        }
        @android.webkit.JavascriptInterface
        fun cmd(cmds: String) :String
        {
            return cmd2(cmds, "")
        }
        @android.webkit.JavascriptInterface
        fun cmd2(cmd: String, mode: String): String
        {
            Log.d("ANDR CMD ", cmd)

            var s = ""
            if(cmd == "initial") s = initial
            else if(cmd=="hideSystemUI") hideSystemUI()
            else if (cmd.indexOf("curl") == 0)
            {
                s = ParseCurlRequest(cmd)
            }
            else if (cmd.indexOf("waitcurl") == 0||cmd.indexOf("asyncurl") == 0)
            {

                println("New WAIT curl " + cmd+" "+mode)
                thread =Thread {
                    try
                    {
                        s = ParseCurlRequest(cmd.substring(4))
                        var h = ""
                        var m=mode
                        println("stream $m")
                        var r = JSONObject()
                        var x = s.indexOf("\n\n");
                        if (x > 0 && cmd.indexOf(" -i") > 0)
                        {
                            h = s.substring(0, x)
                            s = s.substring(x + 2)
                        }
                        r.put("headers",h)
                        r.put("response",s)
                        println(r)
                        this@FullscreenActivity.runOnUiThread{
                            println("answstream $m")
                            this@FullscreenActivity.view.loadUrl("javascript: try{waitcurl(" + r + ",'" + m + "');}catch(e){console.log(e);}")
                        }
                    }
                    catch (e:java.lang.Exception)
                    {
                        println(e.printStackTrace())
                        printToast("Thread err: "+e.message)
                    }
                }

                thread.start()

                s = "wait"
            }
            else if (cmd.indexOf("exit") == 0)
            {
                println("exit ")
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            else if (cmd.indexOf("video") == 0)
            {
                println("VIDEO ")
                //return "wait";
                s = cmd.substring(5)
                println("VIDEO " + s)
                try
                {
                    var intent = Intent(Intent.ACTION_VIEW)

                    if (s.indexOf("#OPTVIMU:") > 0)
                    {
                        var title = s.substring(s.indexOf("#OPTVIMU:") + 9)
                        s = s.substring(0, s.indexOf("#OPT"))
                        var pkg = getVimuPackageInfo()
                        if (pkg == "")
                        {
                            printToast("Установите Vimu Player c PlayMarket или http://4pda.ru/forum/index.php?showtopic=777309!")
                            //MainActivity.view.LoadUrl("dnsconfirm(7, 'Установите MX Player!','Скачать сейчас|_FPOtmena_');");
                            return "vimuerror"
                        }
                        val videoUri = android.net.Uri.parse(s)      //"http://194.54.80.214/1.avi"
                        intent.setDataAndType(videoUri, "video/*")//application/x-mpegURL
                        //if (pkg == "com.mxtech.videoplayer.ad") intent2.SetClassName(pkg, "com.mxtech.videoplayer.ad.ActivityScreen");
                        //else intent2.SetClassName(pkg, "com.mxtech.videoplayer.ActivityScreen");
                        intent.setPackage(pkg)
                        //intent2.PutExtra("filename", s);
                        intent.putExtra("forcename", title)
                        if (intent!= null) this@FullscreenActivity.startActivity(intent)
                        else printToast("Intent Vimu null")
                    }
                    else if (s.indexOf("#OPT2:") > 0)
                    {
                        val title = s.substring(s.indexOf("#OPT2:") + 6)
                        s = s.substring(0, s.indexOf("#OPT2:"))
                        var headers =listOf<String>()
                        if (s.indexOf("#OPT:") > 0)
                        {
                            val ss = s.substring(s.indexOf("#OPT:") + 5)
                            headers = ss.split('|')
                            s = s.substring(0, s.indexOf("#OPT:"))
                        }

                        val pkg = getMXPackageInfo()
                        if (pkg == "")
                        {
                            printToast("Установите MX Player c PlayMarket!")
                            return "mxerror"
                        }
                        println("Play1=" + s)
                        println("Title=" + title)
                        val videoUri = android.net.Uri.parse(s)
                        intent.setDataAndType(videoUri, "video/*")
                        intent.setPackage(pkg)
                        intent.putExtra("title", title)
                        if (headers.isNotEmpty()) intent.putExtra("headers", headers.toTypedArray())
                        intent.putExtra("secure_uri", true)
                        intent.putExtra("orientation", 6)
                        this@FullscreenActivity.startActivity(intent)
                    }
                    else if (s.indexOf("#OPT:") > 0)
                    {
                        val ss = s.substring(s.indexOf("#OPT:") + 5)
                        s = s.substring(0, s.indexOf("#OPT:"))
                        var headers = ss.split('|').toTypedArray()
                        val pkg = getMXPackageInfo()
                        if (pkg == "")
                        {
                            printToast("Установите MX Player!")
                            return "mxerror"
                        }
                        println("Play2=" + s)
                        val videoUri = android.net.Uri.parse(s)
                        intent.setDataAndType(videoUri, "video/*")
                        intent.setPackage(pkg)
                        // intent2.PutExtra("filename", s)
                        intent.putExtra("headers", headers)
                        intent.putExtra("secure_uri", true)
                        intent.putExtra("orientation", 6)
                        this@FullscreenActivity.startActivity(intent)
                    }
                    else
                    {
                        var type="video/*"
                        val ss=s.split("|")
                        if(ss.size>1) {
                            type=ss[1]
                            s=ss[0]
                        }
                        else if (s.indexOf("torrent:") == 0)
                        {
                            type="application/x-bittorrent"
                        }

                        intent.setDataAndType(android.net.Uri.parse(s), type)
                        this@FullscreenActivity.startActivity(intent)

                    }
                }
                catch (e:java.lang.Exception)
                {
                    println(e.printStackTrace())
                    printToast(e.message)
                }
            }

            else if (cmd.indexOf("http://remotefo.rk/treeview") == 0)
            {

                println("New WAIT Plugin " + cmd+" "+mode)
                thread =Thread {
                    try
                    {
                        var h = ""
                        var m=mode
                        println("stream $m")
                        var httpUrl = cmd
                        if (httpUrl.indexOf("/treeview") > 0)
                        {
                            var pattern = "\\?(host=remotefo.rk&?)?(..*?)\\/?(&|$)".toRegex().find(httpUrl)!!.groupValues
                            var d=pattern[2]
                            println("GET LOCAL: " +d)
                           s = getFiles(d)
                        }
                        var r = JSONObject()
                        r.put("headers",h)
                        r.put("response",s)
                        println(r)
                        this@FullscreenActivity.runOnUiThread{
                            println("answstream $m")
                            this@FullscreenActivity.view.loadUrl("javascript: try{waitcurl(" + r + ",'" + m + "');}catch(e){console.log(e);}")
                        }
                    }
                    catch (e:java.lang.Exception)
                    {
                        println(e.printStackTrace())
                        printToast("Thread err: "+e.message)
                    }
                }

                thread.start()

                s = "wait"
            }

            return s
        }

        fun ParseCurlRequest(httpUrl:String):String
        {
            var result = ""
            var verbose = false
            if (httpUrl.indexOf(" -i") > 0) verbose = true
            var autoredirect = httpUrl.indexOf(" -L") > 0

            var pattern = "(?:\")(.*?)(?=\")".toRegex()
            val url = pattern.find(httpUrl)!!.groupValues[1]
            println("URL: $url")

            pattern = "(?:-H\\s\")(.*?)(?=\")".toRegex()
            var matches=pattern.findAll(httpUrl)
            var header=  mutableMapOf<String, String?>()
            matches.forEach {f ->
                val m = f.groupValues
                if(m.size>1) {
                    var value = m[1]
                    if (value.indexOf(": ")>0)
                    {
                        header[value.split(": ")[0]]=value.split(": ")[1]
                    }
                }
            }
            println(header)
            var dataString=""
            if (httpUrl.contains("--data")) {
                dataString = "(?:--data\\s\")(.*?)(?=\")".toRegex().find(httpUrl)!!.groupValues[1]
            }
            result = getRequest(url, header,dataString, verbose, autoredirect)
            return result
        }

        private fun getRequest(url:String, header: MutableMap<String,String?>, dataString:String, verbose:Boolean, autoredirect:Boolean):String {
            var result =""
            val connection = URL(url).openConnection() as HttpURLConnection
            try
            {
                for (el in header) connection.addRequestProperty( el.key, el.value)
                connection.readTimeout = 25*1000
                connection.instanceFollowRedirects=autoredirect
                if (dataString!="") {
                    connection.doOutput = true
                    connection.doInput = true
                    connection.useCaches = false
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                    connection.connect()
                    val os = connection.outputStream
                    val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                    writer.write(dataString)
                    writer.flush()
                    writer.close()
                    os.close()
                }
                else connection.connect()
                println(connection.responseCode)
                if(connection.responseCode!= 200) {
                    printToast("Error connect: "+connection.responseCode)
                    view.loadUrl("javascript:showinform('Error connect: "+connection.responseCode+"',1500)")
                }
                if(verbose) {
                    for (el in connection.headerFields) {
                        result +=el.key+": "+el.value+"\n"
                    }
                    result +="\n"
                }
                result += connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            }
            catch (e:java.lang.Exception)
            {
                println(e.printStackTrace())

                printToast(e.message)
            }
            finally {
                connection.disconnect()
            }
            return result
        }
        fun printToast(text: String?)
        {
            //toasting the text
            Toast.makeText(this@FullscreenActivity,text,Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
    fun getFiles(d:String):String{

        var CH=JSONArray()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || checkPermission
                (READ_EXTERNAL_STORAGE, this)) {

            var res=""
            var files =  File(d).listFiles()
            if(d=="/") {
                var channel=JSONObject("{\"title\":\"External Storage\",\"playlist_url\":\"http://remotefo.rk/treeview?"+Environment.getExternalStorageDirectory().absolutePath+"\"}")
                CH.put(channel)

                files =  File("/storage").listFiles()
            }
            when {
                files != null -> files.forEach { file ->
                    if (file != null) {
                        var channel=JSONObject()
                        channel.put("title",file.name)

                        if (file.isDirectory) {
                            channel.put("playlist_url","http://remotefo.rk/treeview?"+file.absolutePath+"/")
                        } else {
                            if(file.extension.matches("(xml|m3u|txt)".toRegex())) channel.put("playlist_url","http://remotefo.rk/treeview?"+file.absolutePath)
                            else channel.put("stream_url",file.absolutePath)
                            channel.put("description",file.absolutePath+"<br>"+file.length()/(1024*8)+"Кбайт")
                        }
                        CH.put(channel)
                    }
                }
            }
        }
        else CH.put(JSONObject("{\"title\":\"check Permission READ_EXTERNAL_STORAGE\"}"))
        val PL=JSONObject()
        PL.put("channels",CH)
        println(PL)
        return PL.toString()
    }

}
