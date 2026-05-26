package com.example.organizadorapps.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.organizadorapps.AppLauncher
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.DeviceUsageApp
import com.example.organizadorapps.DeviceUsageSummary
import com.example.organizadorapps.QuickActionsBar
import com.example.organizadorapps.R
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.RecentUsageEventItem
import com.example.organizadorapps.UiConstants
import com.example.organizadorapps.UsageStatsHelper
import kotlin.math.abs
import kotlin.math.max

class UsageFragment : Fragment() {

    private var containerRoot: FrameLayout? = null
    private var lastPermissionState: Boolean? = null
    private var selectedDaysBack: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        containerRoot = FrameLayout(requireContext()).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        renderUsageContent()
        return containerRoot!!
    }

    override fun onResume() {
        super.onResume()
        val currentPermissionState = UsageStatsHelper.hasUsageStatsPermission(requireContext())
        if (lastPermissionState != currentPermissionState) renderUsageContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        containerRoot = null
    }

    private fun renderUsageContent() {
        val context = requireContext()
        val hasPermission = UsageStatsHelper.hasUsageStatsPermission(context)
        lastPermissionState = hasPermission

        val allApps = AppRepository.getInstalledLaunchableApps(context)
        val summary = if (hasPermission) {
            UsageStatsHelper.buildSummary(context, allApps, selectedDaysBack)
        } else {
            null
        }

        val scroll = ScrollView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(context, 18),
                AppUiUtils.dp(context, 38),
                AppUiUtils.dp(context, 18),
                AppUiUtils.dp(context, 24)
            )
        }

        root.addView(header(context))
        root.addView(periodSelector(context))

        if (summary == null) {
            root.addView(permissionInfoCard(context))
            root.addView(permissionMetricsGrid(context))
            root.addView(permissionSection(context, "Ranking de uso", "Activa el acceso de uso para ver tus apps más usadas, sesiones recientes y tiempo acumulado."))
            root.addView(permissionSection(context, "Resumen útil", "Con el permiso podremos calcular apps sin uso, tiempo en redes, productividad y patrones de apertura."))
        } else {
            root.addView(metricsGrid(context, summary))
            root.addView(insightStrip(context, summary, allApps.size))
            root.addView(mostUsedSection(context, summary))
            root.addView(bottomSummaryGrid(context, summary, allApps.size))
        }

        scroll.addView(root)
        containerRoot?.removeAllViews()
        containerRoot?.addView(scroll)
    }

    private fun header(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = AppUiUtils.dp(context, 14) }

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(TextView(context).apply {
                    text = "Actividad del dispositivo"
                    textSize = 15f
                    setTextColor(UiConstants.ACCENT)
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })

                addView(QuickActionsBar.build(context, compact = true))
            })

            addView(TextView(context).apply {
                text = "Uso"
                textSize = 42f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                setPadding(0, AppUiUtils.dp(context, 14), 0, 0)
            })

            addView(TextView(context).apply {
                text = "Métricas reales de uso, actividad reciente y accesos rápidos del sistema."
                textSize = 15f
                setTextColor(UiConstants.TEXT_SECONDARY)
                includeFontPadding = false
                setPadding(0, AppUiUtils.dp(context, 10), 0, 0)
            })
        }
    }

    private fun periodSelector(context: Context): LinearLayout {
        val options = listOf(1 to "Hoy", 7 to "7 días", 30 to "30 días")
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(context, Color.argb(172, 15, 23, 42), 32, Color.argb(120, 71, 85, 105))
            setPadding(AppUiUtils.dp(context, 4), AppUiUtils.dp(context, 4), AppUiUtils.dp(context, 4), AppUiUtils.dp(context, 4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 58)
            ).apply { bottomMargin = AppUiUtils.dp(context, 16) }

            options.forEach { (days, label) ->
                addView(TextView(context).apply {
                    text = label
                    textSize = 15f
                    gravity = Gravity.CENTER
                    typeface = if (selectedDaysBack == days) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    setTextColor(if (selectedDaysBack == days) UiConstants.TEXT_PRIMARY else UiConstants.TEXT_SECONDARY)
                    background = if (selectedDaysBack == days) rounded(context, UiConstants.ACCENT, 28, null) else null
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        selectedDaysBack = days
                        renderUsageContent()
                    }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                })
            }
        }
    }

    private fun metricsGrid(context: Context, summary: DeviceUsageSummary): LinearLayout {
        val deltaText = summary.usageDeltaPercent?.let { value ->
            val sign = if (value >= 0) "subió" else "bajó"
            "Uso $sign ${abs(value)}% vs. periodo anterior"
        } ?: "Sin comparación previa suficiente"

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(twoMetricRow(
                context,
                metricCard(context, R.drawable.ic_usage_time, "Tiempo total", UsageStatsHelper.formatCompactDuration(summary.totalUsageMs), deltaText),
                metricCard(context, R.drawable.ic_usage_apps, "Apps abiertas", summary.openedAppsCount.toString(), "${summary.backgroundAppsCount} pasaron a segundo plano")
            ))
            addView(twoMetricRow(
                context,
                metricCard(context, R.drawable.ic_usage_unlock, "Desbloqueos", summary.unlockCount.toString(), summary.unlocksPerHourText),
                metricCard(context, R.drawable.ic_usage_peak, "Hora intensa", summary.intenseHourLabel, "${UsageStatsHelper.formatCompactDuration(summary.intenseHourUsageMs)} acumulados")
            ))
        }
    }

    private fun permissionMetricsGrid(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(twoMetricRow(
                context,
                permissionMetricCard(context, R.drawable.ic_usage_time, "Tiempo total"),
                permissionMetricCard(context, R.drawable.ic_usage_apps, "Apps abiertas")
            ))
            addView(twoMetricRow(
                context,
                permissionMetricCard(context, R.drawable.ic_usage_unlock, "Desbloqueos"),
                permissionMetricCard(context, R.drawable.ic_usage_peak, "Hora intensa")
            ))
        }
    }

    private fun twoMetricRow(context: Context, left: View, right: View): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = AppUiUtils.dp(context, 12) }

            addView(left, LinearLayout.LayoutParams(0, AppUiUtils.dp(context, 142), 1f).apply {
                rightMargin = AppUiUtils.dp(context, 6)
            })
            addView(right, LinearLayout.LayoutParams(0, AppUiUtils.dp(context, 142), 1f).apply {
                leftMargin = AppUiUtils.dp(context, 6)
            })
        }
    }

    private fun metricCard(context: Context, iconRes: Int, label: String, value: String, detail: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = glass(context)
            setPadding(AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12))

            addView(iconBubble(context, iconRes, AppUiUtils.dp(context, 46)))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(AppUiUtils.dp(context, 10), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(context).apply {
                    text = label
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = value
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
                })
                addView(TextView(context).apply {
                    text = detail
                    textSize = 12f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
                })
            })
        }
    }

    private fun permissionMetricCard(context: Context, iconRes: Int, label: String): LinearLayout {
        return metricCard(context, iconRes, label, "Bloqueado", "Concede acceso de uso para calcularlo").apply {
            isClickable = true
            isFocusable = true
            setOnClickListener { UsageStatsHelper.openUsageAccessSettings(context) }
        }
    }

    private fun permissionInfoCard(context: Context): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionTitle(context, "Permiso requerido"))
            addView(TextView(context).apply {
                text = "Android protege las estadísticas de uso. Para construir este dashboard debes activar ‘Acceso de uso’ para OrganizadorApps."
                textSize = 14f
                setTextColor(UiConstants.TEXT_SECONDARY)
                setPadding(0, AppUiUtils.dp(context, 10), 0, AppUiUtils.dp(context, 14))
            })
            addView(permissionButton(context))
        }
    }

    private fun insightStrip(context: Context, summary: DeviceUsageSummary, totalApps: Int): LinearLayout {
        val inactive = (totalApps - summary.openedAppsCount).coerceAtLeast(0)
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            background = glass(context)
            setPadding(AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12), AppUiUtils.dp(context, 12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = AppUiUtils.dp(context, 12) }

            addView(miniInsight(context, "Top app", summary.topApp?.app?.name ?: "Sin dato", UiConstants.ACCENT), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(miniInsight(context, "Sin uso", "$inactive apps", Color.parseColor("#38BDF8")), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(miniInsight(context, "Promedio", averageSessionText(summary), Color.parseColor("#22C55E")), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun miniInsight(context: Context, label: String, value: String, color: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(AppUiUtils.dp(context, 4), 0, AppUiUtils.dp(context, 4), 0)
            addView(TextView(context).apply {
                text = label
                textSize = 11f
                setTextColor(UiConstants.TEXT_SECONDARY)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                includeFontPadding = false
            })
            addView(TextView(context).apply {
                text = value
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(color)
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                includeFontPadding = false
                setPadding(0, AppUiUtils.dp(context, 5), 0, 0)
            })
        }
    }

    private fun mostUsedSection(context: Context, summary: DeviceUsageSummary): LinearLayout {
        val maxTime = max(1L, summary.mostUsedApps.maxOfOrNull { it.totalTimeMs } ?: 1L)
        return sectionCard(context).apply {
            addView(sectionHeader(context, "Apps más usadas", "Top 5"))

            if (summary.mostUsedApps.isEmpty()) {
                addView(emptyText(context, "Aún no hay uso registrado en este periodo."))
            } else {
                summary.mostUsedApps.forEachIndexed { index, item ->
                    addView(mostUsedRow(context, index + 1, item, maxTime))
                }
            }
        }
    }

    private fun mostUsedRow(context: Context, position: Int, item: DeviceUsageApp, maxTime: Long): LinearLayout {
        val percentage = (item.totalTimeMs.toFloat() / maxTime.toFloat()).coerceIn(0.04f, 1f)
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setOnClickListener {
                RecentAppsManager.registerAppOpen(context, item.app)
                AppLauncher.openApp(context, item.app.packageName, item.app.name)
            }
            setPadding(0, AppUiUtils.dp(context, 7), 0, AppUiUtils.dp(context, 7))

            addView(TextView(context).apply {
                text = position.toString()
                textSize = 17f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 28), LinearLayout.LayoutParams.MATCH_PARENT)
            })

            addView(ImageView(context).apply {
                setImageDrawable(item.app.icon)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 38), AppUiUtils.dp(context, 38)).apply {
                    rightMargin = AppUiUtils.dp(context, 12)
                }
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(context).apply {
                    text = item.app.name
                    textSize = 15f
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                })

                addView(FrameLayout(context).apply {
                    background = rounded(context, Color.argb(150, 40, 48, 64), 6, null)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AppUiUtils.dp(context, 6)).apply {
                        topMargin = AppUiUtils.dp(context, 10)
                    }
                    addView(View(context).apply {
                        background = rounded(context, UiConstants.ACCENT, 6, null)
                        layoutParams = FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                            width = (context.resources.displayMetrics.widthPixels * 0.44f * percentage).toInt()
                        }
                    })
                })
            })

            addView(TextView(context).apply {
                text = UsageStatsHelper.formatDuration(item.totalTimeMs)
                textSize = 13f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 78), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun bottomSummaryGrid(context: Context, summary: DeviceUsageSummary, totalApps: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(usefulSummaryCard(context, summary, totalApps))
            addView(unusedAppsCard(context, summary.unusedApps))
            addView(recentActivityCard(context, summary.recentEvents))
        }
    }

    private fun usefulSummaryCard(context: Context, summary: DeviceUsageSummary, totalApps: Int): LinearLayout {
        val inactive = (totalApps - summary.openedAppsCount).coerceAtLeast(0)
        return sectionCard(context).apply {
            addView(sectionTitle(context, "Resumen útil"))
            addView(summaryLine(context, R.drawable.ic_usage_apps, "App más usada", summary.topApp?.app?.name ?: "Sin dato", Color.parseColor("#22C55E")))
            addView(separator(context))
            addView(summaryLine(context, R.drawable.ic_usage_time, "Tiempo en redes", UsageStatsHelper.formatDuration(summary.socialUsageMs), Color.parseColor("#38BDF8")))
            addView(separator(context))
            addView(summaryLine(context, R.drawable.ic_nav_usage, "Productividad", UsageStatsHelper.formatDuration(summary.productivityUsageMs), UiConstants.ACCENT))
            addView(separator(context))
            addView(summaryLine(context, R.drawable.ic_usage_apps, "Apps sin uso", "$inactive de $totalApps", Color.parseColor("#FBBF24")))
        }
    }


    private fun unusedAppsCard(context: Context, unusedApps: List<com.example.organizadorapps.InstalledApp>): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionHeader(context, "Apps sin uso", "Revisar"))
            if (unusedApps.isEmpty()) {
                addView(emptyText(context, "Todas tus apps registraron actividad en este periodo."))
            } else {
                addView(TextView(context).apply {
                    text = "Apps instaladas que no se abrieron en el periodo seleccionado. Toca una para abrirla."
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    setPadding(0, 0, 0, AppUiUtils.dp(context, 8))
                })
                unusedApps.take(6).forEachIndexed { index, app ->
                    addView(unusedAppRow(context, app))
                    if (index < unusedApps.take(6).lastIndex) addView(separator(context))
                }
            }
        }
    }

    private fun unusedAppRow(context: Context, app: com.example.organizadorapps.InstalledApp): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setOnClickListener {
                RecentAppsManager.registerAppOpen(context, app)
                AppLauncher.openApp(context, app.packageName, app.name)
            }
            setPadding(0, AppUiUtils.dp(context, 8), 0, AppUiUtils.dp(context, 8))

            addView(ImageView(context).apply {
                setImageDrawable(app.icon)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 34), AppUiUtils.dp(context, 34)).apply {
                    rightMargin = AppUiUtils.dp(context, 10)
                }
            })
            addView(TextView(context).apply {
                text = app.name
                textSize = 14f
                setTextColor(UiConstants.TEXT_PRIMARY)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_chevron_right)
                setColorFilter(UiConstants.TEXT_SECONDARY)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 20), AppUiUtils.dp(context, 20))
            })
        }
    }

    private fun recentActivityCard(context: Context, recentEvents: List<RecentUsageEventItem>): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionHeader(context, "Actividad reciente", "Últimas"))
            if (recentEvents.isEmpty()) {
                addView(emptyText(context, "Aún no hay aperturas recientes registradas."))
            } else {
                recentEvents.take(5).forEachIndexed { index, item ->
                    addView(recentRow(context, item))
                    if (index < recentEvents.take(5).lastIndex) addView(separator(context))
                }
            }
        }
    }

    private fun recentRow(context: Context, item: RecentUsageEventItem): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AppUiUtils.dp(context, 8), 0, AppUiUtils.dp(context, 8))

            addView(ImageView(context).apply {
                setImageDrawable(item.app.icon)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 34), AppUiUtils.dp(context, 34)).apply {
                    rightMargin = AppUiUtils.dp(context, 10)
                }
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(context).apply {
                    text = item.app.name
                    textSize = 14f
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = "Abierta ${UsageStatsHelper.formatClock(item.time)}"
                    textSize = 12f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 4), 0, 0)
                })
            })

            addView(TextView(context).apply {
                text = UsageStatsHelper.formatDuration(item.durationMs)
                textSize = 12f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.END
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 54), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun summaryLine(context: Context, iconRes: Int, label: String, value: String, color: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AppUiUtils.dp(context, 10), 0, AppUiUtils.dp(context, 10))
            addView(iconBubble(context, iconRes, AppUiUtils.dp(context, 42), color))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    leftMargin = AppUiUtils.dp(context, 10)
                }
                addView(TextView(context).apply {
                    text = label
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = value
                    textSize = 14f
                    setTextColor(color)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 4), 0, 0)
                })
            })
        }
    }

    private fun sectionHeader(context: Context, title: String, action: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, AppUiUtils.dp(context, 14))
            addView(sectionTitle(context, title), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(TextView(context).apply {
                text = action
                textSize = 13f
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
            })
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_chevron_right)
                setColorFilter(UiConstants.ACCENT)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 20), AppUiUtils.dp(context, 20)).apply {
                    leftMargin = AppUiUtils.dp(context, 4)
                }
            })
        }
    }

    private fun sectionTitle(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
        }
    }

    private fun permissionSection(context: Context, title: String, message: String): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionTitle(context, title))
            addView(TextView(context).apply {
                text = message
                textSize = 14f
                setTextColor(UiConstants.TEXT_SECONDARY)
                setPadding(0, AppUiUtils.dp(context, 12), 0, AppUiUtils.dp(context, 14))
            })
            addView(permissionButton(context))
        }
    }

    private fun permissionButton(context: Context): TextView {
        return TextView(context).apply {
            text = "Conceder permiso"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(UiConstants.TEXT_PRIMARY)
            background = rounded(context, UiConstants.ACCENT, 18, null)
            isClickable = true
            isFocusable = true
            setOnClickListener { UsageStatsHelper.openUsageAccessSettings(context) }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 42)
            )
        }
    }

    private fun sectionCard(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = glass(context)
            setPadding(AppUiUtils.dp(context, 16), AppUiUtils.dp(context, 16), AppUiUtils.dp(context, 16), AppUiUtils.dp(context, 16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = AppUiUtils.dp(context, 12) }
        }
    }

    private fun emptyText(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 14f
            setTextColor(UiConstants.TEXT_SECONDARY)
            setPadding(0, AppUiUtils.dp(context, 8), 0, AppUiUtils.dp(context, 8))
        }
    }

    private fun separator(context: Context): View {
        return View(context).apply {
            setBackgroundColor(Color.argb(90, 71, 85, 105))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 1)
            )
        }
    }

    private fun iconBubble(context: Context, iconRes: Int, size: Int, color: Int = UiConstants.ACCENT): FrameLayout {
        return FrameLayout(context).apply {
            background = circle(context, Color.argb(72, Color.red(color), Color.green(color), Color.blue(color)))
            layoutParams = LinearLayout.LayoutParams(size, size)
            addView(ImageView(context).apply {
                setImageResource(iconRes)
                setColorFilter(color)
                layoutParams = FrameLayout.LayoutParams((size * 0.48f).toInt(), (size * 0.48f).toInt(), Gravity.CENTER)
            })
        }
    }

    private fun averageSessionText(summary: DeviceUsageSummary): String {
        val sessions = summary.mostUsedApps.sumOf { it.openCount }.coerceAtLeast(1)
        return UsageStatsHelper.formatDuration(summary.totalUsageMs / sessions)
    }

    private fun glass(context: Context): GradientDrawable {
        return rounded(context, Color.argb(192, 15, 23, 42), 18, Color.argb(120, 71, 85, 105))
    }

    private fun rounded(context: Context, color: Int, radiusDp: Int, strokeColor: Int?): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = AppUiUtils.dp(context, radiusDp).toFloat()
            if (strokeColor != null) setStroke(AppUiUtils.dp(context, 1), strokeColor)
        }
    }

    private fun circle(context: Context, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }
}
