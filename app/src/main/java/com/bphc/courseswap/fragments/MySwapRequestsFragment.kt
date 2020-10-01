package com.bphc.courseswap.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bphc.courseswap.R
import com.bphc.courseswap.adapters.MySwapRequestsAdapter
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.firebase.MessagingService
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import com.bphc.courseswap.viewmodels.MySwapRequestsViewModel
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_my_swap_requests.*


class MySwapRequestsFragment : Fragment(), MySwapRequestsAdapter.OnItemClickListener {

    private var myRequests: ArrayList<Course> = ArrayList()
    private lateinit var mMySwapRequestViewModel: MySwapRequestsViewModel
    private lateinit var requestAdapter: MySwapRequestsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var course: Course

    private val user: User =
        User(Auth.userEmail, Auth.userPhoneNumber, FirebaseInstanceId.getInstance().token)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_swap_requests, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar_my_requests)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        mMySwapRequestViewModel =
            ViewModelProvider(requireActivity()).get(MySwapRequestsViewModel::class.java)

        initRecyclerView(view)
        requestAdapter.setList(myRequests)

        updateUI()

        return view
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            requestAdapter = MySwapRequestsAdapter(this@MySwapRequestsFragment)
            adapter = requestAdapter
        }
    }

    private fun updateUI() {

        mMySwapRequestViewModel.fetchMyRequests(user)
            .observe(requireActivity(), {
                if (it != null) {
                    myRequests.clear()
                    myRequests.addAll(it)
                    requestAdapter.notifyDataSetChanged()
                }
            })

    }

    override fun onItemClick(position: Int) {
        course = myRequests[position]

        myRequests.removeAt(position)

        mMySwapRequestViewModel.deleteRequest(course, user)
            .observe(requireActivity(), {
                if (it != null) {
                    if (it) {
                        requestAdapter.notifyItemRemoved(position)
                    }
                }

            })
    }

}