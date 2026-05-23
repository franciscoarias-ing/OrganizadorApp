package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

        val allApps =
            AppRepository.getInstalledLaunchableApps(requireContext())

        val recentPackages =
            RecentAppsManager.getRecentPackageNames(requireContext())

        val stats = recentPackages
            .groupingBy { it }
            .eachCount()
            .mapNotNull { entry ->

                val app =
                    allApps.find { it.packageName == entry.key }

                app?.let {
                    UsageAppStat(
                        app = it,
                        openCount = entry.value,
                        lastOpenedAt = 0L
                    )
                }
            }
            .sortedByDescending { it.openCount }

        val root = LinearLayout(requireContext()).apply {

            orientation = LinearLayout.VERTICAL

            setBackgroundColor(UiConstants.BACKGROUND)

            setPadding(
                UiConstants.SCREEN_PADDING,
                UiConstants.TOP_PADDING,
                UiConstants.SCREEN_PADDING,
                24
            )
        }

        root.addView(AppUiUtils.kicker(requireContext(), "Actividad"))
        root.addView(AppUiUtils.title(requireContext(), "Uso"))

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Estadísticas locales generadas desde OrganizadorApp."
            )
        )

        if (stats.isEmpty()) {

            root.addView(
                AppUiUtils.actionCard(
                    context = requireContext(),
                    title = "Sin estadísticas todavía",
                    subtitle = "Abre apps desde OrganizadorApp para generar actividad.",
                    icon = "↗"
                ) {}
            )

        } else {

            root.addView(
                RecyclerView(requireContext()).apply {

                    layoutManager =
                        LinearLayoutManager(requireContext())

                    adapter = UsageStatsAdapter(stats)

                    overScrollMode =
                        RecyclerView.OVER_SCROLL_NEVER

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }
            )
        }

        return root
    }
}