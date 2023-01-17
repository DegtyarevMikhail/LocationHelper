package box.withitem.locationhelper.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import box.withitem.locationhelper.R
import box.withitem.locationhelper.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig


class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOSM()
        binding = FragmentMapBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun settingsOSM(){
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
    }

    companion object {

        @JvmStatic
        fun newInstance() = MapFragment()
            }
    }
