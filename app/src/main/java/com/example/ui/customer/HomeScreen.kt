package com.example.ui.customer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LegalDisclaimerCard
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberLight
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun HomeScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Banner with Large Call to Action
            HeroBanner(
                language = language,
                onApplyClick = { viewModel.openWizard("Personal Loan") },
                onCheckEligibilityClick = { viewModel.setCurrentTab("CALCULATOR") }
            )
        }

        item {
            // Quick 4 Primary Action Buttons
            QuickActionGrid(
                language = language,
                onApplyClick = { viewModel.openWizard("Personal Loan") },
                onCheckEligibilityClick = { viewModel.setCurrentTab("CALCULATOR") },
                onTrackClick = { viewModel.setCurrentTab("TRACK") },
                onTalkToAgentClick = { viewModel.setCurrentTab("SUPPORT") }
            )
        }

        item {
            // Loan Categories Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "ঋণের ক্যাটাগরি সমূহ" else "Loan Categories",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = if (language == AppLanguage.BENGALI) "সহজ ৫টি বিভাগ" else "5 Fast Options",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LoanCategoryList(
                language = language,
                onSelectCategory = { category ->
                    viewModel.openWizard(category)
                }
            )
        }

        item {
            // Mandatory Legal Disclaimer
            LegalDisclaimerCard(language = language)
        }

        item {
            // Partner Banks & NBFCs section (Transparent facilitation)
            PartnerTrustCard(language = language)
        }

        item {
            // Helpline & First-time smartphone assistance
            AssistanceHelplineCard(
                language = language,
                onTalkToAgent = { viewModel.setCurrentTab("SUPPORT") }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroBanner(
    language: AppLanguage,
    onApplyClick: () -> Unit,
    onCheckEligibilityClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = EmeraldPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "বিশ্বস্ত লোন সহায়তা" else "Trusted DSA Partner",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "100% Paperless Process",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.get("tagline", language),
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = AppStrings.get("sub_tagline", language),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onApplyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("apply_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.get("apply_now", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onCheckEligibilityClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("check_eligibility_button")
                    ) {
                        Text(
                            text = AppStrings.get("check_eligibility", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionGrid(
    language: AppLanguage,
    onApplyClick: () -> Unit,
    onCheckEligibilityClick: () -> Unit,
    onTrackClick: () -> Unit,
    onTalkToAgentClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            title = AppStrings.get("apply_now", language),
            subtitle = if (language == AppLanguage.BENGALI) "৩ মিনিটে ফর্ম" else "3 Min Flow",
            icon = Icons.Default.FlashOn,
            bgColor = Color(0xFFEFF6FF),
            iconColor = NavyPrimary,
            modifier = Modifier.weight(1f),
            onClick = onApplyClick
        )

        QuickActionButton(
            title = AppStrings.get("track_application", language),
            subtitle = if (language == AppLanguage.BENGALI) "লাইভ স্ট্যাটাস" else "Live Tracker",
            icon = Icons.Default.Search,
            bgColor = Color(0xFFF0FDF4),
            iconColor = EmeraldPrimary,
            modifier = Modifier.weight(1f),
            onClick = onTrackClick
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            title = AppStrings.get("check_eligibility", language),
            subtitle = if (language == AppLanguage.BENGALI) "যোগ্যতা যাচাই" else "Check Limit",
            icon = Icons.Default.Calculate,
            bgColor = Color(0xFFFFFBEB),
            iconColor = Color(0xFFD97706),
            modifier = Modifier.weight(1f),
            onClick = onCheckEligibilityClick
        )

        QuickActionButton(
            title = AppStrings.get("talk_to_agent", language),
            subtitle = if (language == AppLanguage.BENGALI) "সরাসরি ফোন" else "Direct Help",
            icon = Icons.Default.HeadsetMic,
            bgColor = Color(0xFFFAF5FF),
            iconColor = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f),
            onClick = onTalkToAgentClick
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun LoanCategoryList(
    language: AppLanguage,
    onSelectCategory: (String) -> Unit
) {
    val categories = listOf(
        CategoryItem("Personal Loan", AppStrings.get("cat_personal", language), Icons.Default.Person, "₹25,000 - ₹15,00,000", Color(0xFF3B82F6)),
        CategoryItem("Business Loan", AppStrings.get("cat_business", language), Icons.Default.Storefront, "₹1,00,000 - ₹50,00,000", Color(0xFF10B981)),
        CategoryItem("Emergency Loan", AppStrings.get("cat_emergency", language), Icons.Default.LocalHospital, "₹10,000 - ₹1,00,000", Color(0xFFEF4444)),
        CategoryItem("Two Wheeler Loan", AppStrings.get("cat_twowheeler", language), Icons.Default.DirectionsBike, "₹20,000 - ₹2,50,000", Color(0xFFF59E0B)),
        CategoryItem("Other Loan", AppStrings.get("cat_other", language), Icons.Default.AccountBalance, "Home / Education / Gold", Color(0xFF8B5CF6))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectCategory(item.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = item.range,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "আবেদন" else "Apply",
                                color = NavyPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val range: String,
    val accentColor: Color
)

@Composable
private fun PartnerTrustCard(language: AppLanguage) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI) "অনুমোদিত ঋণদাতা অংশীদার" else "Authorised Lending Partners",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (language == AppLanguage.BENGALI)
                    "আমরা আরবিআই লাইসেন্সপ্রাপ্ত ব্যাংক এবং এনবিএফসি প্রতিষ্ঠানের সাথে ঋণ সমন্বয় করি (যেমন HDFC Bank, Bajaj Finance ইত্যাদি)। ঋণ অনুমোদন এবং বিতরণ সরাসরি পার্টনার ব্যাংকের মাধ্যমে সম্পন্ন হয়।"
                else
                    "We facilitate applications with RBI-regulated Scheduled Commercial Banks and NBFCs (e.g. HDFC Bank, Bajaj Finance). KYC, approval, and disbursement are directly processed by authorized lending partners.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun AssistanceHelplineCard(
    language: AppLanguage,
    onTalkToAgent: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HeadsetMic,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "ফর্ম পূরণে সাহায্য লাগবে?" else "Need help filling form?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (language == AppLanguage.BENGALI) "আমাদের ফ্রি হেল্পলাইনে যোগাযোগ করুন" else "Free helpline: 1800-123-LOAN",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Button(
                onClick = onTalkToAgent,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "সহায়তা" else "Help",
                    fontSize = 12.sp
                )
            }
        }
    }
}
