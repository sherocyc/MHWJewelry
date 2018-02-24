package kkk.com.mhwjewelry

data class JewelriesRecord(val id: Long, val jewelry1: Long, val jewelry2: Long, val jewelry3: Long,var status:Int){
    companion object {
        public val PASSED = -1;
        public val AVAILABLE = 0;
        public val CURRENT = 1;
        public val NEXT = 2;
    }
}