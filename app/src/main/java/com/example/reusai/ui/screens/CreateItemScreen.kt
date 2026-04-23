package com.example.reusai.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.reusai.ui.theme.ReusaiTheme
import com.example.reusai.ui.viewmodels.CreateItemUiState
import com.example.reusai.ui.viewmodels.CreateItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

enum class CreateItemStep {
    PHOTOS, DETAILS, REVIEW
}

@Composable
fun CreateItemScreen(
    viewModel: CreateItemViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onPublish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    CreateItemContent(
        uiState = uiState,
        onNavigateBack = { viewModel.previousStep(onNavigateBack) },
        onNextStep = { viewModel.nextStep() },
        onPublish = onPublish,
        onAddPhoto = { uri -> viewModel.addPhoto(context, uri) },
        onRemovePhoto = { viewModel.removePhoto(it) },
        onTitleChange = viewModel::onTitleChange,
        onCategoryChange = viewModel::onCategoryChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTradeToggle = viewModel::onTradeToggle,
        onNeverUsedToggle = viewModel::onNeverUsedToggle
    )
}

@Composable
fun CreateItemContent(
    uiState: CreateItemUiState,
    onNavigateBack: () -> Unit,
    onNextStep: () -> Unit,
    onPublish: () -> Unit,
    onAddPhoto: (Uri) -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTradeToggle: (Boolean) -> Unit,
    onNeverUsedToggle: (Boolean) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onAddPhoto(it) } }
    )

    Scaffold(
        topBar = {
            CreateItemTopBar(
                currentStep = uiState.currentStep,
                onBack = onNavigateBack
            )
        },
        bottomBar = {
            if (uiState.currentStep != CreateItemStep.REVIEW) {
                BottomActionButton(
                    text = "Próximo Passo",
                    onClick = onNextStep
                )
            } else {
                BottomActionButton(
                    text = "Publicar Desapego",
                    onClick = onPublish
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
        ) {
            StepIndicator(currentStep = uiState.currentStep)
            
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.currentStep) {
                    CreateItemStep.PHOTOS -> {
                        PhotosUploadStep(
                            photos = uiState.photos,
                            onAddPhoto = {
                                if (uiState.photos.size < 4) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                            onRemovePhoto = onRemovePhoto
                        )
                        
                        if (uiState.isCompressing) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    CreateItemStep.DETAILS -> ItemDetailsStep(
                        title = uiState.title,
                        onTitleChange = onTitleChange,
                        category = uiState.category,
                        onCategoryChange = onCategoryChange,
                        description = uiState.description,
                        onDescriptionChange = onDescriptionChange,
                        isAvailableForTrade = uiState.isAvailableForTrade,
                        onTradeToggle = onTradeToggle,
                        isNeverUsed = uiState.isNeverUsed,
                        onNeverUsedToggle = onNeverUsedToggle
                    )
                    CreateItemStep.REVIEW -> ReviewStep()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemTopBar(
    currentStep: CreateItemStep,
    onBack: () -> Unit
) {
    val title = when (currentStep) {
        CreateItemStep.PHOTOS -> "Adicionar fotos"
        CreateItemStep.DETAILS -> "Detalhes do item"
        CreateItemStep.REVIEW -> "Revisão"
    }
    
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = if (currentStep == CreateItemStep.PHOTOS) Icons.Default.Close else Icons.Default.ArrowBackIosNew,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StepIndicator(currentStep: CreateItemStep) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepText("FOTOS", currentStep == CreateItemStep.PHOTOS)
            StepText("DETALHES", currentStep == CreateItemStep.DETAILS)
            StepText("REVISÃO", currentStep == CreateItemStep.REVIEW)
        }
        
        // Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
        ) {
            val weight = when (currentStep) {
                CreateItemStep.PHOTOS -> 0.33f
                CreateItemStep.DETAILS -> 0.66f
                CreateItemStep.REVIEW -> 1f
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(weight)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
}

@Composable
fun StepText(text: String, isActive: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun PhotosUploadStep(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Fotos do seu item",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Adicione até 4 fotos do item. Uma boa iluminação ajuda muito!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
        
        PhotoGrid(
            photos = photos,
            onAddPhoto = onAddPhoto,
            onRemovePhoto = onRemovePhoto
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        InfoBox(
            icon = Icons.Outlined.Info,
            text = "Evite fotos borradas e mostre possíveis defeitos. A transparência gera confiança!"
        )
    }
}

@Composable
fun PhotoGrid(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    Column {
        val totalSlots = 4
        val rows = 2
        for (i in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (j in 0 until 2) {
                    val index = i * 2 + j
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (index < photos.size) {
                            PhotoItem(
                                photo = photos[index],
                                isCover = index == 0,
                                onRemove = { onRemovePhoto(photos[index]) }
                            )
                        } else {
                            AddPhotoPlaceholder(onClick = onAddPhoto)
                        }
                    }
                }
            }
            if (i == 0) Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhotoItem(photo: Uri, isCover: Boolean, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = photo,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        if (isCover) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CAPA",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remover",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AddPhotoPlaceholder(onClick: () -> Unit) {
    val strokeColor = MaterialTheme.colorScheme.outline
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = strokeColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adicionar foto",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InfoBox(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsStep(
    title: String,
    onTitleChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isAvailableForTrade: Boolean,
    onTradeToggle: (Boolean) -> Unit,
    isNeverUsed: Boolean,
    onNeverUsedToggle: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val categories = remember {
        listOf(
            "📱 Eletrônicos",
            "Smartphones",
            "Notebooks e computadores",
            "Videogames e acessórios",
            "TVs e áudio",
            "👕 Moda e Acessórios",
            "Roupas (masculino, feminino, infantil)",
            "Calçados",
            "Bolsas e mochilas",
            "Relógios e joias",
            "🏠 Casa e Decoração",
            "Móveis",
            "Eletrodomésticos",
            "Utensílios de cozinha",
            "Itens de decoração",
            "📚 Cultura e Lazer",
            "Livros",
            "Filmes e séries (DVD/Blu-ray)",
            "Instrumentos musicais",
            "Jogos de tabuleiro",
            "🎮 Games",
            "Consoles",
            "Jogos físicos",
            "Acessórios gamer",
            "👶 Infantil",
            "Brinquedos",
            "Roupas infantis",
            "Carrinhos de bebê",
            "Artigos escolares",
            "🏋️ Esporte e Fitness",
            "Equipamentos de academia",
            "Bicicletas e acessórios",
            "Roupas esportivas",
            "Suplementos (com cautela/moderação)",
            "🐶 Pets",
            "Acessórios",
            "Brinquedos",
            "Camas e casinhas"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title Field
        FormFieldLabel("Título do item")
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Ex: Jaqueta de Couro Sintético") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Field
        FormFieldLabel("Categoria")

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (it) focusManager.clearFocus() // Clear focus before opening to hide keyboard
                expanded = it
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Selecione uma categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize().heightIn(max = 280.dp)
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onCategoryChange(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description Field
        FormFieldLabel("Descrição detalhada")
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Ex: Jaqueta preta tamanho M. Usada duas vezes...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Trade Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Disponível para troca",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Outros usuários poderão oferecer itens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Switch(
                    checked = isAvailableForTrade,
                    onCheckedChange = onTradeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Neve used
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Item nunca usado",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = isNeverUsed,
                    onCheckedChange = onNeverUsedToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@Composable
fun FormFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 0.dp)
    )
}

@Composable
fun ReviewStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate, // Using same icon as in design
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tudo pronto!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Seu item está prestes a entrar na comunidade. Revise as informações e publique para começar a receber propostas de troca.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun BottomActionButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private suspend fun compressImage(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    // Note: This is now handled in the ViewModel, but kept here if other parts of UI need it
    // or if you want to keep the UI's own utility functions separate.
    // Ideally, move this to a repository or use case.
    val outputFile = File.createTempFile("compressed_", ".jpg", context.cacheDir).apply {
        deleteOnExit()
    }

    try {
        Glide.with(context)
            .asFile()
            .load(uri)
            .apply(
                RequestOptions()
                    .override(1024, 768)
                    .encodeQuality(70)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .format(DecodeFormat.PREFER_RGB_565)
            )
            .submit()
            .get()
    } catch (e: Exception) {
        throw IOException("Falha ao comprimir imagem: ${e.message}")
    }
}


@Preview(showBackground = true)
@Composable
fun CreateItemStep1Preview() {
    ReusaiTheme {
        CreateItemContent(
            uiState = CreateItemUiState(currentStep = CreateItemStep.PHOTOS),
            onNavigateBack = {},
            onNextStep = {},
            onPublish = {},
            onAddPhoto = {},
            onRemovePhoto = {},
            onTitleChange = {},
            onCategoryChange = {},
            onDescriptionChange = {},
            onTradeToggle = {},
            onNeverUsedToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateItemStep2Preview() {
    ReusaiTheme {
        CreateItemContent(
            uiState = CreateItemUiState(
                currentStep = CreateItemStep.DETAILS,
                title = "Jaqueta de Couro Sintético",
                category = "Vestuário",
                description = "Jaqueta preta tamanho M. Usada duas vezes, sem nenhum rasgo ou marca de uso."
            ),
            onNavigateBack = {},
            onNextStep = {},
            onPublish = {},
            onAddPhoto = {},
            onRemovePhoto = {},
            onTitleChange = {},
            onCategoryChange = {},
            onDescriptionChange = {},
            onTradeToggle = {},
            onNeverUsedToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateItemStep3Preview() {
    ReusaiTheme {
        CreateItemContent(
            uiState = CreateItemUiState(currentStep = CreateItemStep.REVIEW),
            onNavigateBack = {},
            onNextStep = {},
            onPublish = {},
            onAddPhoto = {},
            onRemovePhoto = {},
            onTitleChange = {},
            onCategoryChange = {},
            onDescriptionChange = {},
            onTradeToggle = {},
            onNeverUsedToggle = {}
        )
    }
}
