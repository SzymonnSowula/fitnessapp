# Dziennik praktyk – Jakub Rogóż

**Projekt:** FitnessApp dla seniorów (Android)  
**Okres:** 9 kwietnia 2026 – 5 maja 2026  
**Technologie:** Java, Android SDK, Room, ONNX Runtime, Firebase (usunięte), TTS/Speech Recognition

---

## 9 kwietnia 2026 (czwartek)

Rozpocząłem prace nad projektem od przygotowania ekranu ustawień (`SettingsActivity`). Skonfigurowałem podstawowy widok z opcjami głosowymi oraz zaimplementowałem zapis preferencji w `SharedPreferences`. Dzień zakończyłem na etapie ustawienia struktury – przed wdrożeniem cięższych funkcjonalności.

---

## 10 kwietnia 2026 (piątek)

Wdrożyłem logikę Firebase Auth (logowanie e-mail/password). Niestety od razu pojawiły się problemy ze stabilnością autentykacji, więc spędziłem sporo czasu na debugowaniu i poprawkach. Dodałem też wbudowany ekran dziennika w samej aplikacji, zaimportowałem pliki zasobów (CSV, ONNX, metadata) oraz powiększyłem przyciski pod kątem seniorów. Pod koniec dnia doszedłem do wniosku, że ekran logowania w obecnym kształcie jest zbyt problematyczny – tymczasowo go wyłączyłem i skupiłem się na naprawie błędów ładowania.

---

## 14 kwietnia 2026 (wtorek)

Intensywny dzień pełen implementacji i żmudnego debugowania. Dodałem ekran pytań wstępnych (późniejszy onboarding), usunąłem niepotrzebne ekrany logowania i zaimportowałem model ONNX do folderu `assets`. Zaimplementowałem system rekomendacji ćwiczeń oraz pełną bazę ćwiczeń z podziałem na kategorie (`sila`, `kardio`, `mobilnosc`, `rownowaga`, `postura`, `mieszana`). Generowanie listy rekomendacji wielokrotnie się wysypywało – siedziałem nad tym dość długo, poprawiając kolejne błędy w algorytmie, przyciskach nawigacyjnych oraz crashach przy przejściach między ekranami. Poprawiłem też ogólny wygląd UI, żeby był bardziej przyjazny.

---

## 15 kwietnia 2026 (środa)

Skupiłem się na dwóch rzeczach: dodałem sekcję treningu umysłowego (mind training) oraz znacząco poprawiłem estetykę aplikacji. Kolory stały się bardziej wyraziste, a wszystkie elementy interfejsu zostały powiększone – to kluczowe dla grupy docelowej, czyli seniorów.

---

## 16 kwietnia 2026 (czwartek)

Rozszerzyłem moduł gier umysłowych o dodatkowe tytuły i system poziomów trudności. Każda gra musi mieć skalowalny poziom dla różnych użytkowników. Na koniec dnia napisałem pierwsze testy weryfikujące poprawność działania nowych komponentów.

---

## 17 kwietnia 2026 (piątek)

Dzień poświęcony niemal w całości na design i UX. Zaimplementowałem nowy wygląd ekranu onboardingu, a następnie przeniosłem spójny design na pozostałe ekrany aplikacji, w tym na ekrany mini-gier. Przeprowadziłem refaktoryzację interfejsu ćwiczeń – zastosowałem wyższy kontrast, uprościłem nawigację i dodałem zarządzanie przeciwwskazaniami w ustawieniach. Poprawiłem też wydajność kodu i dodałem przyciski wstecz.

---

## 20 kwietnia 2026 (poniedziałek)

Pracowałem nad kolejną grą – tym razem sortowanie kolorów (liquid sort). Uprościłem też system poziomów we wszystkich grach umysłowych, żeby był bardziej intuicyjny dla seniorów. Wieczorem poprawiłem drobne niedociągnięcia w designie i naprawiłem zgłoszone błędy.

---

## 23 kwietnia 2026 (czwartek)

Duży przełom – wdrożyłem pełen interfejs głosowy (voice interface). Użytkownik może teraz sterować aplikacją głosem, a aplikacja czyta mu treści na głos. Dodałem też samouczki (tutoriale) do gier oraz uprościłem ich interfejs. Zaktualizowałem dokumentację projektu i poprawiłem drobne błędy w TTS.

---

## 24 kwietnia 2026 (piątek)

