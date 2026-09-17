import 'dart:io';
import 'dart:typed_data';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:image/image.dart' as img;
import 'package:intl/intl.dart';
import '../models/app_models.dart';

class PdfService {
  /// Generates a PDF file locally from the selected images.
  /// Runs 100% on-device with zero internet connection or Google Play Services.
  static Future<GeneratedPdfItem> generatePdf({
    required List<ImagePage> pages,
    required PdfQuality quality,
    required PageMarginOption marginOption,
    String? customFileName,
  }) async {
    final pdf = pw.Document();

    for (final page in pages) {
      if (page.isMerged && page.originalPages != null && page.originalPages!.length >= 2) {
        final img1 = await _processImagePage(page.originalPages![0], quality);
        final img2 = await _processImagePage(page.originalPages![1], quality);
        if (img1 != null && img2 != null) {
          final margin = marginOption == PageMarginOption.bordered ? 24.0 : 0.0;
          pdf.addPage(
            pw.Page(
              pageFormat: PdfPageFormat.a4,
              margin: pw.EdgeInsets.all(margin),
              build: (pw.Context context) {
                return pw.Column(
                  children: [
                    pw.Expanded(
                      child: pw.Center(
                        child: pw.Image(img1, fit: pw.BoxFit.contain),
                      ),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.symmetric(vertical: 6),
                      child: pw.Divider(color: PdfColors.grey400, thickness: 0.8),
                    ),
                    pw.Expanded(
                      child: pw.Center(
                        child: pw.Image(img2, fit: pw.BoxFit.contain),
                      ),
                    ),
                  ],
                );
              },
            ),
          );
          continue;
        }
      }

      final pdfImage = await _processImagePage(page, quality);
      if (pdfImage != null) {
        // Margin calculation
        final margin = marginOption == PageMarginOption.bordered ? 24.0 : 0.0;

        pdf.addPage(
          pw.Page(
            pageFormat: PdfPageFormat.a4,
            margin: pw.EdgeInsets.all(margin),
            build: (pw.Context context) {
              return pw.Center(
                child: pw.Image(
                  pdfImage,
                  fit: marginOption == PageMarginOption.fullBleed
                      ? pw.BoxFit.cover
                      : pw.BoxFit.contain,
                ),
              );
            },
          ),
        );
      }
    }

    // Save to App Documents folder
    final outputDir = await getApplicationDocumentsDirectory();
    final pdfDir = Directory('${outputDir.path}/PDF documents(pdf_maker)');
    if (!await pdfDir.exists()) {
      await pdfDir.create(recursive: true);
    }

    final dateStr = DateFormat('yyyyMMdd_HHmmss').format(DateTime.now());
    final fileName = (customFileName != null && customFileName.trim().isNotEmpty)
        ? '${customFileName.trim()}.pdf'
        : 'Doc_$dateStr.pdf';

    final finalFile = File('${pdfDir.path}/$fileName');
    final pdfBytes = await pdf.save();
    await finalFile.writeAsBytes(pdfBytes);

    return GeneratedPdfItem(
      title: fileName,
      path: finalFile.path,
      byteSize: pdfBytes.length,
      createdDate: DateTime.now(),
      pageCount: pages.length,
    );
  }

  /// List saved PDF documents from local storage
  static Future<List<GeneratedPdfItem>> loadSavedPdfs() async {
    try {
      final outputDir = await getApplicationDocumentsDirectory();
      final pdfDir = Directory('${outputDir.path}/PDF documents(pdf_maker)');
      if (!await pdfDir.exists()) return [];

      final files = pdfDir.listSync();
      final List<GeneratedPdfItem> items = [];

      for (final f in files) {
        if (f is File && f.path.toLowerCase().endsWith('.pdf')) {
          final stat = f.statSync();
          final name = f.path.split('/').last;
          items.add(
            GeneratedPdfItem(
              title: name,
              path: f.path,
              byteSize: stat.size,
              createdDate: stat.modified,
              pageCount: 1, // Estimates or parsed count
            ),
          );
        }
      }

      items.sort((a, b) => b.createdDate.compareTo(a.createdDate));
      return items;
    } catch (_) {
      return [];
    }
  }

  static Future<pw.MemoryImage?> _processImagePage(ImagePage page, PdfQuality quality) async {
    final file = File(page.path);
    if (!await file.exists()) return null;
    final rawBytes = await file.readAsBytes();

    img.Image? decoded = img.decodeImage(rawBytes);
    if (decoded == null) return null;

    if (page.rotationDegrees != 0) {
      decoded = img.copyRotate(decoded, angle: page.rotationDegrees);
    }

    List<int> processedBytes;
    if (quality == PdfQuality.standard) {
      if (decoded.width > 1600 || decoded.height > 1600) {
        decoded = img.copyResize(
          decoded,
          width: decoded.width > decoded.height ? 1600 : null,
          height: decoded.height >= decoded.width ? 1600 : null,
        );
      }
      processedBytes = img.encodeJpg(decoded, quality: 80);
    } else {
      processedBytes = img.encodeJpg(decoded, quality: 95);
    }

    return pw.MemoryImage(Uint8List.fromList(processedBytes));
  }
}
