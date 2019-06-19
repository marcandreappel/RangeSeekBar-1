package `fun`.appel.rangeseekbar

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class SavedState : View.BaseSavedState {

    var minValue: Float = 0.toFloat()
    var maxValue: Float = 0.toFloat()
    var rangeInterval: Float = 0.toFloat()
    var tickNumber: Int = 0
    var currSelectedMin: Float = 0.toFloat()
    var currSelectedMax: Float = 0.toFloat()

    constructor(superState: Parcelable) : super(superState)

    private constructor(`in`: Parcel) : super(`in`) {
        minValue = `in`.readFloat()
        maxValue = `in`.readFloat()
        rangeInterval = `in`.readFloat()
        tickNumber = `in`.readInt()
        currSelectedMin = `in`.readFloat()
        currSelectedMax = `in`.readFloat()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeFloat(minValue)
        out.writeFloat(maxValue)
        out.writeFloat(rangeInterval)
        out.writeInt(tickNumber)
        out.writeFloat(currSelectedMin)
        out.writeFloat(currSelectedMax)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}
