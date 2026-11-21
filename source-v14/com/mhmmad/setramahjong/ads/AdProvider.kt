package com.mhmmad.setramahjong.ads
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
data class AdPartner(val name:String,val privacyUrl:String)
data class AdPrivacy(val partners:List<AdPartner> = emptyList(),val review:Boolean=false,val failed:Boolean=false)
interface AdProvider {
    val privacy:kotlinx.coroutines.flow.StateFlow<AdPrivacy>
    fun requestPrivacy()
    fun acceptPartners(granted:Boolean)
    fun permission(granted:Boolean)
    fun prepare()
    fun transition(onShown:()->Unit)
    @Composable fun Banner(modifier:Modifier)
    fun close()
}
