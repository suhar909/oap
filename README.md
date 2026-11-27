# Aim Assist Mod - Minecraft Fabric 1.21.4

Мод для Minecraft Java Edition 1.21.4 (Fabric) с функцией Aim Assist.

## Возможности

- **Автоматическое наведение** на ближайших игроков и мобов
- **Плавное наведение** (smooth aim) с настраиваемой скоростью
- **Проверка видимости** - не будет целиться через стены
- **Настраиваемый FOV** - работает только с целями в поле зрения
- **Горячие клавиши** для включения/выключения

## Горячие клавиши

| Клавиша | Действие |
|---------|----------|
| `V` | Включить/выключить Aim Assist |
| `R` | Удерживать для прицеливания (работает даже когда мод выключен) |

## Настройки (в коде)

В файле `AimAssistClient.java` можно изменить:

| Параметр | По умолчанию | Описание |
|----------|-------------|----------|
| `range` | 4.5 | Дальность обнаружения целей (блоки) |
| `aimSpeed` | 8.0 | Скорость наведения (1-20) |
| `fov` | 90.0 | Поле зрения для обнаружения целей (градусы) |
| `targetPlayers` | true | Целиться в игроков |
| `targetMobs` | true | Целиться в мобов |
| `smoothAim` | true | Плавное наведение |
| `visibilityCheck` | true | Проверка видимости цели |
| `onlyWhenHoldingWeapon` | false | Работать только с оружием в руке |

## Сборка

### Требования
- Java 21 (JDK)
- Internet connection (для загрузки зависимостей)

### Команды

```bash
# Клонировать репозиторий
git clone https://github.com/YOUR_USERNAME/aim-assist-mod.git
cd aim-assist-mod

# Собрать мод (Linux/Mac)
./gradlew build

# Собрать мод (Windows)
gradlew.bat build

# Готовый .jar файл будет в:
# build/libs/aim-assist-1.0.0.jar
```

## Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/installer/)
2. Установите [Fabric API](https://modrinth.com/mod/fabric-api)
3. Скопируйте `aim-assist-1.0.0.jar` в папку `mods/`
4. Запустите Minecraft

## Лицензия

MIT License