Rozszerzyłem bibliotekę komend głosowych – użytkownik ma teraz więcej poleceń do dyspozycji. Poprawiłem też działanie onboardingu po ostatnich zmianach oraz wdrożyłem kolejne zaplanowane funkcje. W drugiej części dnia dodałem grę 2048 do zestawu gier umysłowych i naprawiłem kilka problemów wykrytych przy testowaniu.

---

## 27 kwietnia 2026 (poniedziałek)

Dzień na dokończenie zaplanowanych funkcjonalności z poprzednich sprintów. Implementowałem brakujące elementy logiki oraz naprawiałem niedociągnięcia wykryte podczas testowania aplikacji na urządzeniu.

---

## 28 kwietnia 2026 (wtorek)

Dzień porządków technicznych. Zdecydowałem się usunąć Firebase z projektu – okazało się, że wprowadza zbyt dużo komplikacji względem korzyści. Naprawiłem problem z logowaniem i poprawiłem algorytm oraz wygląd onboardingu. Następnie przeszedłem do solidnego czyszczenia kodu: cztery sprinty naprawcze, w których wyeliminowałem łącznie ponad 200 warningów Android Studio. Poprawiłem lokalizację (`DefaultLocale`), przydziały w `draw`, pętle w adapterach, zagnieżdżone wagi w layoutach, overdraw, nieużywane zasoby, tekst zakodowany na sztywno w XML, oraz podniosłem wersje bibliotek (ONNX, Room) z migracją do TOML. Projekt stał się znacznie czystszy.

---

## 29 kwietnia 2026 (środa)

Pracowałem nad dostępnością i multimodalnością. Dodałem odtwarzacz wideo (`VideoView`) z przykładowym ćwiczeniem „chairsquat”, ale napotkałem problem z przewijaniem – rozwiązałem go kopiując plik wideo z `assets` do pamięci cache przed odtwarzaniem. Poprawiłem logikę nastroju „Dobrze”, przeprojektowałem okno pomocy głosowej na bardziej czytelne dla seniorów oraz usunąłem zbędne ikony pozycji z onboardingu. Włączyłem ciągłe nasłuchiwanie komend głosowych, co znacznie usprawniło obsługę. Wieczorem poprawiłem ikony w grze pamięciowej i naprawiłem przycisk pomocy na ekranie ćwiczeń.

---

## 30 kwietnia 2026 (czwartek)

Dzień poświęcony historii aktywności i dokumentacji agentów. Dodałem nowe encje Room (`ExerciseSession`, `GameSession`) wraz z migracją bazy `3→4`, żeby zapisywać każdą ukończoną sesję. Zaimplementowałem `HistoryActivity` – ekran z listą odbytych treningów i gier. Dodałem też przycisk wstecz do tego ekranu i powiększyłem tekst w dialogu pomocy głosowej. Po południu zaktualizowałem dokumentację agentów OpenCode w pliku `CLAUDE.md` i dodałem agenta `librarian` do struktury projektu.

---

## 4 maja 2026 (poniedziałek)

Poniedziałek pełen optymalizacji i usuwania długu technicznego. Zacząłem od implementacji dynamicznego skalowania UI oraz responsywnych standardów w grach (w tym dynamiczne rozmiary komórek w 2048). Następnie gruntownie posprzątałem kod: wymieniłem ad-hoc wątki (`new Thread()`) na singleton `AppExecutors`, poprawiłem zarządzanie strumieniami, usunąłem operacje `SharedPreferences` w pętli, ograniczyłem zapytania DAO do 50 rekordów i wyeliminowałem `allowMainThreadQueries()`. Zrobiłem też kompleksowe lintowanie – usunąłem nieużywane importy, naprawiłem przestarzałe `Handler()`, wyeliminowałem wycieki pamięci w `SplashActivity` i zlikwidowałem cały hardkodowany tekst w layoutach XML, przenosząc wszystko do `strings.xml`. Na koniec dnia znalazłem i naprawiłem krytyczny błąd w cyklu życia `VoiceManager` – `SplashActivity` niszczył singleton TTS przy starcie, co uniemożliwiało działanie syntezy mowy.

---

## 5 maja 2026 (wtorek)

Dzień wykończeniowy. Poprawiłem design w grach umysłowych – dopracowałem detale wizualne i zapewniłem spójność między poszczególnymi ekranami. Na koniec przeszedłem przez całą aplikację i wprowadziłem ostatnie poprawki oraz polerowanie szczegółów przed kolejnym etapem prac.

---


