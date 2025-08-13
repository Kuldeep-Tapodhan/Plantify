package com.example.plantify.data

// A simple data class to hold treatment information
data class Treatment(
    val pesticide: String,
    val guide: String
)

// A repository object to act as our simple database
object TreatmentRepository {
    private val treatments = mapOf(
        "Apple Scab" to Treatment(
            "Myclobutanil or Captan",
            "1. Prune and destroy infected leaves and fruit.\n2. Apply fungicide from green tip until mid-summer.\n3. Rake and dispose of fallen leaves in autumn."
        ),
        "Tomato Late Blight" to Treatment(
            "Chlorothalonil or Copper-based fungicides",
            "1. Ensure good air circulation.\n2. Water at the base of the plant, avoiding the leaves.\n3. Apply fungicide preventatively, especially in cool, wet weather."
        ),
        "Grape Esca (Black Measles)" to Treatment(
            "Liquid lime-sulfur or Copper-based sprays",
            "1. Prune out dead spurs and cordons.\n2. Apply a dormant spray during the winter.\n3. Ensure good vine nutrition to improve resilience."
        ),
        // Add other disease treatments here...
        "Default" to Treatment(
            "General purpose fungicide/pesticide may be effective.",
            "1. Isolate the affected plant to prevent spread.\n2. Remove and destroy visibly affected parts.\n3. Improve air circulation and ensure proper watering."
        )
    )

    fun getTreatment(diseaseName: String): Treatment {
        return treatments[diseaseName] ?: treatments["Default"]!!
    }
}