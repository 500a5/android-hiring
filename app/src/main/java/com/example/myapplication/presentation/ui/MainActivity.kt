package com.example.myapplication.presentation.ui

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private val ageList = (16..30).toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Hawk.init(this).build()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.loadSavedData()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.popupAge.setOnClickListener { showAgePopupMenu(it) }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            val gender = when (checkedId) {
                binding.rMale.id -> "m"
                binding.rFemale.id -> "f"
                else -> return@setOnCheckedChangeListener
            }
            viewModel.updateGender(gender)
            updateGenderUI(gender)
        }

        binding.btnSend.setOnClickListener { viewModel.sendData() }
    }

    private fun showAgePopupMenu(view: View) {
        PopupMenu(this, view, Gravity.TOP).apply {
            ageList.forEach { age -> menu.add(0, age, 0, age.toString()) }
            setOnMenuItemClickListener { onAgeSelected(it); true }
            show()
        }
    }

    private fun onAgeSelected(item: MenuItem) {
        val selectedAge = item.title.toString().toInt()
        binding.popupAge.text = selectedAge.toString()
        viewModel.updateAge(selectedAge)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isFormValid.collectLatest { binding.btnSend.isEnabled = it }
                }
                launch {
                    viewModel.messageFlow.collectLatest { showMessage(it) }
                }
                launch {
                    viewModel.savedData.collectLatest { (age, gender) ->
                        age?.let { binding.popupAge.text = it.toString() }
                        gender?.let { updateGenderUI(it) }
                    }
                }
            }
        }
    }

    private fun updateGenderUI(gender: String) {
        binding.rMale.setBackgroundResource(if (gender == "m") R.drawable.rounded_button_active else R.drawable.rounded_button_inactive)
        binding.rFemale.setBackgroundResource(if (gender == "f") R.drawable.rounded_button_active else R.drawable.rounded_button_inactive)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
