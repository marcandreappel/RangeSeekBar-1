package com.jaygoo.demo

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.jaygoo.demo.fragments.BaseFragment
import com.jaygoo.demo.fragments.RangeSeekBarFragment
import com.jaygoo.demo.fragments.SingleSeekBarFragment
import com.jaygoo.demo.fragments.VerticalSeekBarFragment
import com.jaygoo.demo.fragments.StepsSeekBarFragment

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    internal var fragments: MutableList<BaseFragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        fragments.clear()
        fragments.add(SingleSeekBarFragment())
        fragments.add(RangeSeekBarFragment())
        fragments.add(StepsSeekBarFragment())
        fragments.add(VerticalSeekBarFragment())

        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.layout_tab)
        viewPager.adapter = PagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        for (s in types) {
            tabLayout.newTab().text = s
        }
    }

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return types.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return types[position]
        }
    }

    companion object {

        private val types = arrayOf("SINGLE", "RANGE", "STEP", "VERTICAL")
    }

}
