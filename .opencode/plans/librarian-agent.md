# Plan: Agent Librarian + Aktualizacja OpenCode Agents

## Cel
Utworzenie agenta `librarian` oraz aktualizacja istniejących agentów (`coder`, `debugger`, `git-pusher`) i `CLAUDE.md` do nowej lokalizacji vaultu Obsidian BRAIN.

## Zadania

### 1. Utworzenie `.opencode/agents/librarian.md`
- Agent dokumentujący zmiany w `C:\Users\kubar\OneDrive\Dokumenty\BRAIN`
- Format: YAML frontmatter + Markdown
- Foldery: Sessions, Decisions, Features, Bugs, Knowledge, Library, Standards
- Aktualizacja Project_Log.md przy każdej zmianie
- Tworzenie indeksów (Decisions_Index.md, Features_Index.md, itp.)

### 2. Aktualizacja `.opencode/agents/coder.md`
- Zmiana ścieżki z `/mnt/c/Users/kubar/Documents/BRAIN/02_Library/Android_Snippets.md`
- Na: `C:\Users\kubar\OneDrive\Dokumenty\BRAIN\02_Library\Android_Snippets.md`

### 3. Aktualizacja `.opencode/agents/debugger.md`
- Zmiana ścieżki z `/mnt/c/Users/kubar/Documents/BRAIN/03_Standards/Android_Bugi.md`
- Na: `C:\Users\kubar\OneDrive\Dokumenty\BRAIN\03_Standards\Android_Bugi.md`

### 4. Aktualizacja `.opencode/agents/git-pusher.md`
- Zmiana ścieżki z `/mnt/c/Users/kubar/Documents/BRAIN/01_Projects/FitnessApp/Project_Log.md`
- Na: `C:\Users\kubar\OneDrive\Dokumenty\BRAIN\01_Projects\FitnessApp\Project_Log.md`

### 5. Aktualizacja `CLAUDE.md`
- Dodanie sekcji "OpenCode Agents" z tabelą 4 agentów
- Opis roli każdego agenta
- Wskazówki jak delegować zadania do agentów

## Vault BRAIN
Lokalizacja: `C:\Users\kubar\OneDrive\Dokumenty\BRAIN`
- `01_Projects/FitnessApp/` — projekt
- `02_Library/` — wiedza ogólna
- `03_Standards/` — standardy
- `04_Archive/` — archiwum

## Akceptacja
Użytkownik wyraził zgodę na wszystkie punkty. Agent ma działać jako "drugi mózg" / baza wiedzy.