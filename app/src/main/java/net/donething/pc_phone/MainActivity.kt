package net.donething.pc_phone

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.donething.pc_phone.databinding.ActivityMainBinding
import net.donething.pc_phone.utils.MyNo

class MainActivity : AppCompatActivity() {
    private val itag = this::class.simpleName

    private lateinit var binding: ActivityMainBinding

    private val permissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_preferences
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 开启通知权限，设置通知频道
        MyNo.setNotificationPermission(this)

        // Check for required permissions
        //        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, permissionsNeeded.toTypedArray(), permissionRequestCode
            )
        } else {
            // onAllPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(code, permissions, grantResults)

        if (code == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // onAllPermissionsGranted()
            } else {
                // Show a message to the user explaining why the permissions are needed
            }
        }
    }
}