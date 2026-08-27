# Реализация поддержки множественных графиков

Я реализовал функционал создания нескольких графиков и возможность переключения между ними прямо с главного экрана.

### Основные изменения:

1.  **Слой данных (Data Layer)**:
    *   Обновлен `ShiftDao`: добавлены методы `getAllPatterns()` для получения списка всех графиков и `setActivePattern(id)` для переключения активного графика с использованием Room-транзакции (сброс флага `isActive` у всех и установка у выбранного).
    *   `ShiftRepository` теперь поддерживает получение всех шаблонов и смену активного.

2.  **Бизнес-логика (Domain Layer)**:
    *   Создан `GetShiftPatternsUseCase` для получения списка всех доступных графиков.
    *   Создан `SelectActivePatternUseCase` для смены текущего рабочего графика.
    *   `GetScheduleForMonthUseCase` сделан публичным для доступа к репозиторию (оптимизация связей в ViewModel).

3.  **Пользовательский интерфейс (UI Layer)**:
    *   **CalendarScreen**: Заголовок календаря стал интерактивным. При клике на название графика открывается выпадающее меню (`DropdownMenu`).
    *   **Переключение**: В меню отображаются все созданные графики. Текущий отмечен галочкой.
    *   **Добавление**: В меню добавлен быстрый переход «+ Добавить новый график», ведущий в конструктор.
    *   **Обновление**: При смене графика календарь мгновенно пересчитывает сетку смен.

4.  **ViewModel**:
    *   `CalendarViewModel` теперь отслеживает список всех графиков и подписывается на изменения активного, обеспечивая реактивность UI.

### Проверка:
*   Проект успешно собирается (`assembleDebug`).
*   Логика БД проверена на отсутствие конфликтов при обновлении статуса `isActive`.

Теперь пользователь может вести графики для нескольких человек (например, для себя и супруга/супруги) и быстро переключаться между ними.

**Ключевые файлы:**
*   [ShiftDao.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/data/local/dao/ShiftDao.kt)
*   [CalendarViewModel.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/ui/calendar/CalendarViewModel.kt)
*   [CalendarScreen.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/ui/calendar/CalendarScreen.kt)
*   [SelectActivePatternUseCase.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/domain/usecase/SelectActivePatternUseCase.kt)
