package com.example.adminblinkitclone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.adminblinkitclone.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Initialize ViewBinding properly
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Apply insets to your root layout (if your layout root has id "main")
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Setup bottom navigation with NavController
        val navController = Navigation.findNavController(this, R.id.fragmentContainerView2)
        NavigationUI.setupWithNavController(binding.bottomMenu, navController)
    }
}
// we ware at this point