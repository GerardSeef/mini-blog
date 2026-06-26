#!/bin/bash

# Script helper para hacer push a GitHub
# Uso: ./push-to-github.sh <usuario-github> <nombre-repo>
# Ejemplo: ./push-to-github.sh tu-usuario mini-blog

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "╔════════════════════════════════════════════════════════════════════╗"
    echo "║         HELPER PARA PUSH A GITHUB - Mini-Blog API                 ║"
    echo "╚════════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Uso: ./push-to-github.sh <usuario-github> <nombre-repo>"
    echo ""
    echo "Ejemplo:"
    echo "  ./push-to-github.sh tu-usuario mini-blog"
    echo ""
    echo "Esto hará:"
    echo "  1. Crear remote origin (https://github.com/tu-usuario/mini-blog.git)"
    echo "  2. Renombrar rama a 'main'"
    echo "  3. Hacer push a GitHub"
    echo ""
    echo "Requisitos:"
    echo "  - Tener Git configurado localmente (git config user.name/user.email)"
    echo "  - Tener autenticación de GitHub configurada (SSH o HTTPS con token)"
    echo "  - El repositorio YA DEBE ESTAR CREADO en GitHub"
    echo ""
    exit 1
fi

GITHUB_USER=$1
REPO_NAME=$2
REPO_URL="https://github.com/${GITHUB_USER}/${REPO_NAME}.git"

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║  Haciendo push a: $REPO_URL"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Agregar remote
echo "1️⃣  Agregando remote origin..."
git remote add origin "$REPO_URL"

# Renombrar rama a main
echo "2️⃣  Renombrando rama a 'main'..."
git branch -M main

# Hacer push
echo "3️⃣  Haciendo push a GitHub..."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "╔════════════════════════════════════════════════════════════════════╗"
    echo "║  ✅ ¡PUSH COMPLETADO EXITOSAMENTE!"
    echo "╚════════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Repositorio disponible en:"
    echo "  $REPO_URL"
    echo ""
    echo "Para clonar:"
    echo "  git clone $REPO_URL"
    echo ""
else
    echo ""
    echo "❌ Error durante el push. Verifica:"
    echo "   - El repositorio existe en GitHub"
    echo "   - Tienes permisos de escritura"
    echo "   - Tu autenticación es válida"
    echo ""
fi
