import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../models/app_models.dart';
import '../services/pdf_service.dart';
import '../services/share_service.dart';

class PreviewBuildScreen extends StatefulWidget {
  final List<ImagePage> initialPages;
  final AppSettings settings;
  final VoidCallback onPdfCreated;

  const PreviewBuildScreen({
    super.key,
    required this.initialPages,
    required this.settings,
    required this.onPdfCreated,
  });

  @override
  State<PreviewBuildScreen> createState() => _PreviewBuildScreenState();
}

class _PreviewBuildScreenState extends State<PreviewBuildScreen> {
  late List<ImagePage> _pages;
  bool _isGenerating = false;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _pages = List.from(widget.initialPages);
  }

  void _rotatePage(int index) {
    setState(() {
      _pages[index].rotationDegrees = (_pages[index].rotationDegrees + 90) % 360;
    });
  }

  void _removePage(int index) {
    if (_pages.length <= 1) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('At least 1 photo is required')),
      );
      return;
    }
    setState(() {
      _pages.removeAt(index);
    });
  }

  void _moveUp(int index) {
    if (index > 0) {
      setState(() {
        final item = _pages.removeAt(index);
        _pages.insert(index - 1, item);
      });
    }
  }

  void _moveDown(int index) {
    if (index < _pages.length - 1) {
      setState(() {
        final item = _pages.removeAt(index);
        _pages.insert(index + 1, item);
      });
    }
  }

  void _mergePages(int index) {
    if (index >= _pages.length - 1) return;
    setState(() {
      final p1 = _pages[index];
      final p2 = _pages[index + 1];
      final mergedPage = ImagePage(
        id: 'merged_${p1.id}_${p2.id}',
        path: p1.path,
        isMerged: true,
        originalPages: [p1, p2],
      );
      _pages.removeAt(index + 1);
      _pages[index] = mergedPage;
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Merged 2 photos onto a single PDF page!'),
        duration: Duration(seconds: 2),
      ),
    );
  }

  void _unmergePage(int index) {
    final page = _pages[index];
    if (!page.isMerged || page.originalPages == null || page.originalPages!.isEmpty) return;
    setState(() {
      final orig = page.originalPages!;
      _pages.removeAt(index);
      _pages.insertAll(index, orig);
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Page split back into separate individual pages.'),
        duration: Duration(seconds: 2),
      ),
    );
  }

  Future<void> _addMorePhotos() async {
    final List<XFile> picked = await _picker.pickMultiImage();
    if (picked.isNotEmpty) {
      setState(() {
        for (final x in picked) {
          _pages.add(ImagePage(
            id: UniqueKey().toString(),
            path: x.path,
          ));
        }
      });
    }
  }

  Future<void> _generatePdf() async {
    PdfQuality quality = widget.settings.defaultQuality;

    if (quality == PdfQuality.alwaysAsk) {
      final selected = await showModalBottomSheet<PdfQuality>(
        context: context,
        builder: (ctx) => SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Choose PDF Quality',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 12),
                ListTile(
                  leading: const Icon(Icons.flash_on, color: Colors.green),
                  title: const Text('Standard Quality (Fast & Small)'),
                  subtitle: const Text('Recommended for WhatsApp and Email'),
                  onTap: () => Navigator.pop(ctx, PdfQuality.standard),
                ),
                ListTile(
                  leading: const Icon(Icons.hd, color: Colors.blue),
                  title: const Text('Original Quality (Large File)'),
                  subtitle: const Text('Full image resolution'),
                  onTap: () => Navigator.pop(ctx, PdfQuality.original),
                ),
              ],
            ),
          ),
        ),
      );
      if (selected == null) return;
      quality = selected;
    }

    setState(() => _isGenerating = true);

    try {
      final generatedPdf = await PdfService.generatePdf(
        pages: _pages,
        quality: quality,
        marginOption: widget.settings.pageMargin,
      );

      widget.onPdfCreated();

      if (!mounted) return;
      setState(() => _isGenerating = false);

      // Show success bottom dialog with direct Share action
      showModalBottomSheet(
        context: context,
        isDismissible: false,
        shape: const RoundedCornerShape(top: 24),
        builder: (ctx) => SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Icon(Icons.check_circle, color: Colors.green, size: 56),
                const SizedBox(height: 12),
                const Text(
                  'PDF Ready!',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                Text(
                  'Saved to Documents • ${generatedPdf.formattedSize}',
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.grey),
                ),
                const SizedBox(height: 24),
                ElevatedButton.icon(
                  icon: const Icon(Icons.share),
                  label: const Text('Share to WhatsApp / Apps'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF006686),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  onPressed: () {
                    Navigator.pop(ctx);
                    ShareService.sharePdf(generatedPdf);
                  },
                ),
                const SizedBox(height: 8),
                TextButton(
                  onPressed: () {
                    Navigator.pop(ctx);
                    Navigator.pop(context); // back to home
                  },
                  child: const Text('Done'),
                ),
              ],
            ),
          ),
        ),
      );
    } catch (e) {
      if (mounted) {
        setState(() => _isGenerating = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to generate PDF: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Review Pages (${_pages.length})'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_photo_alternate),
            tooltip: 'Add more photos',
            onPressed: _addMorePhotos,
          ),
        ],
      ),
      body: _isGenerating
          ? const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('Building PDF on device...'),
                ],
              ),
            )
          : ListView.builder(
              key: const PageStorageKey<String>('preview_pages_list_storage'),
              padding: const EdgeInsets.all(16),
              itemCount: _pages.length,
              itemBuilder: (context, index) {
                final page = _pages[index];
                return Column(
                  children: [
                    Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Row(
                          children: [
                            // Page badge
                            Container(
                              width: 28,
                              height: 28,
                              decoration: const BoxDecoration(
                                color: Color(0xFF006686),
                                shape: BoxShape.circle,
                              ),
                              child: Center(
                                child: Text(
                                  '${index + 1}',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 10),

                            // Merged badge if merged
                            if (page.isMerged)
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                decoration: BoxDecoration(
                                  color: Colors.amber.shade100,
                                  borderRadius: BorderRadius.circular(8),
                                  border: Border.all(color: Colors.amber.shade700),
                                ),
                                child: Row(
                                  children: [
                                    Icon(Icons.merge_type, size: 14, color: Colors.amber.shade900),
                                    const SizedBox(width: 4),
                                    Text(
                                      'Merged (2 Photos)',
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.bold,
                                        color: Colors.amber.shade900,
                                      ),
                                    ),
                                  ],
                                ),
                              ),

                            const SizedBox(width: 8),

                            // Thumbnail with rotation
                            RotatedBox(
                              quarterTurns: page.rotationDegrees ~/ 90,
                              child: ClipRRect(
                                borderRadius: BorderRadius.circular(8),
                                child: Image.file(
                                  File(page.path),
                                  width: 56,
                                  height: 56,
                                  fit: BoxFit.cover,
                                ),
                              ),
                            ),
                            const Spacer(),

                            // If merged, show Split button
                            if (page.isMerged && page.originalPages != null)
                              TextButton.icon(
                                icon: const Icon(Icons.call_split, size: 16),
                                label: const Text('Split', style: TextStyle(fontSize: 12)),
                                onPressed: () => _unmergePage(index),
                              ),

                            // Actions: Rotate, Up, Down, Delete
                            IconButton(
                              icon: const Icon(Icons.rotate_right),
                              onPressed: () => _rotatePage(index),
                              tooltip: 'Rotate',
                            ),
                            if (!widget.settings.simplifiedMode) ...[
                              IconButton(
                                icon: const Icon(Icons.arrow_upward),
                                onPressed: index > 0 ? () => _moveUp(index) : null,
                                tooltip: 'Move Up',
                              ),
                              IconButton(
                                icon: const Icon(Icons.arrow_downward),
                                onPressed: index < _pages.length - 1
                                    ? () => _moveDown(index)
                                    : null,
                                tooltip: 'Move Down',
                              ),
                            ],
                            IconButton(
                              icon: const Icon(Icons.delete_outline, color: Colors.red),
                              onPressed: () => _removePage(index),
                              tooltip: 'Delete',
                            ),
                          ],
                        ),
                      ),
                    ),

                    // Merge pages between pages button if enabled
                    if (widget.settings.enableMergePages && index < _pages.length - 1)
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 4),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Expanded(child: Divider()),
                            const SizedBox(width: 8),
                            OutlinedButton.icon(
                              icon: const Icon(Icons.merge_type, size: 16),
                              label: Text(
                                'Merge Pages (${index + 1} + ${index + 2})',
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                              ),
                              style: OutlinedButton.styleFrom(
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                              ),
                              onPressed: () => _mergePages(index),
                            ),
                            const SizedBox(width: 8),
                            const Expanded(child: Divider()),
                          ],
                        ),
                      ),
                  ],
                );
              },
            ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF006686),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 16),
              shape: RoundedCornerShape(16),
            ),
            onPressed: _isGenerating ? null : _generatePdf,
            child: const Text(
              'Create PDF Now',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
        ),
      ),
    );
  }
}
