#!/bin/sh

export COMPOSE_FILE_PATH="${PWD}/target/classes/docker/docker-compose.yml"

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker volume create seedim-docusign-acs-volume
    docker volume create seedim-docusign-db-volume
    docker volume create seedim-docusign-ass-volume
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d
}

start_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d seedim-docusign-share
}

start_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d seedim-docusign-acs
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
        docker-compose -f "$COMPOSE_FILE_PATH" down
    fi
}

purge() {
    docker volume rm -f seedim-docusign-acs-volume
    docker volume rm -f seedim-docusign-db-volume
    docker volume rm -f seedim-docusign-ass-volume
}

build() {
    $MVN_EXEC clean package
}

build_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" kill seedim-docusign-share
    yes | docker-compose -f "$COMPOSE_FILE_PATH" rm -f seedim-docusign-share
    $MVN_EXEC clean package -pl seedim-docusign-share,seedim-docusign-share-docker
}

build_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" kill seedim-docusign-acs
    yes | docker-compose -f "$COMPOSE_FILE_PATH" rm -f seedim-docusign-acs
    $MVN_EXEC clean package -pl seedim-docusign-integration-tests,seedim-docusign-platform,seedim-docusign-platform-docker
}

tail() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs -f
}

tail_all() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs --tail="all"
}

prepare_test() {
    $MVN_EXEC verify -DskipTests=true -pl seedim-docusign-platform,seedim-docusign-integration-tests,seedim-docusign-platform-docker
}

test() {
    $MVN_EXEC verify -pl seedim-docusign-platform,seedim-docusign-integration-tests
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  build_start_it_supported)
    down
    build
    prepare_test
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  reload_share)
    build_share
    start_share
    tail
    ;;
  reload_acs)
    build_acs
    start_acs
    tail
    ;;
  build_test)
    down
    build
    prepare_test
    start
    test
    tail_all
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|build_start_it_supported|start|stop|purge|tail|reload_share|reload_acs|build_test|test}"
esac