# ===== STAGE 1: BUILD =====
# Беремо образ з Maven + JDK 17 для збірки
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Спершу копіюємо тільки pom.xml і качаємо залежності.
# Це кешується: якщо pom не мінявся - залежності не перекачуються при кожній збірці.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Тепер копіюємо код і збираємо jar (без тестів - вони ганяються окремо в CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== STAGE 2: RUNTIME =====
# Легкий образ тільки з JRE (без Maven, без вихідників) - набагато менший
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копіюємо ТІЛЬКИ зібраний jar з попереднього stage
COPY --from=build /app/target/*.jar app.jar

# Eureka слухає 8761
EXPOSE 8761

# Команда запуску
ENTRYPOINT ["java", "-jar", "app.jar"]