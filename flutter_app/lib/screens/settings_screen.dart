import 'package:flutter/material.dart';
import '../models/app_models.dart';

class SettingsScreen extends StatefulWidget {
  final AppSettings settings;
  final ValueChanged<AppSettings> onUpdateSettings;

  const SettingsScreen({
    super.key,
    required this.settings,
    required this.onUpdateSettings,
  });

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late AppSettings _current;

  @override
  void initState() {
    super.initState();
    _current = widget.settings;
  }

  void _update(AppSettings newSettings) {
    setState(() => _current = newSettings);
    widget.onUpdateSettings(newSettings);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings & Privacy'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // 1. Theme Dropdown
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '1. App Theme',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Choose from 5 modern color styles',
                    style: TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<AppTheme>(
                    value: _current.theme,
                    decoration: InputDecoration(
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    items: AppTheme.values.map((theme) {
                      return DropdownMenuItem<AppTheme>(
                        value: theme,
                        child: Row(
                          children: [
                            Container(
                              width: 18,
                              height: 18,
                              decoration: BoxDecoration(
                                color: Color(theme.primaryColorValue),
                                shape: BoxShape.circle,
                              ),
                            ),
                            const SizedBox(width: 12),
                            Text(theme.displayName),
                          ],
                        ),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) _update(_current.copyWith(theme: val));
                    },
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // 2. Language Dropdown
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '2. Language',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'English, Everyday Hindi, and Hinglish',
                    style: TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<AppLanguage>(
                    value: _current.language,
                    decoration: InputDecoration(
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    items: AppLanguage.values.map((lang) {
                      return DropdownMenuItem<AppLanguage>(
                        value: lang,
                        child: Row(
                          children: [
                            const Icon(Icons.language, size: 20, color: Colors.blueGrey),
                            const SizedBox(width: 12),
                            Text(lang.displayName),
                          ],
                        ),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) _update(_current.copyWith(language: val));
                    },
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // 3. Page Layout Mode Dropdown
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '3. Page Framing',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Choose how photos map to PDF pages',
                    style: TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<PageMarginOption>(
                    value: _current.pageMargin,
                    decoration: InputDecoration(
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    items: PageMarginOption.values.map((m) {
                      return DropdownMenuItem<PageMarginOption>(
                        value: m,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(m.displayName, style: const TextStyle(fontWeight: FontWeight.w600)),
                          ],
                        ),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) _update(_current.copyWith(pageMargin: val));
                    },
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // 4. Default PDF Quality Preset Dropdown
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '4. Default PDF Quality',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Standard Quality downsamples for light PDF weight',
                    style: TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<PdfQuality>(
                    value: _current.defaultQuality,
                    decoration: InputDecoration(
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    items: PdfQuality.values.map((q) {
                      return DropdownMenuItem<PdfQuality>(
                        value: q,
                        child: Text(q.displayName, style: const TextStyle(fontWeight: FontWeight.w600)),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) _update(_current.copyWith(defaultQuality: val));
                    },
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // 5. Simplified Mode & Controls (with Merge Pages Toggle)
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.fromLTRB(16, 16, 16, 4),
                  child: Text(
                    '5. Simplified Mode & Controls',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                ),
                SwitchListTile(
                  title: const Text('Hide Reorder Arrows'),
                  subtitle: const Text('Fewer buttons on cards for a cleaner experience'),
                  value: _current.simplifiedMode,
                  onChanged: (val) {
                    _update(_current.copyWith(simplifiedMode: val));
                  },
                ),
                const Divider(),
                SwitchListTile(
                  title: const Text('Merge Pages Between Pages'),
                  subtitle: const Text('Combine consecutive photos onto a single 2-photo page'),
                  value: _current.enableMergePages,
                  onChanged: (val) {
                    _update(_current.copyWith(enableMergePages: val));
                  },
                ),
                const Divider(),
                SwitchListTile(
                  title: const Text('High Contrast Mode'),
                  subtitle: const Text('Bold borders and high visibility for elders'),
                  value: _current.highContrastMode,
                  onChanged: (val) {
                    _update(_current.copyWith(highContrastMode: val));
                  },
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // Privacy & Open Source Card (Zero Trackers Guarantee)
          Card(
            color: const Color(0xFFF0FDF4),
            shape: RoundedCornerShape(12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Row(
                    children: [
                      Icon(Icons.verified_user, color: Colors.green),
                      SizedBox(width: 8),
                      Text(
                        '100% Private & Open Source',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Colors.green,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    '• Zero internet permissions: Runs completely offline.\n'
                    '• Zero Google Play Services dependency.\n'
                    '• Zero trackers, zero telemetry, zero background probes.\n'
                    '• Your photos and PDFs never leave your device.',
                    style: TextStyle(fontSize: 13, height: 1.5),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'pdf_maker • Version 1.0 (Flutter/Dart)',
                    style: TextStyle(fontSize: 12, color: Colors.grey),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
