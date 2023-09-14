package io.mindspice.jxch.fields.data.enums;

public enum LookUpTime {
    _Missed(30),
    _20(20),
    _15(15),
    _10(10),
    _5(5),
    _IGNORE(0);

    private final int time;

    LookUpTime(int time) { this.time = time; }

    public static LookUpTime getFromTime(double time) {
        for (var filter : LookUpTime.values()) {
            if (time > filter.time) { return filter; }
        }
        return _IGNORE;
    }
}


