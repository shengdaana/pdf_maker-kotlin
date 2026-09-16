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
          // Compression Quality Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Default PDF Quality',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  for (final q in PdfQuality.values)
                    RadioListTile<PdfQuality>(
                      value: q,
                      groupValue: _current.defaultQuality,
                      title: Text(q.displayName),
                      subtitle: Text(q.description, style: const TextStyle(fontSize: 12)),
                      onChanged: (val) {
                        if (val != null) {
                          _update(_current.copyWith(defaultQuality: val));
                        }
                      },
                    ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // Page Margins Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Page Framing',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  for (final m in PageMarginOption.values)
                    RadioListTile<PageMarginOption>(
                      value: m,
                      groupValue: _current.pageMargin,
                      title: Text(m.displayName),
                      subtitle: Text(m.description, style: const TextStyle(fontSize: 12)),
                      onChanged: (val) {
                        if (val != null) {
                          _update(_current.copyWith(pageMargin: val));
                        }
                      },
                    ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          // Elder Accessibility Toggles
          Card(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('High Contrast Mode'),
                  subtitle: const Text('Bold borders and high visibility for elders'),
                  value: _current.highContrastMode,
                  onChanged: (val) {
                    _update(_current.copyWith(highContrastMode: val));
                  },
                ),
                const Divider(),
                SwitchListTile(
                  title: const Text('Simplified Mode'),
                  subtitle: const Text('Hide extra options and provide direct 1-tap PDF flow'),
                  value: _current.simplifiedMode,
                  onChanged: (val) {
                    _update(_current.copyWith(simplifiedMode: val));
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
