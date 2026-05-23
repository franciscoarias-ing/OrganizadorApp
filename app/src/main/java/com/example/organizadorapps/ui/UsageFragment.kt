package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.UiConstants
import com.example.organizadorapps.UsageAppStat
import com.example.organizadorapps.UsageStatsAdapter

class UsageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val allApps = AppRepository.getInstalledLaunchableApps(requireContext())
        val recentPackages = RecentAppsManager.getRecentPackageNames(requireContext())

        val stats = recentPackages.mapNotNull { packageName ->

            val app = allApps.find { it.packageName == packageName }

            app?.let {
                UsageAppStat(
                    app = it,
                    openCount = recentPackages.count { p -> p == packageName },
                    lastOpenedAt = System.currentTimeMillis()
                )
            }

        }.distinctBy { it.app.packageName }

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 42),
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 24)
            )
        }

        root.addView(AppUiUtils.kicker(requireContext(), "Actividad local"))
        root.addView(AppUiUtils.title(requireContext(), "Uso"))
        root.addView(AppUiUtils.subtitle(requireContext(), "Apps abiertas desde OrganizadorApp"))

        root.addView(
            AppUiUtils.actionCard(
                context = requireContext(),
                title = "Apps detectadas",
                subtitle = "${allApps.size} aplicaciones lanzables",
                icon = "⌘"
            ) {}
        )

        root.addView(
            AppUiUtils.actionCard(
                context = requireContext(),
                title = "Recientes registrados",
                subtitle = "${recentPackages.size} aperturas locales",
                icon = "↻"
            ) {}
        )

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = UsageStatsAdapter(stats)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isNestedScrollingEnabled = false

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        root.addView(recyclerView)
        scroll.addView(root)

        return scroll
    }
}