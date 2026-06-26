FROM php:8.2-fpm-alpine

WORKDIR /app

RUN apk add --no-cache \
    postgresql-dev \
    git \
    unzip \
    curl \
    libxml2-dev \
    && docker-php-ext-install pdo pdo_pgsql

COPY --from=composer:latest /usr/bin/composer /usr/local/bin/composer

COPY . .

RUN composer install --no-interaction --optimize-autoloader

RUN php artisan key:generate || true

EXPOSE 8000

CMD ["php", "artisan", "serve", "--host=0.0.0.0", "--port=8000"]
