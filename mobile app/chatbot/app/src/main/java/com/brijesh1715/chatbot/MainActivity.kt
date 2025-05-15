package com.brijesh1715.chatbot

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns // For getting filename from URI
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult // Added this import
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile // Correct import for the icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brijesh1715.chatbot.ui.theme.ChatbotTheme
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.forms.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// --- Data Classes ---
@Serializable
data class LanguagePredictionResponse(
    @SerialName("predicted_language")
    val predictedLanguage: String? = null,
    val error: String? = null
)

// --- API Service ---
object LanguageApiService {
    private const val BASE_URL = "https://ml-project-kaek.onrender.com"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorLogger", message)
                }
            }
            level = LogLevel.ALL
        }
    }

    suspend fun predictLanguage(audioFile: File): LanguagePredictionResponse? {
        return try {
            Log.d("LanguageApiService", "Attempting to upload file: ${audioFile.name}, Size: ${audioFile.length()} bytes")
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Log.e("LanguageApiService", "Audio file is invalid or empty before upload.")
                return LanguagePredictionResponse(error = "Selected audio file is invalid or empty.")
            }

            val response: LanguagePredictionResponse = client.post("$BASE_URL/predict-language") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("audio_file", audioFile.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "audio/mpeg") // Specifically for MP3
                            append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                        })
                    }
                ))
            }.body()
            Log.d("LanguageApiService", "API Response received: $response")
            response
        } catch (e: Exception) {
            Log.e("LanguageApiService", "API Call Failed", e)
            LanguagePredictionResponse(error = "Network request failed: ${e.message}")
        }
    }
}

// --- ViewModel ---
sealed class LanguageUiState {
    object Idle : LanguageUiState()
    object Processing : LanguageUiState()
    data class Success(val predictedLanguage: String, val fileName: String) : LanguageUiState()
    data class Error(val message: String) : LanguageUiState()
}

class LanguagePredictionViewModelFactory(private val applicationContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguagePredictionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanguagePredictionViewModel(applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LanguagePredictionViewModel(private val applicationContext: Context) : ViewModel() {
    private val _uiState = MutableStateFlow<LanguageUiState>(LanguageUiState.Idle)
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    fun predictFromFile(uri: Uri?) {
        if (uri == null) {
            _uiState.value = LanguageUiState.Error("No file selected.")
            return
        }

        _uiState.value = LanguageUiState.Processing
        viewModelScope.launch {
            try {
                val tempFile = createTempFileFromUri(applicationContext, uri)
                if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
                    Log.i("ViewModel", "File copied to cache. Processing file: ${tempFile.absolutePath}, Size: ${tempFile.length()}")
                    val response = LanguageApiService.predictLanguage(tempFile)
                    if (response?.predictedLanguage != null) {
                        _uiState.value = LanguageUiState.Success(response.predictedLanguage, tempFile.name)
                    } else {
                        _uiState.value = LanguageUiState.Error(response?.error ?: "Prediction failed: Unknown API error.")
                    }
                    tempFile.delete()
                    Log.i("ViewModel", "Cleaned up temporary audio file: ${tempFile.name}")
                } else {
                    _uiState.value = LanguageUiState.Error("Failed to process selected file or file is empty.")
                    Log.e("ViewModel", "Failed to create or access temp file from URI.")
                }
            } catch (e: Exception) {
                _uiState.value = LanguageUiState.Error("Error processing file: ${e.message}")
                Log.e("ViewModel", "Exception during file processing or API call", e)
            }
        }
    }

    private suspend fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}.mp3" // Default to .mp3 extension
                val tempFile = File(context.cacheDir, fileName)
                // tempFile.createNewFile() // FileOutputStream will create it
                val outputStream = FileOutputStream(tempFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (e: IOException) {
                Log.e("ViewModelHelper", "Error copying URI to temp file", e)
                null
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        }
        return uri.lastPathSegment?.takeIf { it.isNotEmpty() } ?: "audiofile.mp3" // Fallback with .mp3
    }

    fun resetState() {
        _uiState.value = LanguageUiState.Idle
    }
}

// --- MainActivity and UI Composables ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatbotTheme {
                LanguagePredictionScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePredictionScreen() {
    val context = LocalContext.current.applicationContext
    val viewModel: LanguagePredictionViewModel = viewModel(
        factory = LanguagePredictionViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    val pickAudioLauncher = rememberLauncherForActivityResult( // Usage of the function
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.predictFromFile(uri)
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Voice Language Predictor") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                LanguageUiState.Idle -> {
                    Text("Select an MP3 audio file to predict its language", fontSize = 18.sp, textAlign = TextAlign.Center)
                }
                LanguageUiState.Processing -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text("Identifying language...", fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
                }
                is LanguageUiState.Success -> {
                    Text("File: ${state.fileName}", fontSize = 16.sp, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Predicted Language:", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.predictedLanguage,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Select Another MP3 File")
                    }
                }
                is LanguageUiState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Try Again With Another MP3 File")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                if (uiState is LanguageUiState.Idle || uiState is LanguageUiState.Error) {
                    Button(
                        onClick = {
                            pickAudioLauncher.launch("audio/mpeg") // Specifically launch for MP3 files
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = "Select MP3 File",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select MP3 File", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
fun LanguagePredictionScreenIdlePreview() {
    ChatbotTheme {
        LanguagePredictionScreen()
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
fun LanguagePredictionScreenSuccessPreview() {
    ChatbotTheme {
        val context = LocalContext.current.applicationContext
        val viewModel: LanguagePredictionViewModel = viewModel(
            factory = LanguagePredictionViewModelFactory(context)
        )
        LaunchedEffect(Unit) {
            (viewModel.uiState as MutableStateFlow).value = LanguageUiState.Success("Hindi", "sample_audio.mp3")
        }
        LanguagePredictionScreen()
    }
}
