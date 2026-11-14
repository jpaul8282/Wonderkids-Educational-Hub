package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NaturalForestDark
import com.example.ui.theme.NaturalHoney
import com.example.ui.theme.NaturalOchre
import com.example.ui.theme.NaturalSage
import com.example.ui.theme.PlayfulBlue
import com.example.ui.theme.PlayfulCoral
import com.example.ui.theme.PlayfulGreen
import com.example.ui.theme.PlayfulIndigo
import com.example.ui.theme.PlayfulLime
import com.example.ui.theme.PlayfulOrange
import com.example.ui.theme.PlayfulPink
import com.example.ui.theme.PlayfulPurple
import com.example.ui.theme.PlayfulSky
import com.example.ui.theme.PlayfulTeal
import com.example.ui.theme.PlayfulYellow
import com.example.ui.theme.StarGold

data class AvatarOption(
    val id: String,
    val name: String,
    val title: String,
    val category: String,
    val emoji: String,
    val bgColor: Color,
    val description: String,
    val favoriteSubject: String
)

object AvatarHelper {
    val categories: List<Pair<String, String>> = listOf(
        "ALL" to "All Heroes 🌟",
        "ANIMALS" to "Safari & Animals 🦁",
        "SPACE" to "Space & Tech 🚀",
        "DINOS" to "Dinosaurs & Legends 🦖",
        "FANTASY" to "Magical & Mythic 🦄",
        "HEROES" to "Adventurers 🦸",
        "CREATIVE" to "Art & Science 🎨"
    )

