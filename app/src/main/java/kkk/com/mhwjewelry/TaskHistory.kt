package kkk.com.mhwjewelry

data class TaskHistory(val id:Long,val time:Long,val type:Long,val stepLength:Long){
    companion object {
        val MISSON = 0
        val ALCHEMY = 1
    }
}