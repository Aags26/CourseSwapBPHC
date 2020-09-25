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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bphc.courseswap.R
import com.bphc.courseswap.adapters.MySwapRequestsAdapter
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import com.bphc.courseswap.viewmodels.MySwapRequestsViewModel
import kotlinx.android.synthetic.main.fragment_my_swap_requests.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MySwapRequestsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var myRequests: ArrayList<Course> = ArrayList()
    private lateinit var mMySwapRequestViewModel: MySwapRequestsViewModel
    private lateinit var recyclerView: RecyclerView
    private val user: User = User(Auth.userEmail, Auth.userPhoneNumber)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_swap_requests, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar_my_requests)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        mMySwapRequestViewModel =
            ViewModelProvider(requireActivity()).get(MySwapRequestsViewModel::class.java)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MySwapRequestsAdapter(myRequests)

        updateUI()

        return view
    }

    private fun updateUI() {

        mMySwapRequestViewModel.fetchMyRequests(user)
            .observe(requireActivity(), {
                if (it != null) {
                    myRequests.clear()
                    myRequests.addAll(it)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            })

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MySwapRequestsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}