package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuizRepository
import com.example.model.SpotQuest
import com.example.model.SpotterBadge
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.RevOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboCyan
import com.example.ui.viewmodel.TunidexViewModel

@Composable
fun QuestsAndQuizScreen(
    viewModel: TunidexViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val quests by viewModel.quests.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val quizAnswers by viewModel.quizAnswers.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(16.dp)
    ) {
        // Tab Row (Quests, Badges, Quiz)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonCard,
            contentColor = TurboCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TurboCyan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Quests", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_quests")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Badges", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_badges")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Quiz", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_quiz")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> QuestsTabContent(quests = quests)
            1 -> BadgesTabContent(badges = badges)
            2 -> QuizTabContent(
                quizAnswers = quizAnswers,
                quizScore = quizScore,
                onAnswerQuestion = { q, ansIdx -> viewModel.answerQuizQuestion(q, ansIdx) },
                onResetQuiz = { viewModel.resetQuiz() }
            )
        }
    }
}

@Composable
private fun QuestsTabContent(quests: List<SpotQuest>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(quests, key = { it.id }) { quest ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (quest.isCompleted) Color(0xFF10B981) else CarbonBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = quest.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+${quest.xpReward} XP", color = RevOrange, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = quest.description, color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { quest.progress / quest.target.toFloat() },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (quest.isCompleted) Color(0xFF10B981) else TurboCyan,
                            trackColor = CarbonDark
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (quest.isCompleted) "COMPLETED" else "${quest.progress}/${quest.target}",
                            color = if (quest.isCompleted) Color(0xFF34D399) else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesTabContent(badges: List<SpotterBadge>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(badges, key = { it.id }) { badge ->
            val unlocked = badge.isUnlocked
            val accentColor = if (unlocked) RevOrange else TextMuted

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (unlocked) RevOrange.copy(alpha = 0.6f) else CarbonBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (unlocked) RevOrange.copy(alpha = 0.2f) else CarbonDark)
                            .border(1.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = badge.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = badge.category.uppercase(),
                                color = TurboCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = badge.description, color = TextSecondary, fontSize = 12.sp)
                    }

                    if (unlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizTabContent(
    quizAnswers: Map<Int, Int>,
    quizScore: Int,
    onAnswerQuestion: (com.example.model.QuizQuestion, Int) -> Unit,
    onResetQuiz: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CarbonBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "CAR SPOTTER CHALLENGE", color = TurboCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(text = "Score: $quizScore / ${QuizRepository.questions.size} Correct", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+100 XP per correct automotive trivia answer", color = TextMuted, fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = onResetQuiz,
                        modifier = Modifier.testTag("quiz_reset_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Quiz", tint = TurboCyan)
                    }
                }
            }
        }

        items(QuizRepository.questions, key = { it.id }) { question ->
            val answeredIndex = quizAnswers[question.id]
            val hasAnswered = answeredIndex != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CarbonBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "QUESTION ${question.id}",
                        color = RevOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = question.prompt, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(10.dp))

                    question.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = answeredIndex == optIndex
                        val isCorrectOption = optIndex == question.correctIndex

                        val chipBg = when {
                            !hasAnswered -> CarbonDark
                            isCorrectOption -> Color(0xFF10B981).copy(alpha = 0.25f)
                            isSelected -> Color(0xFFFF1744).copy(alpha = 0.25f)
                            else -> CarbonDark
                        }
                        val chipBorder = when {
                            !hasAnswered -> CarbonBorder
                            isCorrectOption -> Color(0xFF10B981)
                            isSelected -> Color(0xFFFF1744)
                            else -> CarbonBorder
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                .clickable(enabled = !hasAnswered) {
                                    onAnswerQuestion(question, optIndex)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = optionText,
                                color = if (hasAnswered && isCorrectOption) Color(0xFF34D399) else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || (hasAnswered && isCorrectOption)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    if (hasAnswered) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = question.explanation,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
