package com.example.homeworklogapp

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

internal class MyAdapter (
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
):
        FragmentPagerAdapter(fm) {
            override fun getItem(position: Int) : Fragment {
                return when (position) {
                    0 -> {
                        Fragment1()
                    }
                    1 -> {
                        Fragment2()
                    }
                    else -> getItem(position)
                }
            }

            override fun getCount(): Int {
                return totalTabs
            }
        }