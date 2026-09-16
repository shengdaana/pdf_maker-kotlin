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
    'Bordered (Fit A4)',
    'Clean margins around photos so no document edges are cut off',
  ),
  fullBleed(
    'Full-Bleed (Fill Page)',
    'Photos expand to fill the entire A4 page with zero margins',
  );

  final String displayName;
  final String description;
  const PageMarginOption(this.displayName, this.description);
}

class AppSettings {
  final PdfQuality defaultQuality;
  final PageMarginOption pageMargin;
  final bool highContrastMode;
  final bool simplifiedMode;

  const AppSettings({
    this.defaultQuality = PdfQuality.alwaysAsk,
    this.pageMargin = PageMarginOption.bordered,
    this.highContrastMode = false,
    this.simplifiedMode = false,
  });

  AppSettings copyWith({
    PdfQuality? defaultQuality,
    PageMarginOption? pageMargin,
    bool? highContrastMode,
    bool? simplifiedMode,
  }) {
    return AppSettings(
      defaultQuality: defaultQuality ?? this.defaultQuality,
      pageMargin: pageMargin ?? this.pageMargin,
      highContrastMode: highContrastMode ?? this.highContrastMode,
      simplifiedMode: simplifiedMode ?? this.simplifiedMode,
    );
  }
}

class ImagePage {
  final String id;
  final String path;
  int rotationDegrees;

  ImagePage({
    required this.id,
    required this.path,
    this.rotationDegrees = 0,
  });
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
