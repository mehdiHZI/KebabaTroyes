package com.amhsolution.caisse.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.amhsolution.caisse.domain.history.HistoryDateFilter
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryCalendarDialog(
    onDismiss: () -> Unit,
    onConfirm: (HistoryDateFilter) -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var anchorDay by remember { mutableStateOf<LocalDate?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WireframeBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choisir une journee ou une periode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1 clic = debut | 2 clics sur le meme jour = 1 jour | 2 jours differents = periode",
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted
                )

                MonthNavigator(
                    displayedMonth = displayedMonth,
                    onPrevious = { displayedMonth = displayedMonth.minusMonths(1) },
                    onNext = { displayedMonth = displayedMonth.plusMonths(1) }
                )

                WeekdayHeaderRow()

                val days = remember(displayedMonth) { buildMonthGrid(displayedMonth) }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(days.size) { index ->
                        val day = days[index]
                        if (day == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            CalendarDayCell(
                                day = day,
                                isAnchor = day == anchorDay,
                                isToday = day == LocalDate.now(),
                                onClick = {
                                    if (anchorDay == null) {
                                        anchorDay = day
                                    } else if (anchorDay == day) {
                                        onConfirm(HistoryDateFilter.Single(day))
                                    } else {
                                        val start = minOf(anchorDay!!, day)
                                        val end = maxOf(anchorDay!!, day)
                                        onConfirm(HistoryDateFilter.Range(start, end))
                                    }
                                }
                            )
                        }
                    }
                }

                anchorDay?.let { selected ->
                    Text(
                        text = "Selection : ${selected.format(dayFormatter)} — recliquez pour valider ce jour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseBlueDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onConfirm(HistoryDateFilter.today())
                    }) {
                        Text("Aujourd'hui")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    displayedMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious) {
            Text("<")
        }
        Text(
            text = displayedMonth.format(monthFormatter).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.FRANCE) else it.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onNext) {
            Text(">")
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = CaisseMuted
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: LocalDate,
    isAnchor: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isAnchor -> CaisseBlue
        isToday -> WireframeYellowBackground
        else -> WireframeNavBackground
    }
    val contentColor = if (isAnchor) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = when {
        isAnchor -> CaisseBlueDark
        isToday -> WireframeBorder
        else -> WireframeBorder.copy(alpha = 0.4f)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isAnchor || isToday) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

private fun buildMonthGrid(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val leadingEmpty = firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value
    val cells = mutableListOf<LocalDate?>()
    repeat(leadingEmpty.coerceAtLeast(0)) { cells.add(null) }
    for (day in 1..month.lengthOfMonth()) {
        cells.add(month.atDay(day))
    }
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells
}

private val weekdayLabels = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)
private val dayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)
