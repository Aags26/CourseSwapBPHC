package com.bphc.courseswap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.models.User
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_make_swap_request.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MakeSwapRequest : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var user: User

    private lateinit var inputAssignedCourseNumber: String
    private lateinit var inputAssignedCourseName: String
    private lateinit var inputDesiredCourseNumber: String
    private lateinit var inputDesiredCourseName: String


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

        return inflater.inflate(R.layout.fragment_make_swap_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assigned_course_number.requestFocus()
        make_request_button.setOnClickListener {
            if (!validateFields())
                return@setOnClickListener

            user = User(
                Auth.userEmail,
                Auth.userPhoneNumber,
                "$inputAssignedCourseNumber $inputAssignedCourseName",
                "$inputDesiredCourseNumber $inputDesiredCourseName"
            )
            Toast.makeText(context, user.toString(), Toast.LENGTH_LONG).show()
        }

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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MakeSwapRequest().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}