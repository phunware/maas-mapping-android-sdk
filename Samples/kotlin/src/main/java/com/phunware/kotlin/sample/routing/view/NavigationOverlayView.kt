package com.phunware.kotlin.sample.routing.view

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
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.routing.util.ManeuverDisplayHelper
import com.phunware.kotlin.sample.util.extensions.toPx
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.model.RouteManeuverOptions
import java.util.ArrayList

class NavigationOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    companion object {
        /**
         * Helper method to render a maneuverPair.  This is used by the [NavigationOverlayView] and
         * the [RouteSummaryFragment].
         */
        fun renderManeuver(maneuverPair: ManeuverPair,
                           nextDirectionIcon: ImageView,
                           nextDirectionText: TextView,
                           displayHelper: ManeuverDisplayHelper) {
            if (maneuverPair.turnManeuver != null) {
                nextDirectionIcon.setImageResource(displayHelper.getImageResourceForDirection(nextDirectionIcon.context, maneuverPair.turnManeuver!!))

                val textDirection = displayHelper.stringForDirection(nextDirectionText.context, maneuverPair.turnManeuver)
                val textDistance = displayHelper.distanceForDirection(nextDirectionText.context, maneuverPair.mainManeuver, " in")

                val spannableTextDirection = textDirection.toSpannableText(nextDirectionText.context, R.style.DirectionTextAppearance)
                val spannableTextDistance = textDistance.toSpannableText(nextDirectionText.context, R.style.DistanceTextAppearance)

                val spannableBuilder = SpannableStringBuilder().append(spannableTextDirection).append(spannableTextDistance)
                val styledText = spannableBuilder.subSequence(0, spannableBuilder.length)

                nextDirectionText.text = styledText
            } else {
                nextDirectionIcon.setImageResource(displayHelper.getImageResourceForDirection(nextDirectionText.context, maneuverPair.mainManeuver!!))

                val textDirection = displayHelper.stringForDirection(nextDirectionText.context, maneuverPair.mainManeuver)
                val textDistance = displayHelper.distanceForDirection(nextDirectionText.context, maneuverPair.mainManeuver, " for")

                val spannableTextDirection = textDirection.toSpannableText(nextDirectionText.context, R.style.DirectionTextAppearance)
                val spannableTextDistance = textDistance.toSpannableText(nextDirectionText.context, R.style.DistanceTextAppearance)

                val spannableBuilder = SpannableStringBuilder().append(spannableTextDirection).append(spannableTextDistance)
                val styledText = spannableBuilder.subSequence(0, spannableBuilder.length)

                nextDirectionText.text = styledText
            }
        }
    }

    private lateinit var navigator: Navigator
    private lateinit var adapter: ManeuverPagerAdapter

    /**
     * Since this custom view extends a ViewPager, we need to handle "click listening" on our own.
     */
    private var clickListener: OnClickListener? = null

    init {
        // set the viewpager to show multiple pages at once, so maneuver steps peak in from the side
        offscreenPageLimit = 6
        pageMargin = (-40).toPx()
    }

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            val pair = adapter.getItem(position)
            navigator.setCurrentManeuver(pair.mainPos)
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        clickListener = listener
    }

    fun setNavigator(navigator: Navigator) {
        this.navigator = navigator

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

        adapter = ManeuverPagerAdapter(clickListener)
        setAdapter(adapter)
        adapter.setManeuvers(pairs)
        addOnPageChangeListener(pageChangeListener)
        currentItem = 0
        navigator.setCurrentManeuver(0)

    }

    fun dispatchManeuverChanged(navigator: Navigator, position: Int) {
        for (i in 0 until adapter.count) {
            val pair = adapter.getItem(i)
            if (pair.mainPos == position || pair.turnPos == position) {
                currentItem = i
                return
            }
        }
    }

    class ManeuverPair {
        internal var mainPos: Int = 0
        internal var turnPos: Int = 0
        internal var mainManeuver: RouteManeuverOptions? = null
        internal var turnManeuver: RouteManeuverOptions? = null
    }

    private class ManeuverPagerAdapter internal constructor(private val clickListener: OnClickListener?) : PagerAdapter() {
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
            val v = LayoutInflater.from(context).inflate(R.layout.item_maneuver, container, false)
            val nextDirectionIcon = v.findViewById<ImageView>(R.id.next_direction_icon)
            val nextDirectionText = v.findViewById<TextView>(R.id.next_direction_text)

            val currentManeuverPair = maneuvers[position]

            v.setOnClickListener {
                clickListener?.onClick(v)
            }

            renderManeuver(
                    maneuverPair = currentManeuverPair,
                    nextDirectionText = nextDirectionText,
                    nextDirectionIcon = nextDirectionIcon,
                    displayHelper = displayHelper
            )

            container.addView(v)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
    }

    fun getManeuverPair(): ManeuverPair {
        return adapter.getItem(currentItem)
    }
}

fun String.toSpannableText(context: Context, styleResourceId: Int): SpannableString {
    return SpannableString(this).apply {
        setSpan(TextAppearanceSpan(context, styleResourceId), 0, this.length, 0)
    }
}
