package com.backpackingmap.backpackingmap.setup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.backpackingmap.backpackingmap.R
import com.backpackingmap.backpackingmap.databinding.ActivitySetupBinding
import com.backpackingmap.backpackingmap.setup.login.LoginFragment
import com.backpackingmap.backpackingmap.setup.register.RegisterFragment
import com.google.android.material.tabs.TabLayout

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private lateinit var model: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup)
        model = ViewModelProvider(this).get(SetupViewModel::class.java)

        attachTabListener()
        updateOnTabChange()
    }

    private fun attachTabListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // NOTE: There seems to be no clean way to get the tab selected
                // See <https://github.com/material-components/material-components-android/issues/1409>
                when (tab?.text) {
                    getString(R.string.register) -> model.registerSelected.value = true
                    getString(R.string.login) -> model.registerSelected.value = false
                    else -> throw IllegalStateException("Invalid tab selected")
                }
            }
        })
    }

    private fun updateOnTabChange() {
        model.registerSelected.observe(this, Observer { registerSelected ->
            val transaction = supportFragmentManager.beginTransaction()
            if (registerSelected) {
                transaction.replace(R.id.setup_container, RegisterFragment())
            } else {
                transaction.replace(R.id.setup_container, LoginFragment())
            }
            transaction.commit()
        })
    }
}