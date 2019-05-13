package com.phunware.kotlin.sample

/* Copyright (C) 2018 Phunware, Inc.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL Phunware, Inc. BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Phunware, Inc. shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from Phunware, Inc. */

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.phunware.core.PwLog
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.model.RouteManeuverOptions

import java.util.ArrayList

class NavigationOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs), Navigator.OnManeuverChangedListener {

    private lateinit var navigator: Navigator
    private lateinit var adapter: ManeuverPagerAdapter

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            val pair = adapter.getItem(position)
            navigator.setCurrentManeuver(pair.mainPos)
        }
    }

    fun setNavigator(navigator: Navigator) {
        this.navigator = navigator
        navigator.addOnManeuverChangedListener(this)

        val pairs = ArrayList<ManeuverPair>()
        val maneuvers = this.navigator.maneuvers
        var i = 0
        while (i < maneuvers.size) {
            val pair = ManeuverPair()
            pair.mainPos = i
            pair.mainManeuver = maneuvers[i]

            if (i + 1 < maneuvers.size) {
                val next = maneuvers[i + 1]
                if (next.isTurnManeuver || next.isPortalManeuver) {
                    pair.turnPos = i + 1
                    pair.turnManeuver = next
                } else {
                    i--
                }
            }

            pairs.add(pair)
            i += 2
        }

        adapter = ManeuverPagerAdapter(navigator)
        setAdapter(adapter)
        adapter.setManeuvers(pairs)
        addOnPageChangeListener(pageChangeListener)
        currentItem = 0
        navigator.setCurrentManeuver(0)

    }

    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        for (i in 0 until adapter.count) {
            val pair = adapter.getItem(i)
            if (pair.mainPos == position || pair.turnPos == position) {
                currentItem = i
                return
            }
        }
    }

    override fun onRouteSnapFailed() {
        PwLog.e("NavigationOverlayView", "Off route")
    }

    public class ManeuverPair {
        internal var mainPos: Int = 0
        internal var turnPos: Int = 0
        internal var mainManeuver: RouteManeuverOptions? = null
        internal var turnManeuver: RouteManeuverOptions? = null
    }

    private class ManeuverPagerAdapter internal constructor(private val navigator: Navigator) : PagerAdapter() {
        private val displayHelper: ManeuverDisplayHelper = ManeuverDisplayHelper()
        private val maneuvers = ArrayList<ManeuverPair>()

        override fun getCount(): Int = maneuvers.size

        internal fun setManeuvers(maneuvers: Collection<ManeuverPair>) {
            this.maneuvers.clear()
            this.maneuvers.addAll(maneuvers)
            notifyDataSetChanged()
        }

        internal fun getItem(position: Int): ManeuverPair = maneuvers[position]

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val context = container.context
            val v = LayoutInflater.from(context)
                    .inflate(R.layout.item_maneuver, container, false)

            val direction = v.findViewById<ImageView>(R.id.direction)
            val maneuver = v.findViewById<TextView>(R.id.maneuver)
            val nextManeuver = v.findViewById<TextView>(R.id.next_maneuver)
            val nextDirection = v.findViewById<ImageView>(R.id.next_direction)

            val m = maneuvers[position]
            if (m.mainManeuver != null) {
                direction.setImageResource(displayHelper.getImageResourceForDirection(context,
                        m.mainManeuver!!))
                maneuver.text = displayHelper.stringForDirection(context, m.mainManeuver)

                if (m.turnManeuver == null) {
                    nextManeuver.visibility = View.GONE
                    nextDirection.visibility = View.GONE
                    v.findViewById<TextView>(R.id.next).visibility = View.GONE
                } else {
                    v.findViewById<TextView>(R.id.next).visibility = View.VISIBLE
                    nextManeuver.visibility = View.VISIBLE
                    nextDirection.visibility = View.VISIBLE
                    nextDirection.setImageResource(displayHelper
                            .getImageResourceForDirection(context, m.turnManeuver!!))
                    nextManeuver.text = displayHelper.stringForDirection(
                            context, m.turnManeuver)
                }

                if (position == maneuvers.size - 1) {
                    v.findViewById<TextView>(R.id.next).visibility = View.GONE
                    val pointCount = navigator.route.points.size
                    val finalPoint = navigator.route.points[pointCount - 1]
                    val customLocation = context.getString(R.string.custom_location_title)
                    val arrive = context.getString(R.string.to_arrive,
                            if (finalPoint.name == null) customLocation else finalPoint.name)
                    nextManeuver.visibility = View.VISIBLE
                    nextManeuver.text = arrive
                }

                container.addView(v)
                return v
            }
            return 0
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
    }

    fun getManeuverPair() : ManeuverPair
    {
        return adapter.getItem(currentItem)
    }
}
