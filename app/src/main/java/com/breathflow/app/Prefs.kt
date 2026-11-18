package com.breathflow.app

import android.content.Context

/** Remembers the user's last settings between launches. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("breathflow", Context.MODE_PRIVATE)

    var lastTechniqueId: String?
        get() = sp.getString("last_technique", null)
        set(value) { sp.edit().putString("last_technique", value).apply() }

    var pace: Pace
        get() = Pace.entries.getOrElse(sp.getInt("pace", 1)) { Pace.NORMAL }
        set(value) { sp.edit().putInt("pace", value.ordinal).apply() }

    /** Session length in minutes. 0 means open-ended. */
    var lengthMinutes: Int
        get() = sp.getInt("length", 0)
        set(value) { sp.edit().putInt("length", value).apply() }

    var voiceOn: Boolean
        get() = sp.getBoolean("voice", true)
        set(value) { sp.edit().putBoolean("voice", value).apply() }

    var soundOn: Boolean
        get() = sp.getBoolean("sound", true)
        set(value) { sp.edit().putBoolean("sound", value).apply() }
}
