package com.example.organizadorapps.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.DeviceUsageApp
import com.example.organizadorapps.DeviceUsageSummary
import com.example.organizadorapps.RecentUsageEventItem
import com.example.organizadorapps.UiConstants
import com.example.organizadorapps.UsageStatsHelper
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

        if (lastPermissionState != currentPermissionState) {
            renderUsageContent()
        }
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
                AppUiUtils.dp(context, 42),
                AppUiUtils.dp(context, 18),
                AppUiUtils.dp(context, 24)
            )
        }

        root.addView(header(context))
        root.addView(periodSelector(context))

        if (summary == null) {
            root.addView(permissionMetricsGrid(context))
            root.addView(permissionSection(context, "Apps más usadas", "Para mostrar el ranking real de apps, permite el acceso de uso del dispositivo."))
            root.addView(permissionTwoColumns(context))
        } else {
            root.addView(metricsGrid(context, summary))
            root.addView(mostUsedSection(context, summary))
            root.addView(bottomSummaryGrid(context, summary))
        }

        scroll.addView(root)
        containerRoot?.removeAllViews()
        containerRoot?.addView(scroll)
    }

    private fun header(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = AppUiUtils.dp(context, 14) }

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(context).apply {
                    text = "Actividad del dispositivo"
                    textSize = 15f
                    setTextColor(UiConstants.ACCENT)
                    includeFontPadding = false
                })

                addView(TextView(context).apply {
                    text = "Uso"
                    textSize = 42f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 12), 0, 0)
                })

                addView(TextView(context).apply {
                    text = "Resumen del uso de tus aplicaciones."
                    textSize = 16f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 12), 0, 0)
                })
            })

            addView(TextView(context).apply {
                text = "↗"
                textSize = 28f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.ACCENT)
                background = circle(context, Color.argb(190, 39, 27, 72))
                layoutParams = LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, 56),
                    AppUiUtils.dp(context, 56)
                )
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
            ).apply { bottomMargin = AppUiUtils.dp(context, 18) }

            options.forEach { (days, label) ->
                addView(TextView(context).apply {
                    text = label
                    textSize = 16f
                    gravity = Gravity.CENTER
                    typeface = if (selectedDaysBack == days) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    setTextColor(if (selectedDaysBack == days) UiConstants.TEXT_PRIMARY else UiConstants.TEXT_SECONDARY)
                    background = if (selectedDaysBack == days) {
                        rounded(context, UiConstants.ACCENT, 28, null)
                    } else {
                        null
                    }
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
            val sign = if (value >= 0) "↑" else "↓"
            "$sign ${kotlin.math.abs(value)}% vs. periodo anterior"
        } ?: "Sin comparación previa"

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(twoMetricRow(
                context,
                metricCard(context, "◷", "Tiempo de uso ${summary.periodLabel.lowercase()}", UsageStatsHelper.formatDuration(summary.totalUsageMs), deltaText),
                metricCard(context, "▦", "Apps abiertas", summary.openedAppsCount.toString(), "${summary.backgroundAppsCount} en segundo plano")
            ))
            addView(twoMetricRow(
                context,
                metricCard(context, "▢", "Desbloqueos", summary.unlockCount.toString(), summary.unlocksPerHourText),
                metricCard(context, "♨", "Uso más intenso", summary.intenseHourLabel, UsageStatsHelper.formatDuration(summary.intenseHourUsageMs) + " de uso")
            ))
        }
    }

    private fun permissionMetricsGrid(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(twoMetricRow(
                context,
                permissionMetricCard(context, "◷", "Tiempo de uso"),
                permissionMetricCard(context, "▦", "Apps abiertas")
            ))
            addView(twoMetricRow(
                context,
                permissionMetricCard(context, "▢", "Desbloqueos"),
                permissionMetricCard(context, "♨", "Uso más intenso")
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

            addView(left, LinearLayout.LayoutParams(0, AppUiUtils.dp(context, 112), 1f).apply {
                rightMargin = AppUiUtils.dp(context, 6)
            })
            addView(right, LinearLayout.LayoutParams(0, AppUiUtils.dp(context, 112), 1f).apply {
                leftMargin = AppUiUtils.dp(context, 6)
            })
        }
    }

    private fun metricCard(context: Context, icon: String, label: String, value: String, detail: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = glass(context)
            setPadding(AppUiUtils.dp(context, 14), AppUiUtils.dp(context, 14), AppUiUtils.dp(context, 14), AppUiUtils.dp(context, 14))

            addView(TextView(context).apply {
                text = icon
                textSize = 27f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.ACCENT)
                background = circle(context, Color.argb(210, 45, 27, 79))
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 58), AppUiUtils.dp(context, 58))
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(AppUiUtils.dp(context, 12), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(context).apply {
                    text = label
                    textSize = 14f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 1
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = value
                    textSize = 26f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    maxLines = 1
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
                })
                addView(TextView(context).apply {
                    text = detail
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 1
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
                })
            })
        }
    }

    private fun permissionMetricCard(context: Context, icon: String, label: String): LinearLayout {
        return metricCard(context, icon, label, "Permiso", "Toca para conceder").apply {
            isClickable = true
            isFocusable = true
            setOnClickListener { UsageStatsHelper.openUsageAccessSettings(context) }
        }
    }

    private fun mostUsedSection(context: Context, summary: DeviceUsageSummary): LinearLayout {
        val maxTime = max(1L, summary.mostUsedApps.maxOfOrNull { it.totalTimeMs } ?: 1L)

        return sectionCard(context).apply {
            addView(sectionHeader(context, "Apps más usadas ${summary.periodLabel.lowercase()}", "Ver todo"))

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
            setPadding(0, AppUiUtils.dp(context, 7), 0, AppUiUtils.dp(context, 7))

            addView(TextView(context).apply {
                text = position.toString()
                textSize = 18f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 30), LinearLayout.LayoutParams.MATCH_PARENT)
            })

            addView(ImageView(context).apply {
                setImageDrawable(item.app.icon)
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 38), AppUiUtils.dp(context, 38)).apply {
                    rightMargin = AppUiUtils.dp(context, 14)
                }
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(context).apply {
                    text = item.app.name
                    textSize = 16f
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    maxLines = 1
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
                            width = (context.resources.displayMetrics.widthPixels * 0.45f * percentage).toInt()
                        }
                    })
                })
            })

            addView(TextView(context).apply {
                text = UsageStatsHelper.formatDuration(item.totalTimeMs)
                textSize = 14f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 82), LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            addView(TextView(context).apply {
                text = "›"
                textSize = 28f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 20), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun bottomSummaryGrid(context: Context, summary: DeviceUsageSummary): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = AppUiUtils.dp(context, 14) }

            addView(usefulSummaryCard(context, summary), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = AppUiUtils.dp(context, 6)
            })
            addView(recentActivityCard(context, summary.recentEvents), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = AppUiUtils.dp(context, 6)
            })
        }
    }

    private fun permissionTwoColumns(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = AppUiUtils.dp(context, 14) }

            addView(permissionSection(context, "Resumen útil", "Permite acceso de uso para calcular tu app más usada, tiempo en redes y productividad."), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = AppUiUtils.dp(context, 6)
            })
            addView(permissionSection(context, "Actividad reciente", "Permite acceso de uso para listar las últimas apps abiertas del dispositivo."), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = AppUiUtils.dp(context, 6)
            })
        }
    }

    private fun usefulSummaryCard(context: Context, summary: DeviceUsageSummary): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionTitle(context, "Resumen útil"))
            addView(summaryLine(context, "☘", "App más usada", summary.topApp?.app?.name ?: "—", Color.parseColor("#22C55E")))
            addView(separator(context))
            addView(summaryLine(context, "♙", "Tiempo en redes", UsageStatsHelper.formatDuration(summary.socialUsageMs), Color.parseColor("#38BDF8")))
            addView(separator(context))
            addView(summaryLine(context, "▣", "Productividad", UsageStatsHelper.formatDuration(summary.productivityUsageMs), UiConstants.ACCENT))
        }
    }

    private fun recentActivityCard(context: Context, items: List<RecentUsageEventItem>): LinearLayout {
        return sectionCard(context).apply {
            addView(sectionHeader(context, "Actividad reciente", "Ver todo"))

            if (items.isEmpty()) {
                addView(emptyText(context, "No hay actividad reciente para mostrar."))
            } else {
                items.forEachIndexed { index, item ->
                    addView(recentRow(context, item))
                    if (index < items.lastIndex) addView(separator(context))
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
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = "Abierta ${UsageStatsHelper.formatClock(item.time)}"
                    textSize = 12f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    maxLines = 1
                    includeFontPadding = false
                    setPadding(0, AppUiUtils.dp(context, 4), 0, 0)
                })
            })

            addView(TextView(context).apply {
                text = UsageStatsHelper.formatDuration(item.durationMs)
                textSize = 12f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 48), LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            addView(TextView(context).apply {
                text = "•"
                textSize = 20f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 16), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun summaryLine(context: Context, icon: String, label: String, value: String, color: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AppUiUtils.dp(context, 10), 0, AppUiUtils.dp(context, 10))

            addView(TextView(context).apply {
                text = icon
                textSize = 21f
                gravity = Gravity.CENTER
                setTextColor(color)
                background = circle(context, Color.argb(75, Color.red(color), Color.green(color), Color.blue(color)))
                layoutParams = LinearLayout.LayoutParams(AppUiUtils.dp(context, 42), AppUiUtils.dp(context, 42)).apply {
                    rightMargin = AppUiUtils.dp(context, 10)
                }
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

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
                textSize = 14f
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
            })
            addView(TextView(context).apply {
                text = "  ›"
                textSize = 24f
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
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
            setPadding(
                AppUiUtils.dp(context, 16),
                AppUiUtils.dp(context, 16),
                AppUiUtils.dp(context, 16),
                AppUiUtils.dp(context, 16)
            )
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

    private fun glass(context: Context): GradientDrawable {
        return rounded(context, Color.argb(192, 15, 23, 42), 18, Color.argb(120, 71, 85, 105))
    }

    private fun rounded(context: Context, color: Int, radiusDp: Int, strokeColor: Int?): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = AppUiUtils.dp(context, radiusDp).toFloat()
            if (strokeColor != null) {
                setStroke(AppUiUtils.dp(context, 1), strokeColor)
            }
        }
    }

    private fun circle(context: Context, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }
}
