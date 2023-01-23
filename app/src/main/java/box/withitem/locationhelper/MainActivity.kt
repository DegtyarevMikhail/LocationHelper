package box.withitem.locationhelper

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import box.withitem.locationhelper.databinding.ActivityMainBinding
import box.withitem.locationhelper.fragments.MapScreenFragment
import box.withitem.locationhelper.fragments.RecyclerViewFragment

import box.withitem.locationhelper.fragments.SettingsFragment
import box.withitem.locationhelper.model.location.LocationProviderChangedReceiver
import box.withitem.locationhelper.utils.showFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            startSignInIntent()
        }
        val br: BroadcastReceiver = LocationProviderChangedReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(br, filter)

//         if (isDarkTheme(MainActivity())) {Log.d("UI MODE","Dark")}
//         else{Log.d("UI MODE","noDark")}

        supportFragmentManager.addOnBackStackChangedListener {
            changeMenuItems()
        }
    }

    //test
    private fun startSignInIntent() {
        fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//            val response = result.idpResponse
            if (result.resultCode == RESULT_OK) {
                showFragment(MapScreenFragment.newInstance())
                navigationMenu()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_launcher)
            .setAlwaysShowSignInMethodScreen(false)
            .setLockOrientation(true)
            .build()

        val signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            onSignInResult(res)
        }

        signInLauncher.launch(signInIntent)
    }

    private fun navigationMenu() {
        binding.botNavig.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.nav_bar_return_to_map -> {
                    showFragment(MapScreenFragment.newInstance())
                }
                R.id.nav_bar_fab_my_setting_button -> {
                    showFragment(SettingsFragment())
                }
                R.id.nav_bar_accident_list_fragment_button -> {
                    //TODO отобразить фрагмент со списком
                    showFragment(RecyclerViewFragment())
                }

            }
            true
        }
    }


    private fun changeMenuItems() {
        val settingsButton = binding.botNavig.menu.findItem(R.id.nav_bar_fab_my_setting_button)
        val listButton = binding.botNavig.menu.findItem(R.id.nav_bar_accident_list_fragment_button)
        val mapButton = binding.botNavig.menu.findItem(R.id.nav_bar_return_to_map)

//        mapButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
//        mapButton.isVisible = false
//        listButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
//
//        settingsButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

/*        val fc = supportFragmentManager.findFragmentById(R.id.placeHolder)
        when (fc) {
            is MapScreenFragment -> {
                mapButton.isVisible = false
                listButton.isVisible = true
                //listButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                settingsButton.isVisible = true
                //settingsButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            is SettingsFragment -> {
                mapButton.isVisible = true
                //mapButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                listButton.isVisible = true
                //listButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                settingsButton.isVisible = false
            }
        }*/

    }

    /*        для входа через email
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setAndroidPackageName(
                "ru.gb.motohelp",
                true,
                null
            )
            .setHandleCodeInApp(true) // This must be set to true
            .setUrl("https://google.com") // This URL needs to be whitelisted
            .build()*/



//     fun setMarkerLocation(){
//        val fc = supportFragmentManager.findFragmentById(R.id.placeHolder)
//        if(fc is MapScreenFragment){
//            fc.setMarkerLocation()
//        }
//    }

}
