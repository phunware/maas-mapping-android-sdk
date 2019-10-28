package com.phunware.kotlin.sample.routing.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import com.phunware.kotlin.sample.R

class OffRouteDialogFragment : DialogFragment() {

    interface OffRouteDialogListener {
        fun onDismiss(dontShowAgain: Boolean)
        fun onReroute()
    }

    private lateinit var dismissButton: Button
    private lateinit var rerouteButton: Button
    private lateinit var dontShowAgainButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_offroute, container)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        dismissButton = rootView.findViewById(R.id.dismissButton)
        dismissButton.setOnClickListener {
            val listener: OffRouteDialogListener = activity as OffRouteDialogListener
            listener.onDismiss(false)
            dismiss()
        }

        rerouteButton = rootView.findViewById(R.id.rerouteButton)
        rerouteButton.setOnClickListener {
            val listener: OffRouteDialogListener = activity as OffRouteDialogListener
            listener.onReroute()
            dismiss()
        }

        dontShowAgainButton = rootView.findViewById(R.id.dontShowAgainButton)
        dontShowAgainButton.setOnClickListener {
            val listener: OffRouteDialogListener = activity as OffRouteDialogListener
            listener.onDismiss(true)
            dismiss()
        }

        return rootView
    }

}