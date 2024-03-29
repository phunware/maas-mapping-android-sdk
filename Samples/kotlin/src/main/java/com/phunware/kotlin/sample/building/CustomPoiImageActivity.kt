package com.phunware.kotlin.sample.building

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.phunware.kotlin.sample.R
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.model.IconProvider
import com.phunware.mapping.model.PointOptions

internal class CustomPoiImageActivity: LoadBuildingActivity() {

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {

        // Set the icon provider callback to provide custom images for POIs
        mapManager.setIconProvider(object: IconProvider {
            override fun getImage(pointOptions: PointOptions): Bitmap? {
                //This sets the same icon for all POIs
                val drawable =
                    ContextCompat.getDrawable(this@CustomPoiImageActivity, R.drawable.ic_poi_list)
                return (drawable as BitmapDrawable).bitmap
            }
        })

        super.onPhunwareMapReady(phunwareMap)
    }

    override fun onDestroy() {
        mapManager.setIconProvider(null)
        super.onDestroy()
    }
}