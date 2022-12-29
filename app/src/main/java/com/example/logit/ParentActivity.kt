package com.example.logit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.logit.databinding.ActivityParentBinding
import com.example.logit.ViewModelParent

private lateinit var appBarConfiguration: AppBarConfiguration
private lateinit var binding: ActivityParentBinding

class ParentActivity : AppCompatActivity() {

    lateinit var viewModel: ViewModelParent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        // initialize viewModel - note that initialization of ViewModel must happen before initializing binding
        // since binding will initialize FragmentLog (which relies on ViewModel already being initialized by this activity)
        viewModel = ViewModelProvider(this)[ViewModelParent::class.java]

        // initialize data
        viewModel.apply {
            initListCardColors()
            initListSettings()
            initTaskLists()
            createConsolidatedListTodo()
            createConsolidatedListDone()
            initSubjectColor()
        }

        binding = ActivityParentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarParent.toolbar)

        // nav drawer
        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)!!
        val navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_log, R.id.nav_calendar
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}