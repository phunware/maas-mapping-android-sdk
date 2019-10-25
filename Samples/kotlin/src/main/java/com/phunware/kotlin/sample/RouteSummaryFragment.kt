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
package com.phunware.kotlin.sample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.phunware.mapping.model.RouteOptions
import java.util.ArrayList

/**
 * Fragment that given a [RouteOptions] object can display a list of maneuvers.
 */
class RouteSummaryFragment : Fragment() {

    private lateinit var routeSummaryRecyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_route_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        routeSummaryRecyclerView = view.findViewById(R.id.routeSummaryRecyclerView)
        routeSummaryRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        }
    }

    fun setRoute(route: RouteOptions) {
        routeSummaryRecyclerView.adapter = RouteAdapter(route)
    }

    /**
     * Hide the route summary fragment and remove the cancel button from the Toolbar.
     */
    fun hide() {
        requireActivity().supportFragmentManager.beginTransaction().hide(this).commit()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * Show the route summary fragment and show a cancel button in the Toolbar.
     */
    fun show() {
        requireActivity().supportFragmentManager.beginTransaction().show(this).commit()
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24px)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    internal class RouteAdapter(private val route: RouteOptions) :
            RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
        private val displayHelper: ManeuverDisplayHelper = ManeuverDisplayHelper()
        private val pairs = ArrayList<NavigationOverlayView.ManeuverPair>()

        init {
            // Pair up the maneuvers, so that they can easily be displayed as a combination
            // of where you currently are and where your next turn is.
            var i = 0
            while (i < route.maneuvers.size) {
                val pair = NavigationOverlayView.ManeuverPair()
                pair.mainPos = i
                pair.mainManeuver = route.maneuvers[i]

                if (i + 1 < route.maneuvers.size) {
                    val next = route.maneuvers[i + 1]
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
        }

        class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
            val nextDirectionIcon: ImageView = root.findViewById(R.id.next_direction_icon)
            val nextDirectionText: TextView = root.findViewById(R.id.next_direction_text)
        }

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_route_summary_maneuver, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position < pairs.size) {
                val currentManeuverPair = pairs[position]

                NavigationOverlayView.renderManeuver(
                        maneuverPair = currentManeuverPair,
                        nextDirectionText = holder.nextDirectionText,
                        nextDirectionIcon = holder.nextDirectionIcon,
                        displayHelper = displayHelper
                )
            } else {
                // Arrival step
                holder.nextDirectionIcon.setImageResource(R.drawable.ic_poi_list)

                val pointCount = route.points.size
                val finalPoint = route.points[pointCount - 1]

                val youHaveArrivedText = holder.nextDirectionText.context.getString(R.string.you_have_arrived)
                val youHaveArrivedSpannable = youHaveArrivedText.toSpannableText(holder.nextDirectionText.context, R.style.DirectionTextAppearance)
                val finalPoiNameSpannable = " at ${finalPoint.name}".toSpannableText(holder.nextDirectionText.context, R.style.DistanceTextAppearance)

                val spannableBuilder = SpannableStringBuilder().append(youHaveArrivedSpannable).append(finalPoiNameSpannable)
                val styledText = spannableBuilder.subSequence(0, spannableBuilder.length)

                holder.nextDirectionText.text = styledText
            }

        }

        // We will be injecting an extra item for the arrival row
        override fun getItemCount() = pairs.size + 1
    }
}