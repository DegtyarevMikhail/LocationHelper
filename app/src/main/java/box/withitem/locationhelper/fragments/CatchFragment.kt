package box.withitem.locationhelper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import box.withitem.locationhelper.R
import box.withitem.locationhelper.databinding.FragmentCatchBinding
import box.withitem.locationhelper.databinding.FragmentMapBinding


class CatchFragment : Fragment() {
    private lateinit var binding: FragmentCatchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatchBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() = CatchFragment()
            }
    }
