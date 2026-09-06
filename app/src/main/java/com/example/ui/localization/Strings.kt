package com.example.ui.localization

enum class AppLanguage {
    BENGALI,
    ENGLISH
}

object AppStrings {
    fun get(key: String, language: AppLanguage): String {
        val strings = if (language == AppLanguage.BENGALI) bengaliStrings else englishStrings
        return strings[key] ?: englishStrings[key] ?: key
    }

    private val bengaliStrings = mapOf(
        "app_title" to "[Quick Loan] Loans",
        "tagline" to "সহজে Loan-এর জন্য Apply করুন",
        "sub_tagline" to "বিশ্বস্ত ব্যাংক ও NBFC অংশীদারদের সাথে সহজ ঋণ সহায়তা",
        "apply_now" to "Loan Apply করুন",
        "check_eligibility" to "Eligibility Check করুন",
        "track_application" to "Application Track করুন",
        "talk_to_agent" to "Agent-এর সাথে কথা বলুন",
        "emi_calculator" to "EMI ক্যালকুলেটর",
        "disclaimer_title" to "আইনগত সতর্কবার্তা ও পলিসি",
        "disclaimer_body" to "এই অ্যাপ loan facilitation service প্রদান করে। Loan approval/disbursement সংশ্লিষ্ট authorised lender-এর eligibility, KYC, credit assessment এবং policy-এর উপর নির্ভরশীল। আমরা কোনো গ্যারান্টিযুক্ত লোন প্রদান করি না।",
        "lender_notice" to "আমরা সরাসরি আরবিআই লাইসেন্সপ্রাপ্ত ব্যাংক বা এনবিএফসি নই। আমরা একটি অনুমোদিত লোন সহায়তাকারী প্ল্যাটফর্ম (LSP/DSA)।",
        
        // Categories
        "cat_personal" to "ব্যক্তিগত ঋণ (Personal Loan)",
        "cat_business" to "ব্যবসা ঋণ (Business Loan)",
        "cat_emergency" to "জরুরি ঋণ (Emergency Loan)",
        "cat_twowheeler" to "টু-হুইলার ঋণ (Two Wheeler Loan)",
        "cat_other" to "অন্যান্য ঋণ (Other Loan)",
        
        // Navigation
        "nav_home" to "হোম",
        "nav_track" to "ট্র্যাক",
        "nav_calculator" to "ক্যালকুলেটর",
        "nav_portals" to "পোর্টাল",
        "nav_support" to "সাহায্য ও পলিসি",

        // Form Steps
        "step_1_title" to "প্রাথমিক তথ্য",
        "step_2_title" to "পেশা ও আয়",
        "step_3_title" to "লোনের পরিমাণ ও উদ্দেশ্য",
        "step_4_title" to "নথিপত্র আপলোড",
        "step_5_title" to "সম্মতি ও সাবমিট",
        "step_indicator" to "ধাপ %d / %d",

        // Field labels
        "full_name" to "সম্পূর্ণ নাম (প্যান কার্ড অনুযায়ী)",
        "mobile_number" to "মোবাইল নম্বর",
        "dob" to "জন্ম তারিখ (DD/MM/YYYY)",
        "city" to "শহর / গ্রাম",
        "state" to "রাজ্য",
        "employment_type" to "পেশার ধরন",
        "monthly_income" to "মাসিক মোট আয় (টাকায়)",
        "company_name" to "কোম্পানি / দোকানের নাম",
        "work_experience" to "কাজের অভিজ্ঞতা (বছর)",
        "loan_amount" to "প্রয়োজনীয় লোনের পরিমাণ",
        "loan_purpose" to "লোন নেওয়ার উদ্দেশ্য",
        "has_existing_loan" to "বর্তমানে অন্য কোনো লোন আছে কি?",
        "existing_emi" to "বর্তমান মাসিক মোট ইএমআই (যদি থাকে)",
        "yes" to "হ্যাঁ",
        "no" to "না",
        "pan_number" to "প্যান কার্ড নম্বর (ঐচ্ছিক/প্রয়োজনে)",
        "address_proof" to "ঠিকানার প্রমাণ (আধার/ভোটার/বিদ্যুৎ বিল)",
        "income_proof" to "আয়ের প্রমাণ (স্যালারি স্লিপ / ট্রেড লাইসেন্স)",
        "bank_statement" to "ব্যাংক স্টেটমেন্ট (গত ৩-৬ মাস)",
        "consent_text" to "আমি স্বেচ্ছায় স্বীকার করছি যে আমার প্রদত্ত তথ্য সত্য। আমি অনুমোদিত ব্যাংক/NBFC ঋণদাতা অংশীদারদের সাথে আমার আবেদন যাচাইয়ের জন্য তথ্য শেয়ার করার সম্মতি দিচ্ছি।",
        "submit_btn" to "আবেদন জমা দিন",
        "next_btn" to "পরবর্তী ধাপ",
        "prev_btn" to "আগের ধাপ",
        "otp_verification" to "মোবাইল ওটিপি যাচাই",
        "enter_otp" to "৪ সংখ্যার ওটিপি লিখুন (ডেমো: ১২৩৪)",
        "verify_otp" to "যাচাই করুন",
        "success_title" to "আবেদন সফলভাবে জমা হয়েছে!",
        "your_app_id" to "আপনার আবেদন নম্বর (Application ID):",
        "success_note" to "আমাদের অনুমোদিত লোন প্রতিনিধি শীঘ্রই আপনার সাথে যোগাযোগ করবেন।",

        // Tracking
        "enter_app_or_mobile" to "Application ID অথবা মোবাইল নম্বর লিখুন",
        "track_btn" to "স্ট্যাটাস দেখুন",
        "status_NEW" to "নতুন আবেদন (New)",
        "status_DOCUMENT_PENDING" to "নথিপত্র বাকি (Doc Pending)",
        "status_UNDER_REVIEW" to "পর্যালোচনাধীন (Under Review)",
        "status_SENT_TO_LENDER" to "ব্যাংকে পাঠানো হয়েছে (Sent to Lender)",
        "status_LENDER_REVIEW" to "ব্যাংক যাচাইকরণ চলছে (Lender Review)",
        "status_APPROVED" to "অনুমোদিত (Approved)",
        "status_REJECTED" to "বাতিল (Rejected)",
        "status_DISBURSED" to "টাকা বিতরণ সম্পন্ন (Disbursed)",
        "status_CLOSED" to "সম্পন্ন / বন্ধ (Closed)",

        // Eligibility
        "eligibility_title" to "লোন এলিজিবিলিটি চেকার",
        "eligibility_disclaimer" to "ফলাফল শুধুমাত্র প্রাথমিক সম্ভাব্যতা প্রকাশ করে। চূড়ান্ত অনুমোদন সংশ্লিষ্ট লোনদাতার ক্রেডিট পলিসির ওপর নির্ভরশীল।",
        "eligible_msg" to "আপনার আবেদনটি নির্বাচিত অংশীদারদের ঋণ সুবিধার জন্য যোগ্য হতে পারে।",
        "ineligible_msg" to "আরও বিস্তারিত তথ্য প্রয়োজন বা আয়ের সীমা পূরণ হয়নি।",

        // EMI Calculator
        "tenure_months" to "মেয়াদ (মাস)",
        "interest_rate" to "সুদের হার (% বার্ষিক)",
        "est_emi" to "আনুমানিক মাসিক EMI",
        "total_interest" to "মোট প্রদেয় সুদ",
        "total_payment" to "মোট পরিশোধ",
        "illustrative_only" to "এটি শুধুমাত্র একটি দৃষ্টান্তমূলক হিসাব। প্রকৃত হার লোনদাতার শর্তাধীন।",

        // Agent & Admin
        "agent_portal" to "এজেন্ট ড্যাশবোর্ড",
        "admin_portal" to "অ্যাডমিন প্যানেল",
        "customer_dashboard" to "কাস্টমার ড্যাশবোর্ড",
        "demo_mode" to "ডেমো মোড সক্রিয়"
    )

