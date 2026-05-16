package com.example.reusai.data.repository

import com.example.reusai.data.network.ItemResponse
import com.example.reusai.data.network.ReusaiApiService

data class ItemUIModel(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val distance: String,
    val rating: Double,
    val ownerName: String,
    val ownerPhotoUrl: String,
    val ownerRating: Double,
    val ownerPlatformTime: String,
    val ownerTradesCount: Int
)

class ItemRepository(private val apiService: ReusaiApiService) {
// Is missing: status, availableToChange
    // Mock data for now as requested
    private val mockItems = listOf(
        ItemUIModel(
            id = "1",
            title = "Cadeira de Escritório Ergonômica",
            category = "Casa",
            description = "Cadeira em ótimo estado, com ajuste de altura e encosto reclinável. Ideal para home office. Quase não foi usada.",
            imageUrl = "https://images.unsplash.com/photo-1505797149-43b0069ec26b?q=80&w=500",
            distance = "A 2.5 km",
            rating = 4.8,
            ownerName = "Ana Silva",
            ownerPhotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200",
            ownerRating = 4.8,
            ownerPlatformTime = "Na plataforma há 1 ano",
            ownerTradesCount = 15
        ),
        ItemUIModel(
            id = "2",
            title = "Livro de Cálculo - Stewart 8ª Ed.",
            category = "Livros",
            description = "Livro essencial para estudantes de engenharia e exatas. Em excelente estado de conservação.",
            imageUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=500",
            distance = "A 1.2 km",
            rating = 4.5,
            ownerName = "Carlos Oliveira",
            ownerPhotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200",
            ownerRating = 4.7,
            ownerPlatformTime = "Na plataforma há 2 anos",
            ownerTradesCount = 28
        ),
        ItemUIModel(
            id = "3",
            title = "Monitor 24 pol Full HD",
            category = "Eletrônicos",
            description = "Monitor LED Full HD 24 polegadas. Perfeito para trabalhar ou jogar. Entradas HDMI e VGA.",
            imageUrl = "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?q=80&w=500",
            distance = "A 4.0 km",
            rating = 4.8,
            ownerName = "Marcos Souza",
            ownerPhotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200",
            ownerRating = 4.9,
            ownerPlatformTime = "Na plataforma há 6 meses",
            ownerTradesCount = 8
        ),
        ItemUIModel(
            id = "4",
            title = "Violão Acústico Iniciante",
            category = "Casa",
            description = "Violão acústico ideal para quem está começando. Cordas de nylon, macio de tocar.",
            imageUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?q=80&w=500",
            distance = "Sua localização",
            rating = 4.9,
            ownerName = "Juliana Lima",
            ownerPhotoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200",
            ownerRating = 4.6,
            ownerPlatformTime = "Na plataforma há 3 anos",
            ownerTradesCount = 42
        )
    )

    suspend fun getExploreItems(): List<ItemUIModel> {
        // In a real scenario, we would call apiService.getItems() and map to UI model
        return mockItems
    }

    suspend fun getItemById(id: String): ItemUIModel? {
        return mockItems.find { it.id == id }
    }
}
