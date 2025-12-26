package com.example.adminblinkitclone.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.utils.Utils  // ✅ import Utils for status bar helper

class AuthMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_main)

        // ✅ Apply yellow status bar immediately
        Utils.setStatusBarColor(this, R.color.yellow, darkIcons = true)

        // ✅ Handle window insets (if your layout root has id "main")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
