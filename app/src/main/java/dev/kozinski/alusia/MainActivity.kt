package dev.kozinski.alusia

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.kozinski.alusia.ui.theme.AlusiaTheme
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlusiaTheme {
                val clock = Clock.systemDefaultZone()
                val absenceState = rememberAbsenceState(
                    start = LocalDate.now(clock).plusDays(1),
                )

                val navController = rememberNavController()
                NavHost(navController, startDestination = "report-absence") {
                    composable("report-absence") {
                        ReportAbsenceScreen(
                            absenceState = absenceState,
                            onDateRangeFieldClick = {
                                navController.navigate("report-absence/dates")
                            },
                            onSendClick = {
                                try {
                                    startActivity(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_EMAIL, arrayOf("ps42@um.bialystok.pl"))
                                            putExtra(Intent.EXTRA_SUBJECT, "Nieobecność")
                                            putExtra(Intent.EXTRA_TEXT, it)
                                        }
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Nie udało się otworzyć aplikacji email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        )
                    }
                    composable("report-absence/dates") {
                        DateRangePicker(
                            onDismiss = { navController.navigateUp() },
                            onConfirmed = { start, end ->
                                absenceState.start = start.toLocalDate()
                                absenceState.end = end.toLocalDate()
                                navController.navigateUp()
                            },
                            initialSelectedStartDate = absenceState.start,
                            initialSelectedEndDate = absenceState.end,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportAbsenceScreen(
    absenceState: AbsenceState,
    onDateRangeFieldClick: () -> Unit,
    onSendClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zgłoszenie nieobecności") }
            )
        }
    ) { contentPadding ->
        Column(
            Modifier.padding(contentPadding)
        ) {
            Spacer(Modifier.height(Spacing.Base))

            Box(
                Modifier
                    .padding(horizontal = Spacing.Base)
                    .clickable(onClick = onDateRangeFieldClick)
            ) {
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = absenceState.displayRange,
                    innerTextField = { Text(text = absenceState.displayRange) },
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = MutableInteractionSource(),
                    label = { Text("Od – do") },
                )
            }

            Spacer(Modifier.height(Spacing.Base / 2))

            Text(
                text = "Podgląd wiadomości",
                modifier = Modifier.padding(horizontal = Spacing.Base),
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(Modifier.height(Spacing.Base / 2))

            val emailBody = composeEmail(absenceState.start, absenceState.end)
            SelectionContainer {
                Text(
                    text = emailBody,
                    modifier = Modifier.padding(horizontal = Spacing.Base * 2),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(Spacing.Base / 2))

            Button(
                onClick = { onSendClick(emailBody) },
                Modifier
                    .padding(horizontal = Spacing.Base)
                    .align(Alignment.End)
            ) {
                Text("Wyślij")
            }
        }
    }
}

class AbsenceState(
    start: LocalDate,
    end: LocalDate,
) {
    var start by mutableStateOf(start)
    var end by mutableStateOf(end)
    val displayRange get() = "$start – $end"
}

@Composable
fun rememberAbsenceState(
    start: LocalDate,
    end: LocalDate = start,
) = remember(start, end) {
    AbsenceState(start, end)
}

fun composeEmail(
    start: LocalDate,
    end: LocalDate = start,
): String {
    val polish = Locale("pl")
    val formatter = DateTimeFormatter.ofPattern("d MMMM").withLocale(polish)
    val range = if (start == end) {
        start.format(formatter)
    } else if (start.withDayOfMonth(1) == end.withDayOfMonth(1)) {
        "${start.dayOfMonth}-${end.format(formatter)}"
    } else if (start.year == end.year) {
        "od ${start.format(formatter)} do ${end.format(formatter)}"
    } else {
        val withYear = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(polish)
        "od ${start.format(withYear)} do ${end.format(withYear)}"
    }
    return """
        Dzień dobry,
        
        Zgłaszam, że moja córka Alicja Kozińska (grupa Sówki) będzie nieobecna $range.
        
        Pozdrawiam
        Marcin Koziński
    """.trimIndent()
}

@Preview(
    showSystemUi = true
)
@Composable
fun ReportAbsenceScreenPreview() {
    AlusiaTheme {
        ReportAbsenceScreen(
            absenceState = rememberAbsenceState(
                start = LocalDate.ofEpochDay(1),
            ),
            onDateRangeFieldClick = {},
            onSendClick = {},
        )
    }
}

@Composable
fun DateRangePicker(
    onDismiss: () -> Unit,
    onConfirmed: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedStartDate: LocalDate? = null,
    initialSelectedEndDate: LocalDate? = initialSelectedStartDate,
) {
    Surface {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDate?.toEpochMilli(),
            initialSelectedEndDate?.toEpochMilli(),
        )
        Column(
            modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        ) {
            Row(
                Modifier
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Anuluj")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        onConfirmed(
                            datePickerState.selectedStartDateMillis!!,
                            datePickerState.selectedEndDateMillis!!,
                        )
                    },
                    enabled = datePickerState.selectedStartDateMillis != null &&
                            datePickerState.selectedEndDateMillis != null
                ) {
                    Text("OK")
                }
            }
            val dateFormatter = remember { DatePickerFormatter() }
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.weight(1f),
                dateFormatter = dateFormatter,
                headline = {
                    ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                        DateRangePickerDefaults.DateRangePickerHeadline(
                            datePickerState,
                            dateFormatter,
                            modifier = Modifier.padding(
                                // Matching padding to the current implementation
                                PaddingValues(
                                    start = 64.dp,
                                    end = 12.dp,
                                    bottom = 12.dp
                                )
                            )
                        )
                    }
                },
            )
        }
    }
}
@Preview(
    showSystemUi = true,
)
@Composable
fun DateRangePickerPreview() {
    AlusiaTheme {
        DateRangePicker(
            onDismiss = { },
            onConfirmed = { _, _ -> }
        )
    }
}

object Spacing {
    val Base @Composable get() = 16.dp
}

fun Long.toLocalDate(offset: ZoneOffset = ZoneOffset.UTC): LocalDate {
    return Instant.ofEpochMilli(this).atOffset(offset).toLocalDate()
}

fun LocalDate.toEpochMilli(offset: ZoneOffset = ZoneOffset.UTC): Long {
    return atTime(0, 0).toInstant(offset).toEpochMilli()
}