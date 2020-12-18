package com.example.myarsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {


    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.sphere -> startARactivity(1)

            R.id.cylinder -> startARactivity(2)

            R.id.cube -> startARactivity(3)

            R.id.texture -> startARactivity(4)
        }
    }

    private fun startARactivity(i: Int) {
        val intent = Intent(this, ARActivity::class.java)
        intent.putExtra("order", i)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clickListeners()

    }

    private fun clickListeners() {
        sphere.setOnClickListener(this)
        cylinder.setOnClickListener(this)
        cube.setOnClickListener(this)
        texture.setOnClickListener(this)
    }

}