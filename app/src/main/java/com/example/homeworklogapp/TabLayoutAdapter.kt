package com.example.homeworklogapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val totalTabs = 2

class TabLayoutAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,
                       bundleTodo: Bundle, bundleDone: Bundle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragmentTodo = FragmentTodo()
    val fragmentDone = FragmentDone()
    val bundleFragmentTodo = bundleTodo
    val bundleFragmentDone = bundleDone

            override fun getItemCount(): Int {

                fragmentTodo.arguments = bundleFragmentTodo
                fragmentDone.arguments = bundleFragmentDone

                return totalTabs
            }

            override fun createFragment(position: Int): Fragment {

                when (position) {
                    0 -> return fragmentTodo
                }

                return fragmentDone
            }
        }