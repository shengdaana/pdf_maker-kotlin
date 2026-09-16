import 'package:flutter/material.dart';
import 'models/app_models.dart';
import 'screens/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const PdfMakerApp());
}

class PdfMakerApp extends StatefulWidget {
  const PdfMakerApp({super.key});

  @override
  State<PdfMakerApp> createState() => _PdfMakerAppState();
}

class _PdfMakerAppState extends State<PdfMakerApp> {
  AppSettings _settings = const AppSettings();

  void _updateSettings(AppSettings newSettings) {
    setState(() {
      _settings = newSettings;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'pdf_maker',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF006686),
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      home: HomeScreen(
        settings: _settings,
        onUpdateSettings: _updateSettings,
      ),
    );
  }
}
