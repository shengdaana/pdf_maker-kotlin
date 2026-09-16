import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../models/app_models.dart';
import '../services/pdf_service.dart';
import '../services/share_service.dart';
import 'preview_build_screen.dart';
import 'generated_pdfs_screen.dart';
import 'settings_screen.dart';

class HomeScreen extends StatefulWidget {
  final AppSettings settings;
  final ValueChanged<AppSettings> onUpdateSettings;

  const HomeScreen({
    super.key,
    required this.settings,
    required this.onUpdateSettings,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ImagePicker _picker = ImagePicker();
  List<GeneratedPdfItem> _recentPdfs = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadRecents();
  }

  Future<void> _loadRecents() async {
    final pdfs = await PdfService.loadSavedPdfs();
    if (mounted) {
      setState(() {
        _recentPdfs = pdfs;
        _isLoading = false;
      });
    }
  }

  Future<void> _pickPhotos() async {
    try {
      final List<XFile> picked = await _picker.pickMultiImage();
      if (picked.isNotEmpty && mounted) {
        final imagePages = picked.map((x) {
          return ImagePage(
            id: UniqueKey().toString(),
            path: x.path,
          );
        }).toList();

        await Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => PreviewBuildScreen(
              initialPages: imagePages,
              settings: widget.settings,
              onPdfCreated: () => _loadRecents(),
            ),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not open gallery: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final highContrast = widget.settings.highContrastMode;

    return Scaffold(
      backgroundColor: highContrast ? Colors.white : const Color(0xFFF9FAFB),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF006686),
                borderRadius: BorderRadius.circular(6),
              ),
              child: const Text(
                'PDF',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
            ),
            const SizedBox(width: 8),
            const Text(
              'pdf_maker',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 22,
                color: Colors.black87,
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings, color: Colors.black54),
            tooltip: 'Settings',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => SettingsScreen(
                    settings: widget.settings,
                    onUpdateSettings: widget.onUpdateSettings,
                  ),
                ),
              );
            },
          ),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 12),
              // Big elder-friendly Photo Picker Button
              InkWell(
                onTap: _pickPhotos,
                borderRadius: BorderRadius.circular(24),
                child: Container(
                  height: 160,
                  decoration: BoxDecoration(
                    color: const Color(0xFF006686),
                    borderRadius: BorderRadius.circular(24),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF006686).withOpacity(0.3),
                        blurRadius: 16,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.2),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.photo_library,
                          color: Colors.white,
                          size: 40,
                        ),
                      ),
                      const SizedBox(height: 12),
                      const Text(
                        'Select Photos',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Choose one or multiple pictures from gallery',
                        style: TextStyle(
                          color: Colors.white.withOpacity(0.9),
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 24),

              // Recent Documents Section Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Recent Documents',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.black87,
                    ),
                  ),
                  if (_recentPdfs.isNotEmpty)
                    TextButton.icon(
                      icon: const Icon(Icons.folder_open, size: 18),
                      label: const Text('View All'),
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => GeneratedPdfsScreen(
                              pdfs: _recentPdfs,
                              onRefresh: _loadRecents,
                            ),
                          ),
                        );
                      },
                    ),
                ],
              ),
              const SizedBox(height: 8),

              // List of recent PDFs or empty state
              Expanded(
                child: _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : _recentPdfs.isEmpty
                        ? _buildEmptyState()
                        : ListView.separated(
                            itemCount: _recentPdfs.take(5).length,
                            separatorBuilder: (_, __) =>
                                const SizedBox(height: 8),
                            itemBuilder: (context, index) {
                              final pdf = _recentPdfs[index];
                              return Card(
                                shape: RoundedCornerShape(12),
                                elevation: 0.5,
                                child: ListTile(
                                  leading: const CircleAvatar(
                                    backgroundColor: Color(0xFFE0F2FE),
                                    child: Icon(
                                      Icons.picture_as_pdf,
                                      color: Color(0xFF006686),
                                    ),
                                  ),
                                  title: Text(
                                    pdf.title,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  subtitle: Text(
                                    '${pdf.formattedSize} • Tap share to send',
                                    style: const TextStyle(fontSize: 12),
                                  ),
                                  trailing: IconButton(
                                    icon: const Icon(Icons.share,
                                        color: Color(0xFF006686)),
                                    onPressed: () =>
                                        ShareService.sharePdf(pdf),
                                  ),
                                ),
                              );
                            },
                          ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.picture_as_pdf_outlined,
            size: 64,
            color: Colors.grey.shade400,
          ),
          const SizedBox(height: 12),
          Text(
            'No PDFs created yet',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: Colors.grey.shade600,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Tap "Select Photos" above to turn pictures into a PDF',
            style: TextStyle(
              fontSize: 13,
              color: Colors.grey.shade500,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
