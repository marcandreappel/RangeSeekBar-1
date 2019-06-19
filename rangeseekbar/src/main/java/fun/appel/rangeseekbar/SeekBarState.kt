package `fun`.appel.rangeseekbar

class SeekBarState {
    var indicatorText: String? = null
    var value: Float = 0.toFloat() //now progress value
    var isMin: Boolean = false
    var isMax: Boolean = false

    override fun toString(): String {
        return "indicatorText: $indicatorText ,isMin: $isMin ,isMax: $isMax"
    }
}
