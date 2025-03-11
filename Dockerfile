FROM openjdk:23-jdk-slim

WORKDIR /app

COPY target/mini1.jar app.jar

ENV SPRING_APPLICATION_NAME=MiniProject1
ENV SPRING_APPLICATION_USER_DATA_PATH=/data/users.json
ENV SPRING_APPLICATION_PRODUCT_DATA_PATH=/data/products.json
ENV SPRING_APPLICATION_ORDER_DATA_PATH=/data/orders.json
ENV SPRING_APPLICATION_CART_DATA_PATH=/data/carts.json

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]