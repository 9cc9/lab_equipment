#!/bin/bash
##############################################################################
# Spring Boot 应用部署脚本（多应用安全版本）
# 支持 start/stop/restart/status 命令，适配 Java 8+
##############################################################################

# ========================== 基础配置（根据实际项目修改）==========================
APP_NAME="lab_equipment"
JAR_FILE="/home/admin/app/${APP_NAME}/target/${APP_NAME}.jar"
APP_PORT=8085
# ECS 2核2G，本机同跑 MySQL + Redis，Java 堆不宜过大
XMS="256M"
XMX="512M"
METASPACE="128M"
MAX_METASPACE="128M"
LOG_DIR="/home/admin/logs/${APP_NAME}"
##############################################################################

JVM_OPTS="-server \
-Xms${XMS} \
-Xmx${XMX} \
-XX:MetaspaceSize=${METASPACE} \
-XX:MaxMetaspaceSize=${MAX_METASPACE} \
-XX:NewRatio=3 \
-XX:SurvivorRatio=6 \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:InitiatingHeapOccupancyPercent=75 \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintHeapAtGC \
-Xloggc:${LOG_DIR}/gc-${APP_NAME}.log \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${LOG_DIR}/oom-${APP_NAME}.hprof \
-Dfile.encoding=UTF-8 \
-Duser.timezone=Asia/Shanghai \
-Dspring.profiles.active=prod \
-Dserver.port=${APP_PORT}"

check_jar_exists() {
    if [ ! -f "${JAR_FILE}" ]; then
        echo "ERROR: 未找到应用 jar 包 [${JAR_FILE}]，请先执行 mvn clean package 打包！"
        exit 1
    fi
}

create_log_dir() {
    if [ ! -d "${LOG_DIR}" ]; then
        mkdir -p "${LOG_DIR}"
        echo "创建日志目录成功：${LOG_DIR}"
    fi
}

verify_pid() {
    local pid=$1
    if [ -z "${pid}" ]; then
        return 1
    fi

    if ! ps -p "${pid}" > /dev/null 2>&1; then
        return 1
    fi

    local cmd=$(ps -p "${pid}" -o args= 2>/dev/null)
    if echo "${cmd}" | grep -q "${JAR_FILE}"; then
        return 0
    fi

    return 1
}

get_pid() {
    local pid=""

    pid=$(ps -ef | grep "java.*${JAR_FILE}" | grep -v grep | awk '{print $2}' 2>/dev/null | head -n 1)
    if [ -n "${pid}" ] && verify_pid "${pid}"; then
        echo "${pid}"
        return 0
    fi

    local port_pid=$(netstat -tlnp 2>/dev/null | grep ":${APP_PORT}" | awk '{print $7}' | awk -F '/' '{print $1}' | head -n 1)
    if [ -n "${port_pid}" ] && verify_pid "${port_pid}"; then
        echo "${port_pid}"
        return 0
    fi

    local jar_name=$(basename "${JAR_FILE}")
    pid=$(ps -ef | grep "java.*${jar_name}" | grep "${APP_NAME}" | grep -v grep | awk '{print $2}' 2>/dev/null | head -n 1)
    if [ -n "${pid}" ] && verify_pid "${pid}"; then
        echo "${pid}"
        return 0
    fi

    echo ""
}

start() {
    check_jar_exists
    create_log_dir

    PID=$(get_pid)
    if [ -n "${PID}" ]; then
        echo "应用 [${APP_NAME}] 已在运行，PID: ${PID}"
        exit 0
    fi

    echo "================ 启动应用 [${APP_NAME}] ================"
    echo "JVM 参数：${JVM_OPTS}"
    echo "应用端口：${APP_PORT}"
    echo "日志目录：${LOG_DIR}"
    echo "======================================================="

    nohup java ${JVM_OPTS} -jar "${JAR_FILE}" > "${LOG_DIR}/${APP_NAME}.log" 2>&1 &

    sleep 3
    PID=$(get_pid)
    if [ -n "${PID}" ]; then
        echo "应用启动成功！PID: ${PID}"
    else
        echo "应用启动失败！请查看日志：${LOG_DIR}/${APP_NAME}.log"
        exit 1
    fi
}

stop() {
    PID=$(get_pid)
    if [ -z "${PID}" ]; then
        echo "应用 [${APP_NAME}] 未运行"
        exit 0
    fi

    if ! verify_pid "${PID}"; then
        echo "ERROR: PID ${PID} 不是应用 [${APP_NAME}] 的进程，停止操作已取消"
        echo "当前应用的 jar 文件：${JAR_FILE}"
        exit 1
    fi

    echo "================ 停止应用 [${APP_NAME}] ================"
    echo "应用 jar: ${JAR_FILE}"
    echo "正在停止 PID: ${PID} ..."

    kill -15 "${PID}" 2>/dev/null

    for ((i=1; i<=10; i++)); do
        if ! ps -p "${PID}" > /dev/null 2>&1; then
            echo "应用停止成功！"
            exit 0
        fi
        sleep 1
        echo "等待停止...（${i}/10）"
    done

    if ps -p "${PID}" > /dev/null 2>&1; then
        if verify_pid "${PID}"; then
            echo "强制停止 PID: ${PID} ..."
            kill -9 "${PID}" 2>/dev/null
            sleep 1
            if ! ps -p "${PID}" > /dev/null 2>&1; then
                echo "应用强制停止成功！"
            else
                echo "ERROR: 强制停止失败，请手动检查进程 ${PID}"
                exit 1
            fi
        else
            echo "WARNING: PID ${PID} 已不是应用 [${APP_NAME}] 的进程"
        fi
    else
        echo "应用已停止"
    fi
}

restart() {
    stop
    sleep 5
    start
}

status() {
    PID=$(get_pid)
    if [ -n "${PID}" ]; then
        if verify_pid "${PID}"; then
            echo "================ 应用状态 [${APP_NAME}] ================"
            echo "运行状态：运行中"
            echo "进程 PID：${PID}"
            echo "应用端口：${APP_PORT}"
            echo "Jar 文件：${JAR_FILE}"
            echo "启动命令：$(ps -p ${PID} -o args= 2>/dev/null | head -c 100)"
            echo "======================================================="
        else
            echo "WARNING: 检测到端口 ${APP_PORT} 被占用，但不是应用 [${APP_NAME}]"
            echo "占用进程 PID: ${PID}"
            echo "当前应用 jar: ${JAR_FILE}"
        fi
    else
        echo "应用 [${APP_NAME}] 未运行"
    fi
}

usage() {
    echo "用法：$0 {start|stop|restart|status}"
    echo "  start    - 启动应用"
    echo "  stop     - 停止应用"
    echo "  restart  - 重启应用"
    echo "  status   - 查看应用状态"
    exit 1
}

if [ $# -ne 1 ]; then
    usage
fi

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        usage
        ;;
esac
