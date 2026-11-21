package com.mhmmad.setramahjong

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mhmmad.setramahjong.data.*
import com.mhmmad.setramahjong.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

data class GameUi(val ready:Boolean=false,val page:String="home",val session:Session?=null,val sessions:Map<String,Session> = emptyMap(),val busy:Boolean=false,val hint:PairStep?=null,val error:Int?=null,val resultFirst:Boolean=false,val elapsed:Long=0,val combo:Int=0,val bestCombo:Int=0)
class GameViewModel(app:Application):AndroidViewModel(app) {
    private val repo=GameRepository(GameDatabase.open(app))
    private val prefsRepo=PreferenceRepository(app)
    val prefs=prefsRepo.flow.stateIn(viewModelScope,SharingStarted.Eagerly,Preferences())
    val completions=repo.completions.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val totals=repo.totals.stateIn(viewModelScope,SharingStarted.Eagerly,TotalsRow())
    private val state=MutableStateFlow(GameUi())
    val ui=state.asStateFlow()
    private val mutex=Mutex()
    private var foreground=true
    private var lastMatchTime=0L
    init {
        viewModelScope.launch {
            prefsRepo.flow.first()
            try {
                val sessions=withContext(Dispatchers.IO) { repo.load() }.associateBy(Session::mode)
                state.value=state.value.copy(ready=true,sessions=sessions)
            } catch(_:Exception) { state.value=state.value.copy(ready=true,error=R.string.load_error) }
        }
        viewModelScope.launch {
            while(isActive) {
                delay(1000)
                val u=state.value;val s=u.session
                if(foreground && u.page=="game" && s!=null && !s.paused && !s.won && !u.busy) {
                    state.value=u.copy(elapsed=u.elapsed+1)
                    if(state.value.elapsed%5L==0L) persist()
                }
            }
        }
    }
    fun navigate(page:String) {
        if(state.value.page=="game") pause()
        state.value=state.value.copy(page=page,error=null)
    }
    fun setText(key:String,value:String) { viewModelScope.launch { prefsRepo.text(key,value) } }
    fun setBool(key:String,value:Boolean) { viewModelScope.launch { prefsRepo.bool(key,value) } }
    fun setNumber(key:String,value:Int) { viewModelScope.launch { prefsRepo.number(key,value) } }
    fun clearError() { state.value=state.value.copy(error=null) }
    fun dailyDate():String=LocalDate.now().toString()
    fun dailyLevel():Int=(LocalDate.now().toEpochDay().mod(120L)+1).toInt()
    private suspend fun save(s:Session,action:String=""):Session = withContext(Dispatchers.IO) { repo.commit(s,action) }
    private fun updateSaved(s:Session,page:String=state.value.page) {
        state.value=state.value.copy(session=s,sessions=state.value.sessions+(s.mode to s),page=page,elapsed=s.elapsedSeconds,busy=false)
    }
    fun start(level:Int,mode:String="campaign",fresh:Boolean=false) {
        if(state.value.busy)return
        state.value=state.value.copy(busy=true,error=null)
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val key=when(mode) { "daily"->"daily:${dailyDate()}";"tutorial"->"tutorial";else->"campaign:$level" }
                    val old=state.value.sessions[mode]
                    val s=if(!fresh && old?.key==key && !old.won)old.copy(paused=false) else withContext(Dispatchers.Default) {
                        Generator.create(level,if(mode=="daily") LocalDate.now().toEpochDay()*104729L else 0x5345545241L+level*7919L,mode,key)
                    }
                    val saved=save(s)
                    lastMatchTime=0L
                    state.value=state.value.copy(resultFirst=false,hint=null,combo=0)
                    updateSaved(saved,"game")
                } catch(_:Exception) { state.value=state.value.copy(busy=false,error=R.string.save_error) }
            }
        }
    }
    fun continueGame() { val s=state.value.sessions["campaign"]?.takeUnless { it.won }; if(s!=null) start(s.level) else start(nextLevel()) }
    fun nextLevel():Int=((completions.value.filter { it.mode=="campaign" }.maxOfOrNull(CompletionRow::level)?:0)+1).coerceAtMost(120)
    fun tap(id:Int) {
        val u=state.value;val s=u.session?:return
        if(u.busy||s.paused||s.won||u.page!="game")return
        if(s.mode=="tutorial") {
            val target=s.solution.firstOrNull { step -> s.tiles.any { it.id==step.a&&!it.removed } }
            if(target!=null&&id!=target.a&&id!=target.b){state.value=u.copy(error=if(s.remaining==8)R.string.guide_one else if(s.remaining>2)R.string.guide_two else R.string.guide_three);return}
        }
        if(id !in Rules.freeIds(s.tiles)) { state.value=u.copy(error=R.string.blocked_tile);return }
        if(s.selectedId==null||s.selectedId==id) { state.value=u.copy(session=s.copy(selectedId=if(s.selectedId==id)null else id),hint=null);return }
        val n=Rules.match(s,s.selectedId ?: return,id)
        if(n==null) { state.value=u.copy(session=s.copy(selectedId=id),error=R.string.different_tiles,hint=null);return }
        val now=android.os.SystemClock.elapsedRealtime()
        val streak=if(lastMatchTime>0L && now-lastMatchTime<=12000L)u.combo+1 else 1
        lastMatchTime=now
        state.value=state.value.copy(combo=streak,bestCombo=maxOf(u.bestCombo,streak))
        commit(n,"match")
    }
    private fun commit(n:Session,action:String="",page:String?=null) {
        if(state.value.busy)return
        state.value=state.value.copy(busy=true,error=null)
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val first=n.won && completions.value.none { it.key==n.key }
                    val saved=save(n.copy(elapsedSeconds=state.value.elapsed),action)
                    state.value=state.value.copy(hint=null,resultFirst=first)
                    val animate=action=="match" && !prefs.value.reduceMotion
                    val destination=page ?: if(saved.won)"result" else state.value.page
                    updateSaved(saved,if(animate&&saved.won)"game" else destination)
                    if(animate) {
                        state.value=state.value.copy(busy=true)
                        delay(650)
                        state.value=state.value.copy(busy=false,page=if(saved.won&&state.value.page=="game")"result"else state.value.page)
                    }
                    if(saved.won&&saved.mode=="tutorial") prefsRepo.bool("guideDone",true)
                } catch(_:Exception) { state.value=state.value.copy(busy=false,error=R.string.save_error) }
            }
        }
    }
    fun hint() {
        val s=state.value.session?:return;if(state.value.busy)return
        val p=Rules.hint(s)?:return
        state.value=state.value.copy(busy=true)
        viewModelScope.launch { mutex.withLock { try {
            updateSaved(save(s.copy(hints=s.hints+1,elapsedSeconds=state.value.elapsed),"hint"))
            state.value=state.value.copy(hint=p)
        } catch(_:Exception){state.value=state.value.copy(busy=false,error=R.string.save_error)} } }
    }
    fun undo() { val s=state.value.session?:return;val n=Rules.undo(s);if(n==null)state.value=state.value.copy(error=R.string.nothing_undo) else {lastMatchTime=0L;state.value=state.value.copy(combo=0);commit(n,"undo")} }
    fun shuffle() {
        val s=state.value.session?:return;if(state.value.busy)return
        state.value=state.value.copy(busy=true)
        viewModelScope.launch {mutex.withLock { try {
            val n=withContext(Dispatchers.Default){Generator.shuffle(s)}
            if(n==null)state.value=state.value.copy(busy=false,error=R.string.shuffle_failed)
            else updateSaved(save(n.copy(elapsedSeconds=state.value.elapsed),"shuffle"))
        } catch(_:Exception){state.value=state.value.copy(busy=false,error=R.string.save_error)} } }
    }
    fun restart() { val s=state.value.session?:return;start(s.level,s.mode,true) }
    fun pause() {
        lastMatchTime=0L
        state.value=state.value.copy(combo=0)
        val s=state.value.session?:return
        if(s.won||state.value.page!="game")return
        // State changes immediately; no timer runs while the save is pending.
        state.value=state.value.copy(session=s.copy(paused=true),page="pause",busy=true)
        viewModelScope.launch { mutex.withLock { try { val current=state.value.session?:return@withLock;updateSaved(save(current.copy(paused=true,elapsedSeconds=state.value.elapsed)),state.value.page) } catch(_:Exception){state.value=state.value.copy(error=R.string.save_error)} } }
    }
    fun resume() {val s=state.value.session?:return;commit(s.copy(paused=false),page="game")}
    fun foreground(active:Boolean) { foreground=active;if(!active)pause() }
    private fun persist() {val s=state.value.session?:return;if(state.value.busy)return;commit(s)}
    fun reset() {
        if(state.value.busy)return
        state.value=state.value.copy(busy=true,error=null)
        viewModelScope.launch {mutex.withLock {try {
            withContext(Dispatchers.IO){repo.reset()}
            prefsRepo.number("background",0);prefsRepo.bool("guideDone",false)
            state.value=GameUi(ready=true)
        } catch(_:Exception){state.value=state.value.copy(busy=false,error=R.string.save_error)} } }
    }
    fun markAd() {viewModelScope.launch {repo.markAdShown()} }
}
