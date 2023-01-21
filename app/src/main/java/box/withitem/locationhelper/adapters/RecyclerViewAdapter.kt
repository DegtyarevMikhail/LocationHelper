package box.withitem.locationhelper.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import box.withitem.locationhelper.R
import box.withitem.locationhelper.data.MarkerPoint
import box.withitem.locationhelper.databinding.ListItemBinding
import box.withitem.locationhelper.utils.*

import com.google.firebase.auth.FirebaseAuth


class RecyclerViewAdapter(private val listener: Listener) :
    ListAdapter<MarkerPoint, RecyclerViewAdapter.RecyclerViewHolder>(ExampleItemDiffCallback) {
    class RecyclerViewHolder(
        private val itemBinding: ListItemBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        private val user by lazy { FirebaseAuth.getInstance().currentUser }

        /* Bind league name and image. */
        @SuppressLint("SimpleDateFormat")
        fun bind(markerPoint: MarkerPoint, listener: Listener) = with(itemBinding) {

            itemView.setOnClickListener {
                listener.onClick(markerPoint)
            }
            twEventTime.text = setDateTimeMarketPoint(markerPoint)

            twWhatHappened.text = getAccidentDescription()[markerPoint.accidentDescription.toString()]

            val addressText =
                getFullAddress(markerPoint.lat, markerPoint.lon, 1, itemBinding.root.context)!!
            twAddress.text = addressText
            twAftermathInfo.text = getSeverityAccident()[markerPoint.severityAccident.toString()]
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ListItemBinding.inflate(layoutInflater, parent, false)
        return RecyclerViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        val logs = getItem(position)
        holder.bind(logs, listener)

    }

    object ExampleItemDiffCallback : DiffUtil.ItemCallback<MarkerPoint>() {
        override fun areItemsTheSame(oldItem: MarkerPoint, newItem: MarkerPoint): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MarkerPoint, newItem: MarkerPoint): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }
    }


    interface Listener {
        fun onClick(marker: MarkerPoint)
        fun onClickImage(marker: MarkerPoint)
        fun onClickCreateRoad(marker: MarkerPoint)
    }

}