    val avatars: List<AvatarOption> = listOf(
        // Safari & Animals
        AvatarOption(
            id = "lion",
            name = "Leo the Lion",
            title = "King of Courage",
            category = "ANIMALS",
            emoji = "🦁",
            bgColor = PlayfulOrange,
            description = "Bold and brave explorer who never gives up on tough math puzzles!",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "fox",
            name = "Sparky Fox",
            title = "Quick-Witted Detective",
            category = "ANIMALS",
            emoji = "🦊",
            bgColor = PlayfulCoral,
            description = "Sharp, clever problem-solver who loves spelling mysteries and riddles.",
            favoriteSubject = "Word Explorer 📚"
        ),
        AvatarOption(
            id = "owl",
            name = "Professor Hoot",
            title = "Master of Wisdom",
            category = "ANIMALS",
            emoji = "🦉",
            bgColor = PlayfulTeal,
            description = "Observant night scholar reading science encyclopedias under the stars.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "panda",
            name = "Bamboo Panda",
            title = "Zen Master",
            category = "ANIMALS",
            emoji = "🐼",
            bgColor = PlayfulGreen,
            description = "Calm and peaceful thinker with exceptional memory and focus skills.",
            favoriteSubject = "Memory Safari 🦁"
        ),
        AvatarOption(
            id = "cheetah",
            name = "Turbo Cheetah",
            title = "Speed Champion",
            category = "ANIMALS",
            emoji = "🐆",
            bgColor = NaturalHoney,
            description = "Lightning fast sprinter who solves quick arithmetic in record time!",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "koala",
            name = "Cozy Koala",
            title = "Nature Whisperer",
            category = "ANIMALS",
            emoji = "🐨",
            bgColor = PlayfulLime,
            description = "Gentle tree-climber who loves discovering fascinating facts about animals.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "bear",
            name = "Barney Bear",
            title = "Forest Giant",
            category = "ANIMALS",
            emoji = "🐻",
            bgColor = NaturalOchre,
            description = "Warm-hearted buddy who loves team games and sharing bonus stars.",
            favoriteSubject = "Memory Safari 🦁"
        ),
        AvatarOption(
            id = "rabbit",
            name = "Hop Hopper",
            title = "Springy Thinker",
            category = "ANIMALS",
            emoji = "🐰",
            bgColor = PlayfulPink,
            description = "Full of cheerful energy, bouncing eagerly between learning quests!",
            favoriteSubject = "Word Explorer 📚"
        ),

        // Space & Tech
        AvatarOption(
            id = "astronaut",
            name = "Star Voyager",
            title = "Cosmic Pioneer",
            category = "SPACE",
            emoji = "🚀",
            bgColor = PlayfulIndigo,
            description = "Pilots rocket ships through deep space to chart uncharted star clusters.",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "robot",
            name = "Beep Bot 3000",
            title = "Logic Computer",
            category = "SPACE",
            emoji = "🤖",
            bgColor = PlayfulBlue,
            description = "High-tech robot calculating formulas with speed and mechanical precision.",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "alien",
            name = "Zog the Martian",
            title = "Galaxy Friend",
            category = "SPACE",
            emoji = "👽",
            bgColor = PlayfulLime,
            description = "Friendly interplanetary traveler bringing fun extraterrestrial trivia.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "star",
            name = "Nova Spark",
            title = "Shining Wonder",
            category = "SPACE",
            emoji = "⭐",
            bgColor = StarGold,
            description = "Radiant cosmic star brightening every challenge with positive energy.",
            favoriteSubject = "Color & Shapes 🎨"
        ),
        AvatarOption(
            id = "satellite",
            name = "Orbit Scanner",
            title = "Space Radar",
            category = "SPACE",
            emoji = "🛰️",
            bgColor = PlayfulSky,
            description = "Beams scientific data and weather telemetry across the globe.",
            favoriteSubject = "Science & Nature 🔬"
        ),

        // Dinosaurs & Legends
        AvatarOption(
            id = "dino",
            name = "Rex the T-Rex",
            title = "Mighty Dinosaur",
            category = "DINOS",
            emoji = "🦖",
            bgColor = PlayfulGreen,
            description = "Fierce prehistoric dinosaur with a giant heart for conquering number puzzles.",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "stego",
            name = "Spiky Stego",
            title = "Shield Defender",
            category = "DINOS",
            emoji = "🦕",
            bgColor = NaturalSage,
            description = "Gentle Jurassic giant fascinated by prehistoric plants and fossils.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "dragon",
            name = "Blaze Dragon",
            title = "Fire Spark",
            category = "DINOS",
            emoji = "🐲",
            bgColor = PlayfulCoral,
            description = "Breathes fiery sparks of inspiration and cracks hard spelling riddles.",
            favoriteSubject = "Word Explorer 📚"
        ),
        AvatarOption(
            id = "baby_dino",
            name = "Hatch Dino",
            title = "Little Dino",
            category = "DINOS",
            emoji = "🐣",
            bgColor = PlayfulYellow,
            description = "Newly hatched dinosaur explorer eager to learn new words and shapes.",
            favoriteSubject = "Color & Shapes 🎨"
        ),

        // Magical & Mythic
        AvatarOption(
            id = "unicorn",
            name = "Rainbow Star",
            title = "Magical Dreamer",
            category = "FANTASY",
            emoji = "🦄",
            bgColor = PlayfulPink,
            description = "Sprinkles sparkling stardust and rainbow colors across geometric shapes.",
            favoriteSubject = "Color & Shapes 🎨"
        ),
        AvatarOption(
            id = "wizard",
            name = "Cosmic Wizard",
            title = "Spell Caster",
            category = "FANTASY",
            emoji = "🧙",
            bgColor = PlayfulPurple,
            description = "Casts powerful learning spells that reveal hidden answers and secret badges.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "mermaid",
            name = "Aqua Mermaid",
            title = "Ocean Princess",
            category = "FANTASY",
            emoji = "🧜",
            bgColor = PlayfulTeal,
            description = "Swims gracefully through vibrant coral reefs studying sea creatures.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "fairy",
            name = "Pixie Glow",
            title = "Forest Sprite",
            category = "FANTASY",
            emoji = "🧚",
            bgColor = PlayfulPink,
            description = "Flutters on shimmering wings, discovering magical patterns in nature.",
            favoriteSubject = "Color & Shapes 🎨"
        ),

        // Adventurers & Heroes
        AvatarOption(
            id = "superhero",
            name = "Caped Champion",
            title = "Defender of Truth",
            category = "HEROES",
            emoji = "🦸",
            bgColor = PlayfulCoral,
            description = "Flies high above the city to protect kindness, courage, and knowledge.",
            favoriteSubject = "Math Galaxy 🚀"
        ),
        AvatarOption(
            id = "ninja",
            name = "Swift Shadow",
            title = "Master of Agility",
            category = "HEROES",
            emoji = "🥷",
            bgColor = NaturalForestDark,
            description = "Moves silently with unwavering focus and sharp mental concentration.",
            favoriteSubject = "Memory Safari 🦁"
        ),
        AvatarOption(
            id = "explorer",
            name = "Safari Scout",
            title = "Jungle Tracker",
            category = "HEROES",
            emoji = "🧭",
            bgColor = NaturalOchre,
            description = "Equipped with binoculars and map, navigating wild uncharted lands.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "diver",
            name = "Deep Diver",
            title = "Submarine Explorer",
            category = "HEROES",
            emoji = "🤿",
            bgColor = PlayfulSky,
            description = "Explores the deepest marine trenches and encounters friendly sea giants.",
            favoriteSubject = "Science & Nature 🔬"
        ),

        // Art & Science Creatives
        AvatarOption(
            id = "artist",
            name = "Master Painter",
            title = "Color Maestro",
            category = "CREATIVE",
            emoji = "🎨",
            bgColor = PlayfulPurple,
            description = "Blends magical color palettes and solves geometric pattern puzzles.",
            favoriteSubject = "Color & Shapes 🎨"
        ),
        AvatarOption(
            id = "scientist",
            name = "Lab Wonder",
            title = "Atomic Genius",
            category = "CREATIVE",
            emoji = "🔬",
            bgColor = PlayfulTeal,
            description = "Experiments with test tubes, microscopes, and chemical reactions.",
            favoriteSubject = "Science & Nature 🔬"
        ),
        AvatarOption(
            id = "musician",
            name = "Melody Maker",
            title = "Rhythm Master",
            category = "CREATIVE",
            emoji = "🎵",
            bgColor = PlayfulIndigo,
            description = "Listens for harmonic patterns and spells words through musical notes.",
            favoriteSubject = "Word Explorer 📚"
        ),
        AvatarOption(
            id = "chef",
            name = "Cookie Crafter",
            title = "Pastry Prodigy",
            category = "CREATIVE",
            emoji = "🧁",
            bgColor = PlayfulOrange,
            description = "Bakes delicious fraction cookies and masters counting recipes!",
            favoriteSubject = "Math Galaxy 🚀"
        )
    )

    fun getAvatar(id: String): AvatarOption {
        return avatars.find { it.id.equals(id, ignoreCase = true) } ?: avatars.first()
    }
}

