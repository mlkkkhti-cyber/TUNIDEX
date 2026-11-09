package com.example.model

data class SpotQuest(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val progress: Int,
    val target: Int,
    val isCompleted: Boolean = progress >= target
)

data class SpotterBadge(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean
)

data class QuizQuestion(
    val id: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val xpBonus: Int = 100
)

object QuizRepository {
    val questions = listOf(
        QuizQuestion(
            id = 1,
            prompt = "Which car earned the nickname 'Godzilla' after crushing the competition in Australian Touring Cars?",
            options = listOf("Toyota Supra MK4", "Nissan Skyline GT-R (R32/R34)", "Mazda RX-7 FD", "Honda NSX-R"),
            correctIndex = 1,
            explanation = "Wheels magazine first dubbed the Nissan Skyline GT-R 'Godzilla' in 1989 for its unbeatable AWD RB26 dominance."
        ),
        QuizQuestion(
            id = 2,
            prompt = "What legendary material lines the engine bay of the McLaren F1 to deflect exhaust heat?",
            options = listOf("Titanium weave", "Pure 24-Karat Gold foil", "Kevlar aerogel", "Silver leaf"),
            correctIndex = 1,
            explanation = "Gordon Murray selected 16 grams of pure gold foil for its peerless heat reflectivity around the BMW V12."
        ),
        QuizQuestion(
            id = 3,
            prompt = "What is the redline limit of the naturally aspirated 4.0L Flat-6 inside the Porsche 911 GT3 RS (992)?",
            options = listOf("7,500 RPM", "8,200 RPM", "9,000 RPM", "10,000 RPM"),
            correctIndex = 2,
            explanation = "The 992 GT3 RS screams all the way to 9,000 RPM using rigid valve train technology from Porsche motorsport."
        ),
        QuizQuestion(
            id = 4,
            prompt = "Which was the very last road car personally approved by founder Enzo Ferrari before his death?",
            options = listOf("Ferrari Testarossa", "Ferrari F40", "Ferrari 288 GTO", "Ferrari F50"),
            correctIndex = 1,
            explanation = "Enzo Ferrari personally launched the iconic twin-turbo 200-mph F40 in 1987 to celebrate Ferrari's 40th anniversary."
        ),
        QuizQuestion(
            id = 5,
            prompt = "What engine configuration powers the Bugatti Chiron Pur Sport?",
            options = listOf("6.5L Naturally Aspirated V12", "8.0L Quad-Turbo W16", "4.0L Twin-Turbo V8", "Twin-Motor Hybrid V10"),
            correctIndex = 1,
            explanation = "The Chiron Pur Sport utilizes Bugatti's monstrous 8.0L W16 quad-turbocharged powerplant making 1,479 horsepower."
        ),
        QuizQuestion(
            id = 6,
            prompt = "Why does the Mercedes-Benz 300 SL have its world-famous 'Gullwing' upward-opening doors?",
            options = listOf("Aerodynamic styling trend", "Tubular spaceframe chassis prevented traditional sills", "Weight reduction experiment", "Luggage access requirement"),
            correctIndex = 1,
            explanation = "Its high tubular spaceframe sides left no room for conventional doors, necessitating roof-hinged gullwing doors."
        )
    )
}
