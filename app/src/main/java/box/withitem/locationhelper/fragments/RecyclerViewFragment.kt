package box.withitem.locationhelper.fragments

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import box.withitem.locationhelper.data.RecyclerViewAdapter
import box.withitem.locationhelper.data.MarkerPoint
import box.withitem.locationhelper.data.MapViewModel

import box.withitem.locationhelper.databinding.FragmentRecyclerBinding
import box.withitem.locationhelper.utils.showFragment
import java.util.*


class RecyclerViewFragment : Fragment(),RecyclerViewAdapter.Listener {
    private val viewModel: MapViewModel by activityViewModels()
    private var _binding: FragmentRecyclerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. get a reference to recyclerView
        val recyclerView = binding.recyclerView
        // 2. create an adapter
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val mAdapter = RecyclerViewAdapter ( this )
        // 3. set adapter
        recyclerView.adapter = mAdapter
//        viewModel.getData().observe(viewLifecycleOwner) {
//            mAdapter.submitList(it)
//        }
        viewModel.getData().observe(viewLifecycleOwner){
            mAdapter.submitList(it.reversed())
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onClick(marker: MarkerPoint) {

        viewModel.endPointMarker.postValue(marker)
        // viewModel.liveMarker.postValue(marker)
        showFragment(MapScreenFragment.newInstance())
        // showFragment(MarkerInfoFragment.newInstance(Bundle().apply {
        //       putParcelable(MarkerInfoFragment.BUNDLE_EXTRA, markerPoint)
        //   }))
    }

    override fun onClickImage(marker: MarkerPoint) {
        viewModel.endPointMarker.postValue(marker)
        // viewModel.liveMarker.postValue(marker)
        showFragment(MapScreenFragment.newInstance())
    }

    override fun onClickCreateRoad(marker: MarkerPoint) {
        viewModel.endPointMarker.postValue(marker)
        showFragment(MapScreenFragment.newInstance())
    }

}