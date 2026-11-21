package com.mhmmad.setramahjong
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhmmad.setramahjong.ui.SetraApp

class MainActivity:ComponentActivity() {
    private lateinit var vm:GameViewModel
    private lateinit var sounds:SoundPool
    private var tapSound=0
    private var matchSound=0
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),navigationBarStyle=SystemBarStyle.dark(0xFF0B302D.toInt()))
        vm=ViewModelProvider(this)[GameViewModel::class.java]
        sounds=SoundPool.Builder().setMaxStreams(2).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
        tapSound=sounds.load(this,R.raw.tap,1);matchSound=sounds.load(this,R.raw.match,1)
        setContent {
            val u by vm.ui.collectAsStateWithLifecycle();val p by vm.prefs.collectAsStateWithLifecycle()
            var oldKey by remember{mutableStateOf("")};var oldMoves by remember{mutableIntStateOf(0)};var oldSelected by remember{mutableStateOf<Int?>(null)}
            LaunchedEffect(u.session?.key,u.session?.moves,u.session?.selectedId) {
                val s=u.session
                if(s!=null){
                    if(oldKey==s.key&&u.page=="game") {
                        if(s.moves>oldMoves){if(p.sound)sounds.play(matchSound,.25f,.25f,1,0,1f);if(p.haptics)window.decorView.performHapticFeedback(if(android.os.Build.VERSION.SDK_INT>=30)HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)}
                        else if(s.selectedId!=null&&s.selectedId!=oldSelected){if(p.sound)sounds.play(tapSound,.16f,.16f,1,0,1f);if(p.haptics)window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)}
                    }
                    oldKey=s.key;oldMoves=s.moves;oldSelected=s.selectedId
                }
            }
            SetraApp(vm,this)
        }
    }
    override fun onStart(){super.onStart();if(::vm.isInitialized)vm.foreground(true)}
    override fun onStop(){if(::vm.isInitialized)vm.foreground(false);super.onStop()}
    override fun onDestroy(){if(::sounds.isInitialized)sounds.release();super.onDestroy()}
}
