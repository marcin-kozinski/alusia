@file:OptIn(ExperimentalMaterial3Api::class)

package dev.kozinski.alusia

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kozinski.alusia.ui.theme.AlusiaTheme
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlusiaTheme {
                MainScreen(
                    onSendClick = {
                        try {
                            startActivity(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("ps42@um.bialystok.pl"))
                                    putExtra(Intent.EXTRA_SUBJECT, "Nieobecność")
                                    putExtra(Intent.EXTRA_TEXT, it)
                                }
                            )
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                this,
                                "Nie udało się znaleźć aplikacji email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    clock = Clock.systemDefaultZone(),
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    onSendClick: (String) -> Unit,
    clock: Clock
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

            var start by remember { mutableStateOf(LocalDate.now(clock).plusDays(1)) }
            var end by remember { mutableStateOf(LocalDate.now(clock).plusDays(1)) }
            Row {
                Spacer(Modifier.width(Spacing.Base))

                DateField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("Od") },
                )

                Spacer(Modifier.width(Spacing.Base / 2))

                DateField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("Do") },
                )

                Spacer(Modifier.width(Spacing.Base))
            }

            Spacer(Modifier.height(Spacing.Base / 2))

            Text(
                text = "Podgląd wiadomości",
                modifier = Modifier.padding(horizontal = Spacing.Base),
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(Modifier.height(Spacing.Base / 2))

            val emailBody = composeEmail(start, end)
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

@Composable
fun DateField(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val picker = remember(context, value, onValueChange) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onValueChange(LocalDate.of(year, month + 1, day)) },
            value.year,
            value.monthValue - 1,
            value.dayOfMonth
        )
    }
    Box(
        modifier
            .clickable { picker.show() }
    ) {
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
            value = value.toString(),
            innerTextField = { Text(text = value.toString()) },
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = MutableInteractionSource(),
            label = label,
        )
    }

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
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MainScreenPreview() {
    AlusiaTheme {
        MainScreen(
            onSendClick = {},
            clock = Clock.fixed(
                Instant.EPOCH,
                ZoneOffset.UTC
            ),
        )
    }
}

object Spacing {
    val Base = 16.dp
}