package com.mhmmad.setramahjong.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mhmmad.setramahjong.engine.*
import kotlinx.coroutines.flow.*

@Entity(tableName = "sessions") data class SessionRow(@PrimaryKey val slot: String, val payload: String, val updated: Long)
@Entity(tableName = "completions") data class CompletionRow(@PrimaryKey val key: String, val level: Int, val mode: String, val stars: Int, val seconds: Long, val completedAt: Long)
@Entity(tableName = "totals") data class TotalsRow(@PrimaryKey val id: Int = 1, val pairs: Long = 0, val hints: Long = 0, val undos: Long = 0, val shuffles: Long = 0, val seconds: Long = 0, val petals: Int = 0, val sinceAd: Int = 0, val lastAd: Long = 0)
@Dao interface GameDao {
    @Query("SELECT * FROM sessions") suspend fun sessions(): List<SessionRow>
    @Query("SELECT * FROM sessions WHERE slot=:slot") suspend fun session(slot: String): SessionRow?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putSession(row: SessionRow)
    @Query("SELECT * FROM completions ORDER BY level") fun observeCompletions(): Flow<List<CompletionRow>>
    @Query("SELECT * FROM completions WHERE `key`=:key") suspend fun completion(key: String): CompletionRow?
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertCompletion(row: CompletionRow): Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putCompletion(row: CompletionRow)
    @Query("SELECT * FROM totals WHERE id=1") fun observeTotals(): Flow<TotalsRow?>
    @Query("SELECT * FROM totals WHERE id=1") suspend fun totals(): TotalsRow?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putTotals(row: TotalsRow)
    @Query("DELETE FROM sessions") suspend fun clearSessions()
    @Query("DELETE FROM completions") suspend fun clearCompletions()
    @Query("DELETE FROM totals") suspend fun clearTotals()
}
@Database(entities=[SessionRow::class, CompletionRow::class, TotalsRow::class], version=2, exportSchema=true)
abstract class GameDatabase : RoomDatabase() {
    abstract fun dao(): GameDao
    companion object {
        // v1 -> v2 is additive; NEVER use fallbackToDestructiveMigration.
        val MIGRATION_1_2 = object : Migration(1,2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE totals ADD COLUMN sinceAd INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE totals ADD COLUMN lastAd INTEGER NOT NULL DEFAULT 0")
            }
        }
        fun open(context: Context) = Room.databaseBuilder(context, GameDatabase::class.java,"setra.db")
            .addMigrations(MIGRATION_1_2).build()
    }
}
class GameRepository(private val db: GameDatabase) {
    private val dao = db.dao()
    val completions = dao.observeCompletions()
    val totals = dao.observeTotals().map { it ?: TotalsRow() }
    suspend fun load(): List<Session> = dao.sessions().map { SessionCodec.decode(it.payload) }
    /** Atomic move + statistics + first-clear reward + saved won state. */
    suspend fun commit(s: Session, action: String = ""): Session = db.withTransaction {
        val old = dao.session(s.mode)?.let { SessionCodec.decode(it.payload) }
        var totals = dao.totals() ?: TotalsRow()
        totals = totals.copy(
            pairs=totals.pairs + if(action=="match") 1 else 0,
            hints=totals.hints + if(action=="hint") 1 else 0,
            undos=totals.undos + if(action=="undo") 1 else 0,
            shuffles=totals.shuffles + if(action=="shuffle") 1 else 0,
            seconds=totals.seconds + if(old?.key==s.key) (s.elapsedSeconds-old.elapsedSeconds).coerceAtLeast(0) else 0
        )
        var result=s
        if(s.won && s.mode!="tutorial") {
            val row=CompletionRow(s.key,s.level,s.mode,s.stars,s.elapsedSeconds,System.currentTimeMillis())
            val first=dao.insertCompletion(row)!=-1L
            if(first) totals=totals.copy(petals=totals.petals+30,sinceAd=totals.sinceAd+1)
            else dao.completion(s.key)?.let { before ->
                dao.putCompletion(before.copy(stars=maxOf(before.stars,s.stars),seconds=minOf(before.seconds,s.elapsedSeconds)))
            }
            result=s.copy(awarded=true)
        }
        dao.putTotals(totals)
        dao.putSession(SessionRow(s.mode,SessionCodec.encode(result),System.currentTimeMillis()))
        result
    }
    suspend fun markAdShown() = db.withTransaction {
        val t=dao.totals()?:TotalsRow()
        dao.putTotals(t.copy(lastAd=System.currentTimeMillis(),sinceAd=0))
    }
    suspend fun reset() = db.withTransaction { dao.clearSessions();dao.clearCompletions();dao.clearTotals() }
}
private val Context.prefs by preferencesDataStore("preferences")
data class Preferences(val language: String="system",val sound: Boolean=false,val haptics: Boolean=true,val reduceMotion: Boolean=false,val showTimer: Boolean=false,val background: Int=0,val guideDone: Boolean=false,val consent: String="unknown",val matchStyle:Int=0)
class PreferenceRepository(private val context: Context) {
    val flow = context.prefs.data.map { p -> Preferences(p[stringPreferencesKey("language")]?:"system",p[booleanPreferencesKey("sound")]?:false,p[booleanPreferencesKey("haptics")]?:true,p[booleanPreferencesKey("reduceMotion")]?:false,p[booleanPreferencesKey("showTimer")]?:false,p[intPreferencesKey("background")]?:0,p[booleanPreferencesKey("guideDone")]?:false,p[stringPreferencesKey("consent")]?:"unknown",(p[intPreferencesKey("matchStyle")]?:0).coerceIn(0,2)) }
    suspend fun text(key:String,value:String) { context.prefs.edit { it[stringPreferencesKey(key)]=value } }
    suspend fun bool(key:String,value:Boolean) { context.prefs.edit { it[booleanPreferencesKey(key)]=value } }
    suspend fun number(key:String,value:Int) { context.prefs.edit { it[intPreferencesKey(key)]=value } }
}
