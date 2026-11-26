# 📋 Инструкция по сборке и установке Aim Assist Mod

## 🚀 Быстрый старт (3 шага)

### Шаг 1: Установите Java 21
Скачайте JDK 21 с https://adoptium.net/

### Шаг 2: Соберите мод
```bash
# Linux/Mac:
chmod +x gradlew
./gradlew build

# Windows:
gradlew.bat build
```

### Шаг 3: Установите мод
Скопируйте `build/libs/aim-assist-1.0.0.jar` в папку `mods/` вашего Minecraft.

---

## 📦 Загрузка на GitHub

### Вариант 1: Через GitHub CLI
```bash
# Установите GitHub CLI: https://cli.github.com/
gh auth login
gh repo create aim-assist-mod --public --source=. --push
```

### Вариант 2: Вручную
```bash
git init
git add .
git commit -m "Aim Assist Mod for MC 1.21.4"
git remote add origin https://github.com/ВАШ_ЛОГИН/aim-assist-mod.git
git push -u origin main
```

### Вариант 3: Автоматическая сборка через GitHub Actions
После загрузки на GitHub, Actions автоматически соберёт .jar файл.
Скачайте его из вкладки "Actions" → "Build Mod" → "aim-assist-mod"

---

## 🎮 Управление в игре

| Клавиша | Действие |
|---------|----------|
| **V** | Включить/выключить Aim Assist |
| **R** | Удерживать для прицеливания |

## ⚙️ Настройка

Откройте `src/main/java/com/aimassist/AimAssistClient.java` и измените:

```java
private static float range = 4.5f;        // Дальность (блоки)
private static float aimSpeed = 8.0f;     // Скорость (1-20)
private static float fov = 90.0f;         // Поле зрения (градусы)
private static boolean targetPlayers = true;  // Целиться в игроков
private static boolean targetMobs = true;     // Целиться в мобов
private static boolean smoothAim = true;      // Плавное наведение
private static boolean visibilityCheck = true; // Проверка стен
```

---

## 📁 Структура проекта

```
aim-assist-mod/
├── build.gradle              # Настройки сборки
├── gradle.properties         # Версии зависимостей
├── settings.gradle           # Настройки Gradle
├── gradlew / gradlew.bat     # Скрипты сборки
├── src/
│   └── main/
│       ├── java/com/aimassist/
│       │   ├── AimAssistMod.java      # Основной класс
│       │   └── AimAssistClient.java   # Клиентская логика
│       └── resources/
│           ├── fabric.mod.json        # Описание мода
│           └── assets/aimassist/lang/ # Переводы
└── .github/workflows/
    └── build.yml             # Автоматическая сборка
```

## ❓ Частые проблемы

**Q: Build failed: "Could not resolve..."**
A: Проверьте интернет-соединение. Gradle скачивает зависимости при первой сборке.

**Q: "Unsupported class file major version 65"**
A: Нужна Java 21. Проверьте: `java -version`

**Q: Мод не загружается в Minecraft**
A: Убедитесь что Fabric API установлен. Скачайте с https://modrinth.com/mod/fabric-api
