package com.bphc.courseswap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bphc.courseswap.R
import com.bphc.courseswap.models.Course
import kotlinx.android.synthetic.main.my_swap_request.view.*

class MySwapRequestsAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MySwapRequestsAdapter.MySwapRequestsViewHolder>() {

    private var mySwapRequests: ArrayList<Course> = ArrayList()

    fun setList(myRequests: ArrayList<Course>) {
        mySwapRequests = myRequests
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class MySwapRequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assignedCourse: TextView = itemView.my_assigned_course
        val desiredCourse: TextView = itemView.my_desired_course
        private val delete: ImageView = itemView.delete

        init {
            delete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySwapRequestsViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.my_swap_request, parent, false)
        return MySwapRequestsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MySwapRequestsViewHolder, position: Int) {
        val myRequest = mySwapRequests[position]
        holder.assignedCourse.text = myRequest.assignedCourse
        holder.desiredCourse.text = myRequest.desiredCourse
    }

    override fun getItemCount() = mySwapRequests.size

}