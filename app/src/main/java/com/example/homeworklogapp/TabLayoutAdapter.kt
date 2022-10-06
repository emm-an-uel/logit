package com.example.homeworklogapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val totalTabs = 2

class TabLayoutAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

            override fun getItemCount(): Int {
                return totalTabs
            }

            override fun createFragment(position: Int): Fragment {

                val fragmentTodo = FragmentTodo()
                val fragmentDone = FragmentDone()

                when (position) {
                    0 -> return fragmentTodo
                }

                return fragmentDone
            }
        }