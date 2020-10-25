package com.backpackingmap.backpackingmap.main_activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.backpackingmap.backpackingmap.R
import com.backpackingmap.backpackingmap.databinding.ActivityMainBinding
import com.backpackingmap.backpackingmap.map.*
import com.backpackingmap.backpackingmap.switchToSetup

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val model: MainActivityViewModel by viewModels()
    var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val repo = model.repo
        if (repo == null) {
            switchToSetup(this)
            return
        }

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        map = MapView(
            context = applicationContext,
            initialCenter = NaiveCoordinate(-2.804904, 56.340259)
                .toCoordinate("EPSG:4326"),
            // Chosen because it's very close to the most zoomed in OS Leisure
            initialZoom = ZoomLevel(1.7f)
        )

        map?.setLayers(listOf(
            WmtsLayer.Builder(this, model.explorerLayerConfig, repo.tileRepo)
        ))

        binding.mapParent.addView(map, binding.mapParent.layoutParams)
    }

}