package com.entezeer.deeplinkannotation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.entezeer.deeplinkannotation.databinding.ActivityMainBinding
import com.entezeer.processor.DeeplinkActivity

//import com.entezeer.generated.DeeplinkHandler


@DeeplinkActivity("https://o.kg/l/a?t=wl_atmtrns&type1=od&flow1=dengi")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deepLink = intent?.data?.toString()
//        if (deepLink != null) {
//            if (!DeeplinkHandler.navigateToActivity(this, deepLink)) {
//            }
//        }

        binding.button.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}