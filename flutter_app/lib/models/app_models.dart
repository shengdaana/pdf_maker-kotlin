enum AppTheme {
  purple('Royal Purple', 0xFF6750A4),
  lightGreen('Light Green', 0xFF2E7D32),
  pink('Vibrant Pink', 0xFFC2185B),
  cobaltBlue('Cobalt Blue', 0xFF1976D2),
  highContrastDark('High Contrast Dark', 0xFFFBC02D);

  final String displayName;
  final int primaryColorValue;
  const AppTheme(this.displayName, this.primaryColorValue);
}

enum AppLanguage {
  english('English'),
  hindi('हिंदी (Hindi)'),
  hinglish('Hinglish');

  final String displayName;
  const AppLanguage(this.displayName);
}

enum PdfQuality {
  standard(
    'Standard Quality',
    'Compressed for fast sharing on WhatsApp & email (Recommended)',
  ),
  original(
    'Original Quality',
    'Full image resolution with minimal compression',
  ),
  alwaysAsk(
    'Always Ask',
    'Show quality choices every time you generate a PDF',
  );

  final String displayName;
  final String description;
  const PdfQuality(this.displayName, this.description);
}

enum PageMarginOption {
  bordered(
    'Bordered (Fit Page)',
    'Clean margins around photos so no document edges are cut off',
  ),
  fullBleed(
    'Full-Bleed (Fill Page)',
    'Photos expand to fill the entire page with zero margins',
  );

  final String displayName;
  final String description;
  const PageMarginOption(this.displayName, this.description);
}

class AppSettings {
  final AppTheme theme;
  final AppLanguage language;
  final PdfQuality defaultQuality;
  final PageMarginOption pageMargin;
  final bool highContrastMode;
  final bool simplifiedMode;
  final bool enableMergePages;
  final bool autoCropOnImport;

  const AppSettings({
    this.theme = AppTheme.purple,
    this.language = AppLanguage.english,
    this.defaultQuality = PdfQuality.standard,
    this.pageMargin = PageMarginOption.bordered,
    this.highContrastMode = false,
    this.simplifiedMode = false,
    this.enableMergePages = false,
    this.autoCropOnImport = false,
  });

  AppSettings copyWith({
    AppTheme? theme,
    AppLanguage? language,
    PdfQuality? defaultQuality,
    PageMarginOption? pageMargin,
    bool? highContrastMode,
    bool? simplifiedMode,
    bool? enableMergePages,
    bool? autoCropOnImport,
  }) {
    return AppSettings(
      theme: theme ?? this.theme,
      language: language ?? this.language,
      defaultQuality: defaultQuality ?? this.defaultQuality,
      pageMargin: pageMargin ?? this.pageMargin,
      highContrastMode: highContrastMode ?? this.highContrastMode,
      simplifiedMode: simplifiedMode ?? this.simplifiedMode,
      enableMergePages: enableMergePages ?? this.enableMergePages,
      autoCropOnImport: autoCropOnImport ?? this.autoCropOnImport,
    );
  }
}

class ImagePage {
  final String id;
  final String path;
  int rotationDegrees;
  final bool isMerged;
  final List<ImagePage>? originalPages;

  ImagePage({
    required this.id,
    required this.path,
    this.rotationDegrees = 0,
    this.isMerged = false,
    this.originalPages,
  });

  ImagePage copyWith({
    String? id,
    String? path,
    int? rotationDegrees,
    bool? isMerged,
    List<ImagePage>? originalPages,
  }) {
    return ImagePage(
      id: id ?? this.id,
      path: path ?? this.path,
      rotationDegrees: rotationDegrees ?? this.rotationDegrees,
      isMerged: isMerged ?? this.isMerged,
      originalPages: originalPages ?? this.originalPages,
    );
  }
}

class GeneratedPdfItem {
  final String title;
  final String path;
  final int byteSize;
  final DateTime createdDate;
  final int pageCount;

  GeneratedPdfItem({
    required this.title,
    required this.path,
    required this.byteSize,
    required this.createdDate,
    required this.pageCount,
  });

  String get formattedSize {
    if (byteSize < 1024) return '$byteSize B';
    if (byteSize < 1024 * 1024) {
      return '${(byteSize / 1024).toStringAsFixed(1)} KB';
    }
    return '${(byteSize / (1024 * 1024)).toStringAsFixed(1)} MB';
  }
}
