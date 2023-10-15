package network.roanoke.ttms.gui

class SlotRange(var start: Int, var end: Int) {

    fun getStart(): Int {
        return start
    }

    fun getEnd(): Int {
        return end
    }

    fun size(): Int {
        return end - start + 1
    }
}