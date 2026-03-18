#!/bin/bash

# Maven 部署脚本
# 用法:
#   ./deploy.sh -s    部署到 snapshot 仓库
#   ./deploy.sh -r    部署到 release 仓库 (会自动移除版本号中的 -SNAPSHOT 后缀)

# ============================================
# 配置区域 - 请根据实际情况修改仓库地址和ID
# ============================================
SNAPSHOT_REPO_ID="snapshots"
SNAPSHOT_REPO_URL="http://your-nexus-server/repository/maven-snapshots/"

RELEASE_REPO_ID="releases"
RELEASE_REPO_URL="http://your-nexus-server/repository/maven-releases/"
# ============================================

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -s    部署到 snapshot 仓库"
    echo "  -r    部署到 release 仓库 (自动移除 -SNAPSHOT 后缀)"
    echo "  -h    显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -s    # 部署 snapshot 版本"
    echo "  $0 -r    # 部署 release 版本"
}

# 检查是否安装了 mvn
check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}错误: 未找到 mvn 命令，请先安装 Maven${NC}"
        exit 1
    fi
}

# 部署到 snapshot 仓库
deploy_snapshot() {
    echo -e "${YELLOW}正在部署到 snapshot 仓库...${NC}"
    echo -e "仓库ID: ${SNAPSHOT_REPO_ID}"
    echo -e "仓库URL: ${SNAPSHOT_REPO_URL}"
    echo ""
    
    mvn deploy -DaltDeploymentRepository=${SNAPSHOT_REPO_ID}::default::${SNAPSHOT_REPO_URL}
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Snapshot 部署成功!${NC}"
    else
        echo -e "${RED}✗ Snapshot 部署失败!${NC}"
        exit 1
    fi
}

# 部署到 release 仓库
deploy_release() {
    echo -e "${YELLOW}正在部署到 release 仓库...${NC}"
    echo -e "仓库ID: ${RELEASE_REPO_ID}"
    echo -e "仓库URL: ${RELEASE_REPO_URL}"
    echo ""
    
    # 获取当前版本
    CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    
    # 检查是否是 SNAPSHOT 版本
    if [[ "$CURRENT_VERSION" == *"-SNAPSHOT" ]]; then
        echo -e "${YELLOW}检测到 SNAPSHOT 版本: ${CURRENT_VERSION}${NC}"
        echo -e "${YELLOW}正在移除 -SNAPSHOT 后缀...${NC}"
        
        # 移除 SNAPSHOT 后缀
        RELEASE_VERSION=${CURRENT_VERSION%-SNAPSHOT}
        
        echo -e "Release 版本: ${RELEASE_VERSION}"
        
        # 使用 versions-maven-plugin 修改版本
        mvn versions:set -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗ 版本修改失败!${NC}"
            exit 1
        fi
    fi
    
    mvn deploy -DaltDeploymentRepository=${RELEASE_REPO_ID}::default::${RELEASE_REPO_URL}
    
    DEPLOY_RESULT=$?
    
    # 如果之前是 SNAPSHOT 版本，恢复原版本
    if [[ "$CURRENT_VERSION" == *"-SNAPSHOT" ]]; then
        echo -e "${YELLOW}正在恢复 SNAPSHOT 版本...${NC}"
        mvn versions:set -DnewVersion=${CURRENT_VERSION} -DgenerateBackupPoms=false
    fi
    
    if [ $DEPLOY_RESULT -eq 0 ]; then
        echo -e "${GREEN}✓ Release 部署成功!${NC}"
    else
        echo -e "${RED}✗ Release 部署失败!${NC}"
        exit 1
    fi
}

# 主逻辑
main() {
    check_maven
    
    # 如果没有参数，显示帮助
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    # 解析参数
    while getopts "srh" opt; do
        case $opt in
            s)
                deploy_snapshot
                ;;
            r)
                deploy_release
                ;;
            h)
                show_help
                exit 0
                ;;
            \?)
                echo -e "${RED}无效选项: -$OPTARG${NC}"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 如果没有匹配到任何选项
    if [ $OPTIND -eq 1 ]; then
        show_help
        exit 0
    fi
}

main "$@"
