package android.hardware.light.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class LightState {
    public int brightnessMode;
    public int color;
    public int flashMode;
    public int flashOffMs;
    public int flashOnMs;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != LightState.class) {
            return false;
        }
        LightState other = (LightState) otherObject;
        return this.color == other.color && this.flashMode == other.flashMode && this.flashOnMs == other.flashOnMs && this.flashOffMs == other.flashOffMs && this.brightnessMode == other.brightnessMode;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.color))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.flashMode))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.flashOnMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.flashOffMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.brightnessMode)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".color = ");
        builder.append(this.color);
        builder.append(", .flashMode = ");
        builder.append(Flash.toString(this.flashMode));
        builder.append(", .flashOnMs = ");
        builder.append(this.flashOnMs);
        builder.append(", .flashOffMs = ");
        builder.append(this.flashOffMs);
        builder.append(", .brightnessMode = ");
        builder.append(Brightness.toString(this.brightnessMode));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<LightState> readVectorFromParcel(HwParcel parcel) {
        ArrayList<LightState> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            LightState _hidl_vec_element = new LightState();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.color = _hidl_blob.getInt32(0 + _hidl_offset);
        this.flashMode = _hidl_blob.getInt32(4 + _hidl_offset);
        this.flashOnMs = _hidl_blob.getInt32(8 + _hidl_offset);
        this.flashOffMs = _hidl_blob.getInt32(12 + _hidl_offset);
        this.brightnessMode = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<LightState> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((LightState) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.color);
        _hidl_blob.putInt32(4 + _hidl_offset, this.flashMode);
        _hidl_blob.putInt32(8 + _hidl_offset, this.flashOnMs);
        _hidl_blob.putInt32(12 + _hidl_offset, this.flashOffMs);
        _hidl_blob.putInt32(16 + _hidl_offset, this.brightnessMode);
    }
}
