package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.example.organizadorapps.UsageStatsHelper

class UsageFragment : Fragment() {

    private var containerRoot: FrameLayout? = null
    private var lastPermissionState: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        containerRoot = FrameLayout(requireContext()).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
        }

        renderUsageContent()

        return containerRoot!!
    }

    override fun onResume() {
        super.onResume()

        val currentPermissionState = UsageStatsHelper.hasUsageStatsPermission(requireContext())

        if (lastPermissionState != null && lastPermissionState != currentPermissionState) {
            renderUsageContent()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        containerRoot = null
    }

    private fun renderUsageContent() {
        val context = requireContext()

        lastPermissionState = UsageStatsHelper.hasUsageStatsPermission(context)

        val allApps = AppRepository.getInstalledLaunchableApps(context)
        val hasUsagePermission = lastPermissionState == true

        val realRecentApps = if (hasUsagePermission) {
            UsageStatsHelper.getRecentUsedApps(
                context = context,
                allApps = allApps,
                limit = 8,
                daysBack = 7
            )
        } else {
            emptyList()
        }

        val localRecentPackages = RecentAppsManager.getRecentPackageNames(context)

        val localStats = localRecentPackages
            .mapNotNull { packageName ->
                val app = allApps.find { it.packageName == packageName }

                app?.let {
                    UsageAppStat(
                        app = it,
                        openCount = localRecentPackages.count { p -> p == packageName },
                        lastOpenedAt = System.currentTimeMillis()
                    )
                }
            }
            .distinctBy { it.app.packageName }

        val isUsingRealUsage = realRecentApps.isNotEmpty()

        val stats = if (isUsingRealUsage) {
            realRecentApps.map { app ->
                UsageAppStat(
                    app = app,
                    openCount = 0,
                    lastOpenedAt = System.currentTimeMillis(),
                    detailText = "Uso reciente del teléfono"
                )
            }
        } else {
            localStats
        }

        val scroll = ScrollView(context).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(context, 22),
                AppUiUtils.dp(context, 42),
                AppUiUtils.dp(context, 22),
                AppUiUtils.dp(context, 24)
            )
        }

        root.addView(
            AppUiUtils.kicker(
                context,
                if (isUsingRealUsage) "Actividad del teléfono" else "Actividad local"
            )
        )

        root.addView(AppUiUtils.title(context, "Uso"))

        root.addView(
            AppUiUtils.subtitle(
                context,
                if (isUsingRealUsage) {
                    "Apps usadas recientemente en el dispositivo"
                } else {
                    "Apps abiertas desde OrganizadorApp"
                }
            )
        )

        root.addView(
            AppUiUtils.actionCard(
                context = context,
                title = "Apps detectadas",
                subtitle = "${allApps.size} aplicaciones lanzables",
                icon = "⌘"
            ) {}
        )

        root.addView(
            AppUiUtils.actionCard(
                context = context,
                title = if (isUsingRealUsage) {
                    "Acceso de uso activo"
                } else {
                    "Recientes locales"
                },
                subtitle = if (isUsingRealUsage) {
                    "${stats.size} apps recientes del teléfono"
                } else {
                    "${localRecentPackages.size} aperturas registradas en OrganizadorApp"
                },
                icon = if (isUsingRealUsage) "✓" else "↻"
            ) {
                if (!hasUsagePermission) {
                    UsageStatsHelper.openUsageAccessSettings(context)
                }
            }
        )

        if (!hasUsagePermission) {
            root.addView(
                AppUiUtils.actionCard(
                    context = context,
                    title = "Activar uso real",
                    subtitle = "Permite acceso de uso para detectar apps recientes del teléfono",
                    icon = "⚙"
                ) {
                    UsageStatsHelper.openUsageAccessSettings(context)
                }
            )
        }

        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
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

        containerRoot?.removeAllViews()
        containerRoot?.addView(scroll)
    }
}