    private val englishStrings = mapOf(
        "app_title" to "[Quick Loan] Loans",
        "tagline" to "Apply for Loans Easily & Securely",
        "sub_tagline" to "Trusted Loan Facilitation Platform Partnered with Leading Banks & NBFCs",
        "apply_now" to "Apply Now",
        "check_eligibility" to "Check Eligibility",
        "track_application" to "Track Application",
        "talk_to_agent" to "Talk to an Agent",
        "emi_calculator" to "EMI Calculator",
        "disclaimer_title" to "Legal Disclaimer & Compliance",
        "disclaimer_body" to "This app provides loan facilitation and DSA/LSP assistance services. Loan approval, interest rates, KYC verification, and disbursement are strictly determined by the respective authorized bank/NBFC lender according to their credit policies. We do not promise guaranteed loan approvals.",
        "lender_notice" to "We are not a licensed direct lender or RBI-regulated entity ourselves. We operate as an authorized Loan Service Provider (LSP/DSA) facilitator.",
        
        // Categories
        "cat_personal" to "Personal Loan",
        "cat_business" to "Business Loan",
        "cat_emergency" to "Emergency Loan",
        "cat_twowheeler" to "Two Wheeler Loan",
        "cat_other" to "Other Loan",
        
        // Navigation
        "nav_home" to "Home",
        "nav_track" to "Track",
        "nav_calculator" to "Calculator",
        "nav_portals" to "Portals",
        "nav_support" to "Policy & Support",

        // Form Steps
        "step_1_title" to "Basic Details",
        "step_2_title" to "Income & Employment",
        "step_3_title" to "Loan Details & Purpose",
        "step_4_title" to "Document Upload",
        "step_5_title" to "Consent & Submit",
        "step_indicator" to "Step %d of %d",

        // Field labels
        "full_name" to "Full Name (as per PAN Card)",
        "mobile_number" to "Mobile Number",
        "dob" to "Date of Birth (DD/MM/YYYY)",
        "city" to "City / Town",
        "state" to "State",
        "employment_type" to "Employment Type",
        "monthly_income" to "Monthly Net Income (₹)",
        "company_name" to "Company / Business Name",
        "work_experience" to "Work Experience (Years)",
        "loan_amount" to "Requested Loan Amount (₹)",
        "loan_purpose" to "Purpose of Loan",
        "has_existing_loan" to "Do you have any existing loans?",
        "existing_emi" to "Existing Monthly EMI (₹, if any)",
        "yes" to "Yes",
        "no" to "No",
        "pan_number" to "PAN Card Number (when required)",
        "address_proof" to "Address Proof (Aadhaar / Voter / Bill)",
        "income_proof" to "Income Proof (Salary Slip / Trade License)",
        "bank_statement" to "Bank Statement (3-6 Months)",
        "consent_text" to "I hereby declare that the information provided is accurate. I explicitly give my consent to share my application details with authorized lending bank/NBFC partners for evaluation, KYC, and loan processing.",
        "submit_btn" to "Submit Application",
        "next_btn" to "Continue",
        "prev_btn" to "Previous",
        "otp_verification" to "Mobile OTP Verification",
        "enter_otp" to "Enter 4-Digit OTP (Demo: 1234)",
        "verify_otp" to "Verify OTP",
        "success_title" to "Application Submitted Successfully!",
        "your_app_id" to "Your Application ID:",
        "success_note" to "Our authorized loan assistance executive will review your details and contact you shortly.",

        // Tracking
        "enter_app_or_mobile" to "Enter Application ID or Mobile Number",
        "track_btn" to "Check Status",
        "status_NEW" to "New Application",
        "status_DOCUMENT_PENDING" to "Document Pending",
        "status_UNDER_REVIEW" to "Under Review",
        "status_SENT_TO_LENDER" to "Sent to Lender",
        "status_LENDER_REVIEW" to "Lender Review",
        "status_APPROVED" to "Approved",
        "status_REJECTED" to "Rejected",
        "status_DISBURSED" to "Disbursed",
        "status_CLOSED" to "Closed",

        // Eligibility
        "eligibility_title" to "Eligibility Checker",
        "eligibility_disclaimer" to "This indicates provisional eligibility only. Final approval is subject to lender verification and credit policy.",
        "eligible_msg" to "Your application may be eligible for selected loan products with partner lenders.",
        "ineligible_msg" to "More information is required or current criteria is not met.",

        // EMI Calculator
        "tenure_months" to "Tenure (Months)",
        "interest_rate" to "Interest Rate (% p.a.)",
        "est_emi" to "Estimated Monthly EMI",
        "total_interest" to "Total Interest Payable",
        "total_payment" to "Total Repayment Amount",
        "illustrative_only" to "Illustrative calculation only. Actual interest and terms depend on lender policy.",

        // Agent & Admin
        "agent_portal" to "Agent Dashboard",
        "admin_portal" to "Admin Panel",
        "customer_dashboard" to "Customer Dashboard",
        "demo_mode" to "Demo Mode Active"
    )
}
