package tech.yunze.withu.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import tech.yunze.withu.R
import tech.yunze.withu.listeners.FailureCallback


class StatusFragment : Fragment() {

    private var failureListener: FailureCallback? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()){
            failureListener?.onUserFailure()
        }
    }

    fun setFailureListener(listener: FailureCallback){
        this.failureListener = listener
    }

}
