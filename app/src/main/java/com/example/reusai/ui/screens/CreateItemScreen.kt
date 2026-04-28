package com.example.reusai.ui.screens

import android.R.attr.strokeColor
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.reusai.ui.theme.ReusaiTheme
import com.example.reusai.ui.viewmodels.CreateItemUiState
import com.example.reusai.ui.viewmodels.CreateItemViewModel

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
        onPublish = { viewModel.publishItem(onPublish) },
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
                    text = if (uiState.isPublishing) "Publicando..." else "Publicar Desapego",
                    onClick = onPublish,
                    enabled = !uiState.isPublishing
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
    }
}

@Composable
fun PhotoGrid(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoItem(
                uri = photos.getOrNull(0),
                onAdd = onAddPhoto,
                onRemove = { photos.getOrNull(0)?.let { onRemovePhoto(it) } },
                modifier = Modifier.weight(1f)
            )
            PhotoItem(
                uri = photos.getOrNull(1),
                onAdd = onAddPhoto,
                onRemove = { photos.getOrNull(1)?.let { onRemovePhoto(it) } },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoItem(
                uri = photos.getOrNull(2),
                onAdd = onAddPhoto,
                onRemove = { photos.getOrNull(2)?.let { onRemovePhoto(it) } },
                modifier = Modifier.weight(1f)
            )
            PhotoItem(
                uri = photos.getOrNull(3),
                onAdd = onAddPhoto,
                onRemove = { photos.getOrNull(3)?.let { onRemovePhoto(it) } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PhotoItem(
    uri: Uri?,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
//            .background(MaterialTheme.colorScheme.surfaceVariant)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .clickable(enabled = uri == null) { onAdd() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Foto do item",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover foto",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            val strokeColor = MaterialTheme.colorScheme.outline
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Adicionar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
    val focusManager = LocalFocusManager.current
    val categories = listOf("Vestuário", "Eletrônicos", "Casa", "Livros", "Esportes", "Outros")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Sobre o desapego",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Conte-nos mais sobre o que você está desapegando.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Title
        CreateItemTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título do anúncio",
            placeholder = "Ex: Jaqueta de couro preta"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                placeholder = { Text("Selecione uma categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onCategoryChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        CreateItemTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descrição",
            placeholder = "Descreva o estado do item, tamanho, tempo de uso...",
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggles
        ToggleOption(
            title = "Aceito trocas",
            subtitle = "Você está aberto a trocar este item por outro?",
            checked = isAvailableForTrade,
            onCheckedChange = onTradeToggle
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        ToggleOption(
            title = "Produto novo",
            subtitle = "O item nunca foi usado e está na etiqueta/caixa?",
            checked = isNeverUsed,
            onCheckedChange = onNeverUsedToggle
        )
    }
}

@Composable
fun CreateItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
        minLines = minLines,
        maxLines = maxLines
    )
}

@Composable
fun ToggleOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun ReviewStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tudo pronto!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Seu item está prestes a entrar na comunidade. Revise as informações e publique para começar a receber propostas de troca.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BottomActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
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
