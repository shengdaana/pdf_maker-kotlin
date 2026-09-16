import 'package:flutter/material.dart';
import '../models/app_models.dart';
import '../services/pdf_service.dart';
import '../services/share_service.dart';

class GeneratedPdfsScreen extends StatefulWidget {
  final List<GeneratedPdfItem> pdfs;
  final VoidCallback onRefresh;

  const GeneratedPdfsScreen({
    super.key,
    required this.pdfs,
    required this.onRefresh,
  });

  @override
  State<GeneratedPdfsScreen> createState() => _GeneratedPdfsScreenState();
}

class _GeneratedPdfsScreenState extends State<GeneratedPdfsScreen> {
  late List<GeneratedPdfItem> _list;

  @override
  void initState() {
    super.initState();
    _list = List.from(widget.pdfs);
  }

  Future<void> _refresh() async {
    final updated = await PdfService.loadSavedPdfs();
    setState(() {
      _list = updated;
    });
    widget.onRefresh();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Saved PDF Documents'),
      ),
      body: _list.isEmpty
          ? const Center(child: Text('No saved PDFs found'))
          : RefreshIndicator(
              onRefresh: _refresh,
              child: ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: _list.length,
                separatorBuilder: (_, __) => const SizedBox(height: 8),
                itemBuilder: (context, index) {
                  final item = _list[index];
                  return Card(
                    shape: RoundedCornerShape(12),
                    child: ListTile(
                      leading: const CircleAvatar(
                        backgroundColor: Color(0xFFE0F2FE),
                        child: Icon(
                          Icons.picture_as_pdf,
                          color: Color(0xFF006686),
                        ),
                      ),
                      title: Text(
                        item.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      subtitle: Text(
                        '${item.formattedSize} • ${item.createdDate.toString().split('.').first}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      trailing: IconButton(
                        icon: const Icon(Icons.share, color: Color(0xFF006686)),
                        onPressed: () => ShareService.sharePdf(item),
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}
