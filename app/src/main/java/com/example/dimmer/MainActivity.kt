package com.example.dimmer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import java.util.Locale

/** Setup screen: explains how to turn the service and its shortcut on, and sets the dim amount. */
class MainActivity : Activity() {

    // Shows the language picked with the FI/EN toggle; until one is picked, the system language.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        DimService.prefs(newBase).getString(KEY_LANGUAGE, null)?.let {
            applyOverrideConfiguration(Configuration().apply { setLocale(Locale.forLanguageTag(it)) })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val language = findViewById<RadioGroup>(R.id.language)
        val isFinnish = resources.configuration.locales[0].language == "fi"
        language.check(if (isFinnish) R.id.language_fi else R.id.language_en)
        language.setOnCheckedChangeListener { _, checkedId ->
            val picked = if (checkedId == R.id.language_fi) "fi" else "en"
            DimService.prefs(this).edit().putString(KEY_LANGUAGE, picked).apply()
            recreate()
        }

        findViewById<Button>(R.id.open_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        val label = findViewById<TextView>(R.id.dim_value)
        val slider = findViewById<SeekBar>(R.id.dim_slider)
        slider.progress = DimService.dimPercent(this)
        label.text = getString(R.string.dim_value, slider.progress)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = getString(R.string.dim_value, progress)
                DimService.setDimPercent(this@MainActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.version).text = getString(R.string.version, versionName)
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
    }
}
