#!/bin/bash


SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
BASE_DIR=$( dirname "$SCRIPT_DIR" )
igniteConfig=$BASE_DIR/etc/ignite.conf
export SCRIPT_DIR BASE_DIR igniteConfig


case "$1" in
start)
   if [ -f "$igniteConfig" ]; then
     export $(grep -v '^#' $igniteConfig | xargs)
     nohup "$BASE_DIR"/java/bin/java -jar $BASE_DIR/bin/Ignite-*.jar >> "$BASE_DIR"/logs/ignite.log 2>&1 &
     echo $!>"$BASE_DIR"/run/ignite.pid
     echo "$(date) : Bosler agent started..."
   else
     echo "Error: ignite config not found"
   fi

   ;;
stop)
   if [ ! -f "$BASE_DIR"/run/ignite.pid ]; then
     echo "$(date) : [ERROR] No PID file found"
     exit 2
   fi

   echo "$(date) : [INFO] Stopping Ignite, pid=$(cat $BASE_DIR/run/ignite.pid)"
   kill $(cat $BASE_DIR/run/ignite.pid)
   rm $BASE_DIR/run/ignite.pid
   ;;
restart)
   $0 stop
   $0 start
   ;;
status)
   if [ -f "$BASE_DIR"/run/ignite.pid ]; then
      ps -eaf|grep -v grep|grep $(cat $BASE_DIR/run/ignite.pid) > /dev/null
      if [ $? -eq 0 ];then
         echo "$(date) : [INFO] Ignite is running, pid=$(cat $BASE_DIR/run/ignite.pid)"
      else
         echo "$(date) : [ERROR] PID file found but Ignite is NOT running"
         rm $BASE_DIR/run/ignite.pid
      fi
   else
      echo "$(date) : [ERROR] Ignite is NOT running"
      exit 1
   fi
   ;;
*)
   echo "Usage: $0 {start|stop|status|restart}"
esac

exit 0