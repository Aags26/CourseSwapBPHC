package com.bphc.courseswap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bphc.courseswap.R
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.viewmodels.MakeSwapRequestViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_make_swap_request.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class MakeSwapRequestFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mCourse: Course

    private lateinit var inputAssignedCourseNumber: String
    private lateinit var inputAssignedCourseName: String
    private lateinit var inputDesiredCourseNumber: String
    private lateinit var inputDesiredCourseName: String

    private lateinit var mMakeSwapRequestViewModel: MakeSwapRequestViewModel

    private lateinit var navigationView: NavigationView
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    var navController: NavController? = null

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
        val view = inflater.inflate(R.layout.fragment_make_swap_request, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        navigationView = view.findViewById(R.id.main_navigation_bar)
        drawer = view.findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawer,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMakeSwapRequestViewModel = ViewModelProvider(requireActivity()).get(
            MakeSwapRequestViewModel::class.java
        )

        assigned_course_number.requestFocus()
        make_request_button.setOnClickListener {
            if (!validateFields())
                return@setOnClickListener

            mCourse = Course(
                "$inputAssignedCourseNumber $inputAssignedCourseName",
                "$inputDesiredCourseNumber $inputDesiredCourseName"
            )

            Toast.makeText(context, mCourse.toString(), Toast.LENGTH_LONG).show()
            initUI()
        }

        navigationView.setNavigationItemSelectedListener(this)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.swap_requests -> {
                navController = view?.let { Navigation.findNavController(it) }
                navController?.navigate(R.id.action_makeSwapRequestFragment_to_mySwapRequestsFragment)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun validateFields(): Boolean{

        inputAssignedCourseNumber = assigned_course_number.editText?.text.toString().trim()
        inputAssignedCourseName = assigned_course_name.editText?.text.toString().trim()
        inputDesiredCourseNumber = desired_course_number.editText?.text.toString().trim()
        inputDesiredCourseName = desired_course_name.editText?.text.toString().trim()

        if (inputAssignedCourseNumber.isEmpty()) {
            assigned_course_number.error = "Please type in your assigned course number"
            return false
        } else {
            assigned_course_number.error = null
        }

        if (inputAssignedCourseName.isEmpty()) {
            assigned_course_name.error = "Please type in your assigned course name"
            return false
        } else {
            assigned_course_name.error = null
        }

        if (inputDesiredCourseNumber.isEmpty()) {
            desired_course_number.error = "Please type in your desired course number"
            return false
        } else {
            desired_course_number.error = null
        }

        if (inputDesiredCourseName.isEmpty()) {
            desired_course_name.error = "Please type in your desired course name"
            return false
        } else {
            desired_course_name.error = null
        }
        return true
    }

    private fun initUI() {
        mMakeSwapRequestViewModel.addCourse(mCourse).observe(requireActivity(), {
            if (it) {
                Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Not posted", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MakeSwapRequestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}