package com.example.util

import com.example.model.AppLanguage

object AppStrings {

  fun get(language: AppLanguage, key: String): String {
    return when (language) {
      AppLanguage.HINDI -> hindiStrings[key] ?: englishStrings[key] ?: key
      AppLanguage.HINGLISH -> hinglishStrings[key] ?: englishStrings[key] ?: key
      AppLanguage.ENGLISH -> englishStrings[key] ?: key
    }
  }

  private val englishStrings = mapOf(
    "app_name" to "pdf_maker",
    "home_banner_title" to "Convert Photos to PDF",
    "home_banner_desc" to "Select photos from your gallery to create high-quality PDF files to send on WhatsApp or save.",
    "select_from_gallery" to "Select From Gallery",
    "see_generated_pdfs" to "See Generated PDFs",
    "tip_share" to "Tip: You can also select photos in your phone Gallery and tap Share -> pdf_maker!",
    "settings_title" to "Settings & Preferences",
    "settings_subtitle" to "Personalize themes, language, and PDF defaults",
    "theme_title" to "Visual Theme",
    "language_title" to "App Language",
    "page_layout_title" to "PDF Page Sizing & Layout",
    "auto_crop_title" to "Auto-Crop on Import",
    "auto_crop_desc" to "Trims borders with a safety buffer margin",
    "compression_title" to "PDF Quality Preset",
    "save_folder_title" to "Default Save Folder",
    "reset_to_full_photo" to "Reset to Full Photo",
    "save_page" to "Save Page",
    "cancel" to "Cancel",
    "rotate_90" to "Rotate 90°",
    "free_crop_hint" to "Drag corners/edges on the photo to manually crop freely, or choose a ratio below",
    "create_pdf_button" to "Create PDF Now",
    "add_more_photos" to "Add Photos",
    "pages_count" to "Pages",
    "merge_pages_toggle_title" to "Merge Pages Between Pages",
    "merge_pages_toggle_desc" to "Show merge button between adjacent pages in preview tab",
    "merge_pages_btn" to "Merge with Next Page",
    "unmerge_page_btn" to "Split Pages",
    "pages_merged_success" to "Pages merged into 1 page",
    "merged_badge" to "Merged (2 Photos)"
  )

  private val hindiStrings = mapOf(
    "app_name" to "pdf_maker",
    "home_banner_title" to "फ़ोटो को PDF में बदलें",
    "home_banner_desc" to "अपनी गैलरी से फ़ोटो चुनें और बिना किसी परेशानी के WhatsApp पर भेजने या सहेजने के लिए साफ़ PDF बनाएं।",
    "select_from_gallery" to "गैलरी से फ़ोटो चुनें",
    "see_generated_pdfs" to "बनी हुई PDF फ़ाइलें देखें",
    "tip_share" to "सुझाव: आप अपनी गैलरी में फ़ोटो चुनकर सीधे Share -> pdf_maker भी कर सकते हैं!",
    "settings_title" to "सेटिंग्स और विकल्प",
    "settings_subtitle" to "थीम, भाषा और PDF लेआउट सेट करें",
    "theme_title" to "ऐप का रंग / थीम",
    "language_title" to "भाषा (Language)",
    "page_layout_title" to "PDF पेज का आकार (Layout)",
    "auto_crop_title" to "फ़ोटो लाते ही ऑटो-क्रॉप करें",
    "auto_crop_desc" to "कागज़ के किनारे सुरक्षित मार्जिन के साथ छांटे",
    "compression_title" to "PDF की क्वालिटी",
    "save_folder_title" to "फ़ाइल सहेजने का फ़ोल्डर",
    "reset_to_full_photo" to "पूरी फ़ोटो वापस लाएं (Reset)",
    "save_page" to "पेज सहेजें (Save)",
    "cancel" to "रद्द करें (Cancel)",
    "rotate_90" to "90° घुमाएं (Rotate)",
    "free_crop_hint" to "फ़ोटो पर अपनी उंगली से मनचाहा हिस्सा चुनें या नीचे से अनुपात चुनें",
    "create_pdf_button" to "PDF तैयार करें (Create PDF)",
    "add_more_photos" to "और फ़ोटो जोड़ें",
    "pages_count" to "पेज",
    "merge_pages_toggle_title" to "पेज जोड़ने का बटन (Merge Pages)",
    "merge_pages_toggle_desc" to "प्रिव्यू टैब में पेजों के बीच दो पेजों को एक में जोड़ने का बटन दिखाएं",
    "merge_pages_btn" to "अगले पेज के साथ जोड़ें (Merge)",
    "unmerge_page_btn" to "अलग करें (Split)",
    "pages_merged_success" to "पेज सफलता से जुड़ गए",
    "merged_badge" to "जुड़ा हुआ (2 फ़ोटो)"
  )

  private val hinglishStrings = mapOf(
    "app_name" to "pdf_maker",
    "home_banner_title" to "Photos ko PDF banayein",
    "home_banner_desc" to "Apni gallery se photos select karein aur WhatsApp pe share karne ya save karne ke liye badhiya PDF banayein.",
    "select_from_gallery" to "Gallery se Photos Chuniye",
    "see_generated_pdfs" to "Bani hui PDFs Dekhein",
    "tip_share" to "Tip: Aap phone gallery mein photos select karke sidha Share -> pdf_maker bhi kar sakte hain!",
    "settings_title" to "Settings aur Preferences",
    "settings_subtitle" to "Themes, language aur PDF layout customize karein",
    "theme_title" to "App Theme / Color",
    "language_title" to "Bhasha / Language",
    "page_layout_title" to "PDF Page Size aur Layout",
    "auto_crop_title" to "Auto-Crop on Import",
    "auto_crop_desc" to "Photo ke extra borders safety margin ke saath trim karein",
    "compression_title" to "PDF Quality Preset",
    "save_folder_title" to "Save Folder Location",
    "reset_to_full_photo" to "Reset to Full Photo",
    "save_page" to "Save Page",
    "cancel" to "Cancel",
    "rotate_90" to "90° Rotate Karein",
    "free_crop_hint" to "Photo ke kono ko drag karke free crop karein ya neeche se ratio select karein",
    "create_pdf_button" to "PDF Banayein",
    "add_more_photos" to "Aur Photos Add Karein",
    "pages_count" to "Pages",
    "merge_pages_toggle_title" to "Merge Pages Feature",
    "merge_pages_toggle_desc" to "Preview tab mein har page ke beech merge button show karein",
    "merge_pages_btn" to "Agla Page Merge Karein",
    "unmerge_page_btn" to "Alag Karein (Split)",
    "pages_merged_success" to "Pages ek saath merge ho gaye",
    "merged_badge" to "Merged (2 Photos)"
  )
}
