package box.withitem.locationhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import box.withitem.locationhelper.databinding.ActivityMainBinding
import box.withitem.locationhelper.fragments.CatchFragment
import box.withitem.locationhelper.fragments.MapFragment
import box.withitem.locationhelper.utils.openFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBottomNavClicks()
        openFragment(MapFragment.newInstance())
    }
    private fun onBottomNavClicks(){
        binding.bNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.id_home -> openFragment(MapFragment.newInstance())
                R.id.id_tracks -> openFragment(CatchFragment.newInstance())

            }
            true
        }
    }
}