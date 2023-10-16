package network.roanoke.ttms.gui

class SlotRange(private var start: Int, private var end: Int) {

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