import 'package:share_plus/share_plus.dart';
import '../models/app_models.dart';

class ShareService {
  /// Opens Android native share sheet to send PDF to WhatsApp, Email, Drive, etc.
  /// Zero external SDKs, zero internet permission required.
  static Future<void> sharePdf(GeneratedPdfItem pdf) async {
    final xFile = XFile(pdf.path, mimeType: 'application/pdf');
    await Share.shareXFiles(
      [xFile],
      text: 'Here is your PDF document created with pdf_maker',
      subject: pdf.title,
    );
  }
}
