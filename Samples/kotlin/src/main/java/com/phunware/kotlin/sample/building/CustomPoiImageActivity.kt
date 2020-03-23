package com.phunware.kotlin.sample.building

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.phunware.kotlin.sample.R
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.model.IconProvider
import com.phunware.mapping.model.PointOptions

class CustomPoiImageActivity: LoadBuildingActivity() {

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        mapManager.setIconProvider(object: IconProvider {
            override fun getImage(pointOptions: PointOptions): Bitmap? {
                val drawable =
                    ContextCompat.getDrawable(this@CustomPoiImageActivity, R.drawable.ic_poi_list)
                return (drawable as BitmapDrawable).bitmap
            }
        })

        super.onPhunwareMapReady(phunwareMap)
    }
}