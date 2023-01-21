package box.withitem.locationhelper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import box.withitem.locationhelper.adapters.RecyclerViewAdapter
import box.withitem.locationhelper.data.MarkerPoint
import box.withitem.locationhelper.data.MapViewModel
import box.withitem.locationhelper.databinding.FragmentCatchBinding
import box.withitem.locationhelper.utils.showFragment


class RecyclerActivityFragment : Fragment(),RecyclerViewAdapter.Listener {
    private val viewModel: MapViewModel by activityViewModels()
    private var _binding: FragmentCatchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCatchBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // 1. get a reference to recyclerView
        val recyclerView = binding.recyclerView
        // 2. create an adapter
        val mAdapter = RecyclerViewAdapter (this)
        // 3. set adapter
        recyclerView.adapter = mAdapter
        viewModel.getData().observe(viewLifecycleOwner) {
            mAdapter.submitList(it)

        }




    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(marker: MarkerPoint) {
        viewModel.endPointMarker.postValue(marker)
        showFragment(MapFragment.newInstance())
    }

    override fun onClickImage(marker: MarkerPoint) {
        viewModel.endPointMarker.postValue(marker)
        // viewModel.liveMarker.postValue(marker)
        showFragment(MapFragment.newInstance())
    }

    override fun onClickCreateRoad(marker: MarkerPoint) {
        viewModel.endPointMarker.postValue(marker)
        showFragment(MapFragment.newInstance())
    }
}
