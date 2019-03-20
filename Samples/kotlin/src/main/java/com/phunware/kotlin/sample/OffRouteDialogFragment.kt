package com.phunware.kotlin.sample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

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