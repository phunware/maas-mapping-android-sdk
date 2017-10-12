package com.phunware.kotlin.sample

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class DemoAdapter(private val demos: List<Demo>, private val listener: DemoOnClickListener) : RecyclerView.Adapter<DemoAdapter.ViewHolder>() {

    internal interface DemoOnClickListener {
        fun onItemClicked(title: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.demo_row, parent, false)
        return ViewHolder(v, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val d = demos[position]
        holder.title.text = d.title
        holder.description.text = d.description
    }

    override fun getItemCount(): Int = demos.size

    fun getItem(title: String): Demo? {
        if (!TextUtils.isEmpty(title)) {
            demos
                    .filter { it.title == title }
                    .forEach { return it }
        }
        return null
    }

    internal class ViewHolder(itemView: View, private var listener: DemoOnClickListener)
        : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var description: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener { listener.onItemClicked(title.text.toString()) }
        }
    }
}
