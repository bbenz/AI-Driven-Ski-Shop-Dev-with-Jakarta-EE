#!/bin/bash

# Product Catalog Service の開発・実行スクリプト

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Product Catalog Service - 開発・実行スクリプト${NC}"
echo "=================================================="

# ヘルプ表示
show_help() {
    cat << EOF
使用方法: $0 [COMMAND]

COMMANDS:
    build           アプリケーションをビルドします
    dev             開発モードで起動します
    run             JARファイルを実行します
    docker          Docker環境で起動します
    test            テストを実行します
    clean           ビルドファイルを削除します
    stop            Docker環境を停止します
    logs            Docker ログを表示します
    help            このヘルプを表示します

例:
    $0 dev          # 開発モードで起動
    $0 docker       # Docker環境で起動
    $0 test         # テスト実行
EOF
}

# プロジェクトのビルド
build() {
    echo -e "${YELLOW}アプリケーションをビルド中...${NC}"
    mvn clean package -DskipTests
    echo -e "${GREEN}✓ ビルド完了${NC}"
}

# 開発モードで起動
dev() {
    echo -e "${YELLOW}開発モードで起動中...${NC}"
    echo "PostgreSQLがローカルで起動していることを確認してください"
    echo "または docker-compose up -d postgres を実行してください"
    mvn quarkus:dev
}

# JARファイルで実行
run() {
    echo -e "${YELLOW}JARファイルで実行中...${NC}"
    if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
        echo -e "${RED}JARファイルが見つかりません。先にビルドしてください: $0 build${NC}"
        exit 1
    fi
    
    export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5432/product_catalog"
    export QUARKUS_DATASOURCE_USERNAME="postgres"
    export QUARKUS_DATASOURCE_PASSWORD="postgres"
    
    java -jar target/quarkus-app/quarkus-run.jar
}

# Docker環境で起動
docker() {
    echo -e "${YELLOW}Docker環境で起動中...${NC}"
    
    # ビルドしてからDocker起動
    build
    
    echo "Dockerイメージをビルド中..."
    docker build -f src/main/docker/Dockerfile.jvm -t product-catalog-service:latest .
    
    echo "Docker Composeで起動中..."
    docker-compose up -d
    
    echo -e "${GREEN}✓ Docker環境で起動完了${NC}"
    echo ""
    echo "以下のURLでアクセスできます:"
    echo "  API: http://localhost:8083"
    echo "  Swagger UI: http://localhost:8083/q/swagger-ui/"
    echo "  Health Check: http://localhost:8083/q/health"
    echo ""
    echo "ログを確認: $0 logs"
    echo "停止: $0 stop"
}

# テスト実行
test() {
    echo -e "${YELLOW}テストを実行中...${NC}"
    mvn test
    echo -e "${GREEN}✓ テスト完了${NC}"
}

# クリーンアップ
clean() {
    echo -e "${YELLOW}ビルドファイルを削除中...${NC}"
    mvn clean
    echo -e "${GREEN}✓ クリーンアップ完了${NC}"
}

# Docker環境停止
stop() {
    echo -e "${YELLOW}Docker環境を停止中...${NC}"
    docker-compose down
    echo -e "${GREEN}✓ Docker環境停止完了${NC}"
}

# ログ表示
logs() {
    echo -e "${YELLOW}Docker ログを表示中...${NC}"
    docker-compose logs -f product-catalog-service
}

# メイン処理
case "${1:-help}" in
    build)
        build
        ;;
    dev)
        dev
        ;;
    run)
        run
        ;;
    docker)
        docker
        ;;
    test)
        test
        ;;
    clean)
        clean
        ;;
    stop)
        stop
        ;;
    logs)
        logs
        ;;
    help|*)
        show_help
        ;;
esac
