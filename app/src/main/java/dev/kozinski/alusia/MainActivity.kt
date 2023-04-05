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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.datepicker.MaterialDatePicker
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
                val navController = rememberNavController()
                NavHost(navController, startDestination = "report-absence") {
                    composable("report-absence") {
                        val clock = Clock.systemDefaultZone()
                        val mainScreenState = rememberMainScreenState(
                            start = LocalDate.now(clock).plusDays(1),
                            end = LocalDate.now(clock).plusDays(1)
                        )
                        MainScreen(
                            onDateRangeFieldClick = {
                                MaterialDatePicker.Builder.dateRangePicker()
                                    .build()
                                    .apply {
                                        addOnPositiveButtonClickListener { (start, end) ->
                                            mainScreenState.start = localDateOfEpochMilli(start)
                                            mainScreenState.end = localDateOfEpochMilli(end)
                                        }
                                        show(supportFragmentManager, null)
                                    }
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
                            mainScreenState = mainScreenState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onDateRangeFieldClick: () -> Unit,
    onSendClick: (String) -> Unit,
    mainScreenState: MainScreenState,
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
                    .clickable { onDateRangeFieldClick() }
            ) {
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = mainScreenState.displayRange,
                    innerTextField = { Text(text = mainScreenState.displayRange) },
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

            val emailBody = composeEmail(mainScreenState.start, mainScreenState.end)
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

class MainScreenState(
    start: LocalDate,
    end: LocalDate,
) {
    var start by mutableStateOf(start)
    var end by mutableStateOf(end)
    val displayRange get() = "$start – $end"
}

@Composable
fun rememberMainScreenState(
    start: LocalDate,
    end: LocalDate,
) = remember(start, end) {
    MainScreenState(start, end)
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
fun MainScreenPreview() {
    AlusiaTheme {
        MainScreen(
            onDateRangeFieldClick = { },
            onSendClick = {},
            mainScreenState = rememberMainScreenState(
                start = LocalDate.ofEpochDay(1),
                end = LocalDate.ofEpochDay(1),
            ),
        )
    }
}

object Spacing {
    val Base @Composable get() = 16.dp
}

fun localDateOfEpochMilli(epochMilli: Long, offset: ZoneOffset = ZoneOffset.UTC): LocalDate {
    return Instant.ofEpochMilli(epochMilli).atOffset(offset).toLocalDate()
}
