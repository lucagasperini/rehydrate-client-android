package com.lucagasperini.rehydrate

import java.security.InvalidParameterException
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition

class HourBarFormat : Format() {
    override fun format(p0: Any?, p1: StringBuffer?, p2: FieldPosition?): StringBuffer {
        // plot has double on X value, then convert it to Int and write as String on plot
        if(p0 is Double) {
            return StringBuffer(p0.toInt().toString())
        } else {
            throw InvalidParameterException("Cannot format")
        }
    }

    override fun parseObject(p0: String?, p1: ParsePosition?): Any {
        return p0 ?: throw InvalidParameterException("Cannot format")
    }
}

class QuantityBarFormat : Format() {
    override fun format(p0: Any?, p1: StringBuffer?, p2: FieldPosition?): StringBuffer {
        // plot has double on X value, then convert it to Int and write as String on plot
        if(p0 is Double) {
            return StringBuffer(p0.toInt().toString())
        } else {
            throw InvalidParameterException("Cannot format")
        }
    }

    override fun parseObject(p0: String?, p1: ParsePosition?): Any {
        return p0 ?: throw InvalidParameterException("Cannot format")
    }
}

class HistoryHourBarFormat(val labels: List<String>) : Format() {
    override fun format(p0: Any?, p1: StringBuffer?, p2: FieldPosition?): StringBuffer {
        // plot has double on X value, then convert it to Int and write as String on plot
        if(p0 is Double) {
            return StringBuffer(labels[p0.toInt()])
        } else {
            throw InvalidParameterException("Cannot format")
        }
    }

    override fun parseObject(p0: String?, p1: ParsePosition?): Any {
        return p0 ?: throw InvalidParameterException("Cannot format")
    }
}
