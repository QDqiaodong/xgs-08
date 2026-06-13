#!/bin/bash
set -e

# 加载 .env 环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "❌ .env 配置文件不存在！"
    exit 1
fi

echo "=========================================="
echo "  盆景造型养护交流圈 - 启动脚本"
echo "  项目名称: ${COMPOSE_PROJECT_NAME}"
echo "=========================================="

# ==========================================
# 端口占用检查
# ==========================================
echo ""
echo "🔍 正在检查端口占用情况..."

check_port() {
    local PORT=$1
    local NAME=$2
    if lsof -nP -iTCP:${PORT} -sTCP:LISTEN > /dev/null 2>&1; then
        echo "❌ 端口 ${PORT} (${NAME}) 已被占用:"
        lsof -nP -iTCP:${PORT} -sTCP:LISTEN
        echo ""
        echo "请先停止占用端口的进程，或修改 .env 中的端口配置"
        exit 1
    else
        echo "✅ 端口 ${PORT} (${NAME}) 可用"
    fi
}

check_port ${FRONTEND_PORT} "前端"
check_port ${BACKEND_PORT} "后端"
check_port ${MYSQL_PORT} "MySQL"
check_port ${REDIS_PORT} "Redis"

# ==========================================
# Docker 环境检查
# ==========================================
echo ""
echo "🐳 正在检查 Docker 环境..."

if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi
echo "✅ Docker 已安装"

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi
echo "✅ Docker Compose 已安装"

# ==========================================
# 创建必要目录
# ==========================================
echo ""
echo "📁 正在创建必要目录..."
mkdir -p backend/uploads
echo "✅ 目录创建完成"

# ==========================================
# 启动 Docker 容器
# ==========================================
echo ""
echo "🚀 正在启动 Docker 容器集群..."
echo "   镜像源: ${DOCKER_REGISTRY}"
echo ""

docker-compose up -d --build

# ==========================================
# 等待服务就绪
# ==========================================
echo ""
echo "⏳ 正在等待服务启动..."

MAX_RETRIES=30
RETRY_COUNT=0

wait_for_service() {
    local PORT=$1
    local NAME=$2
    local RETRY=0
    
    while [ $RETRY -lt $MAX_RETRIES ]; do
        if lsof -nP -iTCP:${PORT} -sTCP:LISTEN > /dev/null 2>&1; then
            echo "✅ ${NAME} 服务已就绪 (端口: ${PORT})"
            return 0
        fi
        sleep 2
        RETRY=$((RETRY + 1))
        echo -n "."
    done
    echo ""
    echo "⚠️  ${NAME} 启动超时，请手动检查"
    return 1
}

wait_for_service ${FRONTEND_PORT} "前端"
wait_for_service ${BACKEND_PORT} "后端"

sleep 3

# ==========================================
# 验证服务
# ==========================================
echo ""
echo "🔍 正在验证服务可用性..."

if curl -sS "http://127.0.0.1:${FRONTEND_PORT}" > /dev/null 2>&1; then
    echo "✅ 前端服务验证通过 (127.0.0.1)"
else
    echo "⚠️  前端服务验证失败，请检查"
fi

if curl -sS "http://localhost:${FRONTEND_PORT}" > /dev/null 2>&1; then
    echo "✅ 前端服务验证通过 (localhost)"
else
    echo "⚠️  前端服务验证失败，请检查"
fi

# ==========================================
# 输出最终信息
# ==========================================
echo ""
echo "=========================================="
echo "  🎉 服务启动完成！"
echo "=========================================="
echo ""
echo "🌐 访问地址："
echo "  前端首页:    http://localhost:${FRONTEND_PORT}"
echo "  前端首页:    http://127.0.0.1:${FRONTEND_PORT}"
echo ""
echo "🔧 后端接口："
echo "  API 地址:    http://127.0.0.1:${BACKEND_PORT}/api"
echo ""
echo "💾 数据库信息："
echo "  MySQL:       127.0.0.1:${MYSQL_PORT}"
echo "  数据库名:    ${MYSQL_DATABASE}"
echo "  用户名:      ${MYSQL_USER}"
echo "  密码:        ${MYSQL_PASSWORD}"
echo ""
echo "⚡ 缓存服务："
echo "  Redis:       127.0.0.1:${REDIS_PORT}"
echo ""
echo "📦 容器列表："
docker-compose ps
echo ""
echo "📋 常用命令："
echo "  查看日志:    docker-compose logs -f"
echo "  停止服务:    docker-compose down"
echo "  重启服务:    docker-compose restart"
echo "  重新构建:    docker-compose up --build -d"
echo ""
